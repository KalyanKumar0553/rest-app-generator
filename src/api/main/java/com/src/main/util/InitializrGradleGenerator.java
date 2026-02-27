package com.src.main.util;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.src.main.dto.InitializrProjectModel;
import com.src.main.dto.MavenDependencyDTO;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GroovyDslGradleBuildWriter;
import io.spring.initializr.generator.buildsystem.gradle.GroovyDslGradleSettingsWriter;
import io.spring.initializr.generator.buildsystem.gradle.KotlinDslGradleBuildWriter;
import io.spring.initializr.generator.buildsystem.gradle.KotlinDslGradleSettingsWriter;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.version.VersionReference;

@Component
public class InitializrGradleGenerator {

	private final GroovyDslGradleBuildWriter groovyBuildWriter = new GroovyDslGradleBuildWriter();
	private final GroovyDslGradleSettingsWriter groovySettingsWriter = new GroovyDslGradleSettingsWriter();
	private final KotlinDslGradleBuildWriter kotlinBuildWriter = new KotlinDslGradleBuildWriter();
	private final KotlinDslGradleSettingsWriter kotlinSettingsWriter = new KotlinDslGradleSettingsWriter();
	private static final String SPRINGDOC_VERSION = "2.6.0";
	private static final String SWAGGER_CORE_VERSION = "2.2.22";

	public GradleFiles generateFiles(InitializrProjectModel model, List<MavenDependencyDTO> deps) {
		Objects.requireNonNull(model, "model must not be null");
		String groupId = model.getGroupId();
		String artifact = model.getArtifactId();
		String bootVer = model.getBootVersion();
		String appVer = model.getVersion();
		String packaging = model.getPackaging();
		String jdk = model.getJdkVersion();
		boolean kotlin = isKotlinDsl(model.getGenerator());
		String mainClassFqcn = groupId + "." + toPascal(artifact) + "Application" + (kotlin ? "Kt" : "");

		GradleBuild build = createBaseBuild(groupId, artifact, bootVer, appVer, packaging, jdk);
		addModelDependencies(build, deps);
		addStandardDependencies(build, packaging, model.isIncludeOpenapi(), hasJpaDependency(deps),
				model.isIncludeLombok());

		String buildContent = renderBuild(build, kotlin, jdk, mainClassFqcn);
		String settingsContent = renderSettings(artifact, build, kotlin);

		String buildFileName = kotlin ? "build.gradle.kts" : "build.gradle";
		String settingsFileName = kotlin ? "settings.gradle.kts" : "settings.gradle";

		return new GradleFiles(buildFileName, buildContent, settingsFileName, settingsContent);
	}

	public String generateGradle(InitializrProjectModel model, List<MavenDependencyDTO> deps) {
		return generateFiles(model, deps).getBuildContent();
	}

	private GradleBuild createBaseBuild(String groupId, String artifactId, String bootVersion, String appVersion,
			String packaging, String jdk) {

		GradleBuild build = new GradleBuild();

		// project coordinates
		build.settings().group(groupId);
		build.settings().artifact(artifactId);
		build.settings().version(appVersion);

		// plugins
		build.plugins().add("org.springframework.boot", plugin -> plugin.setVersion(bootVersion));
		build.plugins().add("io.spring.dependency-management", plugin -> plugin.setVersion(ProjectMetaDataConstants.DEP_MGMT_PLUGIN_VERSION));
		build.plugins().add("java");
		if (isWarPackaged(packaging)) {
			build.plugins().add("war");
		}

		// repositories -> mavenCentral
		configureRepositories(build);

		return build;
	}

	private void configureRepositories(GradleBuild build) {
		build.repositories().add("mavenCentral",
				MavenRepository.withIdAndUrl("mavenCentral", "https://repo.maven.apache.org/maven2").build());
	}

	private void addModelDependencies(GradleBuild build, List<MavenDependencyDTO> deps) {
		if (deps == null) {
			return;
		}
		deps.stream()
				.filter(Objects::nonNull)
				.map(md -> new String[] { trimOrNull(md.groupId()), trimOrNull(md.artifactId()), md.scope() })
				.filter(parts -> parts[0] != null && parts[1] != null)
				.forEach(parts -> build.dependencies().add(parts[0] + ":" + parts[1],
						Dependency.withCoordinates(parts[0], parts[1]).scope(toScope(parts[2]))));
	}

