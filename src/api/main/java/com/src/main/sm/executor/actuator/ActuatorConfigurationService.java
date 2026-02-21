package com.src.main.sm.executor.actuator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

@Service
public class ActuatorConfigurationService {

	@SuppressWarnings("unchecked")
	public void applyConfiguration(Map<String, Object> yaml, List<String> includedEndpoints,
			Map<String, List<String>> profileIncludedEndpoints, Path rootDir) {
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

		writeGeneratedApplicationConfig(yaml, rootDir, "application", includedEndpoints);
		profileIncludedEndpoints.forEach((profile, endpointsForProfile) ->
				writeGeneratedApplicationConfig(yaml, rootDir, "application-" + profile, endpointsForProfile));
	}

	@SuppressWarnings("unchecked")
	private void writeGeneratedApplicationConfig(Map<String, Object> yaml, Path rootDir, String fileBaseName,
			List<String> includedEndpoints) {
		if (rootDir == null || includedEndpoints == null) {
			return;
		}

		String format = resolveApplicationFormat(yaml);
		Path resourcesDir = rootDir.resolve("src/main/resources");
		Path filePath = resourcesDir.resolve(fileBaseName + ("properties".equals(format) ? ".properties" : ".yml"));

		try {
			Files.createDirectories(resourcesDir);
			if ("properties".equals(format)) {
				Properties props = new Properties();
				if (Files.exists(filePath)) {
					try (InputStream in = Files.newInputStream(filePath)) {
						props.load(in);
					}
				}
				props.setProperty("management.endpoints.web.exposure.include", String.join(",", includedEndpoints));
				props.setProperty("management.endpoint.shutdown.enabled",
						String.valueOf(includedEndpoints.contains("shutdown")));
				try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(filePath), StandardCharsets.UTF_8)) {
					props.store(writer, null);
				}
				return;
			}

			Map<String, Object> content = new LinkedHashMap<>();
			if (Files.exists(filePath)) {
				try (InputStream in = Files.newInputStream(filePath)) {
					Object loaded = new Yaml().load(in);
					if (loaded instanceof Map<?, ?> loadedMap) {
						content = (Map<String, Object>) loadedMap;
					}
				}
			}

			Map<String, Object> management = content.get("management") instanceof Map<?, ?> managementRaw
					? (Map<String, Object>) managementRaw
					: new LinkedHashMap<>();
			content.put("management", management);

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

			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			options.setPrettyFlow(true);
			options.setIndent(2);
			options.setIndicatorIndent(1);
			options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
			options.setWidth(4096);
			Yaml yamlWriter = new Yaml(new Representer(options), options);
			try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(filePath), StandardCharsets.UTF_8)) {
				yamlWriter.dump(content, writer);
			}
		} catch (IOException ignored) {
			// best effort: keep generation flow resilient
		}
	}

	@SuppressWarnings("unchecked")
	private String resolveApplicationFormat(Map<String, Object> yaml) {
		if (yaml == null) {
			return "yaml";
		}
		Object raw = yaml.get("applFormat");
		if (raw == null && yaml.get("preferences") instanceof Map<?, ?> preferences) {
			raw = ((Map<String, Object>) preferences).get("applFormat");
		}
		if (raw == null && yaml.get("app") instanceof Map<?, ?> app) {
			raw = ((Map<String, Object>) app).get("applFormat");
		}
		String normalized = raw == null ? "" : String.valueOf(raw).trim().toLowerCase();
		return "properties".equals(normalized) ? "properties" : "yaml";
	}
}
