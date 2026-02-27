package com.src.main.sm.executor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.InitializrProjectModel;
import com.src.main.dto.MavenDependencyDTO;
import com.src.main.dto.StepResult;
import com.src.main.common.util.StringUtils;
import com.src.main.service.DependencyResolver;
import com.src.main.sm.config.StepExecutor;
import com.src.main.util.GradleVersionResolver;
import com.src.main.util.GradleWrapperInstaller;
import com.src.main.util.InitializrGradleGenerator;
import com.src.main.util.InitializrPomGenerator;
import com.src.main.util.DatabaseDependencyCatalog;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.sm.executor.common.BoilerplateStyle;
import com.src.main.sm.executor.common.BoilerplateStyleResolver;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Component
public class ScaffoldExecutor implements StepExecutor {

	private static final Logger log = LoggerFactory.getLogger(ScaffoldExecutor.class);
	
	private static final String TPL_README = "templates/project/README.md.mustache";
	private static final String TPL_VALIDATION_MESSAGES = "templates/project/messages.properties.mustache";
	private static final String TPL_VALIDATION_CONFIG_JAVA = "validation-message-config.java.mustache";
	private static final String TPL_VALIDATION_CONFIG_KOTLIN = "validation-message-config.kt.mustache";
	private static final String TPL_MAIN_JAVA = "main.java.mustache";
	private static final String TPL_MAIN_KOTLIN = "main.kt.mustache";
	
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
		
		final boolean angular = boolOr(data, ProjectMetaDataConstants.EXTRAS_ANGULAR_INTEGRATION, false);
		
		final Path root = resolveRoot(data);
		
