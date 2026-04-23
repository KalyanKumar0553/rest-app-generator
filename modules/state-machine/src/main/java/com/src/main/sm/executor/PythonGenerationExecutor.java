package com.src.main.sm.executor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.common.LayeredSpecSupport;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ShippableModuleSupport;

@Component("pythonGenerationExecutor")
public class PythonGenerationExecutor implements StepExecutor {

	private static final String PY_TEMPLATE_BASE = "templates/languages/python/project/";

	private final TemplateEngine templateEngine;
	private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

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
			Files.createDirectories(root.resolve("app/generated"));
			Files.createDirectories(root.resolve("tests"));

			Map<String, Object> model = new HashMap<>();
			model.put("appName", appName);
			String orm = resolveOrm(yaml);
			List<String> selectedModules = extractSelectedShippedModules(yaml);
			model.put("orm", orm);
			model.put("ormIsSqlalchemy", "sqlalchemy".equals(orm));
			model.put("ormIsDjango", "django".equals(orm));
			model.put("hasModules", !selectedModules.isEmpty());
			writeRendered(root.resolve("app/main.py"), PY_TEMPLATE_BASE + "main.py.mustache", model);
			Files.writeString(root.resolve("app/__init__.py"), "", UTF_8);
			writeFile(root.resolve("app/generated/__init__.py"), "");
			writeFile(root.resolve("tests/__init__.py"), "");
			writeFile(root.resolve("app/generated/module_manifest.py"), renderModuleManifest(selectedModules, extractModuleConfigs(yaml)));
			writeFile(root.resolve("app/generated/module_bootstrap.py"), renderModuleBootstrap(selectedModules));
			writeRendered(root.resolve("requirements.txt"), PY_TEMPLATE_BASE + "requirements.txt.mustache",
					buildRequirementsModel(model, selectedModules));
			writeRendered(root.resolve("README.md"), PY_TEMPLATE_BASE + "README.md.mustache", model);
			copyShippedModules(root, selectedModules);

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

	private void writeFile(Path output, String content) throws Exception {
		Files.createDirectories(output.getParent());
		Files.writeString(output, content, UTF_8);
	}

	private Map<String, Object> buildRequirementsModel(Map<String, Object> baseModel, List<String> selectedModules) {
		Map<String, Object> requirementsModel = new HashMap<>(baseModel);
		List<Map<String, String>> pythonDependencies = ShippableModuleSupport.resolvePythonPackageDependencies(selectedModules).stream()
				.map(dependency -> Map.of(
						"packageName", dependency.packageName(),
						"version", dependency.version()))
				.toList();
		requirementsModel.put("pythonDependencies", pythonDependencies);
		return requirementsModel;
	}

	private List<String> extractSelectedShippedModules(Map<String, Object> yaml) {
		List<String> selected = new ArrayList<>();
		for (String dependency : LayeredSpecSupport.resolveDependencies(yaml)) {
			if (ShippableModuleSupport.isVisibleModule(dependency)) {
				selected.add(dependency);
			}
		}
		return selected.stream().distinct().toList();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractModuleConfigs(Map<String, Object> yaml) {
		if (yaml == null) {
			return Map.of();
		}
		Object rawModuleConfigs = yaml.get("moduleConfigs");
		if (!(rawModuleConfigs instanceof Map<?, ?> moduleConfigs)) {
			return Map.of();
		}
		Map<String, Object> extracted = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : moduleConfigs.entrySet()) {
			if (entry.getKey() == null) {
				continue;
			}
			String moduleId = String.valueOf(entry.getKey()).trim();
			if (!ShippableModuleSupport.isShippableModule(moduleId)) {
				continue;
			}
			Object value = entry.getValue();
			if (value instanceof Map<?, ?> valueMap) {
				extracted.put(moduleId, new LinkedHashMap<>((Map<String, Object>) valueMap));
				continue;
			}
			if (value instanceof List<?> valueList) {
				extracted.put(moduleId, valueList);
				continue;
			}
			extracted.put(moduleId, value);
		}
		return extracted;
	}

