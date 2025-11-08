package com.src.main.controllers;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.ProjectCreateResponse;
import com.src.main.dto.ProjectStatusResponse;
import com.src.main.dto.ProjectSummary;
import com.src.main.service.ProjectService;
import com.src.main.utils.AppConstants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping(AppConstants.API_PROJECTS)
@Validated
public class ProjectController {

	private final ProjectService service;

	public ProjectController(ProjectService service) {
		this.service = service;
	}

	@PostMapping(consumes = { "text/yaml", "application/x-yaml", MediaType.TEXT_PLAIN_VALUE })
	public ProjectCreateResponse create(@RequestBody @NotBlank String yamlText) {
		return service.create(yamlText);
	}

	@GetMapping(AppConstants.PATH_ID)
	public ResponseEntity<ProjectStatusResponse> status(
			@Parameter(description = "Project ID", required = true, schema = @Schema(format = "uuid")) @PathVariable("id") String id) {
		try {
			return ResponseEntity.ok(service.status(UUID.fromString(id)));
		} catch (NoSuchElementException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping(AppConstants.PATH_ID + AppConstants.PATH_DOWNLOAD)
	public ResponseEntity<byte[]> download(
			@Parameter(description = "Project ID", required = true, schema = @Schema(format = "uuid")) @PathVariable("id") String id) {
		try {
			return service.download(UUID.fromString(id));
		} catch (NoSuchElementException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping
	public java.util.List<ProjectSummary> list() {
		return service.list();
	}
}
