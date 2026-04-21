package com.src.main.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Map;

public final class JsonYamlConverterUtil {

	private static final ObjectMapper JSON = new ObjectMapper();
	private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory())
			.enable(SerializationFeature.INDENT_OUTPUT);

	private JsonYamlConverterUtil() {
	}

	/** Converts JSON text to YAML text */
	public static String jsonToYaml(String jsonText) throws Exception {
		Map<String, Object> map = JSON.readValue(jsonText, Map.class);
		return YAML.writeValueAsString(map);
	}

	/** Converts YAML text to JSON text */
	public static String yamlToJson(String yamlText) throws Exception {
		Map<String, Object> map = YAML.readValue(yamlText, Map.class);
		return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(map);
	}

	public static String mapToJson(Map<String, Object> yamlMap) {
		try {
			return JSON.writeValueAsString(yamlMap);
		} catch (Exception e) {
			throw new RuntimeException("Failed to convert YAML map to JSON", e);
		}
	}
	
	public static String mapToYaml(Map<String, Object> map) {
        try {
            return YAML.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert map to YAML", e);
        }
    }
}
