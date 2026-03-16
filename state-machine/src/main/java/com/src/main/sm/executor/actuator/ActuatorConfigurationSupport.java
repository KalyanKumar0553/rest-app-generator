package com.src.main.sm.executor.actuator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ActuatorConfigurationSupport {

	private static final Set<String> SUPPORTED_ENDPOINTS = Set.of(
			"health", "shutdown", "metrics", "info", "env", "beans", "mappings", "loggers",
			"threaddump", "heapdump", "prometheus", "conditions", "configprops", "caches", "scheduledtasks");

	private static final List<String> DEFAULT_ENDPOINTS = List.of("health", "metrics", "info");

	private ActuatorConfigurationSupport() {
	}

	public static boolean isActuatorEnabled(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object raw = yaml.get("enableActuator");
		if (raw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			raw = ((Map<String, Object>) appRaw).get("enableActuator");
		}
		return parseBoolean(raw);
	}

	@SuppressWarnings("unchecked")
	public static List<String> resolveIncludedEndpoints(Map<String, Object> yaml) {
		if (yaml == null) {
			return new ArrayList<>(DEFAULT_ENDPOINTS);
		}

		Object actuatorRaw = yaml.get("actuator");
		if (!(actuatorRaw instanceof Map<?, ?> actuatorMapRaw)) {
			return new ArrayList<>(DEFAULT_ENDPOINTS);
		}
		Map<String, Object> actuatorMap = (Map<String, Object>) actuatorMapRaw;

		Object endpointsRaw = actuatorMap.get("endpoints");
		if (!(endpointsRaw instanceof Map<?, ?> endpointsMapRaw)) {
			return new ArrayList<>(DEFAULT_ENDPOINTS);
		}
		Map<String, Object> endpointsMap = (Map<String, Object>) endpointsMapRaw;

		Object includeRaw = endpointsMap.get("include");
		if (!(includeRaw instanceof List<?> includeList)) {
			return new ArrayList<>(DEFAULT_ENDPOINTS);
		}

		LinkedHashSet<String> cleaned = includeList.stream()
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.map(String::trim)
				.map(String::toLowerCase)
				.filter(value -> !value.isBlank())
				.filter(SUPPORTED_ENDPOINTS::contains)
				.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

		if (cleaned.isEmpty()) {
			return new ArrayList<>(DEFAULT_ENDPOINTS);
		}
		return new ArrayList<>(cleaned);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<String>> resolveProfileIncludedEndpoints(Map<String, Object> yaml) {
		Map<String, List<String>> result = new LinkedHashMap<>();
		if (yaml == null) {
			return result;
		}

		Object actuatorRaw = yaml.get("actuator");
		if (!(actuatorRaw instanceof Map<?, ?> actuatorMapRaw)) {
			return result;
		}
		Map<String, Object> actuatorMap = (Map<String, Object>) actuatorMapRaw;
		Object profilesRaw = actuatorMap.get("profiles");
		if (!(profilesRaw instanceof Map<?, ?> profilesMapRaw)) {
			return result;
		}

		Map<String, Object> profiles = (Map<String, Object>) profilesMapRaw;
		profiles.forEach((rawProfile, rawConfig) -> {
			String profile = normalizeProfile(rawProfile);
			if (profile == null || "default".equals(profile)) {
				return;
			}
			List<String> included = resolveIncludeList(rawConfig);
			if (!included.isEmpty()) {
				result.put(profile, included);
			}
		});

		return result;
	}

	@SuppressWarnings("unchecked")
	private static List<String> resolveIncludeList(Object profileConfig) {
		if (!(profileConfig instanceof Map<?, ?> profileMapRaw)) {
			return new ArrayList<>();
		}
		Map<String, Object> profileMap = (Map<String, Object>) profileMapRaw;
		Object endpointsRaw = profileMap.get("endpoints");
		if (!(endpointsRaw instanceof Map<?, ?> endpointsMapRaw)) {
			return new ArrayList<>();
		}
		Map<String, Object> endpoints = (Map<String, Object>) endpointsMapRaw;
		Object includeRaw = endpoints.get("include");
		if (!(includeRaw instanceof List<?> includeList)) {
			return new ArrayList<>();
		}

		LinkedHashSet<String> cleaned = includeList.stream()
				.filter(Objects::nonNull)
				.map(String::valueOf)
				.map(String::trim)
				.map(String::toLowerCase)
				.filter(value -> !value.isBlank())
				.filter(SUPPORTED_ENDPOINTS::contains)
				.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
		return new ArrayList<>(cleaned);
	}

	private static String normalizeProfile(Object rawProfile) {
		if (rawProfile == null) {
			return null;
		}
		String profile = String.valueOf(rawProfile).trim().toLowerCase();
		if (profile.isBlank()) {
			return null;
		}
		return profile;
	}

	private static boolean parseBoolean(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Boolean bool) {
			return bool;
		}
		String normalized = String.valueOf(value).trim().toLowerCase();
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
	}
}
