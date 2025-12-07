package com.src.main.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.JSONResponseDTO;
import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectRunDetailsResponseDTO;
import com.src.main.dto.ProjectSummaryDTO;
import com.src.main.mapper.ProjectRunMapper;
import com.src.main.model.ProjectRunEntity;
import com.src.main.service.ProjectOrchestrationService;
import com.src.main.service.ProjectService;
import com.src.main.util.AppConstants;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping(AppConstants.API_PROJECTS)
@Validated
@AllArgsConstructor
public class ProjectController {

	private final ProjectService projectService;
	private final ProjectOrchestrationService orchestrationService;


	@PreAuthorize("hasRole('USER')")
	@PostMapping(consumes = { "text/yaml", "application/x-yaml",MediaType.TEXT_PLAIN_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCreateResponseDTO create(@RequestBody String yamlText, Authentication authentication) {
		return projectService.create(yamlText, authentication.getName());
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping
	public ResponseEntity<JSONResponseDTO<List<ProjectSummaryDTO>>> fetchProjecs(Principal principal) {
		return projectService.fetchProjecs(principal.getName());
	}
	
	private String currentUserId(Principal principal) {
        return principal.getName();
    }


    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{projectId}/spec")
    public ResponseEntity<Void> updateSpec(@PathVariable UUID projectId,
                                           @RequestBody String yaml,
                                           Principal principal) {
        String userId = currentUserId(principal);
        orchestrationService.updateSpec(projectId, userId, yaml);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{projectId}/save-and-generate")
    public ResponseEntity<ProjectRunDetailsResponseDTO> saveAndGenerate(@PathVariable UUID projectId,
                                                              @RequestBody String yaml,
                                                              Principal principal) {
        String userId = currentUserId(principal);
        ProjectRunEntity run = orchestrationService.updateSpecAndGenerate(projectId, userId, yaml);
        return ResponseEntity.accepted().body(ProjectRunMapper.toDto(run));
    }


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{projectId}/generate")
    public ResponseEntity<ProjectRunDetailsResponseDTO> generate(@PathVariable UUID projectId,Principal principal) {
        String userId = currentUserId(principal);
        ProjectRunEntity run = orchestrationService.generateCode(projectId, userId);
        return ResponseEntity.accepted().body(ProjectRunMapper.toDto(run));
    }
}
