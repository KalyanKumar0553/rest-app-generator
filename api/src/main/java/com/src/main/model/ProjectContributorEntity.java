package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = AppDbTables.PROJECT_CONTRIBUTORS, uniqueConstraints = @UniqueConstraint(name = "uq_project_contributors_project_user", columnNames = {"project_id", "user_id"}))
public class ProjectContributorEntity {
	@Id
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private ProjectEntity project;
	@Column(name = "user_id", nullable = false, length = 100)
	private String userId;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "can_edit_draft", nullable = false)
	private boolean canEditDraft;
	@Column(name = "can_generate", nullable = false)
	private boolean canGenerate;
	@Column(name = "can_manage_collaboration", nullable = false)
	private boolean canManageCollaboration;
	@Column(name = "disabled", nullable = false)
	private boolean disabled;
	@Column(name = "disabled_at")
	private OffsetDateTime disabledAt;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = OffsetDateTime.now();
		}
	}

	public ProjectContributorEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public ProjectEntity getProject() {
		return this.project;
	}

	public String getUserId() {
		return this.userId;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
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

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setProject(final ProjectEntity project) {
		this.project = project;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
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

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectContributorEntity)) return false;
		final ProjectContributorEntity other = (ProjectContributorEntity) o;
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
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$disabledAt = this.getDisabledAt();
		final Object other$disabledAt = other.getDisabledAt();
		if (this$disabledAt == null ? other$disabledAt != null : !this$disabledAt.equals(other$disabledAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectContributorEntity;
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
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $disabledAt = this.getDisabledAt();
		result = result * PRIME + ($disabledAt == null ? 43 : $disabledAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectContributorEntity(id=" + this.getId() + ", userId=" + this.getUserId() + ", createdAt=" + this.getCreatedAt() + ", canEditDraft=" + this.isCanEditDraft() + ", canGenerate=" + this.isCanGenerate() + ", canManageCollaboration=" + this.isCanManageCollaboration() + ", disabled=" + this.isDisabled() + ", disabledAt=" + this.getDisabledAt() + ")";
	}
}
