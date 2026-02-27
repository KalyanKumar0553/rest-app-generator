package com.src.main.sm.executor.mapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.src.main.common.util.StringUtils;

public final class MapperGenerationSupport {

	private MapperGenerationSupport() {
	}

	@SuppressWarnings("unchecked")
	public static List<MapperGenerationUnit> resolveUnits(
			Map<String, Object> yaml,
			String basePackage,
			String packageStructure) {
		Object rawMappers = yaml.get("mappers");
		if (!(rawMappers instanceof List<?> mapperList) || mapperList.isEmpty()) {
			return List.of();
		}

		Map<String, ModelRef> sourceByName = resolveSourceModels(yaml, basePackage);
		Map<String, ModelRef> targetByName = resolveTargetModels(yaml, basePackage, packageStructure);
		String mapperPackage = resolveMapperPackage(basePackage, packageStructure);

		List<MapperGenerationUnit> units = new ArrayList<>();
		Set<String> seenClassNames = new LinkedHashSet<>();
		for (Object rawMapper : mapperList) {
			if (!(rawMapper instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Map<String, Object> mapper = (Map<String, Object>) rawMap;
			String mapperName = StringUtils.trimToNull(str(mapper.get("name")));
			String fromModel = StringUtils.trimToNull(str(mapper.get("fromModel")));
			String toModel = StringUtils.trimToNull(str(mapper.get("toModel")));
			if (mapperName == null || fromModel == null || toModel == null) {
				continue;
			}

			ModelRef sourceRef = sourceByName.get(fromModel.toLowerCase(Locale.ROOT));
			ModelRef targetRef = targetByName.get(toModel.toLowerCase(Locale.ROOT));
			if (sourceRef == null || targetRef == null) {
				continue;
			}

			List<MapperMappingLine> forwardMappings = new ArrayList<>();
			List<MapperMappingLine> reverseMappings = new ArrayList<>();
			Object rawMappings = mapper.get("mappings");
			if (rawMappings instanceof List<?> mappingList) {
				for (Object rawMapping : mappingList) {
					if (!(rawMapping instanceof Map<?, ?> rawMappingMap)) {
						continue;
					}
					Map<String, Object> mapping = (Map<String, Object>) rawMappingMap;
					String sourceField = StringUtils.trimToNull(str(mapping.get("sourceField")));
					String targetField = StringUtils.trimToNull(str(mapping.get("targetField")));
					if (sourceField == null || targetField == null) {
						continue;
					}
					if (!sourceRef.fieldsByName.containsKey(sourceField.toLowerCase(Locale.ROOT))
							|| !targetRef.fieldsByName.containsKey(targetField.toLowerCase(Locale.ROOT))) {
						continue;
					}
					forwardMappings.add(new MapperMappingLine(toAccessorSuffix(sourceField), toAccessorSuffix(targetField)));
					reverseMappings.add(new MapperMappingLine(toAccessorSuffix(targetField), toAccessorSuffix(sourceField)));
				}
			}

			String classNameBase = toPascalIdentifier(mapperName);
			String className = classNameBase.endsWith("Mapper") ? classNameBase : classNameBase + "Mapper";
			className = dedupeClassName(className, seenClassNames);
			units.add(new MapperGenerationUnit(
					mapperPackage,
					className,
					sourceRef.simpleName,
					targetRef.simpleName,
					sourceRef.fqcn,
					targetRef.fqcn,
					forwardMappings,
					reverseMappings));
		}
		return units;
	}

	private static String dedupeClassName(String className, Set<String> seenClassNames) {
		String resolved = className;
		int suffix = 1;
		while (seenClassNames.contains(resolved.toLowerCase(Locale.ROOT))) {
			suffix += 1;
			resolved = className + suffix;
		}
		seenClassNames.add(resolved.toLowerCase(Locale.ROOT));
		return resolved;
	}

	private static String resolveMapperPackage(String basePackage, String packageStructure) {
		boolean domainStructure = "domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"));
		return domainStructure ? basePackage + ".domain.mapper" : basePackage + ".mapper";
	}

