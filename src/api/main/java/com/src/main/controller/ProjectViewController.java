package com.src.main.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.service.ProjectViewService;

import lombok.AllArgsConstructor;

@RestController
@Validated
@RequestMapping("/api/project-view")
@AllArgsConstructor
public class ProjectViewController {

	private final ProjectViewService projectViewService;

	@PostMapping(value = "/generate-zip", consumes = { "text/yaml", "application/x-yaml", MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<byte[]> generateZip(@RequestBody String yamlText) {
		byte[] zip = projectViewService.generateZip(yamlText);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"project-preview.zip\"")
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(zip);
	}
}
