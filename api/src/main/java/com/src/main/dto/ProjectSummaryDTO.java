package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDTO {
	private String projectId;
	private String artifact;
	private UUID id;
	private String name;
	private String description;
	private String generator;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private String ownerId;
	private boolean contributorAccess;
}
