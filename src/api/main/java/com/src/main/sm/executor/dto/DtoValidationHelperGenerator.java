package com.src.main.sm.executor.dto;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;

@Component
public class DtoValidationHelperGenerator {

	private static final String TPL_VALIDATION_FIELD_MATCH = "templates/validation/field_match.mustache";
	private static final String TPL_VALIDATION_FIELD_MATCH_VALIDATOR = "templates/validation/field_match_validator.mustache";
	private static final String TPL_VALIDATION_CONDITIONAL_REQUIRED = "templates/validation/conditional_required.mustache";
	private static final String TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR = "templates/validation/conditional_required_validator.mustache";

	private final TemplateEngine templateEngine;

	public DtoValidationHelperGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void ensureCrossFieldValidationHelpers(Path root, String basePkg) {
		try {
			Path baseDir = root.resolve("src/main/java/" + basePkg.replace('.', '/') + "/validation");
			Files.createDirectories(baseDir);

			Map<String, String> files = Map.of("FieldMatch.java", TPL_VALIDATION_FIELD_MATCH,
					"FieldMatchValidator.java", TPL_VALIDATION_FIELD_MATCH_VALIDATOR, "ConditionalRequired.java",
					TPL_VALIDATION_CONDITIONAL_REQUIRED, "ConditionalRequiredValidator.java",
					TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR);

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
					body = templateEngine.render(templatePath, Map.of("basePkg", basePkg));
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
