package com.src.main.sm.executor.rest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

@Component
public class RestSharedSupportGenerator {

	private static final String TEMPLATE = "templates/rest/rest-utils.java.mustache";
	private static final String SUPPORT_CLASS = "RestUtils";

	private final TemplateEngine templateEngine;

	public RestSharedSupportGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path projectRoot, String supportPackage) throws Exception {
		Path outDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(supportPackage));
		Files.createDirectories(outDir);
		String content = templateEngine.render(TEMPLATE, Map.of("supportPackage", supportPackage));
		Files.writeString(outDir.resolve(SUPPORT_CLASS + ".java"), content, UTF_8);
	}
}
