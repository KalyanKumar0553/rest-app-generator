package com.src.main.workflow.engine;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowJsonHelper {

	private final ObjectMapper objectMapper;

	public WorkflowJsonHelper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public List<String> readStringList(String rawJson) {
		if (rawJson == null || rawJson.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(rawJson, new TypeReference<List<String>>() {
			});
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid workflow JSON array: " + ex.getMessage(), ex);
		}
	}
}
