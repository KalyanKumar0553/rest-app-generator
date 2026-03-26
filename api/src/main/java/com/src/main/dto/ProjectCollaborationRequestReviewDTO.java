package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectCollaborationRequestReviewDTO {
	@NotBlank(message = "status is required")
	private String status;
	private boolean canEditDraft;
	private boolean canGenerate;
	private boolean canManageCollaboration;

	public ProjectCollaborationRequestReviewDTO() {
	}

	public String getStatus() {
		return this.status;
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

	public void setStatus(final String status) {
		this.status = status;
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
		if (!(o instanceof ProjectCollaborationRequestReviewDTO)) return false;
		final ProjectCollaborationRequestReviewDTO other = (ProjectCollaborationRequestReviewDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isCanEditDraft() != other.isCanEditDraft()) return false;
		if (this.isCanGenerate() != other.isCanGenerate()) return false;
		if (this.isCanManageCollaboration() != other.isCanManageCollaboration()) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationRequestReviewDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isCanEditDraft() ? 79 : 97);
		result = result * PRIME + (this.isCanGenerate() ? 79 : 97);
		result = result * PRIME + (this.isCanManageCollaboration() ? 79 : 97);
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationRequestReviewDTO(status=" + this.getStatus() + ", canEditDraft=" + this.isCanEditDraft() + ", canGenerate=" + this.isCanGenerate() + ", canManageCollaboration=" + this.isCanManageCollaboration() + ")";
	}
}
