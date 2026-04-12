package com.src.main.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.dto.ProjectTabDefinitionDTO;

@Service
public class ProjectDraftService {

	private static final Set<String> CORE_TAB_KEYS = Set.of(
			"general",
			"modules",
			"actuator",
			"entities",
			"data-objects",
			"mappers",
			"controllers",
			"collaborate",
			"explore");

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;
	private final ProjectTabDefinitionService projectTabDefinitionService;

	public ProjectDraftService(ObjectMapper objectMapper, ProjectTabDefinitionService projectTabDefinitionService) {
		this.objectMapper = objectMapper;
		this.projectTabDefinitionService = projectTabDefinitionService;
	}

	public String serialize(Map<String, Object> draftData) {
		try {
			return objectMapper.writeValueAsString(draftData == null ? Collections.emptyMap() : draftData);
		} catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Invalid draft payload: " + ex.getMessage(), ex);
		}
	}

	public Map<String, Object> deserialize(String draftData) {
		if (draftData == null || draftData.isBlank()) {
			return Collections.emptyMap();
		}
		try {
			return objectMapper.readValue(draftData, MAP_TYPE);
		} catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Invalid stored draft payload: " + ex.getMessage(), ex);
		}
	}

	public String resolveGenerator(Map<String, Object> draftData, String fallbackGenerator) {
		Object settingsRaw = draftData == null ? null : draftData.get("settings");
		if (settingsRaw instanceof Map<?, ?> settings) {
			Object language = settings.get("language");
			if (language != null) {
				String value = String.valueOf(language).trim();
				if (!value.isBlank()) {
					return value;
				}
			}
		}
		if (fallbackGenerator != null && !fallbackGenerator.isBlank()) {
			return fallbackGenerator.trim();
		}
		return "java";
	}

	public List<String> resolveSelectedDependencies(Map<String, Object> draftData) {
		if (draftData == null || draftData.isEmpty()) {
			return List.of();
		}
		Object selectedDependencies = draftData.get("selectedDependencies");
		if (selectedDependencies instanceof List<?> dependencies) {
			return dependencies.stream()
					.filter(Objects::nonNull)
					.map(String::valueOf)
					.map(String::trim)
					.filter(value -> !value.isBlank())
					.toList();
		}
		Object dependenciesRaw = draftData.get("dependencies");
		if (dependenciesRaw instanceof String dependencyString && !dependencyString.isBlank()) {
			return java.util.Arrays.stream(dependencyString.split(","))
					.map(String::trim)
					.filter(value -> !value.isBlank())
					.toList();
		}
		return List.of();
	}

	public List<ProjectTabDefinitionDTO> getTabDetails(String generator, List<String> dependencies) {
		return getTabDetails(generator, dependencies, Collections.emptySet());
	}

	public List<ProjectTabDefinitionDTO> getTabDetails(String generator, List<String> dependencies, String tabKey) {
		return getTabDetails(generator, dependencies, Collections.emptySet(), tabKey);
	}

	public List<ProjectTabDefinitionDTO> getTabDetails(String generator, List<String> dependencies, Set<String> configEnabledModuleKeys) {
		return getTabDetails(generator, dependencies, configEnabledModuleKeys, null);
	}

	public List<ProjectTabDefinitionDTO> getTabDetails(String generator, List<String> dependencies, Set<String> configEnabledModuleKeys, String tabKey) {
		String normalizedGenerator = resolveGenerator(Map.of(), generator).toLowerCase(Locale.ROOT);
		Set<String> selectedDependencies = normalizeValues(dependencies);
		Set<String> enabledModuleKeys = normalizeValues(configEnabledModuleKeys);
		String normalizedTabKey = normalizeTabKey(tabKey);
		return projectTabDefinitionService.getEnabledTabs(normalizedGenerator).stream()
				.filter(tab -> normalizedTabKey.isBlank() || normalizedTabKey.equalsIgnoreCase(tab.getKey()))
				.filter(tab -> shouldIncludeTab(tab, selectedDependencies, enabledModuleKeys))
				.toList();
	}

	public Map<String, Object> extractTabData(Map<String, Object> draftData, String tabKey, String generator) {
		String normalizedTabKey = normalizeTabKey(tabKey);
		if (normalizedTabKey.isBlank()) {
			throw new IllegalArgumentException("tabKey must be provided");
		}
		Map<String, Object> source = draftData == null ? Collections.emptyMap() : draftData;
		Map<String, Object> extracted = new LinkedHashMap<>();
		if (isModuleTab(normalizedTabKey)) {
			Object moduleConfigsRaw = source.get("moduleConfigs");
			if (moduleConfigsRaw instanceof Map<?, ?> moduleConfigs) {
				Object moduleConfig = ((Map<String, Object>) moduleConfigs).get(normalizedTabKey);
				Map<String, Object> scopedConfigs = new LinkedHashMap<>();
				scopedConfigs.put(normalizedTabKey, moduleConfig);
				extracted.put("moduleConfigs", scopedConfigs);
			}
			return extracted;
		}
		for (String ownedKey : ownedDraftKeys(normalizedTabKey, generator)) {
			if (source.containsKey(ownedKey)) {
				extracted.put(ownedKey, source.get(ownedKey));
			}
		}
		return extracted;
	}

	public Map<String, Object> mergeTabData(Map<String, Object> existingDraftData, String tabKey, Map<String, Object> tabData, String generator) {
		String normalizedTabKey = normalizeTabKey(tabKey);
		if (normalizedTabKey.isBlank()) {
			throw new IllegalArgumentException("tabKey must be provided");
		}
		if ("explore".equals(normalizedTabKey) || "collaborate".equals(normalizedTabKey)) {
			throw new IllegalArgumentException("Explore tab does not support draft updates");
		}

		Map<String, Object> merged = new LinkedHashMap<>(existingDraftData == null ? Collections.emptyMap() : existingDraftData);
		Map<String, Object> normalizedTabData = tabData == null ? Collections.emptyMap() : new LinkedHashMap<>(tabData);
		if (isModuleTab(normalizedTabKey)) {
			Map<String, Object> existingModuleConfigs = merged.get("moduleConfigs") instanceof Map<?, ?> existing
					? new LinkedHashMap<>((Map<String, Object>) existing)
					: new LinkedHashMap<>();
			Object incomingConfigsRaw = normalizedTabData.get("moduleConfigs");
			if (incomingConfigsRaw instanceof Map<?, ?> incomingConfigs) {
				Object incomingModuleConfig = ((Map<String, Object>) incomingConfigs).get(normalizedTabKey);
				existingModuleConfigs.put(normalizedTabKey,
						incomingModuleConfig instanceof Map<?, ?>
								? new LinkedHashMap<>((Map<String, Object>) incomingModuleConfig)
								: incomingModuleConfig);
			}
			merged.put("moduleConfigs", existingModuleConfigs);
			return merged;
		}
		for (String ownedKey : ownedDraftKeys(normalizedTabKey, generator)) {
			if (normalizedTabData.containsKey(ownedKey)) {
				merged.put(ownedKey, normalizedTabData.get(ownedKey));
			}
		}
		return merged;
	}

	public List<String> ownedDraftKeys(String tabKey, String generator) {
		String normalizedTabKey = normalizeTabKey(tabKey);
		String normalizedGenerator = resolveGenerator(Map.of(), generator).toLowerCase(Locale.ROOT);
		return switch (normalizedTabKey) {
		case "general" -> List.of("settings", "database", "preferences", "dependencies", "selectedDependencies");
		case "modules" -> List.of("dependencies", "selectedDependencies", "moduleConfigs", "selectedPlugins");
		case "actuator" -> List.of("actuator");
		case "entities" -> List.of("entities", "relations");
		case "data-objects" -> List.of("dataObjects", "enums");
		case "mappers" -> List.of("mappers");
		case "controllers" -> List.of("controllers");
		default -> isModuleTab(normalizedTabKey)
				? List.of("moduleConfigs")
				: throwUnsupportedTabKey(tabKey, normalizedGenerator);
		};
	}

	private boolean isModuleTab(String tabKey) {
		return !CORE_TAB_KEYS.contains(tabKey);
	}

	private String normalizeTabKey(String tabKey) {
		return tabKey == null ? "" : tabKey.trim().toLowerCase(Locale.ROOT);
	}

	private Set<String> normalizeValues(java.util.Collection<String> values) {
		return values == null ? Collections.emptySet()
				: values.stream()
						.filter(Objects::nonNull)
						.map(String::valueOf)
						.map(String::trim)
						.filter(value -> !value.isBlank())
						.map(value -> value.toLowerCase(Locale.ROOT))
						.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
	}

	private boolean shouldIncludeTab(
			ProjectTabDefinitionDTO tab,
			Set<String> selectedDependencies,
			Set<String> enabledModuleKeys) {
		String componentKey = normalizeTabKey(tab.getComponentKey());
		if (!componentKey.startsWith("module-")) {
			return true;
		}
		String tabKey = normalizeTabKey(tab.getKey());
		return selectedDependencies.contains(tabKey) && enabledModuleKeys.contains(tabKey);
	}

	private List<String> throwUnsupportedTabKey(String tabKey, String generator) {
		throw new IllegalArgumentException("Unsupported tabKey: " + tabKey + " for generator " + generator);
	}
}
