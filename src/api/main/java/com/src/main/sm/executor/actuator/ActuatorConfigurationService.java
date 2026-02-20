package com.src.main.sm.executor.actuator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ActuatorConfigurationService {

	@SuppressWarnings("unchecked")
	public void applyConfiguration(Map<String, Object> yaml, List<String> includedEndpoints) {
		if (yaml == null) {
			return;
		}
		Map<String, Object> properties = yaml.get("properties") instanceof Map<?, ?> propsRaw
				? (Map<String, Object>) propsRaw
				: new LinkedHashMap<>();
		yaml.put("properties", properties);

		Map<String, Object> management = properties.get("management") instanceof Map<?, ?> managementRaw
				? (Map<String, Object>) managementRaw
				: new LinkedHashMap<>();
		properties.put("management", management);

		Map<String, Object> endpoints = management.get("endpoints") instanceof Map<?, ?> endpointsRaw
				? (Map<String, Object>) endpointsRaw
				: new LinkedHashMap<>();
		management.put("endpoints", endpoints);

		Map<String, Object> web = endpoints.get("web") instanceof Map<?, ?> webRaw
				? (Map<String, Object>) webRaw
				: new LinkedHashMap<>();
		endpoints.put("web", web);

		Map<String, Object> exposure = web.get("exposure") instanceof Map<?, ?> exposureRaw
				? (Map<String, Object>) exposureRaw
				: new LinkedHashMap<>();
		web.put("exposure", exposure);
		exposure.put("include", includedEndpoints);

		Map<String, Object> endpoint = management.get("endpoint") instanceof Map<?, ?> endpointRaw
				? (Map<String, Object>) endpointRaw
				: new LinkedHashMap<>();
		management.put("endpoint", endpoint);

		Map<String, Object> shutdown = endpoint.get("shutdown") instanceof Map<?, ?> shutdownRaw
				? (Map<String, Object>) shutdownRaw
				: new LinkedHashMap<>();
		endpoint.put("shutdown", shutdown);
		shutdown.put("enabled", includedEndpoints.contains("shutdown"));
	}
}
