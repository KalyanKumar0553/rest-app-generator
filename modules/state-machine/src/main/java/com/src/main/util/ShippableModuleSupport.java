package com.src.main.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.src.main.dto.MavenDependencyDTO;
import com.src.main.sm.executor.common.GenerationLanguage;

public final class ShippableModuleSupport {

	public static final String RESOURCE_ROOT = "shipped-modules";
	public static final String MODULE_BASE_PACKAGE = "com.src.main";

	private static final String LOMBOK_VERSION = "1.18.38";
	private static final String MUSTACHE_VERSION = "0.9.10";
	private static final String INITIALIZR_VERSION = "0.22.0";
	private static final String STATE_MACHINE_VERSION = "3.2.1";
	private static final String JSON_VERSION = "20240303";
	private static final String JACKSON_YAML_VERSION = "2.17.2";
	private static final String GOOGLE_AUTH_VERSION = "1.23.0";
	private static final String GOOGLE_API_CLIENT_VERSION = "2.6.0";
	private static final String GOOGLE_HTTP_JACKSON_VERSION = "1.43.3";
	private static final String NIMBUS_VERSION = "9.37.3";
	private static final String JJWT_VERSION = "0.11.5";
	private static final String AZURE_EMAIL_VERSION = "1.0.0";
	private static final String AZURE_COMMUNICATION_COMMON_VERSION = "1.2.0";
	private static final String AZURE_STORAGE_BLOB_VERSION = "12.31.0";
	private static final String TWILIO_VERSION = "10.3.0";
	private static final String PYTHON_AZURE_STORAGE_BLOB_VERSION = "12.26.0";

	private static final Map<String, ModuleDefinition> MODULES = new LinkedHashMap<>();

