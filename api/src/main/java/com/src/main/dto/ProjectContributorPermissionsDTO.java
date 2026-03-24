package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectContributorPermissionsDTO {
	private boolean canEditDraft;
	private boolean canGenerate;
	private boolean canManageCollaboration;
}
