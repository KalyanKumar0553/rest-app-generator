package com.src.main.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.LinkedHashSet;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.dto.ProjectTabDefinitionDTO;

@Service
public class ProjectDraftService {

	private static final Set<String> SHIPPABLE_MODULE_KEYS = Set.of("rbac", "auth", "state-machine", "subscription");

	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;

	public ProjectDraftService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
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
		String normalizedGenerator = generator == null ? "java" : generator.trim().toLowerCase();
		List<ProjectTabDefinitionDTO> tabs = new java.util.ArrayList<>();
		if ("node".equals(normalizedGenerator) || "python".equals(normalizedGenerator)) {
			tabs.addAll(List.of(
					new ProjectTabDefinitionDTO("general", "General", "public", "node-general", 10),
					new ProjectTabDefinitionDTO("entities", "Entities", "storage", "entities", 20),
					new ProjectTabDefinitionDTO("data-objects", "Data Objects", "category", "data-objects", 30),
					new ProjectTabDefinitionDTO("mappers", "Mappers", "shuffle", "mappers", 40),
					new ProjectTabDefinitionDTO("modules", "Modules", "widgets", "modules", 50)));
		} else {
			tabs.addAll(List.of(
					new ProjectTabDefinitionDTO("general", "General", "public", "java-general", 10),
					new ProjectTabDefinitionDTO("actuator", "Actuator", "device_hub", "actuator", 20),
					new ProjectTabDefinitionDTO("entities", "Entities", "storage", "entities", 30),
					new ProjectTabDefinitionDTO("data-objects", "Data Objects", "category", "data-objects", 40),
					new ProjectTabDefinitionDTO("mappers", "Mappers", "shuffle", "mappers", 50),
					new ProjectTabDefinitionDTO("modules", "Modules", "widgets", "modules", 60)));
		}
		tabs.addAll(buildModuleTabs(dependencies, tabs.get(tabs.size() - 1).getOrder() + 10));
		int controllersOrder = tabs.get(tabs.size() - 1).getOrder() + 10;
		tabs.add(new ProjectTabDefinitionDTO("controllers", "Controllers", "tune", "controllers", controllersOrder));
		int tailOrder = controllersOrder + 10;
		tabs.add(new ProjectTabDefinitionDTO("collaborate", "Collaborate", "groups", "collaborate", tailOrder));
		tabs.add(new ProjectTabDefinitionDTO("explore", "Explore", "search", "explore", tailOrder + 10));
		return List.copyOf(tabs);
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
		String normalizedGenerator = resolveGenerator(Map.of(), generator).toLowerCase();
		return switch (normalizedTabKey) {
		case "general" -> List.of("settings", "database", "preferences", "dependencies", "selectedDependencies");
		case "modules" -> List.of("dependencies", "selectedDependencies", "moduleConfigs");
		case "actuator" -> List.of("actuator");
		case "entities" -> List.of("entities", "relations");
		case "data-objects" -> List.of("dataObjects", "enums");
		case "mappers" -> List.of("mappers");
		case "controllers" -> List.of("controllers");
		case "rbac", "auth", "state-machine", "subscription" -> List.of("moduleConfigs");
		default -> throw new IllegalArgumentException("Unsupported tabKey: " + tabKey + " for generator " + normalizedGenerator);
		};
	}

	private boolean isModuleTab(String tabKey) {
		return SHIPPABLE_MODULE_KEYS.contains(tabKey);
	}

	private List<ProjectTabDefinitionDTO> buildModuleTabs(List<String> dependencies, int startingOrder) {
		LinkedHashSet<String> moduleKeys = new LinkedHashSet<>();
		if (dependencies != null) {
			dependencies.stream()
					.filter(Objects::nonNull)
					.map(String::trim)
					.map(String::toLowerCase)
					.filter(SHIPPABLE_MODULE_KEYS::contains)
					.forEach(moduleKeys::add);
		}
		int order = startingOrder;
		List<ProjectTabDefinitionDTO> tabs = new java.util.ArrayList<>();
		for (String moduleKey : moduleKeys) {
			tabs.add(switch (moduleKey) {
			case "rbac" -> new ProjectTabDefinitionDTO("rbac", "RBAC", "admin_panel_settings", "module-rbac", order);
			case "auth" -> new ProjectTabDefinitionDTO("auth", "Auth", "lock", "module-auth", order);
			case "state-machine" -> new ProjectTabDefinitionDTO("state-machine", "State Machine", "schema", "module-state-machine", order);
			case "subscription" -> new ProjectTabDefinitionDTO("subscription", "Subscription", "workspace_premium", "module-subscription", order);
			default -> null;
			});
			order += 10;
		}
		return tabs.stream().filter(Objects::nonNull).toList();
	}

	private String normalizeTabKey(String tabKey) {
		return tabKey == null ? "" : tabKey.trim().toLowerCase();
	}
}
