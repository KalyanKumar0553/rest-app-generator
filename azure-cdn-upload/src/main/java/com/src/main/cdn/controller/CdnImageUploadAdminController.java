package com.src.main.cdn.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.src.main.cdn.dto.CdnImageBatchSettingsResponseDTO;
import com.src.main.cdn.dto.CdnImageBatchToggleRequestDTO;
import com.src.main.cdn.dto.CdnImageDraftResponseDTO;
import com.src.main.cdn.dto.CdnImageTriggerRequestDTO;
import com.src.main.cdn.dto.CdnImageTriggerResponseDTO;
import com.src.main.cdn.dto.CdnImageUploadResponseDTO;
import com.src.main.cdn.service.CdnImageUploadService;
import com.src.main.cdn.service.CdnImageUploadSettingsService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/cdn-images")
public class CdnImageUploadAdminController {

	private final CdnImageUploadService uploadService;
	private final CdnImageUploadSettingsService settingsService;

	public CdnImageUploadAdminController(
			CdnImageUploadService uploadService,
			CdnImageUploadSettingsService settingsService) {
		this.uploadService = uploadService;
		this.settingsService = settingsService;
	}

	@PostMapping(path = "/drafts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('cdn.image.upload.manage')")
	public ResponseEntity<CdnImageUploadResponseDTO> uploadDrafts(
			@RequestParam("files") List<MultipartFile> files,
			Principal principal) {
		return ResponseEntity.status(HttpStatus.CREATED).body(uploadService.createDrafts(files, actor(principal)));
	}

	@GetMapping("/drafts")
	@PreAuthorize("hasAuthority('cdn.image.upload.read')")
	public List<CdnImageDraftResponseDTO> listDrafts() {
		return uploadService.listDrafts();
	}

	@PostMapping("/trigger")
	@PreAuthorize("hasAuthority('cdn.image.upload.process')")
	public ResponseEntity<CdnImageTriggerResponseDTO> triggerUpload(
			@Valid @RequestBody(required = false) CdnImageTriggerRequestDTO request,
			Principal principal) {
		List<java.util.UUID> ids = request == null ? List.of() : request.getDraftIds();
		return ResponseEntity.ok(uploadService.queueDrafts(ids, actor(principal)));
	}

	@GetMapping("/batch-processing")
	@PreAuthorize("hasAuthority('cdn.image.upload.read')")
	public ResponseEntity<CdnImageBatchSettingsResponseDTO> getBatchProcessingStatus() {
		return ResponseEntity.ok(settingsService.getSettings());
	}

	@PutMapping("/batch-processing")
	@PreAuthorize("hasAuthority('cdn.image.upload.process')")
	public ResponseEntity<CdnImageBatchSettingsResponseDTO> updateBatchProcessing(
			@Valid @RequestBody CdnImageBatchToggleRequestDTO request,
			Principal principal) {
		return ResponseEntity.ok(settingsService.updateBatchProcessing(request.getEnabled(), actor(principal)));
	}

	private String actor(Principal principal) {
		return principal == null ? "system" : principal.getName();
	}
}
