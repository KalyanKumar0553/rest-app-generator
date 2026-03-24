package com.src.main.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCollaborationEditorDTO {
	private String sessionId;
	private String userId;
	private String label;
	private OffsetDateTime lastSeenAt;
}
