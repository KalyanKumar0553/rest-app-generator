package com.src.main.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.src.main.service.DependencyCatalogService;
import com.src.main.util.AppConstants;

@RestController
@RequestMapping(AppConstants.API_ROOT + "/openapi/dependencies")
public class DependenciesController {
	private final DependencyCatalogService dependencyCatalogService;

	@GetMapping
	public ResponseEntity<List<String>> list() {
		return ResponseEntity.ok(dependencyCatalogService.listDependencies());
	}

	public DependenciesController(final DependencyCatalogService dependencyCatalogService) {
		this.dependencyCatalogService = dependencyCatalogService;
	}
}
