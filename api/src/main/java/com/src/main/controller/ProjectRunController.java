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
import com.src.main.service.ProjectUserIdentityService;

@RestController
@RequestMapping("/api/runs")
public class ProjectRunController {

    private final ProjectOrchestrationService orchestrationService;
    private final ProjectUserIdentityService projectUserIdentityService;

    public ProjectRunController(ProjectOrchestrationService orchestrationService,
            ProjectUserIdentityService projectUserIdentityService) {
        this.orchestrationService = orchestrationService;
        this.projectUserIdentityService = projectUserIdentityService;
    }

    private String currentUserId(Principal principal) {
        return projectUserIdentityService.currentUserId(principal);
    }

    @GetMapping("/{runId}")
    public ResponseEntity<ProjectRunDetailsResponseDTO> getRun(@PathVariable("runId") UUID runId,
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
                run.getZip() != null && run.getZip().length > 0,
                run.getErrorMessage(),
                run.getCreatedAt(),
                run.getUpdatedAt());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{runId}/download")
    public ResponseEntity<byte[]> downloadRun(@PathVariable("runId") UUID runId, Principal principal) {
        return orchestrationService.download(runId, currentUserId(principal));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<java.util.List<ProjectRunDetailsResponseDTO>> getRunsForProject(@PathVariable("projectId") UUID projectId,
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
                        run.getZip() != null && run.getZip().length > 0,
                        run.getErrorMessage(),
                        run.getCreatedAt(),
                        run.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