	static {
		register(ModuleDefinition.hidden("common")
				.javaVariant(VariantDefinition.builder()
						.externalDependencies(List.of(
								dependency("org.springframework", "spring-web"),
								dependency("com.fasterxml.jackson.core", "jackson-databind"),
								dependency("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", JACKSON_YAML_VERSION),
								dependency("com.fasterxml.jackson.dataformat", "jackson-dataformat-xml"),
								dependency("com.fasterxml.jackson.dataformat", "jackson-dataformat-csv")))
						.build())
				.build());

		register(ModuleDefinition.hidden("communication")
				.javaVariant(VariantDefinition.builder()
						.requires("common")
						.externalDependencies(List.of(
								dependency("org.springframework.boot", "spring-boot-starter"),
								dependency("org.thymeleaf", "thymeleaf"),
								dependency("com.azure", "azure-communication-email", AZURE_EMAIL_VERSION),
								dependency("com.azure", "azure-communication-common", AZURE_COMMUNICATION_COMMON_VERSION),
								dependency("com.twilio.sdk", "twilio", TWILIO_VERSION)))
						.build())
				.build());

		register(ModuleDefinition.visible("rbac")
				.javaVariant(VariantDefinition.builder()
						.externalDependencies(List.of(
								dependency("org.springframework.boot", "spring-boot-starter-web"),
								dependency("org.springframework.boot", "spring-boot-starter-validation"),
								dependency("org.springframework.boot", "spring-boot-starter-data-jpa"),
								dependency("org.springframework.boot", "spring-boot-starter-security")))
						.includesFlywayMigrations(true)
						.build())
				.nodeVariant(VariantDefinition.builder()
						.nodePackageDependencies(List.of())
						.usesNodePrisma(true)
						.pythonPackageDependencies(List.of())
						.build())
				.pythonVariant(VariantDefinition.builder()
						.pythonPackageDependencies(List.of())
						.build())
				.build());

		register(ModuleDefinition.visible("subscription")
				.javaVariant(VariantDefinition.builder()
						.requires("common", "rbac")
						.externalDependencies(List.of(
								dependency("org.springframework.boot", "spring-boot-starter"),
								dependency("org.springframework.boot", "spring-boot-starter-web"),
								dependency("org.springframework.boot", "spring-boot-starter-validation"),
								dependency("org.springframework.boot", "spring-boot-starter-data-jpa"),
								dependency("org.springframework.boot", "spring-boot-starter-security"),
								dependency("org.springframework.boot", "spring-boot-starter-cache"),
								dependency("org.springframework.boot", "spring-boot-starter-aop")))
						.includesFlywayMigrations(true)
						.build())
				.nodeVariant(VariantDefinition.builder()
						.requires("rbac")
						.nodePackageDependencies(List.of())
						.usesNodePrisma(true)
						.pythonPackageDependencies(List.of())
						.build())
				.pythonVariant(VariantDefinition.builder()
						.requires("rbac")
						.pythonPackageDependencies(List.of())
						.build())
				.build());

		register(ModuleDefinition.visible("auth")
				.javaVariant(VariantDefinition.builder()
						.requires("common", "communication", "rbac")
						.externalDependencies(List.of(
								dependency("org.springframework.boot", "spring-boot-starter"),
								dependency("org.springframework.boot", "spring-boot-starter-web"),
								dependency("org.springframework.boot", "spring-boot-starter-actuator"),
								dependency("org.springframework.boot", "spring-boot-starter-validation"),
								dependency("org.springframework.boot", "spring-boot-starter-data-jpa"),
								dependency("org.springframework.boot", "spring-boot-starter-security"),
								dependency("org.springframework.boot", "spring-boot-starter-cache"),
								dependency("org.springframework.boot", "spring-boot-starter-oauth2-client"),
								dependency("org.postgresql", "postgresql", null, "runtime", false),
								dependency("io.jsonwebtoken", "jjwt-api", JJWT_VERSION),
								dependency("io.jsonwebtoken", "jjwt-jackson", JJWT_VERSION, "runtime", false),
								dependency("io.jsonwebtoken", "jjwt-impl", JJWT_VERSION, "runtime", false),
								dependency("com.google.auth", "google-auth-library-oauth2-http", GOOGLE_AUTH_VERSION),
								dependency("com.google.api-client", "google-api-client", GOOGLE_API_CLIENT_VERSION),
								dependency("com.google.http-client", "google-http-client-jackson2", GOOGLE_HTTP_JACKSON_VERSION),
								dependency("com.nimbusds", "nimbus-jose-jwt", NIMBUS_VERSION)))
						.includesFlywayMigrations(true)
						.build())
				.nodeVariant(VariantDefinition.builder()
						.requires("rbac")
						.nodePackageDependencies(List.of(
								nodeDependency("@prisma/client", "^6.5.0"),
								nodeDependency("jsonwebtoken", "^9.0.2"),
								nodeDependency("bcryptjs", "^2.4.3"),
								nodeDevDependency("prisma", "^6.5.0"),
								nodeDevDependency("@types/jsonwebtoken", "^9.0.6"),
								nodeDevDependency("@types/bcryptjs", "^2.4.6")))
						.usesNodePrisma(true)
						.build())
				.pythonVariant(VariantDefinition.builder()
						.requires("rbac")
						.pythonPackageDependencies(List.of())
						.build())
				.build());

		register(ModuleDefinition.visible("state-machine")
				.javaVariant(VariantDefinition.builder()
						.requires("common")
						.externalDependencies(List.of(
								dependency("io.spring.initializr", "initializr-generator-spring", INITIALIZR_VERSION),
								dependency("org.springframework.boot", "spring-boot-starter"),
								dependency("org.springframework.boot", "spring-boot-starter-cache"),
								dependency("org.springframework.boot", "spring-boot-starter-validation"),
								dependency("org.springframework.boot", "spring-boot-starter-webflux"),
								dependency("org.springframework.statemachine", "spring-statemachine-starter", STATE_MACHINE_VERSION),
								dependency("org.yaml", "snakeyaml"),
								dependency("com.github.spullara.mustache.java", "compiler", MUSTACHE_VERSION),
								dependency("org.json", "json", JSON_VERSION),
								dependency("org.apache.commons", "commons-lang3"),
								dependency("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", JACKSON_YAML_VERSION)))
						.build())
				.nodeVariant(VariantDefinition.builder()
						.nodePackageDependencies(List.of())
						.usesNodePrisma(true)
						.build())
				.pythonVariant(VariantDefinition.builder()
						.pythonPackageDependencies(List.of())
						.build())
				.build());

		register(ModuleDefinition.visible("swagger")
				.javaVariant(VariantDefinition.builder()
						.externalDependencies(List.of(
								dependency("org.springdoc", "springdoc-openapi-starter-webmvc-ui", "2.6.0"),
								dependency("io.swagger.core.v3", "swagger-models", "2.2.22"),
								dependency("io.swagger.core.v3", "swagger-annotations", "2.2.22")))
						.build())
				.nodeVariant(VariantDefinition.builder()
						.nodePackageDependencies(List.of(
								nodeDependency("swagger-ui-express", "^5.0.1"),
								nodeDevDependency("@types/swagger-ui-express", "^4.1.8")))
						.build())
				.pythonVariant(VariantDefinition.builder()
						.pythonPackageDependencies(List.of(
								pythonDependency("pydantic", "2.10.6")))
						.build())
				.build());

		register(ModuleDefinition.visible("cdn")
				.javaVariant(VariantDefinition.builder()
						.requires("common")
						.externalDependencies(List.of(
								dependency("org.springframework.boot", "spring-boot-starter"),
								dependency("org.springframework.boot", "spring-boot-starter-web"),
								dependency("org.springframework.boot", "spring-boot-starter-validation"),
								dependency("org.springframework.boot", "spring-boot-starter-data-jpa"),
								dependency("org.springframework.boot", "spring-boot-starter-security"),
								dependency("com.azure", "azure-storage-blob", AZURE_STORAGE_BLOB_VERSION),
								dependency("org.postgresql", "postgresql", null, "runtime", false)))
						.includesFlywayMigrations(true)
						.build())
				.nodeVariant(VariantDefinition.builder()
						.nodePackageDependencies(List.of(
								nodeDependency("@azure/storage-blob", "^12.28.0"),
								nodeDependency("multer", "^2.0.2"),
								nodeDevDependency("@types/multer", "^2.0.0")))
						.build())
				.pythonVariant(VariantDefinition.builder()
						.pythonPackageDependencies(List.of(
								pythonDependency("python-multipart", "0.0.20"),
								pythonDependency("azure-storage-blob", PYTHON_AZURE_STORAGE_BLOB_VERSION)))
						.build())
				.build());
	}

