package com.src.main.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.util.ShippableModuleSupport;

@Service
public class DependencyCatalogService {

	private static final Logger log = LoggerFactory.getLogger(DependencyCatalogService.class);
	private static final String DEPENDENCIES_RESOURCE = "templates/project/dependencies.json";
	private static final String STATE_MACHINE_RESOURCE_PREFIX = "shipped-modules/state-machine/src/main/resources/";

	private final ObjectMapper objectMapper;

	public DependencyCatalogService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Cacheable(cacheNames = "dependencyCatalog", sync = true)
	public List<String> listDependencies() {
		String resolvedResource = resolveResourcePath(DEPENDENCIES_RESOURCE);
		if (resolvedResource == null) {
			log.warn("Dependencies resource not found at {}", DEPENDENCIES_RESOURCE);
			return List.of();
		}

		try (InputStream input = getClass().getClassLoader().getResourceAsStream(resolvedResource)) {
			JsonNode root = objectMapper.readTree(input);
			if (root == null || !root.isArray()) {
				log.warn("Dependencies resource is not a JSON array at {}", resolvedResource);
				return List.of();
			}

			List<String> dependencies = new ArrayList<>();
			for (JsonNode node : root) {
				String id = node.path("id").asText("").trim();
				if (!id.isEmpty()) {
					dependencies.add(id);
				}
			}
			for (String moduleId : ShippableModuleSupport.visibleModuleIds()) {
				if (!dependencies.contains(moduleId)) {
					dependencies.add(moduleId);
				}
			}
			return List.copyOf(dependencies);
		} catch (Exception ex) {
			log.error("Failed to read dependencies resource", ex);
			throw new IllegalStateException("Failed to load dependencies catalog", ex);
		}
	}

	private String resolveResourcePath(String resourcePath) {
		if (resourcePath == null || resourcePath.isBlank()) {
			return null;
		}
		ClassLoader classLoader = getClass().getClassLoader();
		if (classLoader.getResource(resourcePath) != null) {
			return resourcePath;
		}
		String prefixedPath = STATE_MACHINE_RESOURCE_PREFIX + resourcePath;
		if (classLoader.getResource(prefixedPath) != null) {
			return prefixedPath;
		}
		return null;
	}
}
