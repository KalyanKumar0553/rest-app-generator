package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectCollaborationPresenceResponseDTO extends ProjectCollaborationStateDTO {
	private String sessionId;

	public ProjectCollaborationPresenceResponseDTO(
			String sessionId,
			int activeEditors,
			java.util.List<ProjectCollaborationEditorDTO> editors,
			java.util.List<ProjectCollaborationActionDTO> recentActions) {
		super(activeEditors, editors, recentActions);
		this.sessionId = sessionId;
	}
}