	private ShippableModuleSupport() {
	}

	public static List<String> visibleModuleIds() {
		return MODULES.values().stream()
				.filter(ModuleDefinition::visible)
				.map(ModuleDefinition::id)
				.toList();
	}

	public static boolean isShippableModule(String id) {
		return MODULES.containsKey(normalize(id));
	}

	public static boolean isVisibleModule(String id) {
		ModuleDefinition definition = MODULES.get(normalize(id));
		return definition != null && definition.visible();
	}

	public static List<String> expandSelectedModules(Collection<String> selected) {
		return expandSelectedModules(selected, GenerationLanguage.JAVA);
	}

	public static List<String> expandSelectedModules(Collection<String> selected, GenerationLanguage language) {
		LinkedHashSet<String> ordered = new LinkedHashSet<>();
		if (selected == null) {
			return List.of();
		}
		for (String moduleId : selected) {
			String normalized = normalize(moduleId);
			if (!MODULES.containsKey(normalized)) {
				continue;
			}
			visit(normalized, ordered, new LinkedHashSet<>(), language);
		}
		return List.copyOf(ordered);
	}

	public static List<MavenDependencyDTO> resolveExternalDependencies(Collection<String> selectedModules) {
		return resolveExternalDependencies(selectedModules, GenerationLanguage.JAVA);
	}

	public static List<MavenDependencyDTO> resolveExternalDependencies(Collection<String> selectedModules,
			GenerationLanguage language) {
		List<String> expanded = expandSelectedModules(selectedModules, language);
		LinkedHashSet<MavenDependencyDTO> dependencies = new LinkedHashSet<>();
		for (String moduleId : expanded) {
			VariantDefinition definition = variant(moduleId, language);
			if (definition == null) {
				continue;
			}
			dependencies.addAll(definition.externalDependencies());
		}
		if (requiresFlyway(expanded, language)) {
			dependencies.add(dependency("org.flywaydb", "flyway-core"));
		}
		return List.copyOf(dependencies);
	}

	public static List<NodePackageDependency> resolveNodePackageDependencies(Collection<String> selectedModules) {
		List<String> expanded = expandSelectedModules(selectedModules, GenerationLanguage.NODE);
		LinkedHashSet<NodePackageDependency> dependencies = new LinkedHashSet<>();
		for (String moduleId : expanded) {
			VariantDefinition definition = variant(moduleId, GenerationLanguage.NODE);
			if (definition == null) {
				continue;
			}
			dependencies.addAll(definition.nodePackageDependencies());
		}
		return List.copyOf(dependencies);
	}

