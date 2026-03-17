package com.src.main.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.src.main.util.ProjectMetaDataConstants;

@Service
public class ProjectYamlService {

	@SuppressWarnings("unchecked")
	public Map<String, Object> parseSpec(String yamlText) {
		try {
			Object parsed = new Yaml().load(yamlText);
			if (!(parsed instanceof Map<?, ?> spec)) {
				throw new IllegalArgumentException("YAML must be a mapping at the root");
			}
			return (Map<String, Object>) spec;
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid YAML: " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getRequiredAppSection(Map<String, Object> spec) {
		Object appSection = spec.get("app");
		if (!(appSection instanceof Map<?, ?> app)) {
			throw new IllegalArgumentException("Missing required 'app' section");
		}
		return (Map<String, Object>) app;
	}

	public String getString(Map<String, Object> values, String key, String defaultValue) {
		Object value = values.getOrDefault(key, defaultValue);
		return value == null ? defaultValue : String.valueOf(value).trim();
	}

	public String extractProjectName(String yamlText) {
		Map<String, Object> spec = parseSpec(yamlText);
		Map<String, Object> app = getRequiredAppSection(spec);
		String name = getString(app, ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME);
		if (name.isBlank()) {
			throw new IllegalArgumentException("app.name must be provided");
		}
		return name;
	}
}
