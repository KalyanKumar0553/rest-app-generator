package com.src.main.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.AiLabsAvailabilityDTO;
import com.src.main.service.AiLabsQuotaService;
import com.src.main.service.ConfigMetadataService;
import com.src.main.service.ProjectUserIdentityService;

@RestController
@RequestMapping("/api/ai-labs")
public class AiLabsAvailabilityController {

	private static final String AI_LABS_FEATURE_KEY = "app.feature.ai-labs.enabled";

	private final ConfigMetadataService configMetadataService;
	private final ProjectUserIdentityService projectUserIdentityService;
	private final AiLabsQuotaService aiLabsQuotaService;

	public AiLabsAvailabilityController(
			ConfigMetadataService configMetadataService,
			ProjectUserIdentityService projectUserIdentityService,
			AiLabsQuotaService aiLabsQuotaService) {
		this.configMetadataService = configMetadataService;
		this.projectUserIdentityService = projectUserIdentityService;
		this.aiLabsQuotaService = aiLabsQuotaService;
	}

	@GetMapping("/availability")
	public AiLabsAvailabilityDTO getAvailability(Principal principal) {
		boolean enabled = configMetadataService.isPropertyEnabled(AI_LABS_FEATURE_KEY, false);
		if (principal == null) {
			return new AiLabsAvailabilityDTO(enabled, null, 0, null, false);
		}
		AiLabsQuotaService.AiLabsQuotaSnapshot snapshot = aiLabsQuotaService
				.getSnapshot(projectUserIdentityService.currentUserId(principal));
		return new AiLabsAvailabilityDTO(
				enabled,
				snapshot.usageLimitValue(),
				snapshot.usedCount(),
				snapshot.remainingCountValue(),
				enabled && snapshot.limitReached());
	}
}
