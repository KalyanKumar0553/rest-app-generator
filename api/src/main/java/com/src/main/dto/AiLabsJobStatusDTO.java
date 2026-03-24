package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiLabsJobStatusDTO {
	private UUID jobId;
	private String status;
	private String prompt;
	private List<AiLabsStepDTO> steps;
	private String streamPreview;
	private String projectId;
	private String generator;
	private String errorMessage;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
