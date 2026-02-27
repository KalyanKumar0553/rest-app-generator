package com.src.main.sm.executor.common;

import java.util.Map;

public final class GenerationLanguageResolver {

	private GenerationLanguageResolver() {
	}

	@SuppressWarnings("unchecked")
	public static GenerationLanguage resolveFromYaml(Map<String, Object> yaml) {
		if (yaml == null) {
			return GenerationLanguage.JAVA;
		}
		Object appRaw = yaml.get("app");
		if (appRaw instanceof Map<?, ?> appMapRaw) {
			Map<String, Object> appMap = (Map<String, Object>) appMapRaw;
			Object generator = appMap.get("generator");
			if (generator != null) {
				return fromGenerator(generator.toString());
			}
		}
		Object generator = yaml.get("generator");
		if (generator != null) {
			return fromGenerator(generator.toString());
		}
		return GenerationLanguage.JAVA;
	}

	public static GenerationLanguage fromGenerator(String raw) {
		if (raw == null) {
			return GenerationLanguage.JAVA;
		}
		String normalized = raw.trim().toLowerCase();
		if ("kotlin".equals(normalized) || "kt".equals(normalized)) {
			return GenerationLanguage.KOTLIN;
		}
		return GenerationLanguage.JAVA;
	}
}

