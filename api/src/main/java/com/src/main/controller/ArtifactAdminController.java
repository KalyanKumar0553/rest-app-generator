package com.src.main.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.src.main.dto.ArtifactAppRequestDTO;
import com.src.main.dto.ArtifactAppResponseDTO;
import com.src.main.dto.ArtifactAppVersionResponseDTO;
import com.src.main.dto.ArtifactPublishRequestDTO;
import com.src.main.dto.ArtifactVersionCreateRequestDTO;
import com.src.main.service.ArtifactAdminService;
import com.src.main.service.ProjectUserIdentityService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/artifacts/apps")
public class ArtifactAdminController {
	private final ArtifactAdminService artifactAdminService;
	private final ProjectUserIdentityService projectUserIdentityService;

	@GetMapping
	@PreAuthorize("hasAuthority(\'artifact.app.read\')")
	public List<ArtifactAppResponseDTO> listApps() {
		return artifactAdminService.listApps();
	}

	@GetMapping("/{appId}")
	@PreAuthorize("hasAuthority(\'artifact.app.read\')")
	public ArtifactAppResponseDTO getApp(@PathVariable UUID appId) {
		return artifactAdminService.getApp(appId);
	}

	@PostMapping
	@PreAuthorize("hasAuthority(\'artifact.app.manage\')")
	public ResponseEntity<ArtifactAppResponseDTO> createApp(@Valid @RequestBody ArtifactAppRequestDTO request, Principal principal) {
		return ResponseEntity.ok(artifactAdminService.createApp(request, currentUserId(principal)));
	}

	@PutMapping("/{appId}")
	@PreAuthorize("hasAuthority(\'artifact.app.manage\')")
	public ResponseEntity<ArtifactAppResponseDTO> updateApp(@PathVariable UUID appId, @Valid @RequestBody ArtifactAppRequestDTO request, Principal principal) {
		return ResponseEntity.ok(artifactAdminService.updateApp(appId, request, currentUserId(principal)));
	}

	@GetMapping("/{appId}/versions")
	@PreAuthorize("hasAuthority(\'artifact.app.read\')")
	public List<ArtifactAppVersionResponseDTO> listVersions(@PathVariable UUID appId) {
		return artifactAdminService.listVersions(appId);
	}

	@PostMapping("/{appId}/versions")
	@PreAuthorize("hasAuthority(\'artifact.app.manage\')")
	public ResponseEntity<ArtifactAppVersionResponseDTO> createVersion(@PathVariable UUID appId, @RequestBody(required = false) ArtifactVersionCreateRequestDTO request, Principal principal) {
		return ResponseEntity.ok(artifactAdminService.createVersion(appId, request == null ? null : request.getVersionCode(), currentUserId(principal)));
	}

	@PostMapping("/{appId}/publish")
	@PreAuthorize("hasAuthority(\'artifact.app.publish\')")
	public ResponseEntity<ArtifactAppResponseDTO> publish(@PathVariable UUID appId, @RequestBody(required = false) ArtifactPublishRequestDTO request, Principal principal) {
		return ResponseEntity.ok(artifactAdminService.publish(appId, request == null ? null : request.getVersionCode(), currentUserId(principal)));
	}

	private String currentUserId(Principal principal) {
		return projectUserIdentityService.currentUserId(principal);
	}

	public ArtifactAdminController(final ArtifactAdminService artifactAdminService, final ProjectUserIdentityService projectUserIdentityService) {
		this.artifactAdminService = artifactAdminService;
		this.projectUserIdentityService = projectUserIdentityService;
	}
}
