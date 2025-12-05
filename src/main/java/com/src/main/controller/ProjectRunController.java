package com.src.main.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.ProjectRunDetailsResponseDTO;
import com.src.main.service.ProjectOrchestrationService;

@RestController
@RequestMapping("/api/runs")
public class ProjectRunController {

    private final ProjectOrchestrationService orchestrationService;

    public ProjectRunController(ProjectOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    private String currentUserId(Principal principal) {
        return principal.getName();
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ProjectRunDetailsResponseDTO> getRun(@PathVariable UUID runId,
            Principal principal) {
        String userId = currentUserId(principal);
        var run = orchestrationService.getRun(runId, userId);

        var dto = new ProjectRunDetailsResponseDTO(
                run.getId(),
                run.getProject().getId(),
                run.getOwnerId(),
                run.getType(),
                run.getStatus(),
                run.getRunNumber(),
                run.getErrorMessage(),
                run.getCreatedAt(),
                run.getUpdatedAt());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<java.util.List<ProjectRunDetailsResponseDTO>> getRunsForProject(@PathVariable UUID projectId,
            Principal principal) {
        String userId = currentUserId(principal);
        var runs = orchestrationService.getRunsForProject(projectId, userId);

        var dtos = runs.stream()
                .map(run -> new ProjectRunDetailsResponseDTO(
                        run.getId(),
                        run.getProject().getId(),
                        run.getOwnerId(),
                        run.getType(),
                        run.getStatus(),
                        run.getRunNumber(),
                        run.getErrorMessage(),
                        run.getCreatedAt(),
                        run.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
