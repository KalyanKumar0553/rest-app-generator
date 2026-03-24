package com.src.main.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCollaborationActionDTO {
	private String actionId;
	private String projectId;
	private String sessionId;
	private String userId;
	private String tabKey;
	private String actionType;
	private Integer draftVersion;
	private String message;
	private OffsetDateTime createdAt;
}
