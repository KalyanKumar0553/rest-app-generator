package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailsDTO {
	private String projectId;
	private UUID id;
	private String name;
	private String description;
	private String generator;
	private String artifact;
	private String yaml;
	private Map<String, Object> draftData;
	private Integer draftVersion;
	private List<ProjectTabDefinitionDTO> tabDetails;
	private String ownerId;
	private boolean contributorAccess;
	private boolean canManageContributors;
	private String collaborationInviteToken;
	private List<ProjectContributorDTO> contributors;
	private List<ProjectCollaborationRequestDTO> collaborationRequests;
	private UUID latestRunId;
	private String latestRunStatus;
	private Integer latestRunNumber;
	private boolean latestRunHasZip;
	private String latestRunZipBase64;
	private String latestRunZipFileName;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
