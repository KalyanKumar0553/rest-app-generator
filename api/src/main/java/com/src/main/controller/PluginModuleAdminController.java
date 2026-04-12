package com.src.main.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.src.main.dto.PluginModuleResponseDTO;
import com.src.main.dto.PluginModuleSaveRequestDTO;
import com.src.main.dto.PluginModuleVersionSaveRequestDTO;
import com.src.main.service.PluginModuleService;
import com.src.main.service.ProjectUserIdentityService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/plugin-modules")
public class PluginModuleAdminController {

	private final PluginModuleService pluginModuleService;
	private final ProjectUserIdentityService projectUserIdentityService;

	public PluginModuleAdminController(
			PluginModuleService pluginModuleService,
			ProjectUserIdentityService projectUserIdentityService) {
		this.pluginModuleService = pluginModuleService;
		this.projectUserIdentityService = projectUserIdentityService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('plugin.module.read')")
	public List<PluginModuleResponseDTO> list() {
		return pluginModuleService.getAdminModules();
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('plugin.module.manage')")
	public ResponseEntity<PluginModuleResponseDTO> create(
			@Valid @ModelAttribute PluginModuleSaveRequestDTO module,
			@Valid @ModelAttribute PluginModuleVersionSaveRequestDTO version,
			@RequestParam("artifact") MultipartFile artifact,
			Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pluginModuleService.createModule(module, version, artifact, currentUserId(principal)));
	}

	@PutMapping("/{moduleId}")
	@PreAuthorize("hasAuthority('plugin.module.manage')")
	public ResponseEntity<PluginModuleResponseDTO> update(
			@PathVariable UUID moduleId,
			@Valid @ModelAttribute PluginModuleSaveRequestDTO request) {
		return ResponseEntity.ok(pluginModuleService.updateModule(moduleId, request));
	}

	@PostMapping(path = "/{moduleId}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('plugin.module.manage')")
	public ResponseEntity<PluginModuleResponseDTO> uploadVersion(
			@PathVariable UUID moduleId,
			@Valid @ModelAttribute PluginModuleVersionSaveRequestDTO version,
			@RequestParam("artifact") MultipartFile artifact,
			Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pluginModuleService.uploadVersion(moduleId, version, artifact, currentUserId(principal)));
	}

	@PostMapping("/{moduleId}/versions/{versionId}/publish")
	@PreAuthorize("hasAuthority('plugin.module.publish')")
	public ResponseEntity<PluginModuleResponseDTO> publishVersion(
			@PathVariable UUID moduleId,
			@PathVariable UUID versionId) {
		return ResponseEntity.ok(pluginModuleService.publishVersion(moduleId, versionId));
	}

	private String currentUserId(Principal principal) {
		return projectUserIdentityService.currentUserId(principal);
	}
}
