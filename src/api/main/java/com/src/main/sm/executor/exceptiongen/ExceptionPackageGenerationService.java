package com.src.main.sm.executor.exceptiongen;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

@Service
public class ExceptionPackageGenerationService {

	private static final String GENERIC_EXCEPTION_TEMPLATE = "templates/exception/generic-exception.java.mustache";
	private static final String GLOBAL_EXCEPTION_HANDLER_TEMPLATE = "templates/exception/global-exception-handler.java.mustache";

	private final TemplateEngine templateEngine;

	public ExceptionPackageGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path root, String exceptionPackage, boolean useLombok) throws Exception {
		Path outDir = root.resolve(PathUtils.javaSrcPathFromPackage(exceptionPackage));
		Files.createDirectories(outDir);

		Map<String, Object> model = new LinkedHashMap<>();
		model.put("packageName", exceptionPackage);
		model.put("useLombok", useLombok);

		String genericExceptionCode = templateEngine.render(GENERIC_EXCEPTION_TEMPLATE, model);
		Files.writeString(outDir.resolve("GenericException.java"), genericExceptionCode, UTF_8);

		String globalExceptionHandlerCode = templateEngine.render(GLOBAL_EXCEPTION_HANDLER_TEMPLATE, model);
		Files.writeString(outDir.resolve("GlobalExceptionHandler.java"), globalExceptionHandlerCode, UTF_8);
	}
}
