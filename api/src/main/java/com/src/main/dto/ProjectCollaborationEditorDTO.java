package com.src.main.dto;

import java.time.OffsetDateTime;

public class ProjectCollaborationEditorDTO {
	private String sessionId;
	private String userId;
	private String label;
	private OffsetDateTime lastSeenAt;

	public String getSessionId() {
		return this.sessionId;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getLabel() {
		return this.label;
	}

	public OffsetDateTime getLastSeenAt() {
		return this.lastSeenAt;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setLastSeenAt(final OffsetDateTime lastSeenAt) {
		this.lastSeenAt = lastSeenAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationEditorDTO)) return false;
		final ProjectCollaborationEditorDTO other = (ProjectCollaborationEditorDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$sessionId = this.getSessionId();
		final Object other$sessionId = other.getSessionId();
		if (this$sessionId == null ? other$sessionId != null : !this$sessionId.equals(other$sessionId)) return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
		final Object this$label = this.getLabel();
		final Object other$label = other.getLabel();
		if (this$label == null ? other$label != null : !this$label.equals(other$label)) return false;
		final Object this$lastSeenAt = this.getLastSeenAt();
		final Object other$lastSeenAt = other.getLastSeenAt();
		if (this$lastSeenAt == null ? other$lastSeenAt != null : !this$lastSeenAt.equals(other$lastSeenAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationEditorDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $sessionId = this.getSessionId();
		result = result * PRIME + ($sessionId == null ? 43 : $sessionId.hashCode());
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		final Object $label = this.getLabel();
		result = result * PRIME + ($label == null ? 43 : $label.hashCode());
		final Object $lastSeenAt = this.getLastSeenAt();
		result = result * PRIME + ($lastSeenAt == null ? 43 : $lastSeenAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationEditorDTO(sessionId=" + this.getSessionId() + ", userId=" + this.getUserId() + ", label=" + this.getLabel() + ", lastSeenAt=" + this.getLastSeenAt() + ")";
	}

	public ProjectCollaborationEditorDTO() {
	}

	public ProjectCollaborationEditorDTO(final String sessionId, final String userId, final String label, final OffsetDateTime lastSeenAt) {
		this.sessionId = sessionId;
		this.userId = userId;
		this.label = label;
		this.lastSeenAt = lastSeenAt;
	}
}
