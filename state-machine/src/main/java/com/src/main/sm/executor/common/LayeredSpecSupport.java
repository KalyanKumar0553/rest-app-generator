package com.src.main.sm.executor.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.src.main.common.util.StringUtils;
import com.src.main.util.ProjectMetaDataConstants;

public final class LayeredSpecSupport {

	private LayeredSpecSupport() {
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> core(Map<String, Object> yaml) {
		if (yaml == null) {
			return Collections.emptyMap();
		}
		Object raw = yaml.get("core");
		if (raw instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return Collections.emptyMap();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> runtime(Map<String, Object> yaml) {
		if (yaml == null) {
			return Collections.emptyMap();
		}
		Object raw = yaml.get("runtime");
		if (raw instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return Collections.emptyMap();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> runtimeSection(Map<String, Object> yaml, String runtimeKey) {
		Map<String, Object> runtime = runtime(yaml);
		Object raw = runtime.get(runtimeKey);
		if (raw instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return Collections.emptyMap();
	}

	public static Map<String, Object> activeRuntimeSection(Map<String, Object> yaml) {
		String active = GenerationLanguageResolver.resolveFromYaml(yaml).templateFolder();
		return runtimeSection(yaml, active);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> coreApp(Map<String, Object> yaml) {
		Object raw = core(yaml).get("app");
		if (raw instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return Collections.emptyMap();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> coreDatabase(Map<String, Object> yaml) {
		Object raw = core(yaml).get("database");
		if (raw instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return Collections.emptyMap();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> coreModules(Map<String, Object> yaml) {
		Object raw = core(yaml).get("modules");
		if (raw instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return Collections.emptyMap();
	}

	public static String resolveBasePackage(Map<String, Object> yaml, String fallback) {
		String runtimePackage = str(activeRuntimeSection(yaml).get("packageName"));
		String javaRuntimePackage = str(runtimeSection(yaml, "java").get("packageName"));
		return StringUtils.firstNonBlank(
				runtimePackage,
				javaRuntimePackage,
				str(yaml == null ? null : yaml.get("basePackage")),
				str(coreApp(yaml).get("groupId")),
				fallback);
	}

	public static String resolvePackageStructure(Map<String, Object> yaml, String fallback) {
		return StringUtils.firstNonBlank(
				str(runtimeSection(yaml, "java").get("packageStructure")),
				str(yaml == null ? null : yaml.get("packages")),
				fallback);
	}

	public static String resolveDatabaseCode(Map<String, Object> yaml) {
		return StringUtils.firstNonBlank(
				str(coreDatabase(yaml).get("database")),
				str(yaml == null ? null : yaml.get("database")),
				nestedString(yaml, "app", "database"));
	}

	public static String resolveDatabaseType(Map<String, Object> yaml) {
		return StringUtils.firstNonBlank(
				str(coreDatabase(yaml).get("dbType")),
				str(yaml == null ? null : yaml.get("dbType")),
				nestedString(yaml, "app", "dbType"));
	}

	public static boolean resolveOpenApiEnabled(Map<String, Object> yaml, boolean fallback) {
		Object value = firstNonNull(
				activeRuntimeSection(yaml).get("enableOpenapi"),
				runtimeSection(yaml, "java").get("enableOpenapi"),
				yaml == null ? null : yaml.get("enableOpenapi"));
		return parseBoolean(value, fallback);
	}

	public static boolean resolveActuatorEnabled(Map<String, Object> yaml, boolean fallback) {
		Object value = firstNonNull(
				activeRuntimeSection(yaml).get("enableActuator"),
				runtimeSection(yaml, "java").get("enableActuator"),
				yaml == null ? null : yaml.get("enableActuator"));
		return parseBoolean(value, fallback);
	}

	public static String resolveBuildTool(Map<String, Object> yaml, String fallback) {
		return StringUtils.firstNonBlank(
				str(activeRuntimeSection(yaml).get(ProjectMetaDataConstants.BUILD_TOOL)),
				str(runtimeSection(yaml, "java").get(ProjectMetaDataConstants.BUILD_TOOL)),
				nestedString(yaml, "app", ProjectMetaDataConstants.BUILD_TOOL),
				fallback);
	}

	public static String resolveArtifactId(Map<String, Object> yaml, String fallback) {
		return StringUtils.firstNonBlank(
				str(activeRuntimeSection(yaml).get(ProjectMetaDataConstants.ARTIFACT_ID)),
				str(runtimeSection(yaml, "java").get(ProjectMetaDataConstants.ARTIFACT_ID)),
				str(coreApp(yaml).get(ProjectMetaDataConstants.ARTIFACT_ID)),
				nestedString(yaml, "app", ProjectMetaDataConstants.ARTIFACT_ID),
				fallback);
	}

	public static String resolveAppName(Map<String, Object> yaml, String fallback) {
		return StringUtils.firstNonBlank(
				str(coreApp(yaml).get("name")),
				nestedString(yaml, "app", "name"),
				fallback);
	}

	public static String resolveApplicationFormat(Map<String, Object> yaml, String fallback) {
		return StringUtils.firstNonBlank(
				str(activeRuntimeSection(yaml).get("applFormat")),
				str(yaml == null ? null : yaml.get("applFormat")),
				nestedString(yaml, "preferences", "applFormat"),
				nestedString(yaml, "app", "applFormat"),
				fallback);
	}

	@SuppressWarnings("unchecked")
	public static List<String> resolveDependencies(Map<String, Object> yaml) {
		List<String> resolved = new ArrayList<>();
		Map<String, Object> modules = coreModules(yaml);
		Object selectedRaw = modules.get("selected");
		if (selectedRaw instanceof List<?> selected) {
			for (Object item : selected) {
				String value = str(item);
				if (StringUtils.isNotBlank(value)) {
					resolved.add(value.trim());
				}
			}
		}
		Object customRaw = modules.get("customDependencies");
		if (customRaw instanceof List<?> custom) {
			for (Object item : custom) {
				String value = str(item);
				if (StringUtils.isNotBlank(value)) {
					resolved.add(value.trim());
				}
			}
		}
		if (!resolved.isEmpty()) {
			return resolved.stream().distinct().toList();
		}
		Object raw = yaml == null ? null : yaml.get("dependencies");
		if (raw instanceof List<?> items) {
			List<String> values = new ArrayList<>();
			for (Object item : items) {
				String value = str(item);
				if (StringUtils.isNotBlank(value)) {
					values.add(value.trim());
				}
			}
			return values.stream().distinct().toList();
		}
		return List.of();
	}

	@SuppressWarnings("unchecked")
	private static String nestedString(Map<String, Object> yaml, String section, String key) {
		if (yaml == null) {
			return null;
		}
		Object raw = yaml.get(section);
		if (raw instanceof Map<?, ?> map) {
			return str(((Map<String, Object>) map).get(key));
		}
		return null;
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

	private static Object firstNonNull(Object... values) {
		for (Object value : values) {
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
