package com.src.main.sm.executor.rest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Component
public class RestControllerGenerator {

	private static final String TEMPLATE_JAVA = "controller.java.mustache";
	private static final String TEMPLATE_KOTLIN = "controller.kt.mustache";
	private final TemplateEngine templateEngine;

	public RestControllerGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path projectRoot, RestGenerationUnit unit, GenerationLanguage language) throws Exception {
		Path outDir = projectRoot.resolve(PathUtils.srcPathFromPackage(unit.getControllerPackage(), language));
		Files.createDirectories(outDir);
		String template = language == GenerationLanguage.KOTLIN ? TEMPLATE_KOTLIN : TEMPLATE_JAVA;
		String content = templateEngine.renderAny(TemplatePathResolver.candidates(language, "rest", template),
				unit.toTemplateModel());
		Files.writeString(outDir.resolve(unit.getControllerClass() + "." + language.fileExtension()), content, UTF_8);
	}
}
