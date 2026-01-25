package com.src.main.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.util.AppConstants;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(AppConstants.API_ROOT + "/openapi/dependencies")
@AllArgsConstructor
public class DependenciesController {

	private static final Logger log = LoggerFactory.getLogger(DependenciesController.class);
	private static final String DEPENDENCIES_RESOURCE = "templates/project/dependencies.json";

	private final ObjectMapper objectMapper;

	@GetMapping
	public ResponseEntity<List<String>> list() {
		ClassPathResource resource = new ClassPathResource(DEPENDENCIES_RESOURCE);
		if (!resource.exists()) {
			log.warn("Dependencies resource not found at {}", DEPENDENCIES_RESOURCE);
			return ResponseEntity.ok(List.of());
		}

		try (InputStream input = resource.getInputStream()) {
			JsonNode root = objectMapper.readTree(input);
			if (root == null || !root.isArray()) {
				log.warn("Dependencies resource is not a JSON array at {}", DEPENDENCIES_RESOURCE);
				return ResponseEntity.ok(List.of());
			}

			List<String> dependencies = new ArrayList<>();
			for (JsonNode node : root) {
				String id = node.path("id").asText("").trim();
				if (!id.isEmpty()) {
					dependencies.add(id);
				}
			}
			return ResponseEntity.ok(dependencies);
		} catch (Exception ex) {
			log.error("Failed to read dependencies resource", ex);
			return ResponseEntity.internalServerError().build();
		}
	}
}
