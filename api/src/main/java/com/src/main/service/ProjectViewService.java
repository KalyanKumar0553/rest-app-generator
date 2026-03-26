package com.src.main.service;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import com.src.main.exception.GenericException;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.workflow.generation.ProjectGenerationStrategySelector;

@Service
public class ProjectViewService {
	private final ProjectGenerationStrategySelector projectGenerationStrategySelector;

	public byte[] generateZip(String yamlText) {
		Map<String, Object> spec = parseYaml(yamlText);
		Map<String, Object> app = extractApp(spec);
		return projectGenerationStrategySelector.select(GenerationLanguageResolver.resolveFromYaml(spec)).generatePreviewZip(spec, app);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseYaml(String yamlText) {
		try {
			Object data = new Yaml().load(yamlText);
			if (!(data instanceof Map<?, ?> map)) {
				throw new GenericException(HttpStatus.BAD_REQUEST, "YAML root must be an object.");
			}
			return (Map<String, Object>) map;
		} catch (GenericException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Invalid YAML: " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractApp(Map<String, Object> spec) {
		Object appRaw = spec.get("app");
		if (!(appRaw instanceof Map<?, ?> app)) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Missing required \'app\' section.");
		}
		return (Map<String, Object>) app;
	}

	public ProjectViewService(final ProjectGenerationStrategySelector projectGenerationStrategySelector) {
		this.projectGenerationStrategySelector = projectGenerationStrategySelector;
	}
}
