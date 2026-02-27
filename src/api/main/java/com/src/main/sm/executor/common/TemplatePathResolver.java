package com.src.main.sm.executor.common;

import java.util.List;

public final class TemplatePathResolver {

	private TemplatePathResolver() {
	}

	public static List<String> candidates(GenerationLanguage language, String domain, String fileName) {
		String languagePathV2 = "templates/languages/" + language.templateFolder() + "/" + domain + "/" + fileName;
		String languagePath = "templates/" + language.templateFolder() + "/" + domain + "/" + fileName;
		String legacyPath = "templates/" + domain + "/" + fileName;
		return List.of(languagePathV2, languagePath, legacyPath);
	}
}
