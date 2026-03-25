package com.src.main.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.AiLabsAvailabilityDTO;
import com.src.main.service.ConfigMetadataService;

@RestController
@RequestMapping("/api/ai-labs")
public class AiLabsAvailabilityController {

	private static final String AI_LABS_FEATURE_KEY = "app.feature.ai-labs.enabled";

	private final ConfigMetadataService configMetadataService;

	public AiLabsAvailabilityController(ConfigMetadataService configMetadataService) {
		this.configMetadataService = configMetadataService;
	}

	@GetMapping("/availability")
	public AiLabsAvailabilityDTO getAvailability() {
		return new AiLabsAvailabilityDTO(configMetadataService.isPropertyEnabled(AI_LABS_FEATURE_KEY, false));
	}
}
