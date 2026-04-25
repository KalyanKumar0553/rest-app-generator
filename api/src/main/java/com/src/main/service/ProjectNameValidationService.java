package com.src.main.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.src.main.exception.ProjectNameAlreadyExistsException;
import com.src.main.repository.ProjectRepository;

@Service
public class ProjectNameValidationService {

	private final ProjectRepository projectRepository;

	public ProjectNameValidationService(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	public void ensureUniqueProjectName(String projectName,
			ProjectUserIdentityService.ResolvedProjectUser owner,
			UUID excludedProjectId) {
		String normalizedName = normalizeProjectName(projectName);
		if (projectRepository.existsByOwnerIdInAndNameIgnoreCase(owner.keys(), normalizedName, excludedProjectId)) {
			throw new ProjectNameAlreadyExistsException();
		}
	}

	private String normalizeProjectName(String projectName) {
		if (projectName == null || projectName.trim().isEmpty()) {
			throw new com.src.main.exception.SpecificException(HttpStatus.BAD_REQUEST, "PROJECT_NAME_REQUIRED", "Project name is required.");
		}
		return projectName.trim();
	}
}
