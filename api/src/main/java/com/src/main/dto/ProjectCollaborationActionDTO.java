package com.src.main.dto;

import java.time.OffsetDateTime;

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

	public String getActionId() {
		return this.actionId;
	}

	public String getProjectId() {
		return this.projectId;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getTabKey() {
		return this.tabKey;
	}

	public String getActionType() {
		return this.actionType;
	}

	public Integer getDraftVersion() {
		return this.draftVersion;
	}

	public String getMessage() {
		return this.message;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setActionId(final String actionId) {
		this.actionId = actionId;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public void setTabKey(final String tabKey) {
		this.tabKey = tabKey;
	}

	public void setActionType(final String actionType) {
		this.actionType = actionType;
	}

	public void setDraftVersion(final Integer draftVersion) {
		this.draftVersion = draftVersion;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationActionDTO)) return false;
		final ProjectCollaborationActionDTO other = (ProjectCollaborationActionDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$actionId = this.getActionId();
		final Object other$actionId = other.getActionId();
		if (this$actionId == null ? other$actionId != null : !this$actionId.equals(other$actionId)) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
		final Object this$sessionId = this.getSessionId();
		final Object other$sessionId = other.getSessionId();
		if (this$sessionId == null ? other$sessionId != null : !this$sessionId.equals(other$sessionId)) return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
		final Object this$tabKey = this.getTabKey();
		final Object other$tabKey = other.getTabKey();
		if (this$tabKey == null ? other$tabKey != null : !this$tabKey.equals(other$tabKey)) return false;
		final Object this$actionType = this.getActionType();
		final Object other$actionType = other.getActionType();
		if (this$actionType == null ? other$actionType != null : !this$actionType.equals(other$actionType)) return false;
		final Object this$message = this.getMessage();
		final Object other$message = other.getMessage();
		if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationActionDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $actionId = this.getActionId();
		result = result * PRIME + ($actionId == null ? 43 : $actionId.hashCode());
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		final Object $sessionId = this.getSessionId();
		result = result * PRIME + ($sessionId == null ? 43 : $sessionId.hashCode());
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		final Object $tabKey = this.getTabKey();
		result = result * PRIME + ($tabKey == null ? 43 : $tabKey.hashCode());
		final Object $actionType = this.getActionType();
		result = result * PRIME + ($actionType == null ? 43 : $actionType.hashCode());
		final Object $message = this.getMessage();
		result = result * PRIME + ($message == null ? 43 : $message.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationActionDTO(actionId=" + this.getActionId() + ", projectId=" + this.getProjectId() + ", sessionId=" + this.getSessionId() + ", userId=" + this.getUserId() + ", tabKey=" + this.getTabKey() + ", actionType=" + this.getActionType() + ", draftVersion=" + this.getDraftVersion() + ", message=" + this.getMessage() + ", createdAt=" + this.getCreatedAt() + ")";
	}

	public ProjectCollaborationActionDTO() {
	}

	public ProjectCollaborationActionDTO(final String actionId, final String projectId, final String sessionId, final String userId, final String tabKey, final String actionType, final Integer draftVersion, final String message, final OffsetDateTime createdAt) {
		this.actionId = actionId;
		this.projectId = projectId;
		this.sessionId = sessionId;
		this.userId = userId;
		this.tabKey = tabKey;
		this.actionType = actionType;
		this.draftVersion = draftVersion;
		this.message = message;
		this.createdAt = createdAt;
	}
}
