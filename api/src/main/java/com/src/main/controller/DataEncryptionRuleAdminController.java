package com.src.main.controller;

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

import com.src.main.dto.DataEncryptionRuleRequestDTO;
import com.src.main.dto.DataEncryptionRuleResponseDTO;
import com.src.main.service.DataEncryptionRuleAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/data-encryption-rules")
@RequiredArgsConstructor
public class DataEncryptionRuleAdminController {

	private final DataEncryptionRuleAdminService service;

	@GetMapping
	@PreAuthorize("hasAuthority('config.encryption.read')")
	public List<DataEncryptionRuleResponseDTO> listRules() {
		return service.listRules();
	}

	@PostMapping
	@PreAuthorize("hasAuthority('config.encryption.manage')")
	public ResponseEntity<DataEncryptionRuleResponseDTO> createRule(@Valid @RequestBody DataEncryptionRuleRequestDTO request) {
		return ResponseEntity.ok(service.createRule(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('config.encryption.manage')")
	public ResponseEntity<DataEncryptionRuleResponseDTO> updateRule(@PathVariable("id") UUID id,
			@Valid @RequestBody DataEncryptionRuleRequestDTO request) {
		return ResponseEntity.ok(service.updateRule(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('config.encryption.manage')")
	public ResponseEntity<Void> deleteRule(@PathVariable("id") UUID id) {
		service.deleteRule(id);
		return ResponseEntity.noContent().build();
	}
}
