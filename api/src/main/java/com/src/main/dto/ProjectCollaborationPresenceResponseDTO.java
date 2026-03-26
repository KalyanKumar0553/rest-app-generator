package com.src.main.dto;

public class ProjectCollaborationPresenceResponseDTO extends ProjectCollaborationStateDTO {
	private String sessionId;

	public ProjectCollaborationPresenceResponseDTO(String sessionId, int activeEditors, java.util.List<ProjectCollaborationEditorDTO> editors, java.util.List<ProjectCollaborationActionDTO> recentActions) {
		super(activeEditors, editors, recentActions);
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationPresenceResponseDTO(sessionId=" + this.getSessionId() + ")";
	}

	public ProjectCollaborationPresenceResponseDTO() {
	}

	public ProjectCollaborationPresenceResponseDTO(final String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationPresenceResponseDTO)) return false;
		final ProjectCollaborationPresenceResponseDTO other = (ProjectCollaborationPresenceResponseDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (!super.equals(o)) return false;
		final Object this$sessionId = this.getSessionId();
		final Object other$sessionId = other.getSessionId();
		if (this$sessionId == null ? other$sessionId != null : !this$sessionId.equals(other$sessionId)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationPresenceResponseDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = super.hashCode();
		final Object $sessionId = this.getSessionId();
		result = result * PRIME + ($sessionId == null ? 43 : $sessionId.hashCode());
		return result;
	}
}
