package com.src.main.sm.executor.crud;

import org.apache.logging.log4j.util.Strings;

import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.ModelSpecDTO;

public final class CrudGenerationSupport {

	private CrudGenerationSupport() {
	}

	public static CrudGenerationUnit buildUnit(ModelSpecDTO model, String basePackage, String packageStructure) {
		String entityName = CaseUtils.toPascal(StringUtils.firstNonBlank(model.getName(), "Entity"));
		String idName = StringUtils.firstNonBlank(model.getId() != null ? model.getId().getField() : null, "id");
		JavaTypeRef idType = mapJavaType(model.getId() != null ? model.getId().getType() : null);
		boolean domainStructure = "domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"));
		String normalizedEntity = normalizePackageSegment(entityName);

		String modelPackage = domainStructure
				? basePackage + ".domain." + normalizedEntity + ".model"
				: basePackage + ".model";
		String repositoryPackage = domainStructure
				? basePackage + ".domain." + normalizedEntity + ".repository"
				: basePackage + ".repository";
		return new CrudGenerationUnit(entityName, idName, idType.simpleName(), idType.importName(), modelPackage,
				repositoryPackage);
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

	private record JavaTypeRef(String simpleName, String importName) {
	}
}