		final Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get("yaml");
		final GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);
			final boolean openapi = resolveOpenApiEnabled(data, yaml) && hasRestEndpointEntities(yaml);
			final boolean includeLombok = resolveLombokEnabled(yaml);
			final String databaseCode = resolveDatabaseCode(yaml);
			final List<String> depReq = resolveDependenciesFromYaml(yaml);


		createMinimalLayout(root, packageName, buildTool, language);
			List<MavenDependencyDTO> resolvedDeps = dependencyResolver.resolveForMaven(depReq, bootVersion, openapi);
			List<MavenDependencyDTO> deps = new ArrayList<>(resolvedDeps == null ? List.of() : resolvedDeps);
			enrichDependenciesWithDatabase(deps, databaseCode);

		InitializrProjectModel model = new InitializrProjectModel(groupId, artifactId, version, name, description,
				packaging, generator, jdkVersion, bootVersion, openapi, includeLombok, angular);

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
		writeMainClass(root, packageName, mainClassName, language);
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

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private static boolean boolOr(ExtendedState data, String key, boolean dflt) {
		Object v = data.getVariables().get(key);
		if (v == null)
			return dflt;
		if (v instanceof Boolean)
			return (Boolean) v;
		return Boolean.parseBoolean(v.toString());
	}

	@SuppressWarnings("unchecked")
	private static boolean resolveOpenApiEnabled(ExtendedState data, Map<String, Object> yaml) {
		Object explicit = data.getVariables().get(ProjectMetaDataConstants.EXTRAS_OPENAPI);
		if (explicit != null) {
			return parseBoolean(explicit, false);
		}
		if (yaml != null) {
			Object raw = yaml.get("enableOpenapi");
			if (raw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
				raw = ((Map<String, Object>) appRaw).get("enableOpenapi");
			}
			if (raw != null) {
				return parseBoolean(raw, false);
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static boolean resolveActuatorEnabled(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object raw = yaml.get("enableActuator");
		if (raw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			raw = ((Map<String, Object>) appRaw).get("enableActuator");
		}
		return parseBoolean(raw, false);
	}

	private static boolean parseBoolean(Object value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Boolean bool) {
			return bool;
		}
		String normalized = String.valueOf(value).trim().toLowerCase();
		if (normalized.isEmpty()) {
			return defaultValue;
		}
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
	}

	private static boolean resolveLombokEnabled(Map<String, Object> yaml) {
		return BoilerplateStyleResolver.resolveFromYaml(yaml, true) == BoilerplateStyle.LOMBOK;
	}

	@SuppressWarnings("unchecked")
	private static boolean hasEntities(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object modelsRaw = yaml.get("models");
		if (!(modelsRaw instanceof List<?> models)) {
			return false;
		}
		return models.stream().anyMatch(item -> item instanceof Map<?, ?>);
	}

	@SuppressWarnings("unchecked")
	private static boolean hasRestEndpointEntities(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object modelsRaw = yaml.get("models");
		if (!(modelsRaw instanceof List<?> models)) {
			return false;
		}
		return models.stream()
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.map(modelRaw -> ((Map<String, Object>) modelRaw).get("addRestEndpoints"))
				.anyMatch(addRestEndpoints -> parseBoolean(addRestEndpoints, false));
	}

	private static List<String> removeJpaDependencies(List<String> dependencies) {
		if (dependencies == null || dependencies.isEmpty()) {
			return List.of();
		}
		return dependencies.stream()
				.filter(Objects::nonNull)
				.filter(dependency -> {
					String token = dependency.trim().toLowerCase();
					return !"jpa".equals(token) && !"data-jpa".equals(token)
							&& !"spring-boot-starter-data-jpa".equals(token);
				})
				.collect(java.util.stream.Collectors.toList());
	}

	private static List<String> ensureJpaDependencies(List<String> dependencies) {
		List<String> merged = new ArrayList<>();
		if (dependencies != null) {
			merged.addAll(dependencies);
		}
		boolean hasJpa = merged.stream().filter(item -> item != null).map(String::trim).map(String::toLowerCase)
				.anyMatch(token -> "jpa".equals(token) || "data-jpa".equals(token)
						|| "spring-boot-starter-data-jpa".equals(token));
		if (!hasJpa) {
			merged.add("data-jpa");
		}
		return merged;
	}

	@SuppressWarnings("unchecked")
	private static List<String> extractDependenciesFromYaml(Map<String, Object> yaml) {
		if (yaml == null) {
			return List.of();
		}
		Object depsRaw = yaml.get("dependencies");
		if (!(depsRaw instanceof List<?> rawList)) {
			return List.of();
		}
		return rawList.stream()
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.collect(java.util.stream.Collectors.toList());
	}

	private static List<String> resolveDependenciesFromYaml(Map<String, Object> yaml) {
		Set<String> dependencies = new LinkedHashSet<>(extractDependenciesFromYaml(yaml));
		boolean noSql = isNoSqlDatabase(yaml);

		if (hasRestEndpointEntities(yaml)) {
			dependencies.add("web");
		}
		if (hasValidationConstraints(yaml)) {
			dependencies.add("validation");
		}
		if (resolveActuatorEnabled(yaml)) {
			dependencies.add("actuator");
		}

		List<String> merged = noSql
				? removeJpaDependencies(new ArrayList<>(dependencies))
				: (hasEntities(yaml)
						? ensureJpaDependencies(new ArrayList<>(dependencies))
						: removeJpaDependencies(new ArrayList<>(dependencies)));

		return merged.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(item -> !item.isEmpty())
				.collect(java.util.stream.Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private static boolean hasValidationMessages(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object messagesRaw = yaml.get("messages");
		if (!(messagesRaw instanceof Map<?, ?> messagesMap) || messagesMap.isEmpty()) {
			return false;
		}
		return messagesMap.keySet().stream()
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.anyMatch(key -> key.startsWith("validation."));
	}

	@SuppressWarnings("unchecked")
	private static boolean hasValidationConstraints(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		if (hasValidationMessages(yaml)) {
			return true;
		}

		Object modelsRaw = yaml.get("models");
		boolean modelHasConstraints = modelsRaw instanceof List<?> models && models.stream()
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.flatMap(modelRaw -> {
					Object fieldsRaw = ((Map<String, Object>) modelRaw).get("fields");
					if (!(fieldsRaw instanceof List<?> fields)) {
						return java.util.stream.Stream.empty();
					}
					return fields.stream();
				})
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.map(fieldRaw -> ((Map<String, Object>) fieldRaw).get("constraints"))
				.anyMatch(constraints -> constraints instanceof List<?> list && !list.isEmpty());
		if (modelHasConstraints) {
			return true;
		}

		Object dtosRaw = yaml.get("dtos");
		if (!(dtosRaw instanceof List<?> dtos)) {
			return false;
		}
		return dtos.stream()
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.anyMatch(dtoRaw -> {
					Map<String, Object> dtoMap = (Map<String, Object>) dtoRaw;
					Object classConstraints = dtoMap.get("classConstraints");
					if (classConstraints instanceof List<?> list && !list.isEmpty()) {
						return true;
					}
					Object fieldsRaw = dtoMap.get("fields");
					if (!(fieldsRaw instanceof List<?> fields)) {
						return false;
					}
					return fields.stream()
							.filter(Map.class::isInstance)
							.map(Map.class::cast)
							.map(fieldRaw -> ((Map<String, Object>) fieldRaw).get("constraints"))
							.anyMatch(constraints -> constraints instanceof List<?> list && !list.isEmpty());
				});
	}

	@SuppressWarnings("unchecked")
	private static boolean isNoSqlDatabase(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object dbTypeRaw = yaml.get("dbType");
		if (dbTypeRaw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			dbTypeRaw = ((Map<String, Object>) appRaw).get("dbType");
		}
		if (dbTypeRaw != null && "NOSQL".equalsIgnoreCase(String.valueOf(dbTypeRaw).trim())) {
			return true;
		}
		String dbCode = resolveDatabaseCode(yaml);
		return dbCode != null && "MONGODB".equalsIgnoreCase(dbCode.trim());
	}

	@SuppressWarnings("unchecked")
	private static String resolveDatabaseCode(Map<String, Object> yaml) {
		if (yaml == null) {
			return null;
		}
		Object db = yaml.get("database");
		if (db != null) {
			return String.valueOf(db);
		}
		Object appRaw = yaml.get("app");
		if (appRaw instanceof Map<?, ?> appMap) {
			Object appDb = ((Map<String, Object>) appMap).get("database");
			if (appDb != null) {
				return String.valueOf(appDb);
			}
		}
		return null;
	}

	private static void enrichDependenciesWithDatabase(List<MavenDependencyDTO> deps, String databaseCode) {
		if (deps == null) {
			return;
		}
		DatabaseDependencyCatalog.resolve(databaseCode).ifPresent(databaseDependency -> {
			boolean exists = deps.stream().filter(Objects::nonNull).anyMatch(existing ->
					databaseDependency.groupId().equalsIgnoreCase(existing.groupId())
							&& databaseDependency.artifactId().equalsIgnoreCase(existing.artifactId()));
			if (!exists) {
				deps.add(databaseDependency);
			}
		});
	}

	private static void createMinimalLayout(Path root, String packageName, String buildTool, GenerationLanguage language) throws Exception {
		Path mainSource = root.resolve(PathUtils.srcPathFromPackage(packageName, language));
		Path mainRes = root.resolve("src/main/resources");
		Path testSource = root.resolve("src/test/" + language.templateFolder() + "/" + packageName.replace('.', '/'));
		Files.createDirectories(mainSource);
		Files.createDirectories(mainRes);
		Files.createDirectories(testSource);
	}

	private void writeMainClass(Path root, String packageName, String mainClassName, GenerationLanguage language) throws Exception {
		Path target = root.resolve(PathUtils.srcPathFromPackage(packageName, language))
				.resolve(mainClassName + "." + language.fileExtension());
		String mainTemplate = language == GenerationLanguage.KOTLIN ? TPL_MAIN_KOTLIN : TPL_MAIN_JAVA;
		String rendered = tpl.renderAny(TemplatePathResolver.candidates(language, "project", mainTemplate),
				Map.of("basePkg", packageName, "mainClass", mainClassName));

		Files.writeString(target, rendered, UTF_8);
	}

	private void writeResources(Path root, String appName, Map<String, Object> yaml) throws Exception {
		Path resDir = root.resolve("src/main/resources");
		Files.createDirectories(resDir);
		writeMessagesIfAny(root, yaml);
		writeValidationConfigIfAny(root, yaml);
	}

	private void writeDocsAndGitignore(Path root, String appName) throws Exception {
		// Ensure project root exists
		Files.createDirectories(root);

		// Template rendering helper (assuming tpl is your Mustache or TemplateEngine
		// instance)
		String readmeRendered = tpl.render(TPL_README, Map.of("appName", appName));

		// Write README.md
		Path readmePath = root.resolve("README.md");
		Files.writeString(readmePath, readmeRendered, StandardCharsets.UTF_8);
	}

	private static String toPascal(String s) {
		if (s == null || s.isBlank())
			return "Application";
		String[] parts = s.replace('-', ' ').replace('_', ' ').trim().split("\\s+");
		return java.util.Arrays.stream(parts)
				.filter(p -> !p.isEmpty())
				.map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1))
				.collect(java.util.stream.Collectors.joining());
	}

	@SuppressWarnings("unchecked")
	private void writeMessagesIfAny(Path root, Map<String, Object> yaml) {
		try {
			if (yaml == null) {
				return;
			}
			Object messagesRaw = yaml.get("messages");
			if (!(messagesRaw instanceof Map<?, ?> messagesMap) || messagesMap.isEmpty()) {
				return;
			}
			boolean hasValidationKey = messagesMap.keySet().stream().map(String::valueOf)
					.anyMatch(key -> key.startsWith("validation."));
			if (!hasValidationKey) {
				return;
			}

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
			if (yaml.get("messages") instanceof Map<?, ?> msgs) {
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
			entries.stream()
					.filter(Objects::nonNull)
					.filter(e -> e.get("key") != null)
					.forEach(e -> sb.append(escapePropKey(e.get("key")))
							.append('=')
							.append(escapePropValue(e.get("value") == null ? "" : e.get("value")))
							.append(System.lineSeparator()));
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private void writeValidationConfigIfAny(Path root, Map<String, Object> yaml) {
		try {
			if (yaml == null) {
				return;
			}
			Object messagesRaw = yaml.get("messages");
			if (!(messagesRaw instanceof Map<?, ?> messagesMap) || messagesMap.isEmpty()) {
				return;
			}
			boolean hasValidationKey = messagesMap.keySet().stream().map(String::valueOf)
					.anyMatch(key -> key.startsWith("validation."));
			if (!hasValidationKey) {
				return;
			}

			String basePackage = StringUtils.firstNonBlank(str(yaml.get("basePackage")), ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), "technical");
			String configPackage = "domain".equalsIgnoreCase(packageStructure)
					? basePackage + ".domain.config"
					: basePackage + ".config";
			GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);
			Path outDir = root.resolve(PathUtils.srcPathFromPackage(configPackage, language));
			Files.createDirectories(outDir);
			String configTemplate = language == GenerationLanguage.KOTLIN ? TPL_VALIDATION_CONFIG_KOTLIN
					: TPL_VALIDATION_CONFIG_JAVA;
			String content = tpl.renderAny(TemplatePathResolver.candidates(language, "validation", configTemplate),
					Map.of("packageName", configPackage));
			Files.writeString(outDir.resolve("ValidationMessageConfig." + language.fileExtension()), content, UTF_8);
		} catch (Exception ex) {
			log.warn("Failed to write ValidationMessageConfig: {}", ex.getMessage());
		}
	}

	/** Minimal .properties escaping for keys. */
	private static String escapePropKey(String s) {
		StringBuilder b = new StringBuilder(s.length() + 8);
		s.chars().forEach(ch -> {
			char c = (char) ch;
			switch (c) {
			case ' ', '\t', '\f', '=', ':', '#', '!' -> {
				b.append('\\').append(c);
			}
			case '\\' -> b.append("\\\\");
			case '\n' -> b.append("\\n");
			case '\r' -> b.append("\\r");
			default -> b.append(c);
			}
		});
		return b.toString();
	}

	/** Minimal .properties escaping for values. */
	private static String escapePropValue(String s) {
		StringBuilder b = new StringBuilder(s.length() + 8);
		s.chars().forEach(ch -> {
			char c = (char) ch;
			switch (c) {
			case '\\' -> b.append("\\\\");
			case '\n' -> b.append("\\n");
			case '\r' -> b.append("\\r");
			default -> b.append(c);
			}
		});
		return b.toString();
	}
}
