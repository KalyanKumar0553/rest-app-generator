package com.src.main.dto;

import lombok.Data;

@Data
public class ProjectContributorPermissionUpdateDTO {
	private boolean canEditDraft;
	private boolean canGenerate;
	private boolean canManageCollaboration;
}
