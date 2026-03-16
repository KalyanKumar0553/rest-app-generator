package com.src.main.sm.executor.common;

import java.util.Map;

public final class BoilerplateStyleResolver {

	private BoilerplateStyleResolver() {
	}

	@SuppressWarnings("unchecked")
	public static BoilerplateStyle resolveFromYaml(Map<String, Object> yaml, boolean defaultLombokEnabled) {
		if (yaml == null) {
			return defaultLombokEnabled ? BoilerplateStyle.LOMBOK : BoilerplateStyle.PLAIN;
		}
		Object raw = yaml.get("enableLombok");
		if (raw == null) {
			raw = yaml.get("optionalLombok");
		}
		if (raw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			raw = ((Map<String, Object>) appRaw).get("enableLombok");
			if (raw == null) {
				raw = ((Map<String, Object>) appRaw).get("optionalLombok");
			}
		}
		boolean enabled = parseBoolean(raw, defaultLombokEnabled);
		return enabled ? BoilerplateStyle.LOMBOK : BoilerplateStyle.PLAIN;
	}

	public static boolean parseBoolean(Object value, boolean defaultValue) {
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
}

