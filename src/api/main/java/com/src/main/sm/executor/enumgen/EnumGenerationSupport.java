package com.src.main.sm.executor.enumgen;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.EnumSpecDTO;

public final class EnumGenerationSupport {

	private EnumGenerationSupport() {
	}

	public static String resolveEnumPackage(String basePackage, String packageStructure) {
		if ("domain".equalsIgnoreCase(StringUtils.firstNonBlank(packageStructure, "technical"))) {
			return basePackage + ".domain.enums";
		}
		return basePackage + ".model.enums";
	}

	public static List<EnumSpecResolved> resolveEnums(List<EnumSpecDTO> enums) {
		if (enums == null || enums.isEmpty()) {
			return new ArrayList<>();
		}
		Set<String> seen = new LinkedHashSet<>();
		return enums.stream()
				.filter(java.util.Objects::nonNull)
				.map(enumSpec -> new EnumSpecResolved(
						CaseUtils.toPascal(StringUtils.firstNonBlank(enumSpec.getName(), "").trim()),
						normalizeStorage(enumSpec.getStorage()),
						normalizeConstants(enumSpec.getConstants())))
				.filter(spec -> !spec.name().isBlank())
				.filter(spec -> !spec.constants().isEmpty())
				.filter(spec -> seen.add(spec.name()))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public static Map<String, EnumSpecResolved> byName(List<EnumSpecResolved> enums) {
		java.util.LinkedHashMap<String, EnumSpecResolved> map = new java.util.LinkedHashMap<>();
		if (enums == null) {
			return map;
		}
		enums.stream()
				.filter(java.util.Objects::nonNull)
				.filter(item -> item.name() != null)
				.forEach(item -> map.put(item.name(), item));
		return map;
	}

	private static List<String> normalizeConstants(List<String> constants) {
		if (constants == null || constants.isEmpty()) {
			return List.of();
		}
		Set<String> dedup = constants.stream()
				.map(EnumGenerationSupport::normalizeConstant)
				.filter(normalized -> !normalized.isBlank())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		return new ArrayList<>(dedup);
	}

	private static String normalizeConstant(String raw) {
		String value = StringUtils.firstNonBlank(raw, "").trim();
		if (value.isBlank()) {
			return "";
		}
		String sanitized = value.replaceAll("[^A-Za-z0-9_]", "_").replaceAll("_+", "_").toUpperCase(Locale.ROOT);
		if (sanitized.isBlank()) {
			return "";
		}
		if (Character.isDigit(sanitized.charAt(0))) {
			return "_" + sanitized;
		}
		return sanitized;
	}

	private static String normalizeStorage(String raw) {
		String normalized = StringUtils.firstNonBlank(raw, "STRING").trim().toUpperCase(Locale.ROOT);
		if ("ORDINAL".equals(normalized)) {
			return "ORDINAL";
		}
		return "STRING";
	}
}
