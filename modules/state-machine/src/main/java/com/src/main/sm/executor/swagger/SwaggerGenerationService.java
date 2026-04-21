package com.src.main.sm.executor.swagger;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Service
public class SwaggerGenerationService {

	private static final String TEMPLATE_JAVA = "openapi-config.java.mustache";
	private static final String TEMPLATE_KOTLIN = "openapi-config.kt.mustache";

	private final TemplateEngine templateEngine;

	public SwaggerGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path root, String swaggerPackage, String appName, List<SwaggerGroupSpec> groups,
			GenerationLanguage language) throws Exception {
		Path outDir = root.resolve(PathUtils.srcPathFromPackage(swaggerPackage, language));
		Files.createDirectories(outDir);

		Map<String, Object> model = new LinkedHashMap<>();
		model.put("packageName", swaggerPackage);
		model.put("appName", appName);
		model.put("groups", groups);

		String template = language == GenerationLanguage.KOTLIN ? TEMPLATE_KOTLIN : TEMPLATE_JAVA;
		String code = templateEngine.renderAny(TemplatePathResolver.candidates(language, "swagger", template), model);
		Files.writeString(outDir.resolve("OpenApiConfig." + language.fileExtension()), code, UTF_8);
	}
}
