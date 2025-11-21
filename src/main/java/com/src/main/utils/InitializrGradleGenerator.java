package com.src.main.utils;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.src.main.dto.InitializrProjectModel;
import com.src.main.dto.MavenDependency;

import io.spring.initializr.generator.buildsystem.Dependency;
import io.spring.initializr.generator.buildsystem.DependencyScope;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuild;
import io.spring.initializr.generator.buildsystem.gradle.GroovyDslGradleBuildWriter;
import io.spring.initializr.generator.buildsystem.gradle.GroovyDslGradleSettingsWriter;
import io.spring.initializr.generator.buildsystem.gradle.KotlinDslGradleBuildWriter;
import io.spring.initializr.generator.buildsystem.gradle.KotlinDslGradleSettingsWriter;
import io.spring.initializr.generator.io.IndentingWriter;
import io.spring.initializr.generator.version.VersionReference;

@Component
public class InitializrGradleGenerator {

	// Writers for both DSLs
	private final GroovyDslGradleBuildWriter groovyBuildWriter = new GroovyDslGradleBuildWriter();
	private final GroovyDslGradleSettingsWriter groovySettingsWriter = new GroovyDslGradleSettingsWriter();
	private final KotlinDslGradleBuildWriter kotlinBuildWriter = new KotlinDslGradleBuildWriter();
	private final KotlinDslGradleSettingsWriter kotlinSettingsWriter = new KotlinDslGradleSettingsWriter();

	/**
	 * Main API: generate both build + settings files.
	 * 
	 * Uses model.getGenerator() to choose DSL: - "kotlin", "kotlin-dsl", "kts" =>
	 * Kotlin DSL - anything else or null => Groovy DSL
	 */
	public GradleFiles generateFiles(InitializrProjectModel model, List<MavenDependency> deps) {
		Objects.requireNonNull(model, "model must not be null");

		// Decide DSL
		String genRaw = nz(model.getGenerator(), "groovy");
		boolean kotlin = isKotlinDsl(genRaw);

		GradleBuild build = createBaseBuild(model);

		// ----- Dependencies from model -----
		if (deps != null) {
			for (MavenDependency md : deps) {
				if (md == null) {
					continue;
				}
				String g = trimOrNull(md.groupId());
				String a = trimOrNull(md.artifactId());
				if (g == null || a == null) {
					continue;
				}
				build.dependencies().add(g + ":" + a, Dependency.withCoordinates(g, a).scope(toScope(md.scope())));
			}
		}

		// ----- Standard additions -----
		addStandardDependencies(build, model);

		// Render build + settings
		String buildContent = renderBuild(build, kotlin);
		String settingsContent = renderSettings(build, model, kotlin);

		String buildFileName = kotlin ? "build.gradle.kts" : "build.gradle";
		String settingsFileName = kotlin ? "settings.gradle.kts" : "settings.gradle";

		return new GradleFiles(buildFileName, buildContent, settingsFileName, settingsContent);
	}