	@SuppressWarnings("unchecked")
	private static Map<String, ModelRef> resolveSourceModels(Map<String, Object> yaml, String basePackage) {
		Map<String, ModelRef> byName = new LinkedHashMap<>();
		Object rawDtos = yaml.get("dtos");
		if (!(rawDtos instanceof List<?> dtoList)) {
			return byName;
		}
		for (Object rawDto : dtoList) {
			if (!(rawDto instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Map<String, Object> dto = (Map<String, Object>) rawMap;
			String name = StringUtils.trimToNull(str(dto.get("name")));
			if (name == null) {
				continue;
			}
			String simpleName = toPascalIdentifier(name);
			String dtoType = StringUtils.firstNonBlank(StringUtils.trimToNull(str(dto.get("type"))), "request");
			String subPackage = "response".equalsIgnoreCase(dtoType) ? "response" : "request";
			String fqcn = basePackage + ".dto." + subPackage + "." + simpleName;
			byName.put(name.toLowerCase(Locale.ROOT), new ModelRef(simpleName, fqcn, resolveFieldMap(dto)));
		}
		return byName;
	}

	private static Map<String, ModelRef> resolveTargetModels(
			Map<String, Object> yaml,
			String basePackage,
			String packageStructure) {
		Map<String, ModelRef> byName = new LinkedHashMap<>();
		byName.putAll(resolveSourceModels(yaml, basePackage));

		@SuppressWarnings("unchecked")
		Object rawModels = yaml.get("models");
		if (!(rawModels instanceof List<?> modelList)) {
			return byName;
		}
		boolean domainStructure = "domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"));
		for (Object rawModel : modelList) {
			if (!(rawModel instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Map<String, Object> model = (Map<String, Object>) rawMap;
			String name = StringUtils.trimToNull(str(model.get("name")));
			if (name == null) {
				continue;
			}
			String simpleName = toPascalIdentifier(name);
			String fqcn;
			if (domainStructure) {
				fqcn = basePackage + ".domain." + normalizePackageSegment(simpleName) + ".model." + simpleName;
			} else {
				fqcn = basePackage + ".model." + simpleName;
			}
			byName.putIfAbsent(name.toLowerCase(Locale.ROOT), new ModelRef(simpleName, fqcn, resolveFieldMap(model)));
		}
		return byName;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> resolveFieldMap(Map<String, Object> model) {
		Map<String, String> fields = new LinkedHashMap<>();
		Object rawFields = model.get("fields");
		if (!(rawFields instanceof List<?> fieldList)) {
			return fields;
		}
		for (Object rawField : fieldList) {
			if (!(rawField instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Map<String, Object> field = (Map<String, Object>) rawMap;
			String name = StringUtils.trimToNull(str(field.get("name")));
			String type = StringUtils.trimToNull(str(field.get("type")));
			if (name == null || type == null) {
				continue;
			}
			fields.put(name.toLowerCase(Locale.ROOT), type);
		}
		return fields;
	}

	private static String toAccessorSuffix(String fieldName) {
		if (fieldName == null || fieldName.isBlank()) {
			return "";
		}
		if (fieldName.length() == 1) {
			return fieldName.toUpperCase(Locale.ROOT);
		}
		return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}

	private static String normalizePackageSegment(String value) {
		String normalized = StringUtils.firstNonBlank(value, "entity")
				.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
				.replaceAll("[^a-zA-Z0-9_]", "_")
				.replaceAll("_+", "_")
				.toLowerCase(Locale.ROOT);
		if (normalized.isBlank()) {
			return "entity";
		}
		if (!Character.isJavaIdentifierStart(normalized.charAt(0))) {
			return "x_" + normalized;
		}
		return normalized;
	}

	private static String toPascalIdentifier(String raw) {
		String value = StringUtils.firstNonBlank(raw, "Mapper");
		String[] parts = value.split("[^a-zA-Z0-9]+");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part == null || part.isBlank()) {
				continue;
			}
			String normalized = part.trim();
			builder.append(Character.toUpperCase(normalized.charAt(0)));
			if (normalized.length() > 1) {
				builder.append(normalized.substring(1));
			}
		}
		if (builder.isEmpty()) {
			builder.append("Mapper");
		}
		if (!Character.isJavaIdentifierStart(builder.charAt(0))) {
			builder.insert(0, 'M');
		}
		return builder.toString();
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private record ModelRef(
			String simpleName,
			String fqcn,
			Map<String, String> fieldsByName) {
	}
}
