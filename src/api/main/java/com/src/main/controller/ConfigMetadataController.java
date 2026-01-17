package com.src.main.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.ConfigPropertyResponseDTO;
import com.src.main.dto.ConfigPropertySaveRequestDTO;
import com.src.main.service.ConfigMetadataService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/config")
@AllArgsConstructor
public class ConfigMetadataController {

	private final ConfigMetadataService configMetadataService;

	@PostMapping
	public ResponseEntity<ConfigPropertyResponseDTO> saveProperty(@RequestBody ConfigPropertySaveRequestDTO request) {
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
	
	@PostMapping("/reload")
    @PreAuthorize("hasRole('CONFIG_ADMIN')")
    public ResponseEntity<Void> reloadDefaults() {
		configMetadataService.reloadDefaults();
        return ResponseEntity.noContent().build();
    }
}
