package com.src.main.service;

import java.util.List;
import java.util.UUID;

import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectContributorDTO;
import com.src.main.dto.ProjectContributorUpsertRequestDTO;
import com.src.main.dto.ProjectDetailsDTO;
import com.src.main.dto.ProjectSummaryDTO;

public interface ProjectService {
	List<ProjectSummaryDTO> list(String userId);

	ProjectCreateResponseDTO create(String yamlText, String ownerId);

	ProjectDetailsDTO getDetails(UUID projectId, String userId);

	List<ProjectContributorDTO> getContributors(UUID projectId, String userId);

	List<ProjectContributorDTO> addContributor(UUID projectId, String ownerId, ProjectContributorUpsertRequestDTO request);

	void removeContributor(UUID projectId, String ownerId, String contributorUserId);

	void deleteProject(UUID projectId, String userId);
}
