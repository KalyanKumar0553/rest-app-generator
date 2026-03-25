package com.src.main.service;

import java.util.List;
import java.util.UUID;

import com.src.main.dto.ProjectCollaborationInviteDTO;
import com.src.main.dto.ProjectCollaborationRequestCreateDTO;
import com.src.main.dto.ProjectCollaborationRequestDTO;
import com.src.main.dto.ProjectCollaborationRequestReviewDTO;
import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectContributorDTO;
import com.src.main.dto.ProjectContributorPermissionUpdateDTO;
import com.src.main.dto.ProjectContributorUpsertRequestDTO;
import com.src.main.dto.ProjectDraftResponseDTO;
import com.src.main.dto.ProjectDraftTabDataDTO;
import com.src.main.dto.ProjectDraftTabPatchRequestDTO;
import com.src.main.dto.ProjectDraftUpsertRequestDTO;
import com.src.main.dto.ProjectDetailsDTO;
import com.src.main.dto.ProjectImportRequestDTO;
import com.src.main.dto.ProjectSummaryDTO;
import com.src.main.dto.ProjectTabDefinitionDTO;
import com.src.main.model.ProjectEntity;

public interface ProjectService {
	List<ProjectSummaryDTO> list(String userId);

	ProjectCreateResponseDTO create(String yamlText, String ownerId);

	ProjectDraftResponseDTO createDraft(ProjectDraftUpsertRequestDTO request, String ownerId);

	ProjectDraftResponseDTO updateDraft(UUID projectId, ProjectDraftUpsertRequestDTO request, String ownerId);

	ProjectDraftResponseDTO patchDraftTab(UUID projectId, ProjectDraftTabPatchRequestDTO request, String ownerId);

	ProjectEntity getAccessibleProject(UUID projectId, String userId);

	ProjectDetailsDTO getDetails(UUID projectId, String userId);

	ProjectDraftTabDataDTO getDraftTabData(UUID projectId, String tabKey, String userId);

	List<ProjectTabDefinitionDTO> getTabDetails(String generator, List<String> dependencies);

	List<ProjectContributorDTO> getContributors(UUID projectId, String userId);

	List<ProjectContributorDTO> addContributor(UUID projectId, String ownerId, ProjectContributorUpsertRequestDTO request);

	List<ProjectContributorDTO> updateContributorPermissions(UUID projectId, UUID contributorId, String ownerId,
			ProjectContributorPermissionUpdateDTO request);

	void removeContributor(UUID projectId, String ownerId, String contributorUserId);

	List<ProjectCollaborationRequestDTO> getCollaborationRequests(UUID projectId, String userId);

	ProjectCollaborationRequestDTO reviewCollaborationRequest(UUID projectId, UUID requestId, String ownerId,
			ProjectCollaborationRequestReviewDTO request);

	ProjectCollaborationInviteDTO getCollaborationInvite(String inviteToken, String userId);

	ProjectCollaborationRequestDTO requestCollaboration(String inviteToken, String userId,
			ProjectCollaborationRequestCreateDTO request);

	ProjectSummaryDTO importProject(ProjectImportRequestDTO request, String userId);

	void deleteProject(UUID projectId, String userId);
}
