package com.src.main.dto;

import lombok.Data;

@Data
public class ProjectCollaborationRequestCreateDTO {
	private boolean canEditDraft = true;
	private boolean canGenerate = false;
	private boolean canManageCollaboration = false;
}
