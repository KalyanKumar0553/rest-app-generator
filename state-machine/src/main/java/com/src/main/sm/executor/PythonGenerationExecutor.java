package com.src.main.sm.executor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.util.ProjectMetaDataConstants;

@Component("pythonGenerationExecutor")
public class PythonGenerationExecutor implements StepExecutor {

	private static final String PY_TEMPLATE_BASE = "templates/languages/python/project/";

	private final TemplateEngine templateEngine;

	public PythonGenerationExecutor(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Object rootRaw = data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR);
			if (rootRaw == null) {
				return StepResult.error("PYTHON_GENERATION", "Root directory not found in extended state.");
			}
			Path root = Path.of(String.valueOf(rootRaw));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);
			if (language != GenerationLanguage.PYTHON) {
				return StepResult.ok(Map.of("status", "Skipped for " + language.name()));
			}

			String appName = "generated-python-app";
			if (yaml != null && yaml.get("app") instanceof Map<?, ?> appRaw) {
				appName = String.valueOf(((Map<String, Object>) appRaw).getOrDefault("name", appName));
			} else if (yaml != null && yaml.get("core") instanceof Map<?, ?> coreRaw) {
				Object coreAppRaw = ((Map<String, Object>) coreRaw).get("app");
				if (coreAppRaw instanceof Map<?, ?> appRaw) {
					appName = String.valueOf(((Map<String, Object>) appRaw).getOrDefault("name", appName));
				}
			}

			Files.createDirectories(root.resolve("app"));
			Files.createDirectories(root.resolve("app/models"));
			Files.createDirectories(root.resolve("tests"));

			Map<String, Object> model = new HashMap<>();
			model.put("appName", appName);
			String orm = resolveOrm(yaml);
			model.put("orm", orm);
			model.put("ormIsSqlalchemy", "sqlalchemy".equals(orm));
			model.put("ormIsDjango", "django".equals(orm));
			writeRendered(root.resolve("app/main.py"), PY_TEMPLATE_BASE + "main.py.mustache", model);
			writeRendered(root.resolve("requirements.txt"), PY_TEMPLATE_BASE + "requirements.txt.mustache", model);
			writeRendered(root.resolve("README.md"), PY_TEMPLATE_BASE + "README.md.mustache", model);

			return StepResult.ok(Map.of("status", "Python scaffold generated"));
		} catch (Exception ex) {
			return StepResult.error("PYTHON_GENERATION", ex.getMessage());
		}
	}

	private void writeRendered(Path output, String templatePath, Map<String, Object> model) throws Exception {
		Files.createDirectories(output.getParent());
		String content = templateEngine.render(templatePath, model);
		Files.writeString(output, content, UTF_8);
	}

	@SuppressWarnings("unchecked")
	private String resolveOrm(Map<String, Object> yaml) {
		if (yaml == null) {
			return "sqlalchemy";
		}
		Object pythonRaw = yaml.get("python");
		if (pythonRaw instanceof Map<?, ?> pythonMapRaw) {
			Object orm = ((Map<String, Object>) pythonMapRaw).get("orm");
			if (orm != null) {
				return normalizeOrm(String.valueOf(orm));
			}
		}
		Object runtimeRaw = yaml.get("runtime");
		if (runtimeRaw instanceof Map<?, ?> runtimeMapRaw) {
			Object pythonRuntimeRaw = ((Map<String, Object>) runtimeMapRaw).get("python");
			if (pythonRuntimeRaw instanceof Map<?, ?> pythonRuntimeMapRaw) {
				Object orm = ((Map<String, Object>) pythonRuntimeMapRaw).get("orm");
				if (orm != null) {
					return normalizeOrm(String.valueOf(orm));
				}
			}
		}
		Object orm = yaml.get("orm");
		return orm == null ? "sqlalchemy" : normalizeOrm(String.valueOf(orm));
	}

	private String normalizeOrm(String raw) {
		String normalized = raw == null ? "" : raw.trim().toLowerCase();
		return "django".equals(normalized) ? "django" : "sqlalchemy";
	}
}
