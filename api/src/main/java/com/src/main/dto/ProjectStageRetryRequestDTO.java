package com.src.main.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectStageRetryRequestDTO {
	private UUID runId;

	@NotBlank(message = "stage is required")
	private String stage;
}
