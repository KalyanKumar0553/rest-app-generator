package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ProjectContributorDTO {
	private UUID id;
	private String userId;
	private boolean canEditDraft;
	private boolean canGenerate;
	private boolean canManageCollaboration;
	private boolean disabled;
	private OffsetDateTime disabledAt;
	private OffsetDateTime createdAt;

	public UUID getId() {
		return this.id;
	}

	public String getUserId() {
		return this.userId;
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

	public boolean isDisabled() {
		return this.disabled;
	}

	public OffsetDateTime getDisabledAt() {
		return this.disabledAt;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
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

	public void setDisabled(final boolean disabled) {
		this.disabled = disabled;
	}

	public void setDisabledAt(final OffsetDateTime disabledAt) {
		this.disabledAt = disabledAt;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectContributorDTO)) return false;
		final ProjectContributorDTO other = (ProjectContributorDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isCanEditDraft() != other.isCanEditDraft()) return false;
		if (this.isCanGenerate() != other.isCanGenerate()) return false;
		if (this.isCanManageCollaboration() != other.isCanManageCollaboration()) return false;
		if (this.isDisabled() != other.isDisabled()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
		final Object this$disabledAt = this.getDisabledAt();
		final Object other$disabledAt = other.getDisabledAt();
		if (this$disabledAt == null ? other$disabledAt != null : !this$disabledAt.equals(other$disabledAt)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectContributorDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isCanEditDraft() ? 79 : 97);
		result = result * PRIME + (this.isCanGenerate() ? 79 : 97);
		result = result * PRIME + (this.isCanManageCollaboration() ? 79 : 97);
		result = result * PRIME + (this.isDisabled() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		final Object $disabledAt = this.getDisabledAt();
		result = result * PRIME + ($disabledAt == null ? 43 : $disabledAt.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectContributorDTO(id=" + this.getId() + ", userId=" + this.getUserId() + ", canEditDraft=" + this.isCanEditDraft() + ", canGenerate=" + this.isCanGenerate() + ", canManageCollaboration=" + this.isCanManageCollaboration() + ", disabled=" + this.isDisabled() + ", disabledAt=" + this.getDisabledAt() + ", createdAt=" + this.getCreatedAt() + ")";
	}

	public ProjectContributorDTO() {
	}

	public ProjectContributorDTO(final UUID id, final String userId, final boolean canEditDraft, final boolean canGenerate, final boolean canManageCollaboration, final boolean disabled, final OffsetDateTime disabledAt, final OffsetDateTime createdAt) {
		this.id = id;
		this.userId = userId;
		this.canEditDraft = canEditDraft;
		this.canGenerate = canGenerate;
		this.canManageCollaboration = canManageCollaboration;
		this.disabled = disabled;
		this.disabledAt = disabledAt;
		this.createdAt = createdAt;
	}
}
