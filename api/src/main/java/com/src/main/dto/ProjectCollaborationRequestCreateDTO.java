package com.src.main.dto;

public class ProjectCollaborationRequestCreateDTO {
	private boolean canEditDraft = true;
	private boolean canGenerate = false;
	private boolean canManageCollaboration = false;

	public ProjectCollaborationRequestCreateDTO() {
	}

	public boolean isCanEditDraft() {
		return this.canEditDraft;
	}

	public boolean isCanGenerate() {
		return this.canGenerate;
	}

	public boolean isCanManageCollaboration() {
		return this.canManageCollaboration;
	}

	public void setCanEditDraft(final boolean canEditDraft) {
		this.canEditDraft = canEditDraft;
	}

	public void setCanGenerate(final boolean canGenerate) {
		this.canGenerate = canGenerate;
	}

	public void setCanManageCollaboration(final boolean canManageCollaboration) {
		this.canManageCollaboration = canManageCollaboration;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationRequestCreateDTO)) return false;
		final ProjectCollaborationRequestCreateDTO other = (ProjectCollaborationRequestCreateDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isCanEditDraft() != other.isCanEditDraft()) return false;
		if (this.isCanGenerate() != other.isCanGenerate()) return false;
		if (this.isCanManageCollaboration() != other.isCanManageCollaboration()) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationRequestCreateDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isCanEditDraft() ? 79 : 97);
		result = result * PRIME + (this.isCanGenerate() ? 79 : 97);
		result = result * PRIME + (this.isCanManageCollaboration() ? 79 : 97);
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationRequestCreateDTO(canEditDraft=" + this.isCanEditDraft() + ", canGenerate=" + this.isCanGenerate() + ", canManageCollaboration=" + this.isCanManageCollaboration() + ")";
	}
}
