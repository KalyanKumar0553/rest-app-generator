package com.src.main.controller;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.src.main.dto.ConfigPropertyResponseDTO;
import com.src.main.dto.ConfigPropertySaveRequestDTO;
import com.src.main.dto.SingleConfigEntryRequestDTO;
import com.src.main.service.ConfigMetadataService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/config", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConfigMetadataController {
	private final ConfigMetadataService configMetadataService;

	@PostMapping
	public ResponseEntity<ConfigPropertyResponseDTO> saveProperty(@Valid @RequestBody ConfigPropertySaveRequestDTO request) {
		return ResponseEntity.ok(configMetadataService.saveOrUpdateProperty(request));
	}

	@GetMapping
	public ResponseEntity<List<ConfigPropertyResponseDTO>> getAll() {
		return ResponseEntity.ok(configMetadataService.getAllProperties());
	}

	@GetMapping("/category/{category}")
	public ResponseEntity<List<ConfigPropertyResponseDTO>> getByCategory(@PathVariable("category") String category) {
		return ResponseEntity.ok(configMetadataService.getPropertiesByCategory(category));
	}

	@GetMapping("/features")
	@PreAuthorize("hasAuthority(\'config.feature.read\')")
	public ResponseEntity<List<ConfigPropertyResponseDTO>> getFeatureProperties() {
		return ResponseEntity.ok(configMetadataService.getPropertiesByCategory("FEATURES"));
	}

	@PutMapping("/features/value")
	@PreAuthorize("hasAuthority(\'config.feature.manage\')")
	public ResponseEntity<ConfigPropertyResponseDTO> updateFeatureValue(@Valid @RequestBody SingleConfigEntryRequestDTO request) {
		return ResponseEntity.ok(configMetadataService.updateCurrentValue(request));
	}

	@PostMapping("/reload")
	@PreAuthorize("hasAuthority(\'config.reload\')")
	public ResponseEntity<Void> reloadDefaults() {
		configMetadataService.reloadDefaults();
		return ResponseEntity.noContent().build();
	}

	public ConfigMetadataController(final ConfigMetadataService configMetadataService) {
		this.configMetadataService = configMetadataService;
	}
}
