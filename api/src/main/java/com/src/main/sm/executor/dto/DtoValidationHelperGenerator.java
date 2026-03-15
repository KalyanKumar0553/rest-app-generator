package com.src.main.sm.executor.dto;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Component
public class DtoValidationHelperGenerator {

	private static final String TPL_VALIDATION_FIELD_MATCH_JAVA = "field_match.java.mustache";
	private static final String TPL_VALIDATION_FIELD_MATCH_VALIDATOR_JAVA = "field_match_validator.java.mustache";
	private static final String TPL_VALIDATION_CONDITIONAL_REQUIRED_JAVA = "conditional_required.java.mustache";
	private static final String TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR_JAVA = "conditional_required_validator.java.mustache";
	private static final String TPL_VALIDATION_FIELD_MATCH_KOTLIN = "field_match.kt.mustache";
	private static final String TPL_VALIDATION_FIELD_MATCH_VALIDATOR_KOTLIN = "field_match_validator.kt.mustache";
	private static final String TPL_VALIDATION_CONDITIONAL_REQUIRED_KOTLIN = "conditional_required.kt.mustache";
	private static final String TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR_KOTLIN = "conditional_required_validator.kt.mustache";

	private final TemplateEngine templateEngine;

	public DtoValidationHelperGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void ensureCrossFieldValidationHelpers(Path root, String basePkg, GenerationLanguage language) {
		try {
			Path baseDir = root.resolve(PathUtils.srcPathFromPackage(basePkg + ".validation", language));
			Files.createDirectories(baseDir);

			boolean kotlin = language == GenerationLanguage.KOTLIN;
			Map<String, String> files = kotlin
					? Map.of(
							"FieldMatch.kt", TPL_VALIDATION_FIELD_MATCH_KOTLIN,
							"FieldMatchValidator.kt", TPL_VALIDATION_FIELD_MATCH_VALIDATOR_KOTLIN,
							"ConditionalRequired.kt", TPL_VALIDATION_CONDITIONAL_REQUIRED_KOTLIN,
							"ConditionalRequiredValidator.kt", TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR_KOTLIN)
					: Map.of(
							"FieldMatch.java", TPL_VALIDATION_FIELD_MATCH_JAVA,
							"FieldMatchValidator.java", TPL_VALIDATION_FIELD_MATCH_VALIDATOR_JAVA,
							"ConditionalRequired.java", TPL_VALIDATION_CONDITIONAL_REQUIRED_JAVA,
							"ConditionalRequiredValidator.java", TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR_JAVA);

			files.forEach((fileName, templatePath) -> {
				Path target = baseDir.resolve(fileName);
				if (Files.exists(target)) {
					return;
				}
				if (templatePath == null || templatePath.isBlank()) {
					return;
				}
				String body;
				try {
					body = templateEngine.renderAny(TemplatePathResolver.candidates(language, "validation", templatePath),
							Map.of("basePkg", basePkg));
				} catch (Exception renderErr) {
					return;
				}
				try {
					Files.writeString(target, body, StandardCharsets.UTF_8);
				} catch (Exception ignored) {
				}
			});
		} catch (Exception ignored) {
		}
	}
}
