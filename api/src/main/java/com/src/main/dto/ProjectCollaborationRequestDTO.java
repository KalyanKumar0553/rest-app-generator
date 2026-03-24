package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCollaborationRequestDTO {
	private UUID id;
	private String requesterId;
	private String status;
	private ProjectContributorPermissionsDTO requestedPermissions;
	private ProjectContributorPermissionsDTO grantedPermissions;
	private String reviewedBy;
	private OffsetDateTime reviewedAt;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
