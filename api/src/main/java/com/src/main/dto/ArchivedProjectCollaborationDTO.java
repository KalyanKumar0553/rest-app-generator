package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedProjectCollaborationDTO {
	private UUID contributorId;
	private UUID projectId;
	private String projectName;
	private String ownerId;
	private String generator;
	private String inviteToken;
	private OffsetDateTime disabledAt;
}
