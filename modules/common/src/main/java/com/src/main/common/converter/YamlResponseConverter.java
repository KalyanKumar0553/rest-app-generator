package com.src.main.common.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

final class YamlResponseConverter implements ResponseConverter {

	private static final ObjectMapper YAML_MAPPER = new YAMLMapper();

	@Override
	public Format format() {
		return Format.YAML;
	}

	@Override
	public String convert(JsonNode json) {
		try {
			return YAML_MAPPER.writeValueAsString(json);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to convert JSON to YAML", ex);
		}
	}
}
