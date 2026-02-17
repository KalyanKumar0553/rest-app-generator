package com.src.main.sm.executor.rest;

import org.apache.logging.log4j.util.Strings;

import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.ModelSpecDTO;

public final class RestGenerationSupport {

	private RestGenerationSupport() {
	}

	public static RestGenerationUnit buildUnit(ModelSpecDTO model, String basePackage, String packageStructure) {
		String entityName = CaseUtils.toPascal(StringUtils.firstNonBlank(model.getName(), "Entity"));
		String idName = StringUtils.firstNonBlank(model.getId() != null ? model.getId().getField() : null, "id");
		String idType = mapJavaType(model.getId() != null ? model.getId().getType() : null);

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

		return new RestGenerationUnit(entityName, idName, idType, endpointPath, modelPackage, repositoryPackage,
				servicePackage, controllerPackage, utilPackage);
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

	private static String mapJavaType(String rawType) {
		if (Strings.isBlank(rawType)) {
			return "Long";
		}
		String normalized = rawType.trim();
		return switch (normalized) {
		case "Int", "Integer", "int" -> "Integer";
		case "Long", "long" -> "Long";
		case "Boolean", "boolean" -> "Boolean";
		case "Decimal", "BigDecimal" -> "java.math.BigDecimal";
		case "UUID" -> "java.util.UUID";
		case "Date", "LocalDate" -> "java.time.LocalDate";
		case "DateTime", "LocalDateTime" -> "java.time.LocalDateTime";
		case "OffsetDateTime" -> "java.time.OffsetDateTime";
		case "Instant" -> "java.time.Instant";
		case "String", "Text" -> "String";
		default -> normalized;
		};
	}
}
