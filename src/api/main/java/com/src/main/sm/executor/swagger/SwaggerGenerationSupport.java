package com.src.main.sm.executor.swagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.ModelSpecDTO;

public final class SwaggerGenerationSupport {

	private SwaggerGenerationSupport() {
	}

	public static boolean isOpenApiEnabled(Object raw) {
		if (raw == null) {
			return false;
		}
		if (raw instanceof Boolean enabled) {
			return enabled;
		}
		String normalized = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
	}

	public static String resolveSwaggerPackage(String basePackage, String packageStructure) {
		if ("domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"))) {
			return basePackage + ".domain.config";
		}
		return basePackage + ".config";
	}

	public static List<SwaggerGroupSpec> buildGroups(List<ModelSpecDTO> models) {
		if (models == null) {
			return new ArrayList<>();
		}
		return models.stream()
				.filter(model -> Boolean.TRUE.equals(model.getAddRestEndpoints()))
				.filter(model -> !Boolean.TRUE.equals(model.getAddCrudOperations()))
				.map(model -> {
					String entity = CaseUtils.toPascal(StringUtils.firstNonBlank(model.getName(), "Entity"));
					String endpoint = toKebabCase(entity) + "s";
					String beanName = toCamelCase(entity) + "ApiGroup";
					String pathPattern = "/api/" + endpoint + "/**";
					return new SwaggerGroupSpec(beanName, endpoint, quotePath(pathPattern), false, "");
				})
				.collect(Collectors.toMap(SwaggerGroupSpec::groupName, group -> group, (left, right) -> left,
						LinkedHashMap::new))
				.values()
				.stream()
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@SuppressWarnings("unchecked")
	public static List<SwaggerGroupSpec> buildGroupsFromYaml(Map<String, Object> yaml, List<ModelSpecDTO> models) {
		if (yaml == null) {
			return buildGroups(models);
		}

		Object restSpecRaw = yaml.get("rest-spec");
		if (!(restSpecRaw instanceof List<?> restSpecs) || restSpecs.isEmpty()) {
			return buildGroups(models);
		}

		Map<String, GroupAccumulator> groupMap = new LinkedHashMap<>();
		for (Object restSpecObj : restSpecs) {
			if (!(restSpecObj instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Map<String, Object> restSpec = (Map<String, Object>) rawMap;

			String configName = StringUtils.firstNonBlank(str(restSpec.get("name")), "default-api");
			String normalizedBasePath = normalizeBasePath(
					StringUtils.firstNonBlank(str(restSpec.get("basePath")), str(restSpec.get("resourceName")), configName));
			if (normalizedBasePath == null) {
				continue;
			}
			List<String> basePaths = resolveOperationBasePaths(normalizedBasePath);

			Map<String, Object> methodsMap = methodsMap(restSpec.get("methods"));
			if (methodsMap.isEmpty()) {
				continue;
			}

			Map<String, Set<OperationEntry>> groupOperations = collectGroupedOperations(configName, basePaths, methodsMap);
			if (groupOperations.isEmpty()) {
				groupOperations.put(configName, new LinkedHashSet<>());
			}

			for (Map.Entry<String, Set<OperationEntry>> groupEntry : groupOperations.entrySet()) {
				String groupName = StringUtils.firstNonBlank(groupEntry.getKey(), configName);
				Set<OperationEntry> selectedOps = new LinkedHashSet<>(groupEntry.getValue());
				Set<OperationEntry> combinedOps = new LinkedHashSet<>(selectedOps);
				for (String basePath : basePaths) {
					for (OperationTarget target : allKnownTargets(basePath)) {
						if (!containsOperation(selectedOps, target.path, target.httpMethod)) {
							combinedOps.add(new OperationEntry(target.path, target.httpMethod, null, List.of(), false, true));
						}
					}
				}
				GroupAccumulator accumulator = groupMap.computeIfAbsent(groupName, ignored -> new GroupAccumulator(groupName));
				basePaths.forEach(basePath -> accumulator.pathPatterns.add(basePath + "/**"));
				accumulator.operations.addAll(combinedOps);
			}
		}

		if (groupMap.isEmpty()) {
			return buildGroups(models);
		}

		return groupMap.values().stream()
				.sorted(Comparator.comparing(group -> group.groupName.toLowerCase(Locale.ROOT)))
				.map(group -> new SwaggerGroupSpec(
						toCamelCase(group.groupName) + "ApiGroup",
						group.groupName,
						group.pathPatterns.stream().sorted().map(SwaggerGenerationSupport::quotePath).collect(Collectors.joining(", ")),
						!group.operations.isEmpty(),
						buildCustomizerBody(group.operations)))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> methodsMap(Object methodsRaw) {
		if (!(methodsRaw instanceof Map<?, ?> raw)) {
			return Collections.emptyMap();
		}
		Map<String, Object> methods = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			methods.put(String.valueOf(entry.getKey()), entry.getValue());
		}
		return methods;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Set<OperationEntry>> collectGroupedOperations(
			String fallbackGroup,
			List<String> basePaths,
			Map<String, Object> methodsMap) {
		Map<String, Set<OperationEntry>> grouped = new LinkedHashMap<>();
		for (Map.Entry<String, Object> methodEntry : methodsMap.entrySet()) {
			String methodKey = String.valueOf(methodEntry.getKey());
			String groupName = StringUtils.firstNonBlank(fallbackGroup, "API") + " Group";
			String description = defaultDescription(methodKey, fallbackGroup);
			boolean deprecated = false;
			List<String> tags = new ArrayList<>(List.of(defaultTag(methodKey)));

			if (methodEntry.getValue() instanceof Map<?, ?> blockRaw) {
				Map<String, Object> block = (Map<String, Object>) blockRaw;
				if (block.get("documentation") instanceof Map<?, ?> docRaw) {
					Map<String, Object> doc = (Map<String, Object>) docRaw;
					groupName = StringUtils.firstNonBlank(str(doc.get("group")), fallbackGroup + " Group");
					description = StringUtils.firstNonBlank(StringUtils.trimToNull(str(doc.get("description"))), description);
					deprecated = Boolean.TRUE.equals(doc.get("deprecated"));
					Object tagsRaw = doc.get("descriptionTags");
					tags.clear();
					if (tagsRaw instanceof List<?> list) {
						for (Object tagObj : list) {
							String tag = StringUtils.trimToNull(str(tagObj));
							if (tag != null) {
								tags.add(tag);
							}
						}
					}
					if (tags.isEmpty()) {
						tags.add(defaultTag(methodKey));
					}
				}
			}

			Set<OperationEntry> entries = grouped.computeIfAbsent(groupName, ignored -> new LinkedHashSet<>());
			for (String basePath : basePaths) {
				OperationTarget target = toOperationTarget(basePath, methodKey);
				if (target == null) {
					continue;
				}
				entries.add(new OperationEntry(target.path, target.httpMethod, description, tags, deprecated, false));
			}
		}
		return grouped;
	}

	private static OperationTarget toOperationTarget(String basePath, String methodKeyRaw) {
		String methodKey = methodKeyRaw == null ? "" : methodKeyRaw.trim();
		if (methodKey.isEmpty()) {
			return null;
		}
		String pathWithId = basePath + "/{id}";
		String bulkPath = basePath + "/bulk";

		return switch (methodKey) {
		case "list" -> new OperationTarget(basePath, "GET");
		case "get", "getById" -> new OperationTarget(pathWithId, "GET");
		case "create" -> new OperationTarget(basePath, "POST");
		case "update" -> new OperationTarget(pathWithId, "PUT");
		case "patch" -> new OperationTarget(pathWithId, "PATCH");
		case "delete" -> new OperationTarget(basePath, "DELETE");
		case "deleteById" -> new OperationTarget(pathWithId, "DELETE");
		case "bulkInsert" -> new OperationTarget(bulkPath, "POST");
		case "bulkUpdate" -> new OperationTarget(bulkPath, "PUT");
		case "bulkDelete" -> new OperationTarget(bulkPath, "DELETE");
		default -> null;
		};
	}

	private static List<OperationTarget> allKnownTargets(String basePath) {
		String pathWithId = basePath + "/{id}";
		String bulkPath = basePath + "/bulk";
		return List.of(
				new OperationTarget(basePath, "GET"),
				new OperationTarget(pathWithId, "GET"),
				new OperationTarget(basePath, "POST"),
				new OperationTarget(pathWithId, "PUT"),
				new OperationTarget(pathWithId, "PATCH"),
				new OperationTarget(basePath, "DELETE"),
				new OperationTarget(pathWithId, "DELETE"),
				new OperationTarget(bulkPath, "POST"),
				new OperationTarget(bulkPath, "PUT"),
				new OperationTarget(bulkPath, "DELETE"));
	}

	private static boolean containsOperation(Set<OperationEntry> operations, String path, String httpMethod) {
		for (OperationEntry operation : operations) {
			if (operation.path.equals(path) && operation.httpMethod.equals(httpMethod)) {
				return true;
			}
		}
		return false;
	}

	private static String buildCustomizerBody(Set<OperationEntry> entries) {
		if (entries == null || entries.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		entries.stream()
				.sorted(Comparator
						.comparing((OperationEntry entry) -> entry.path)
						.thenComparingInt(entry -> httpMethodRank(entry.httpMethod))
						.thenComparing(entry -> entry.removeOnly))
				.forEach(entry -> {
			if (operationAccessor(entry.httpMethod) == null || operationSetter(entry.httpMethod) == null) {
				return;
			}

			if (entry.removeOnly) {
				builder.append("                removeOperation(openApi, \"")
						.append(escapeJava(entry.path))
						.append("\", \"")
						.append(entry.httpMethod)
						.append("\");\n");
				return;
			}

			String tagsArg = "List.of()";
			if (entry.tags != null && !entry.tags.isEmpty()) {
				String tags = entry.tags.stream()
						.map(tag -> "\"" + escapeJava(tag) + "\"")
						.collect(Collectors.joining(", "));
				tagsArg = "List.of(" + tags + ")";
			}
			String descriptionArg = entry.description == null ? "" : entry.description;
			builder.append("                applyOperationMetadata(openApi, \"")
					.append(escapeJava(entry.path))
					.append("\", \"")
					.append(entry.httpMethod)
					.append("\", \"")
					.append(escapeJava(descriptionArg))
					.append("\", ")
					.append(tagsArg)
					.append(", ")
					.append(entry.deprecated)
					.append(");\n");
		});
		return builder.toString();
	}

	private static int httpMethodRank(String httpMethod) {
		return switch (httpMethod) {
		case "GET" -> 1;
		case "POST" -> 2;
		case "PUT" -> 3;
		case "PATCH" -> 4;
		case "DELETE" -> 5;
		default -> 99;
		};
	}

	private static String operationAccessor(String httpMethod) {
		return switch (httpMethod) {
		case "GET" -> "getGet";
		case "POST" -> "getPost";
		case "PUT" -> "getPut";
		case "PATCH" -> "getPatch";
		case "DELETE" -> "getDelete";
		default -> null;
		};
	}

	private static String operationSetter(String httpMethod) {
		return switch (httpMethod) {
		case "GET" -> "setGet";
		case "POST" -> "setPost";
		case "PUT" -> "setPut";
		case "PATCH" -> "setPatch";
		case "DELETE" -> "setDelete";
		default -> null;
		};
	}

	private static String normalizeBasePath(String basePathOrName) {
		String value = StringUtils.trimToNull(basePathOrName);
		if (value == null) {
			return null;
		}

		String path = value.startsWith("/") ? value : "/" + toKebabCase(value);
		if (!path.startsWith("/api/")) {
			path = "/api" + path;
		}
		path = path.replaceAll("/+", "/");
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	private static List<String> resolveOperationBasePaths(String basePath) {
		LinkedHashSet<String> paths = new LinkedHashSet<>();
		paths.add(basePath);
		String generatedPath = basePath.endsWith("s") ? basePath : basePath + "s";
		paths.add(generatedPath);
		return new ArrayList<>(paths);
	}

	private static String quotePath(String path) {
		return "\"" + path + "\"";
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private static String toKebabCase(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return value
				.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
				.replaceAll("[_\\s]+", "-")
				.replaceAll("-+", "-")
				.replaceAll("[^a-zA-Z0-9-]", "")
				.toLowerCase(Locale.ROOT);
	}

	private static String toCamelCase(String value) {
		if (value == null || value.isBlank()) {
			return "entity";
		}
		String pascal = CaseUtils.toPascal(value);
		return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
	}

	private static String defaultDescription(String methodKey, String configName) {
		String readable = switch (StringUtils.firstNonBlank(methodKey, "")) {
		case "list" -> "List";
		case "get", "getById" -> "Get By Key";
		case "create" -> "Create";
		case "update" -> "Update";
		case "patch" -> "Patch";
		case "delete", "deleteById" -> "Delete";
		case "bulkInsert" -> "Bulk Insert";
		case "bulkUpdate" -> "Bulk Update";
		case "bulkDelete" -> "Bulk Delete";
		default -> "Operation";
		};
		String target = StringUtils.firstNonBlank(configName, "API");
		return readable + " operation for " + target;
	}

	private static String defaultTag(String methodKey) {
		return StringUtils.firstNonBlank(methodKey, "default");
	}

	private static String escapeJava(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "");
	}

	private static final class GroupAccumulator {
		private final String groupName;
		private final Set<String> pathPatterns = new LinkedHashSet<>();
		private final Set<OperationEntry> operations = new LinkedHashSet<>();

		private GroupAccumulator(String groupName) {
			this.groupName = groupName;
		}
	}

	private record OperationTarget(String path, String httpMethod) {
	}

	private record OperationEntry(String path, String httpMethod, String description, List<String> tags, boolean deprecated,
			boolean removeOnly) {
	}
}
