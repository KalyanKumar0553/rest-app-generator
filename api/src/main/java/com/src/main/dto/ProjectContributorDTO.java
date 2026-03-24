package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectContributorDTO {
	private UUID id;
	private String userId;
	private boolean canEditDraft;
	private boolean canGenerate;
	private boolean canManageCollaboration;
	private OffsetDateTime createdAt;
}