	public static List<PythonPackageDependency> resolvePythonPackageDependencies(Collection<String> selectedModules) {
		List<String> expanded = expandSelectedModules(selectedModules, GenerationLanguage.PYTHON);
		LinkedHashSet<PythonPackageDependency> dependencies = new LinkedHashSet<>();
		for (String moduleId : expanded) {
			VariantDefinition definition = variant(moduleId, GenerationLanguage.PYTHON);
			if (definition == null) {
				continue;
			}
			dependencies.addAll(definition.pythonPackageDependencies());
		}
		return List.copyOf(dependencies);
	}

	public static boolean requiresFlyway(Collection<String> selectedModules) {
		return requiresFlyway(selectedModules, GenerationLanguage.JAVA);
	}

	public static boolean requiresFlyway(Collection<String> selectedModules, GenerationLanguage language) {
		return expandSelectedModules(selectedModules, language).stream()
				.map(moduleId -> variant(moduleId, language))
				.filter(Objects::nonNull)
				.anyMatch(VariantDefinition::includesFlywayMigrations);
	}

	public static boolean requiresModuleScanning(Collection<String> selectedModules) {
		return !expandSelectedModules(selectedModules, GenerationLanguage.JAVA).isEmpty();
	}

	public static boolean requiresNodePrisma(Collection<String> selectedModules) {
		return expandSelectedModules(selectedModules, GenerationLanguage.NODE).stream()
				.map(moduleId -> variant(moduleId, GenerationLanguage.NODE))
				.filter(Objects::nonNull)
				.anyMatch(VariantDefinition::usesNodePrisma);
	}

	public static String resourcePrefix(String moduleId) {
		return resourcePrefix(moduleId, GenerationLanguage.JAVA);
	}

	public static String resourcePrefix(String moduleId, GenerationLanguage language) {
		if (language == GenerationLanguage.NODE) {
			return RESOURCE_ROOT + "/node/" + normalize(moduleId);
		}
		if (language == GenerationLanguage.PYTHON) {
			return RESOURCE_ROOT + "/python/" + normalize(moduleId);
		}
		return RESOURCE_ROOT + "/" + normalize(moduleId);
	}

	public static String resourceSearchPattern(String moduleId) {
		return resourceSearchPattern(moduleId, GenerationLanguage.JAVA);
	}

	public static String resourceSearchPattern(String moduleId, GenerationLanguage language) {
		return resourcePrefix(moduleId, language) + "/**";
	}

	private static VariantDefinition variant(String moduleId, GenerationLanguage language) {
		ModuleDefinition definition = MODULES.get(normalize(moduleId));
		return definition == null ? null : definition.variant(language);
	}

	private static void visit(String moduleId, Set<String> ordered, Set<String> visiting, GenerationLanguage language) {
		if (ordered.contains(moduleId)) {
			return;
		}
		if (!visiting.add(moduleId)) {
			throw new IllegalStateException("Circular shipped module dependency detected: " + moduleId);
		}
		VariantDefinition definition = variant(moduleId, language);
		if (definition != null) {
			for (String dependency : definition.requiredModules()) {
				visit(dependency, ordered, visiting, language);
			}
		}
		visiting.remove(moduleId);
		ordered.add(moduleId);
	}

	private static void register(ModuleDefinition definition) {
		MODULES.put(definition.id(), definition);
	}

	private static String normalize(String moduleId) {
		if (moduleId == null) {
			return "";
		}
		String normalized = moduleId.trim().toLowerCase(Locale.ROOT);
		return "azure-cdn-upload".equals(normalized) ? "cdn" : normalized;
	}

	private static MavenDependencyDTO dependency(String groupId, String artifactId) {
		return new MavenDependencyDTO(groupId, artifactId, null, null, false);
	}

	private static MavenDependencyDTO dependency(String groupId, String artifactId, String version) {
		return new MavenDependencyDTO(groupId, artifactId, version, null, false);
	}

	private static MavenDependencyDTO dependency(String groupId, String artifactId, String version, String scope,
			boolean optional) {
		return new MavenDependencyDTO(groupId, artifactId, version, scope, optional);
	}

