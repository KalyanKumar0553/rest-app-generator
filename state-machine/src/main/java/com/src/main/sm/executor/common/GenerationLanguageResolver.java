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
		Object runtimeRaw = yaml.get("runtime");
		if (runtimeRaw instanceof Map<?, ?> runtimeMapRaw) {
			Map<String, Object> runtimeMap = (Map<String, Object>) runtimeMapRaw;
			Object active = runtimeMap.get("active");
			if (active != null) {
				return fromGenerator(active.toString());
			}
		}
		Object appRaw = yaml.get("app");
		if (appRaw instanceof Map<?, ?> appMapRaw) {
			Map<String, Object> appMap = (Map<String, Object>) appMapRaw;
			Object language = appMap.get("language");
			if (language != null) {
				return fromGenerator(language.toString());
			}
			Object generator = appMap.get("generator");
			if (generator != null) {
				return fromGenerator(generator.toString());
			}
		}
		Object coreRaw = yaml.get("core");
		if (coreRaw instanceof Map<?, ?> coreMapRaw) {
			Object coreAppRaw = ((Map<String, Object>) coreMapRaw).get("app");
			if (coreAppRaw instanceof Map<?, ?> coreAppMapRaw) {
				Map<String, Object> coreAppMap = (Map<String, Object>) coreAppMapRaw;
				Object language = coreAppMap.get("language");
				if (language != null) {
					return fromGenerator(language.toString());
				}
			}
		}
		Object language = yaml.get("language");
		if (language != null) {
			return fromGenerator(language.toString());
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
		if ("node".equals(normalized) || "nodejs".equals(normalized) || "javascript".equals(normalized) || "js".equals(normalized)) {
			return GenerationLanguage.NODE;
		}
		if ("python".equals(normalized) || "py".equals(normalized)) {
			return GenerationLanguage.PYTHON;
		}
		return GenerationLanguage.JAVA;
	}
}