	private String renderModuleManifest(List<String> selectedModules, Map<String, Object> moduleConfigs) {
		String modulesLiteral = selectedModules.stream()
				.map(moduleId -> "        \"" + escapePython(moduleId) + "\"")
				.collect(Collectors.joining(",\n"));
		if (modulesLiteral.isBlank()) {
			modulesLiteral = "";
		}
		return """
				module_manifest = {
				    "generator": "python",
				    "selectedModules": [
				%s
				    ],
				    "moduleConfigs": %s,
				}
				""".formatted(modulesLiteral, renderPythonObject(moduleConfigs, 1));
	}

	private String renderModuleBootstrap(List<String> selectedModules) {
		if (selectedModules.isEmpty()) {
			return """
					from fastapi import FastAPI
					
					def configure_generated_modules(_app: FastAPI) -> None:
					    return
					""";
		}

		String imports = selectedModules.stream()
				.map(moduleId -> "from app.modules.%s import register_module as register_%s_module"
						.formatted(moduleId.replace('-', '_'), moduleId.replace('-', '_')))
				.collect(Collectors.joining("\n"));
		String registryEntries = selectedModules.stream()
				.map(moduleId -> "    \"" + escapePython(moduleId) + "\": register_" + moduleId.replace('-', '_') + "_module")
				.collect(Collectors.joining(",\n"));
		return """
				from fastapi import FastAPI
				
				from app.generated.module_manifest import module_manifest
				%s
				
				module_registrars = {
				%s
				}
				
				def configure_generated_modules(app: FastAPI) -> None:
				    for module_id in module_manifest["selectedModules"]:
				        register_module = module_registrars.get(module_id)
				        if register_module is None:
				            continue
				        config = module_manifest.get("moduleConfigs", {}).get(module_id, {})
				        register_module(app, config, module_manifest)
				""".formatted(imports, registryEntries);
	}

	private String renderPythonObject(Object value, int indentLevel) {
		String indent = "    ".repeat(Math.max(0, indentLevel));
		String childIndent = "    ".repeat(Math.max(0, indentLevel + 1));
		if (value instanceof Map<?, ?> map) {
			if (map.isEmpty()) {
				return "{}";
			}
			return map.entrySet().stream()
					.map(entry -> childIndent + "\"" + escapePython(String.valueOf(entry.getKey())) + "\": "
							+ renderPythonObject(entry.getValue(), indentLevel + 1))
					.collect(Collectors.joining(",\n", "{\n", "\n" + indent + "}"));
		}
		if (value instanceof List<?> list) {
			if (list.isEmpty()) {
				return "[]";
			}
			return list.stream()
					.map(item -> childIndent + renderPythonObject(item, indentLevel + 1))
					.collect(Collectors.joining(",\n", "[\n", "\n" + indent + "]"));
		}
		if (value instanceof Number || value instanceof Boolean) {
			if (value instanceof Boolean bool) {
				return bool ? "True" : "False";
			}
			return String.valueOf(value);
		}
		if (value == null) {
			return "None";
		}
		return "\"" + escapePython(String.valueOf(value)) + "\"";
	}

	private String escapePython(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private void copyShippedModules(Path root, List<String> selectedModules) throws Exception {
		List<String> shippedModules = ShippableModuleSupport.expandSelectedModules(selectedModules, GenerationLanguage.PYTHON);
		if (shippedModules.isEmpty()) {
			return;
		}
		writeFile(root.resolve("app/modules/__init__.py"), "");
		for (String moduleId : shippedModules) {
			Resource[] resources = resourceResolver.getResources(
					ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
							+ ShippableModuleSupport.resourceSearchPattern(moduleId, GenerationLanguage.PYTHON));
			String prefix = ShippableModuleSupport.resourcePrefix(moduleId, GenerationLanguage.PYTHON) + "/";
			for (Resource resource : resources) {
				if (!resource.exists() || !resource.isReadable()) {
					continue;
				}
				String externalForm = resource.getURL().toExternalForm();
				int prefixIndex = externalForm.indexOf(prefix);
				if (prefixIndex < 0) {
					continue;
				}
				String relativePath = externalForm.substring(prefixIndex + prefix.length());
				if (relativePath.isBlank() || relativePath.endsWith("/")) {
					continue;
				}
				Path output = root.resolve("app/modules").resolve(pythonModulePackage(moduleId)).resolve(relativePath);
				Files.createDirectories(output.getParent());
				try (InputStream inputStream = resource.getInputStream()) {
					Files.copy(inputStream, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	private String pythonModulePackage(String moduleId) {
		return moduleId.replace('-', '_');
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
