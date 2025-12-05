package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectRunDetailsResponseDTO {
	private UUID runId;
	private UUID projectId;
	private String ownerId;
	private ProjectRunType type;
	private ProjectRunStatus status;
	private int runNumber;
	private String errorMessage;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
