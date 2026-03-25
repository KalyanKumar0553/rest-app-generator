package com.src.main.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.ProjectTabDefinitionAdminRequestDTO;
import com.src.main.dto.ProjectTabDefinitionAdminResponseDTO;
import com.src.main.service.ProjectTabDefinitionService;
import com.src.main.service.ProjectUserIdentityService;

@RestController
@RequestMapping("/api/v1/admin/project-tab-definitions")
public class ProjectTabDefinitionAdminController {

	private final ProjectTabDefinitionService projectTabDefinitionService;
	private final ProjectUserIdentityService projectUserIdentityService;

	public ProjectTabDefinitionAdminController(
			ProjectTabDefinitionService projectTabDefinitionService,
			ProjectUserIdentityService projectUserIdentityService) {
		this.projectTabDefinitionService = projectTabDefinitionService;
		this.projectUserIdentityService = projectUserIdentityService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('project.tab.layout.read')")
	public List<ProjectTabDefinitionAdminResponseDTO> list() {
		return projectTabDefinitionService.getAdminTabs();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('project.tab.layout.manage')")
	public ResponseEntity<ProjectTabDefinitionAdminResponseDTO> create(
			@RequestBody ProjectTabDefinitionAdminRequestDTO request,
			Principal principal) {
		return ResponseEntity.ok(projectTabDefinitionService.create(request, currentUserId(principal)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('project.tab.layout.manage')")
	public ResponseEntity<ProjectTabDefinitionAdminResponseDTO> update(
			@PathVariable UUID id,
			@RequestBody ProjectTabDefinitionAdminRequestDTO request,
			Principal principal) {
		return ResponseEntity.ok(projectTabDefinitionService.update(id, request, currentUserId(principal)));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('project.tab.layout.manage')")
	public ResponseEntity<Void> delete(@PathVariable UUID id) {
		projectTabDefinitionService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private String currentUserId(Principal principal) {
		return projectUserIdentityService.currentUserId(principal);
	}
}
