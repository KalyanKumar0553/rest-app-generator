package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCollaborationInviteDTO {
	private String inviteToken;
	private String projectId;
	private String projectName;
	private String generator;
	private String ownerId;
	private boolean contributorAccess;
	private boolean requestPending;
}
