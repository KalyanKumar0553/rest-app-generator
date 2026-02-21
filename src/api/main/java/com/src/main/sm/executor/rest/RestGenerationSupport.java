package com.src.main.sm.executor.rest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.logging.log4j.util.Strings;

import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.ModelSpecDTO;

public final class RestGenerationSupport {

	private RestGenerationSupport() {
	}

	public static RestGenerationUnit buildUnit(ModelSpecDTO model, String basePackage, String packageStructure) {
		String entityName = CaseUtils.toPascal(StringUtils.firstNonBlank(model.getName(), "Entity"));
		String idName = StringUtils.firstNonBlank(model.getId() != null ? model.getId().getField() : null, "id");
		JavaTypeRef idType = mapJavaType(model.getId() != null ? model.getId().getType() : null);

		String endpointPath = toKebabCase(entityName) + "s";
		boolean domainStructure = "domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"));
		String normalizedEntity = normalizePackageSegment(entityName);

		String modelPackage;
		String repositoryPackage;
		String servicePackage;
		String controllerPackage;
		String utilPackage;

		if (domainStructure) {
			String domainRoot = basePackage + ".domain." + normalizedEntity;
			modelPackage = domainRoot + ".model";
			repositoryPackage = domainRoot + ".repository";
			servicePackage = domainRoot + ".service";
			controllerPackage = domainRoot + ".controller";
			utilPackage = basePackage + ".domain.util";
		} else {
			modelPackage = basePackage + ".model";
			repositoryPackage = basePackage + ".repository";
			servicePackage = basePackage + ".service";
			controllerPackage = basePackage + ".controller";
			utilPackage = basePackage + ".util";
		}

		String allowedSortFieldsLiteral = resolveAllowedSortFieldsLiteral(model, idName);
		return new RestGenerationUnit(entityName, idName, idType.simpleName(), idType.importName(), endpointPath, modelPackage, repositoryPackage,
				servicePackage, controllerPackage, utilPackage, allowedSortFieldsLiteral);
	}

	public static String resolveUtilPackage(String basePackage, String packageStructure) {
		boolean domainStructure = "domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"));
		return domainStructure ? basePackage + ".domain.util" : basePackage + ".util";
	}

	private static String normalizePackageSegment(String value) {
		String normalized = CaseUtils.toSnake(StringUtils.firstNonBlank(value, "entity"));
		normalized = normalized.replaceAll("[^a-zA-Z0-9_]", "_").replaceAll("_+", "_").toLowerCase();
		if (Strings.isBlank(normalized)) {
			return "entity";
		}
		if (!Character.isJavaIdentifierStart(normalized.charAt(0))) {
			return "x_" + normalized;
		}
		return normalized;
	}

	private static String toKebabCase(String value) {
		return value
				.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
				.replaceAll("[_\\s]+", "-")
				.replaceAll("-+", "-")
				.toLowerCase();
	}

	private static JavaTypeRef mapJavaType(String rawType) {
		if (Strings.isBlank(rawType)) {
			return new JavaTypeRef("Long", null);
		}
		String normalized = rawType.trim();
		return switch (normalized) {
		case "Int", "Integer", "int" -> new JavaTypeRef("Integer", null);
		case "Long", "long" -> new JavaTypeRef("Long", null);
		case "Boolean", "boolean" -> new JavaTypeRef("Boolean", null);
		case "Decimal", "BigDecimal" -> new JavaTypeRef("BigDecimal", "java.math.BigDecimal");
		case "UUID" -> new JavaTypeRef("UUID", "java.util.UUID");
		case "Date", "LocalDate" -> new JavaTypeRef("LocalDate", "java.time.LocalDate");
		case "DateTime", "LocalDateTime" -> new JavaTypeRef("LocalDateTime", "java.time.LocalDateTime");
		case "OffsetDateTime" -> new JavaTypeRef("OffsetDateTime", "java.time.OffsetDateTime");
		case "Instant" -> new JavaTypeRef("Instant", "java.time.Instant");
		case "String", "Text" -> new JavaTypeRef("String", null);
		default -> toJavaTypeRef(normalized);
		};
	}

	private static JavaTypeRef toJavaTypeRef(String normalized) {
		if (normalized.contains(".")) {
			String simple = normalized.substring(normalized.lastIndexOf('.') + 1);
			return new JavaTypeRef(simple, normalized);
		}
		return new JavaTypeRef(normalized, null);
	}

	private static String resolveAllowedSortFieldsLiteral(ModelSpecDTO model, String idName) {
		LinkedHashSet<String> allowed = new LinkedHashSet<>();
		allowed.add(StringUtils.firstNonBlank(idName, "id"));

		List<FieldSpecDTO> fields = model != null && model.getFields() != null ? model.getFields() : new ArrayList<>();
		fields.stream()
				.map(FieldSpecDTO::getName)
				.map(name -> StringUtils.firstNonBlank(name, "").trim())
				.filter(name -> !name.isEmpty())
				.forEach(allowed::add);

		String joined = allowed.stream()
				.map(field -> "\"" + field + "\"")
				.collect(java.util.stream.Collectors.joining(", "));
		return "Set.of(" + joined + ")";
	}

	private record JavaTypeRef(String simpleName, String importName) {
	}
}