	/**
	 * Legacy API: kept for backward compatibility. Returns only the build file
	 * content.
	 */
	public String generateGradle(InitializrProjectModel model, List<MavenDependency> deps) {
		return generateFiles(model, deps).getBuildContent();
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private GradleBuild createBaseBuild(InitializrProjectModel model) {
		GradleBuild build = new GradleBuild();

		String groupId = req(model.getGroupId(), "groupId");
		String artifact = req(model.getArtifactId(), "artifactId");
		String bootVer = nz(model.getBootVersion(), "3.3.5");
		String appVer = nz(model.getVersion(), "0.0.1-SNAPSHOT");
		String packaging = nz(model.getPackaging(), "jar");
		String jdk = nz(model.getJdkVersion(), "21");

		// ----- Settings & basic info -----
		build.settings().group(groupId);
		build.settings().artifact(artifact);
		build.settings().version(appVer);

		// Java version (toolchain property etc.)
		build.properties().version("java", jdk);

		build.plugins().add("org.springframework.boot", plugin -> plugin.setVersion(bootVer));

		// Dependency management plugin
		build.plugins().add("io.spring.dependency-management", plugin -> plugin.setVersion("1.1.6"));

		// Java plugin
		build.plugins().add("java");

		// WAR plugin (if applicable)
		if ("war".equalsIgnoreCase(packaging)) {
			build.plugins().add("war");
		}

		return build;
	}

	private void addStandardDependencies(GradleBuild build, InitializrProjectModel model) {
		// Lombok (compile only + annotation processor)
		build.dependencies().add("lombok",
				Dependency.withCoordinates("org.projectlombok", "lombok").scope(DependencyScope.COMPILE_ONLY));

		build.dependencies().add("lombok-ap",
				Dependency.withCoordinates("org.projectlombok", "lombok").scope(DependencyScope.ANNOTATION_PROCESSOR));

		// JPA
		build.dependencies().add("spring-data-jpa",
				Dependency.withCoordinates("org.springframework.boot", "spring-boot-starter-data-jpa")
						.scope(DependencyScope.COMPILE));

		// Jakarta Persistence API
		build.dependencies().add("jakarta-persistence-api",
				Dependency.withCoordinates("jakarta.persistence", "jakarta.persistence-api")
						.version(VersionReference.ofValue("3.1.0")).scope(DependencyScope.COMPILE_ONLY));

		// Tomcat provided for WAR
		String packaging = nz(model.getPackaging(), "jar");
		if ("war".equalsIgnoreCase(packaging)) {
			build.dependencies().add("tomcat",
					Dependency.withCoordinates("org.springframework.boot", "spring-boot-starter-tomcat")
							.scope(DependencyScope.PROVIDED_RUNTIME));
		}

		// Optional H2 (runtime)
//		build.dependencies().add("h2",
//				Dependency.withCoordinates("com.h2database", "h2").scope(DependencyScope.RUNTIME));
	}

	private String renderBuild(GradleBuild build, boolean kotlin) {
		StringWriter out = new StringWriter();
		try (IndentingWriter iw = new IndentingWriter(out, s -> "    ")) {
			if (kotlin) {
				kotlinBuildWriter.writeTo(iw, build);
			} else {
				groovyBuildWriter.writeTo(iw, build);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to render build.gradle" + (kotlin ? ".kts" : ""), e);
		}
		return out.toString();
	}

	private String renderSettings(GradleBuild build, InitializrProjectModel model, boolean kotlin) {
		// You can customize root project name if needed from model.getName() or
		// artifact
		StringWriter out = new StringWriter();
		try (IndentingWriter iw = new IndentingWriter(out, s -> "    ")) {
			if (kotlin) {
				kotlinSettingsWriter.writeTo(iw, build);
			} else {
				groovySettingsWriter.writeTo(iw, build);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to render settings.gradle" + (kotlin ? ".kts" : ""), e);
		}
		return out.toString();
	}

	private static boolean isKotlinDsl(String genRaw) {
		if (genRaw == null)
			return false;
		String v = genRaw.trim().toLowerCase();
		return "kotlin".equals(v) || "kotlin-dsl".equals(v) || "kts".equals(v);
	}

	private static String req(String v, String name) {
		if (v == null || v.isBlank())
			throw new IllegalArgumentException(name + " must not be null/blank");
		return v.trim();
	}

	private static String nz(String v, String dflt) {
		return (v == null || v.isBlank()) ? dflt : v.trim();
	}

	private static String trimOrNull(String v) {
		return (v == null || v.isBlank()) ? null : v.trim();
	}

	private DependencyScope toScope(String raw) {
		if (raw == null || raw.isBlank())
			return DependencyScope.COMPILE;
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

	/**
	 * Simple holder for generated Gradle files.
	 */
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
