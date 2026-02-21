package com.src.main.sm.executor.swagger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
					return new SwaggerGroupSpec(beanName, endpoint, pathPattern);
				})
				.collect(Collectors.toMap(SwaggerGroupSpec::groupName, group -> group, (left, right) -> left,
						LinkedHashMap::new))
				.values()
				.stream()
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private static String toKebabCase(String value) {
		return value
				.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
				.replaceAll("[_\\s]+", "-")
				.replaceAll("-+", "-")
				.toLowerCase(Locale.ROOT);
	}

	private static String toCamelCase(String value) {
		if (value == null || value.isBlank()) {
			return "entity";
		}
		String pascal = CaseUtils.toPascal(value);
		return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
	}
}
