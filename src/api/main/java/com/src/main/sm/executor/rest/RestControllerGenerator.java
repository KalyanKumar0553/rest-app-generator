package com.src.main.sm.executor.rest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

@Component
public class RestControllerGenerator {

	private static final String TEMPLATE = "templates/rest/controller.java.mustache";
	private final TemplateEngine templateEngine;

	public RestControllerGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path projectRoot, RestGenerationUnit unit) throws Exception {
		Path outDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(unit.getControllerPackage()));
		Files.createDirectories(outDir);
		String content = templateEngine.render(TEMPLATE, unit.toTemplateModel());
		Files.writeString(outDir.resolve(unit.getControllerClass() + ".java"), content, UTF_8);
	}
}
