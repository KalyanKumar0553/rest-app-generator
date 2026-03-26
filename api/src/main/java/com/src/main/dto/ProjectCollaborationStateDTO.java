package com.src.main.dto;

import java.util.List;

public class ProjectCollaborationStateDTO {
	private int activeEditors;
	private List<ProjectCollaborationEditorDTO> editors;
	private List<ProjectCollaborationActionDTO> recentActions;

	public int getActiveEditors() {
		return this.activeEditors;
	}

	public List<ProjectCollaborationEditorDTO> getEditors() {
		return this.editors;
	}

	public List<ProjectCollaborationActionDTO> getRecentActions() {
		return this.recentActions;
	}

	public void setActiveEditors(final int activeEditors) {
		this.activeEditors = activeEditors;
	}

	public void setEditors(final List<ProjectCollaborationEditorDTO> editors) {
		this.editors = editors;
	}

	public void setRecentActions(final List<ProjectCollaborationActionDTO> recentActions) {
		this.recentActions = recentActions;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationStateDTO)) return false;
		final ProjectCollaborationStateDTO other = (ProjectCollaborationStateDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getActiveEditors() != other.getActiveEditors()) return false;
		final Object this$editors = this.getEditors();
		final Object other$editors = other.getEditors();
		if (this$editors == null ? other$editors != null : !this$editors.equals(other$editors)) return false;
		final Object this$recentActions = this.getRecentActions();
		final Object other$recentActions = other.getRecentActions();
		if (this$recentActions == null ? other$recentActions != null : !this$recentActions.equals(other$recentActions)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationStateDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getActiveEditors();
		final Object $editors = this.getEditors();
		result = result * PRIME + ($editors == null ? 43 : $editors.hashCode());
		final Object $recentActions = this.getRecentActions();
		result = result * PRIME + ($recentActions == null ? 43 : $recentActions.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationStateDTO(activeEditors=" + this.getActiveEditors() + ", editors=" + this.getEditors() + ", recentActions=" + this.getRecentActions() + ")";
	}

	public ProjectCollaborationStateDTO() {
	}

	public ProjectCollaborationStateDTO(final int activeEditors, final List<ProjectCollaborationEditorDTO> editors, final List<ProjectCollaborationActionDTO> recentActions) {
		this.activeEditors = activeEditors;
		this.editors = editors;
		this.recentActions = recentActions;
	}
}
