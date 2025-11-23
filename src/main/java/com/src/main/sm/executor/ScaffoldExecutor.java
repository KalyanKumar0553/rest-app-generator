package com.src.main.sm.executor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.InitializrProjectModel;
import com.src.main.dto.MavenDependency;
import com.src.main.dto.StepResult;
import com.src.main.service.DependencyResolver;
import com.src.main.sm.config.StepExecutor;
import com.src.main.util.GradleVersionResolver;
import com.src.main.util.GradleWrapperInstaller;
import com.src.main.util.InitializrGradleGenerator;
import com.src.main.util.InitializrPomGenerator;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class ScaffoldExecutor implements StepExecutor {

	private static final Logger log = LoggerFactory.getLogger(ScaffoldExecutor.class);
	
	private static final String TPL_README = "templates/project/README.md.mustache";
	private static final String TPL_GITIGNORE = "templates/project/gitignore.mustache";
	private static final String TPL_VALIDATION_MESSAGES = "templates/project/messages.properties.mustache";
	private static final String TPL_MAIN = "templates/project/main.mustache";
	
	private final DependencyResolver dependencyResolver;
	private final InitializrPomGenerator pomGenerator;
	private final InitializrGradleGenerator gradleGenerator;
	private final TemplateEngine tpl;

	private GradleWrapperInstaller gradleWrapperInstaller;

	public ScaffoldExecutor(DependencyResolver dependencyResolver, InitializrPomGenerator pomGenerator,
			InitializrGradleGenerator gradleGenerator, GradleWrapperInstaller gradleWrapperInstaller,
			TemplateEngine tpl) {
		this.dependencyResolver = dependencyResolver;
		this.pomGenerator = pomGenerator;
		this.gradleGenerator = gradleGenerator;
		this.tpl = tpl;
		this.gradleWrapperInstaller = gradleWrapperInstaller;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) throws Exception {
		final String groupId = strOr(data, ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP);
		final String artifactId = strOr(data, ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT);
		final String version = strOr(data, ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION);
		final String name = strOr(data, ProjectMetaDataConstants.NAME, null, ProjectMetaDataConstants.DEFAULT_NAME);
		final String description = strOr(data, ProjectMetaDataConstants.DESCRIPTION, null, ProjectMetaDataConstants.DEFAULT_DESCRIPTION);
		final String bootVersion = strOr(data, ProjectMetaDataConstants.BOOT_VERSION, null, ProjectMetaDataConstants.DEFAULT_BOOT_VERSION);
		final String jdkVersion = strOr(data, ProjectMetaDataConstants.JDK_VERSION, null, ProjectMetaDataConstants.DEFAULT_JDK);
		final String packaging = strOr(data, ProjectMetaDataConstants.PACKAGING, null, ProjectMetaDataConstants.DEFAULT_PACKAGING);
		final String buildTool = strOr(data, ProjectMetaDataConstants.BUILD_TOOL, null, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL);
		final String generator = strOr(data, ProjectMetaDataConstants.GENERATOR, ProjectMetaDataConstants.GENERATOR, ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR);
		final String packageName = strOr(data, ProjectMetaDataConstants.PACKAGE_NAME, null, groupId);
		
		final boolean openapi = boolOr(data, ProjectMetaDataConstants.EXTRAS_OPENAPI, true);
		final boolean angular = boolOr(data, ProjectMetaDataConstants.EXTRAS_ANGULAR_INTEGRATION, false);
		
		final Path root = resolveRoot(data);
		
		final Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get("yaml");
		
		final List<String> depReq = (List<String>) data.getVariables().getOrDefault(ProjectMetaDataConstants.DEPENDENCIES,List.of("web", "validation", "actuator", "test"));


		createMinimalLayout(root, packageName, buildTool);
		List<MavenDependency> deps = dependencyResolver.resolveForMaven(depReq, bootVersion, openapi);

		InitializrProjectModel model = new InitializrProjectModel(groupId, artifactId, version, name, description, packaging,generator,jdkVersion, bootVersion, openapi, angular);

		log.info("Scaffolding project: {} ",model);

		if ("gradle".equalsIgnoreCase(buildTool)) {
			InitializrGradleGenerator.GradleFiles files = gradleGenerator.generateFiles(model,deps);
			Files.writeString(root.resolve(files.getBuildFileName()), files.getBuildContent(), UTF_8);
			Files.writeString(root.resolve(files.getSettingsFileName()), files.getSettingsContent(), UTF_8);
			gradleWrapperInstaller.installWrapper(root, GradleVersionResolver.forBoot(model.getBootVersion()));
		} else {
			String pom = pomGenerator.generatePom(model, deps);
			Files.writeString(root.resolve("pom.xml"), pom, UTF_8);
		}
		String mainClassName = toPascal(artifactId) + "Application";
		writeMainClass(root, packageName, mainClassName);
		writeResources(root, name, yaml);
		writeDocsAndGitignore(root, name);
		Map<String,Object> result = new HashMap<>();
		result.put("status", "Success");
		result.put(ProjectMetaDataConstants.ROOT_DIR, root.toAbsolutePath().toString());
		result.put(ProjectMetaDataConstants.GROUP_ID, groupId);
		result.put(ProjectMetaDataConstants.ARTIFACT_ID,artifactId);
		result.put(ProjectMetaDataConstants.VERSION,version);
		result.put(ProjectMetaDataConstants.NAME,name);
		result.put(ProjectMetaDataConstants.DESCRIPTION,description);
		result.put(ProjectMetaDataConstants.BOOT_VERSION,bootVersion);
		result.put(ProjectMetaDataConstants.JDK_VERSION,jdkVersion);
		result.put(ProjectMetaDataConstants.PACKAGING,packaging);
		result.put(ProjectMetaDataConstants.BUILD_TOOL,buildTool);
		result.put(ProjectMetaDataConstants.GENERATOR,generator);
		result.put(ProjectMetaDataConstants.PACKAGE_NAME,packageName);
		return StepResult.ok(result);
	}

	private static Path resolveRoot(ExtendedState data) {
		Object r = data.getVariables().getOrDefault(ProjectMetaDataConstants.ROOT_DIR, data.getVariables().get("root"));
		if (r == null)
			throw new IllegalArgumentException("Root directory not provided (AppConstants.ROOT_DIR or 'root').");
		return (r instanceof Path) ? (Path) r : Path.of(r.toString());
	}

	private static String strOr(ExtendedState data, String primaryKey, String legacyKey, String dflt) {
		Object v = primaryKey == null ? null : data.getVariables().get(primaryKey);
		if (v == null && legacyKey != null)
			v = data.getVariables().get(legacyKey);
		return v == null ? dflt : v.toString();
	}

	private static boolean boolOr(ExtendedState data, String key, boolean dflt) {
		Object v = data.getVariables().get(key);
		if (v == null)
			return dflt;
		if (v instanceof Boolean)
			return (Boolean) v;
		return Boolean.parseBoolean(v.toString());
	}

	private static void createMinimalLayout(Path root, String packageName, String buildTool) throws Exception {
		Path mainJava = root.resolve("src/main/java/" + packageName.replace('.', '/'));
		Path mainRes = root.resolve("src/main/resources");
		Path testJava = root.resolve("src/test/java/" + packageName.replace('.', '/'));
		Files.createDirectories(mainJava);
		Files.createDirectories(mainRes);
		Files.createDirectories(testJava);
	}

	private void writeMainClass(Path root, String packageName, String mainClassName) throws Exception {
		Path target = root.resolve("src/main/java/" + packageName.replace('.', '/') + "/" + mainClassName + ".java");
		String rendered = tpl.render(TPL_MAIN, Map.of("basePkg", packageName, "mainClass", mainClassName));

		Files.writeString(target, rendered, UTF_8);
	}

	private void writeResources(Path root, String appName, Map<String, Object> yaml) throws Exception {
		Path resDir = root.resolve("src/main/resources");
		Files.createDirectories(resDir);
		writeMessagesIfAny(root, yaml);
	}

	private void writeDocsAndGitignore(Path root, String appName) throws Exception {
		// Ensure project root exists
		Files.createDirectories(root);

		// Template rendering helper (assuming tpl is your Mustache or TemplateEngine
		// instance)
		String readmeRendered = tpl.render(TPL_README, Map.of("appName", appName));
		String gitignoreRendered = tpl.render(TPL_GITIGNORE, Map.of());

		// Write README.md
		Path readmePath = root.resolve("README.md");
		Files.writeString(readmePath, readmeRendered, StandardCharsets.UTF_8);

		// Write .gitignore
		Path gitignorePath = root.resolve(".gitignore");
		Files.writeString(gitignorePath, gitignoreRendered, StandardCharsets.UTF_8);
	}

	private static String toPascal(String s) {
		if (s == null || s.isBlank())
			return "Application";
		String[] parts = s.replace('-', ' ').replace('_', ' ').trim().split("\\s+");
		StringBuilder b = new StringBuilder();
		for (String p : parts) {
			if (!p.isEmpty())
				b.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
		}
		return b.append("").toString();
	}

	@SuppressWarnings("unchecked")
	private void writeMessagesIfAny(Path root, Map<String, Object> yaml) {
		try {
			Path resDir = root.resolve("src/main/resources");
			Files.createDirectories(resDir);
			Path target = resDir.resolve("messages.properties");

			// don't overwrite if something else already created it
			if (Files.exists(target)) {
				log.debug("messages.properties exists; skipping scaffold write");
				return;
			}

			// collect messages from YAML
			List<Map<String, String>> entries = List.of();
			if (yaml != null && yaml.get("messages") instanceof Map<?, ?> msgs) {
				entries = ((Map<String, Object>) msgs).entrySet().stream().sorted(Map.Entry.comparingByKey())
						.map(e -> Map.of("key", String.valueOf(e.getKey()), "value",
								e.getValue() == null ? "" : String.valueOf(e.getValue())))
						.toList();
			}

			// 1) Try Mustache
			String body = null;
			try {
				body = tpl.render(TPL_VALIDATION_MESSAGES, Map.of("entries", entries, "messages", entries));
			} catch (Exception renderEx) {
				log.debug("TPL_MESSAGES render failed, will fallback: {}", renderEx.getMessage());
			}

			// 2) Fallback if Mustache missing/blank
			if (body == null || body.isBlank()) {
				body = buildMessagesProperties(entries);
			}			
			Files.writeString(target, body, UTF_8);
		} catch (Exception ex) {
			log.warn("Failed to write messages.properties: {}", ex.getMessage());
			// last-ditch: at least create a tiny placeholder
			try {
				Path target = root.resolve("src/main/resources/messages.properties");
				if (!Files.exists(target)) {
					Files.writeString(target, "# messages (scaffold fallback)\n", UTF_8);
				}
			} catch (Exception ignored) {
			}
		}
	}

	/** Builds a valid .properties body from key/value pairs. */
	private static String buildMessagesProperties(List<Map<String, String>> entries) {
		StringBuilder sb = new StringBuilder();
		sb.append("# Application messages").append(System.lineSeparator());
		if (entries != null) {
			for (Map<String, String> e : entries) {
				String k = e.get("key");
				String v = e.get("value");
				if (k == null)
					continue;
				sb.append(escapePropKey(k)).append('=').append(escapePropValue(v == null ? "" : v))
						.append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	/** Minimal .properties escaping for keys. */
	private static String escapePropKey(String s) {
		StringBuilder b = new StringBuilder(s.length() + 8);
		for (char c : s.toCharArray()) {
			switch (c) {
			case ' ', '\t', '\f', '=', ':', '#', '!' -> {
				b.append('\\').append(c);
			}
			case '\\' -> b.append("\\\\");
			case '\n' -> b.append("\\n");
			case '\r' -> b.append("\\r");
			default -> b.append(c);
			}
		}
		return b.toString();
	}

	/** Minimal .properties escaping for values. */
	private static String escapePropValue(String s) {
		StringBuilder b = new StringBuilder(s.length() + 8);
		for (char c : s.toCharArray()) {
			switch (c) {
			case '\\' -> b.append("\\\\");
			case '\n' -> b.append("\\n");
			case '\r' -> b.append("\\r");
			default -> b.append(c);
			}
		}
		return b.toString();
	}
}
