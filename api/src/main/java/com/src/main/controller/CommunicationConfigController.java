package com.src.main.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.CommunicationConfigRequestDTO;
import com.src.main.dto.CommunicationConfigResponseDTO;
import com.src.main.service.CommunicationConfigService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/communication-config", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommunicationConfigController {
	private final CommunicationConfigService communicationConfigService;

	public CommunicationConfigController(CommunicationConfigService communicationConfigService) {
		this.communicationConfigService = communicationConfigService;
	}

	@PostMapping
	@Operation(summary = "Create or update a communication service configuration")
	public ResponseEntity<CommunicationConfigResponseDTO> save(@Valid @RequestBody CommunicationConfigRequestDTO request) {
		return ResponseEntity.ok(communicationConfigService.saveOrUpdate(request));
	}

	@GetMapping
	@Operation(summary = "List all communication service configurations")
	public ResponseEntity<List<CommunicationConfigResponseDTO>> getAll() {
		return ResponseEntity.ok(communicationConfigService.findAll());
	}

	@GetMapping("/enabled")
	@Operation(summary = "List enabled communication service configurations")
	public ResponseEntity<List<CommunicationConfigResponseDTO>> getEnabled() {
		return ResponseEntity.ok(communicationConfigService.findEnabled());
	}
}
