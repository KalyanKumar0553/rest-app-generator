package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCollaborationActionRequestDTO {
	@NotBlank(message = "sessionId is required")
	private String sessionId;

	@NotBlank(message = "tabKey is required")
	private String tabKey;

	@NotBlank(message = "actionType is required")
	private String actionType;

	private Integer draftVersion;

	private String message;
}
