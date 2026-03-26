package com.src.main.dto;

public class ProjectEditorPresenceRequestDTO {
	private String sessionId;

	public ProjectEditorPresenceRequestDTO() {
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectEditorPresenceRequestDTO)) return false;
		final ProjectEditorPresenceRequestDTO other = (ProjectEditorPresenceRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$sessionId = this.getSessionId();
		final Object other$sessionId = other.getSessionId();
		if (this$sessionId == null ? other$sessionId != null : !this$sessionId.equals(other$sessionId)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectEditorPresenceRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $sessionId = this.getSessionId();
		result = result * PRIME + ($sessionId == null ? 43 : $sessionId.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectEditorPresenceRequestDTO(sessionId=" + this.getSessionId() + ")";
	}
}
