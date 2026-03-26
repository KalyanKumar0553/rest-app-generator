package com.src.main.dto;

public class ProjectContributorPermissionsDTO {
	private boolean canEditDraft;
	private boolean canGenerate;
	private boolean canManageCollaboration;

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
		if (!(o instanceof ProjectContributorPermissionsDTO)) return false;
		final ProjectContributorPermissionsDTO other = (ProjectContributorPermissionsDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isCanEditDraft() != other.isCanEditDraft()) return false;
		if (this.isCanGenerate() != other.isCanGenerate()) return false;
		if (this.isCanManageCollaboration() != other.isCanManageCollaboration()) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectContributorPermissionsDTO;
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
		return "ProjectContributorPermissionsDTO(canEditDraft=" + this.isCanEditDraft() + ", canGenerate=" + this.isCanGenerate() + ", canManageCollaboration=" + this.isCanManageCollaboration() + ")";
	}

	public ProjectContributorPermissionsDTO() {
	}

	public ProjectContributorPermissionsDTO(final boolean canEditDraft, final boolean canGenerate, final boolean canManageCollaboration) {
		this.canEditDraft = canEditDraft;
		this.canGenerate = canGenerate;
		this.canManageCollaboration = canManageCollaboration;
	}
}
