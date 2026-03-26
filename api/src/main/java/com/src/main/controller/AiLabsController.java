package com.src.main.controller;

import java.security.Principal;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.src.main.dto.AiLabsGenerateRequestDTO;
import com.src.main.dto.AiLabsGenerateResponseDTO;
import com.src.main.dto.AiLabsJobStatusDTO;
import com.src.main.service.AiLabsService;
import com.src.main.service.ProjectUserIdentityService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai-labs")
public class AiLabsController {
	private final AiLabsService aiLabsService;
	private final ProjectUserIdentityService projectUserIdentityService;

	@PostMapping("/jobs")
	public AiLabsGenerateResponseDTO generateProject(@Valid @RequestBody AiLabsGenerateRequestDTO request, Principal principal) {
		return aiLabsService.createJob(request.getPrompt(), projectUserIdentityService.currentUserId(principal));
	}

	@GetMapping("/jobs/{jobId}")
	public AiLabsJobStatusDTO getJob(@PathVariable("jobId") UUID jobId) {
		return aiLabsService.getJob(jobId);
	}

	@GetMapping(value = "/jobs/{jobId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamJob(@PathVariable("jobId") UUID jobId) {
		return aiLabsService.subscribe(jobId);
	}

	public AiLabsController(final AiLabsService aiLabsService, final ProjectUserIdentityService projectUserIdentityService) {
		this.aiLabsService = aiLabsService;
		this.projectUserIdentityService = projectUserIdentityService;
	}
}