	private void addStandardDependencies(GradleBuild build, String packaging, boolean includeOpenApi, boolean includeJpa,
			boolean includeLombok) {
		if (includeLombok) {
			build.dependencies().add("lombok",
					Dependency.withCoordinates("org.projectlombok", "lombok").scope(DependencyScope.COMPILE_ONLY));
			build.dependencies().add("lombok-ap",
					Dependency.withCoordinates("org.projectlombok", "lombok").scope(DependencyScope.ANNOTATION_PROCESSOR));
		}

		if (includeJpa) {
			// Jakarta Persistence API
			build.dependencies().add("jakarta-persistence-api",
					Dependency.withCoordinates("jakarta.persistence", "jakarta.persistence-api")
							.version(VersionReference.ofValue("3.1.0")).scope(DependencyScope.COMPILE_ONLY));
		}

		// Tomcat provided for WAR
		if (isWarPackaged(packaging)) {
			build.dependencies().add("tomcat",
					Dependency.withCoordinates("org.springframework.boot", "spring-boot-starter-tomcat")
							.scope(DependencyScope.PROVIDED_RUNTIME));
		}
		if (includeOpenApi) {
			build.dependencies().add("springdoc-openapi-ui",
					Dependency.withCoordinates("org.springdoc", "springdoc-openapi-starter-webmvc-ui")
							.version(VersionReference.ofValue(SPRINGDOC_VERSION))
							.scope(DependencyScope.COMPILE));
			build.dependencies().add("swagger-models",
					Dependency.withCoordinates("io.swagger.core.v3", "swagger-models")
							.version(VersionReference.ofValue(SWAGGER_CORE_VERSION))
							.scope(DependencyScope.COMPILE));
			build.dependencies().add("swagger-annotations",
					Dependency.withCoordinates("io.swagger.core.v3", "swagger-annotations")
							.version(VersionReference.ofValue(SWAGGER_CORE_VERSION))
							.scope(DependencyScope.COMPILE));
		}
	}

	private String renderBuild(GradleBuild build, boolean kotlinDsl, String jdk, String mainClassFqcn) {
	    StringWriter out = new StringWriter();
	    try (IndentingWriter iw = new IndentingWriter(out, s -> "    ")) {
	        if (kotlinDsl) {
	            kotlinBuildWriter.writeTo(iw, build);
	        } else {
	            groovyBuildWriter.writeTo(iw, build);
	        }
	    } catch (Exception e) {
	        throw new IllegalStateException("Failed to render build.gradle" + (kotlinDsl ? ".kts" : ""), e);
	    }

	    String result = out.toString();
	    String normalizedJdk = normalizeJavaVersion(jdk); // e.g. "17", "21", handles "1.8" → "8"

	    // If the writer generated a toolchain block, force its version to match normalizedJdk
	    // Handles any of: JavaLanguageVersion.of(8), of(11), of(17), of(21), with or without quotes
	    if (result.contains("JavaLanguageVersion.of(")) {
	        result = result.replaceAll(
	                "JavaLanguageVersion\\.of\\([^)]*\\)",
	                "JavaLanguageVersion.of(" + normalizedJdk + ")"
	        );
	    }

	    if (kotlinDsl) {
	    	result = ensureKotlinGradlePlugins(result);
	    	result = ensureKotlinBootMainClass(result, mainClassFqcn);
	    }

	    return result;
	}

	private String ensureKotlinGradlePlugins(String gradleKts) {
		if (gradleKts == null || gradleKts.isBlank() || gradleKts.contains("org.jetbrains.kotlin.jvm")) {
			return gradleKts;
		}
		String kotlinPlugins = """
        id("org.jetbrains.kotlin.jvm") version "1.9.25"
        id("org.jetbrains.kotlin.plugin.spring") version "1.9.25"
        id("org.jetbrains.kotlin.plugin.jpa") version "1.9.25"
""";
		return gradleKts.replaceFirst("(?m)^\\s*id\\(\"java\"\\)\\s*$", kotlinPlugins + "    id(\"java\")");
	}

	private String ensureKotlinBootMainClass(String gradleKts, String mainClassFqcn) {
		if (gradleKts == null || gradleKts.isBlank() || mainClassFqcn == null || mainClassFqcn.isBlank()
				|| gradleKts.contains("tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>(\"bootJar\")")) {
			return gradleKts;
		}
		return gradleKts + """

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    mainClass.set("%s")
}
""".formatted(mainClassFqcn);
	}

