package com.src.main.sm.executor.rest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

@Component
public class RestServiceGenerator {

	private static final String TEMPLATE = "templates/rest/service.java.mustache";
	private final TemplateEngine templateEngine;

	public RestServiceGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path projectRoot, RestGenerationUnit unit) throws Exception {
		Path outDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(unit.getServicePackage()));
		Files.createDirectories(outDir);
		String content = templateEngine.render(TEMPLATE, unit.toTemplateModel());
		Files.writeString(outDir.resolve(unit.getServiceClass() + ".java"), content, UTF_8);
	}
}
