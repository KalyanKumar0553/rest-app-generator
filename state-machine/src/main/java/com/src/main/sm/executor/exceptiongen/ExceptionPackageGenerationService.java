package com.src.main.sm.executor.exceptiongen;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Service
public class ExceptionPackageGenerationService {

	private static final String GENERIC_EXCEPTION_TEMPLATE_JAVA = "generic-exception.java.mustache";
	private static final String GLOBAL_EXCEPTION_HANDLER_TEMPLATE_JAVA = "global-exception-handler.java.mustache";
	private static final String GENERIC_EXCEPTION_TEMPLATE_KOTLIN = "generic-exception.kt.mustache";
	private static final String GLOBAL_EXCEPTION_HANDLER_TEMPLATE_KOTLIN = "global-exception-handler.kt.mustache";

	private final TemplateEngine templateEngine;

	public ExceptionPackageGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path root, String exceptionPackage, boolean useLombok, GenerationLanguage language) throws Exception {
		Path outDir = root.resolve(PathUtils.srcPathFromPackage(exceptionPackage, language));
		Files.createDirectories(outDir);

		Map<String, Object> model = new LinkedHashMap<>();
		model.put("packageName", exceptionPackage);
		model.put("useLombok", useLombok);

		boolean kotlin = language == GenerationLanguage.KOTLIN;
		String genericTemplate = kotlin ? GENERIC_EXCEPTION_TEMPLATE_KOTLIN : GENERIC_EXCEPTION_TEMPLATE_JAVA;
		String handlerTemplate = kotlin ? GLOBAL_EXCEPTION_HANDLER_TEMPLATE_KOTLIN : GLOBAL_EXCEPTION_HANDLER_TEMPLATE_JAVA;
		String genericExceptionCode = templateEngine
				.renderAny(TemplatePathResolver.candidates(language, "exception", genericTemplate), model);
		Files.writeString(outDir.resolve("GenericException." + language.fileExtension()), genericExceptionCode, UTF_8);

		String globalExceptionHandlerCode = templateEngine
				.renderAny(TemplatePathResolver.candidates(language, "exception", handlerTemplate), model);
		Files.writeString(outDir.resolve("GlobalExceptionHandler." + language.fileExtension()), globalExceptionHandlerCode, UTF_8);
	}
}
