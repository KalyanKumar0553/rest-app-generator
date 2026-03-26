package com.src.main.agent.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AgentSpecParserService {

	private static final Pattern JSON_FENCE_PATTERN = Pattern.compile(
			"```(?:json)?\\s*(.*?)```", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;

	public AgentSpecParserService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public Map<String, Object> parseSpec(String rawOutput) {
		if (rawOutput == null || rawOutput.isBlank()) {
			throw new IllegalArgumentException("Agent returned an empty specification.");
		}
		String trimmed = rawOutput.trim();
		Map<String, Object> parsed = tryParseJson(trimmed);
		if (parsed != null) {
			return normalize(parsed);
		}
		Matcher jsonMatcher = JSON_FENCE_PATTERN.matcher(trimmed);
		if (jsonMatcher.find()) {
			parsed = tryParseJson(jsonMatcher.group(1).trim());
			if (parsed != null) {
				return normalize(parsed);
			}
		}
		throw new IllegalArgumentException("Agent returned invalid specification format. Expected JSON.");
	}

	public String serializeSpec(Map<String, Object> spec) {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to serialize project specification.", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> applyOverrides(Map<String, Object> baseSpec, Map<String, Object> overrides) {
		if (overrides == null || overrides.isEmpty()) {
			return baseSpec;
		}
		Map<String, Object> merged = new LinkedHashMap<>(baseSpec);
		for (Map.Entry<String, Object> entry : overrides.entrySet()) {
			Object baseValue = merged.get(entry.getKey());
			Object overrideValue = entry.getValue();
			if (baseValue instanceof Map && overrideValue instanceof Map) {
				merged.put(entry.getKey(), applyOverrides(
						(Map<String, Object>) baseValue, (Map<String, Object>) overrideValue));
			} else {
				merged.put(entry.getKey(), overrideValue);
			}
		}
		return merged;
	}

	private Map<String, Object> tryParseJson(String text) {
		try {
			return objectMapper.readValue(text, MAP_TYPE);
		} catch (Exception ex) {
			return null;
		}
	}

	private Map<String, Object> normalize(Map<String, Object> spec) {
		Map<String, Object> normalized = new LinkedHashMap<>(spec);
		normalized.computeIfAbsent("settings", k -> new LinkedHashMap<>());
		normalized.computeIfAbsent("database", k -> new LinkedHashMap<>());
		normalized.computeIfAbsent("preferences", k -> new LinkedHashMap<>());
		normalized.computeIfAbsent("entities", k -> new ArrayList<>());
		normalized.computeIfAbsent("relations", k -> new ArrayList<>());
		normalized.computeIfAbsent("dataObjects", k -> new ArrayList<>());
		normalized.computeIfAbsent("enums", k -> new ArrayList<>());
		normalized.computeIfAbsent("mappers", k -> new ArrayList<>());
		normalized.computeIfAbsent("controllers", k -> new LinkedHashMap<>());
		normalized.computeIfAbsent("selectedDependencies", k -> new ArrayList<>());
		return normalized;
	}
}
