package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCollaborationRequestReviewDTO {
	@NotBlank(message = "status is required")
	private String status;
	private boolean canEditDraft;
	private boolean canGenerate;
	private boolean canManageCollaboration;
}
