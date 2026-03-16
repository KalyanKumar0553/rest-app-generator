package com.src.main.sm.executor.exceptiongen;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.src.main.common.util.StringUtils;

public final class ExceptionPackageGenerationSupport {

	private ExceptionPackageGenerationSupport() {
	}

	public static String resolveExceptionPackage(String basePackage, String packageStructure) {
		if ("domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"))) {
			return basePackage + ".domain.exception";
		}
		return basePackage + ".exception";
	}

	@SuppressWarnings("unchecked")
	public static boolean isExceptionPackageRequired(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}

		Object restSpecRaw = yaml.get("rest-spec");
		if (restSpecRaw instanceof List<?> restSpecList && !restSpecList.isEmpty()) {
			return true;
		}

		Object modelsRaw = yaml.get("models");
		if (!(modelsRaw instanceof List<?> models) || models.isEmpty()) {
			return false;
		}

		return models.stream()
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.anyMatch(modelRaw -> {
					Object addRestEndpoints = ((Map<String, Object>) modelRaw).get("addRestEndpoints");
					Object addCrudOperations = ((Map<String, Object>) modelRaw).get("addCrudOperations");
					return parseBoolean(addRestEndpoints) || parseBoolean(addCrudOperations);
				});
	}

	private static boolean parseBoolean(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Boolean bool) {
			return bool;
		}
		String normalized = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
	}
}
