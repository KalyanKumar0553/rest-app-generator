package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectCollaborationActionRequestDTO {
	@NotBlank(message = "sessionId is required")
	private String sessionId;
	@NotBlank(message = "tabKey is required")
	private String tabKey;
	@NotBlank(message = "actionType is required")
	private String actionType;
	private Integer draftVersion;
	private String message;

	public ProjectCollaborationActionRequestDTO() {
	}

	public String getSessionId() {
		return this.sessionId;
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

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
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

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationActionRequestDTO)) return false;
		final ProjectCollaborationActionRequestDTO other = (ProjectCollaborationActionRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$draftVersion = this.getDraftVersion();
		final Object other$draftVersion = other.getDraftVersion();
		if (this$draftVersion == null ? other$draftVersion != null : !this$draftVersion.equals(other$draftVersion)) return false;
		final Object this$sessionId = this.getSessionId();
		final Object other$sessionId = other.getSessionId();
		if (this$sessionId == null ? other$sessionId != null : !this$sessionId.equals(other$sessionId)) return false;
		final Object this$tabKey = this.getTabKey();
		final Object other$tabKey = other.getTabKey();
		if (this$tabKey == null ? other$tabKey != null : !this$tabKey.equals(other$tabKey)) return false;
		final Object this$actionType = this.getActionType();
		final Object other$actionType = other.getActionType();
		if (this$actionType == null ? other$actionType != null : !this$actionType.equals(other$actionType)) return false;
		final Object this$message = this.getMessage();
		final Object other$message = other.getMessage();
		if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationActionRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $draftVersion = this.getDraftVersion();
		result = result * PRIME + ($draftVersion == null ? 43 : $draftVersion.hashCode());
		final Object $sessionId = this.getSessionId();
		result = result * PRIME + ($sessionId == null ? 43 : $sessionId.hashCode());
		final Object $tabKey = this.getTabKey();
		result = result * PRIME + ($tabKey == null ? 43 : $tabKey.hashCode());
		final Object $actionType = this.getActionType();
		result = result * PRIME + ($actionType == null ? 43 : $actionType.hashCode());
		final Object $message = this.getMessage();
		result = result * PRIME + ($message == null ? 43 : $message.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationActionRequestDTO(sessionId=" + this.getSessionId() + ", tabKey=" + this.getTabKey() + ", actionType=" + this.getActionType() + ", draftVersion=" + this.getDraftVersion() + ", message=" + this.getMessage() + ")";
	}
}