	private String renderSettings(String artifactId, GradleBuild build, boolean kotlinDsl) {
		StringWriter out = new StringWriter();
		try (IndentingWriter iw = new IndentingWriter(out, s -> "    ")) {
			if (kotlinDsl) {
				kotlinSettingsWriter.writeTo(iw, build);
			} else {
				groovySettingsWriter.writeTo(iw, build);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to render settings.gradle" + (kotlinDsl ? ".kts" : ""), e);
		}
		return out.toString();
	}

	// --- small helpers ---

	private static boolean isWarPackaged(String packaging) {
		return "war".equalsIgnoreCase(packaging);
	}

	private static boolean isKotlinDsl(String raw) {
		if (raw == null)
			return false;
		String v = raw.trim().toLowerCase();
		return "kotlin".equals(v) || "kotlin-dsl".equals(v) || "kts".equals(v);
	}

	private static String normalizeJavaVersion(String v) {
		if (v == null || v.isBlank())
			return "21";
		String trimmed = v.trim();
		if ("8".equals(trimmed) || "1.8".equals(trimmed))
			return "8";
		return trimmed;
	}

	private static String toPascal(String raw) {
		if (raw == null || raw.isBlank()) {
			return "MyApp";
		}
		String normalized = raw.replace('-', ' ').replace('_', ' ');
		StringBuilder result = new StringBuilder();
		for (String token : normalized.trim().split("\\s+")) {
			if (token.isBlank()) {
				continue;
			}
			result.append(Character.toUpperCase(token.charAt(0)));
			if (token.length() > 1) {
				result.append(token.substring(1).toLowerCase());
			}
		}
		return result.isEmpty() ? "MyApp" : result.toString();
	}

	private static String req(String v, String name) {
		if (v == null || v.isBlank()) {
			throw new IllegalArgumentException(name + " must not be null/blank");
		}
		return v.trim();
	}

	private static String nz(String v, String dflt) {
		return (v == null || v.isBlank()) ? dflt : v.trim();
	}

	private static String trimOrNull(String v) {
		return (v == null || v.isBlank()) ? null : v.trim();
	}

	private static boolean hasJpaDependency(List<MavenDependencyDTO> deps) {
		if (deps == null || deps.isEmpty()) {
			return false;
		}
		return deps.stream().filter(Objects::nonNull)
				.anyMatch(d -> "org.springframework.boot".equalsIgnoreCase(d.groupId())
						&& "spring-boot-starter-data-jpa".equalsIgnoreCase(d.artifactId()));
	}

	private DependencyScope toScope(String raw) {
		if (raw == null || raw.isBlank()) {
			return DependencyScope.COMPILE;
		}
		switch (raw.trim().toLowerCase()) {
		case "annotation_processor":
		case "annotation-processor":
			return DependencyScope.ANNOTATION_PROCESSOR;
		case "compile_only":
		case "compile-only":
			return DependencyScope.COMPILE_ONLY;
		case "runtime":
		case "runtimeonly":
			return DependencyScope.RUNTIME;
		case "provided":
		case "providedruntime":
		case "provided_runtime":
			return DependencyScope.PROVIDED_RUNTIME;
		case "test":
		case "test_compile":
		case "test-compile":
			return DependencyScope.TEST_COMPILE;
		case "test_runtime":
		case "test-runtime":
			return DependencyScope.TEST_RUNTIME;
		default:
			return DependencyScope.COMPILE;
		}
	}

	public static class GradleFiles {
		private final String buildFileName;
		private final String buildContent;
		private final String settingsFileName;
		private final String settingsContent;

		public GradleFiles(String buildFileName, String buildContent, String settingsFileName, String settingsContent) {
			this.buildFileName = buildFileName;
			this.buildContent = buildContent;
			this.settingsFileName = settingsFileName;
			this.settingsContent = settingsContent;
		}

		public String getBuildFileName() {
			return buildFileName;
		}

		public String getBuildContent() {
			return buildContent;
		}

		public String getSettingsFileName() {
			return settingsFileName;
		}

		public String getSettingsContent() {
			return settingsContent;
		}
	}
}