	private static MavenDependencyDTO optionalCompileOnly(String groupId, String artifactId, String version) {
		return new MavenDependencyDTO(groupId, artifactId, version, "compile_only", true);
	}

	private static MavenDependencyDTO annotationProcessor(String groupId, String artifactId, String version) {
		return new MavenDependencyDTO(groupId, artifactId, version, "annotation_processor", false);
	}

	private static NodePackageDependency nodeDependency(String packageName, String version) {
		return new NodePackageDependency(packageName, version, false);
	}

	private static NodePackageDependency nodeDevDependency(String packageName, String version) {
		return new NodePackageDependency(packageName, version, true);
	}

	public record NodePackageDependency(String packageName, String version, boolean devDependency) {
	}

	public record PythonPackageDependency(String packageName, String version) {
	}

	private record ModuleDefinition(String id, boolean visible, Map<GenerationLanguage, VariantDefinition> variants) {

		private static Builder visible(String id) {
			return new Builder(id, true);
		}

		private static Builder hidden(String id) {
			return new Builder(id, false);
		}

		private VariantDefinition variant(GenerationLanguage language) {
			VariantDefinition direct = variants.get(language);
			if (direct != null) {
				return direct;
			}
			return language == GenerationLanguage.JAVA ? null : variants.get(GenerationLanguage.JAVA);
		}

		private static final class Builder {
			private final String id;
			private final boolean visible;
			private final Map<GenerationLanguage, VariantDefinition> variants = new LinkedHashMap<>();

			private Builder(String id, boolean visible) {
				this.id = normalize(id);
				this.visible = visible;
			}

			private Builder javaVariant(VariantDefinition definition) {
				variants.put(GenerationLanguage.JAVA, definition);
				return this;
			}

			private Builder nodeVariant(VariantDefinition definition) {
				variants.put(GenerationLanguage.NODE, definition);
				return this;
			}

			private Builder pythonVariant(VariantDefinition definition) {
				variants.put(GenerationLanguage.PYTHON, definition);
				return this;
			}

			private ModuleDefinition build() {
				return new ModuleDefinition(id, visible, Map.copyOf(variants));
			}
		}
	}

	private record VariantDefinition(List<String> requiredModules, List<MavenDependencyDTO> externalDependencies,
			List<NodePackageDependency> nodePackageDependencies, List<PythonPackageDependency> pythonPackageDependencies,
			boolean includesFlywayMigrations,
			boolean usesNodePrisma) {

		private static Builder builder() {
			return new Builder();
		}

		private static final class Builder {
			private final List<String> requiredModules = new ArrayList<>();
			private final List<MavenDependencyDTO> externalDependencies = new ArrayList<>();
			private final List<NodePackageDependency> nodePackageDependencies = new ArrayList<>();
			private final List<PythonPackageDependency> pythonPackageDependencies = new ArrayList<>();
			private boolean includesFlywayMigrations;
			private boolean usesNodePrisma;

			private Builder requires(String... modules) {
				for (String module : modules) {
					requiredModules.add(normalize(module));
				}
				return this;
			}

			private Builder externalDependencies(List<MavenDependencyDTO> dependencies) {
				if (dependencies != null) {
					externalDependencies.addAll(dependencies);
				}
				return this;
			}

			private Builder nodePackageDependencies(List<NodePackageDependency> dependencies) {
				if (dependencies != null) {
					nodePackageDependencies.addAll(dependencies);
				}
				return this;
			}

			private Builder pythonPackageDependencies(List<PythonPackageDependency> dependencies) {
				if (dependencies != null) {
					pythonPackageDependencies.addAll(dependencies);
				}
				return this;
			}

			private Builder includesFlywayMigrations(boolean includesFlywayMigrations) {
				this.includesFlywayMigrations = includesFlywayMigrations;
				return this;
			}

			private Builder usesNodePrisma(boolean usesNodePrisma) {
				this.usesNodePrisma = usesNodePrisma;
				return this;
			}

			private VariantDefinition build() {
				return new VariantDefinition(List.copyOf(requiredModules), List.copyOf(externalDependencies),
						List.copyOf(nodePackageDependencies), List.copyOf(pythonPackageDependencies),
						includesFlywayMigrations, usesNodePrisma);
			}
		}
	}

	private static PythonPackageDependency pythonDependency(String packageName, String version) {
		return new PythonPackageDependency(packageName, version);
	}
}
