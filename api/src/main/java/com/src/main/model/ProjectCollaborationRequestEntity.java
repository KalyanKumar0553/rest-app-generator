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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.PROJECT_COLLABORATION_REQUESTS)
public class ProjectCollaborationRequestEntity {
	@Id
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private ProjectEntity project;
	@Column(name = "requester_id", nullable = false, length = 100)
	private String requesterId;
	@Column(name = "status", nullable = false, length = 32)
	private String status;
	@Column(name = "requested_can_edit_draft", nullable = false)
	private boolean requestedCanEditDraft;
	@Column(name = "requested_can_generate", nullable = false)
	private boolean requestedCanGenerate;
	@Column(name = "requested_can_manage_collaboration", nullable = false)
	private boolean requestedCanManageCollaboration;
	@Column(name = "granted_can_edit_draft", nullable = false)
	private boolean grantedCanEditDraft;
	@Column(name = "granted_can_generate", nullable = false)
	private boolean grantedCanGenerate;
	@Column(name = "granted_can_manage_collaboration", nullable = false)
	private boolean grantedCanManageCollaboration;
	@Column(name = "reviewed_by", length = 100)
	private String reviewedBy;
	@Column(name = "reviewed_at")
	private OffsetDateTime reviewedAt;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		OffsetDateTime now = OffsetDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = OffsetDateTime.now();
	}

	public ProjectCollaborationRequestEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public ProjectEntity getProject() {
		return this.project;
	}

	public String getRequesterId() {
		return this.requesterId;
	}

	public String getStatus() {
		return this.status;
	}

	public boolean isRequestedCanEditDraft() {
		return this.requestedCanEditDraft;
	}

	public boolean isRequestedCanGenerate() {
		return this.requestedCanGenerate;
	}

	public boolean isRequestedCanManageCollaboration() {
		return this.requestedCanManageCollaboration;
	}

	public boolean isGrantedCanEditDraft() {
		return this.grantedCanEditDraft;
	}

	public boolean isGrantedCanGenerate() {
		return this.grantedCanGenerate;
	}

	public boolean isGrantedCanManageCollaboration() {
		return this.grantedCanManageCollaboration;
	}

	public String getReviewedBy() {
		return this.reviewedBy;
	}

	public OffsetDateTime getReviewedAt() {
		return this.reviewedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setProject(final ProjectEntity project) {
		this.project = project;
	}

	public void setRequesterId(final String requesterId) {
		this.requesterId = requesterId;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setRequestedCanEditDraft(final boolean requestedCanEditDraft) {
		this.requestedCanEditDraft = requestedCanEditDraft;
	}

	public void setRequestedCanGenerate(final boolean requestedCanGenerate) {
		this.requestedCanGenerate = requestedCanGenerate;
	}

	public void setRequestedCanManageCollaboration(final boolean requestedCanManageCollaboration) {
		this.requestedCanManageCollaboration = requestedCanManageCollaboration;
	}

	public void setGrantedCanEditDraft(final boolean grantedCanEditDraft) {
		this.grantedCanEditDraft = grantedCanEditDraft;
	}

	public void setGrantedCanGenerate(final boolean grantedCanGenerate) {
		this.grantedCanGenerate = grantedCanGenerate;
	}

	public void setGrantedCanManageCollaboration(final boolean grantedCanManageCollaboration) {
		this.grantedCanManageCollaboration = grantedCanManageCollaboration;
	}

	public void setReviewedBy(final String reviewedBy) {
		this.reviewedBy = reviewedBy;
	}

	public void setReviewedAt(final OffsetDateTime reviewedAt) {
		this.reviewedAt = reviewedAt;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectCollaborationRequestEntity)) return false;
		final ProjectCollaborationRequestEntity other = (ProjectCollaborationRequestEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isRequestedCanEditDraft() != other.isRequestedCanEditDraft()) return false;
		if (this.isRequestedCanGenerate() != other.isRequestedCanGenerate()) return false;
		if (this.isRequestedCanManageCollaboration() != other.isRequestedCanManageCollaboration()) return false;
		if (this.isGrantedCanEditDraft() != other.isGrantedCanEditDraft()) return false;
		if (this.isGrantedCanGenerate() != other.isGrantedCanGenerate()) return false;
		if (this.isGrantedCanManageCollaboration() != other.isGrantedCanManageCollaboration()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$requesterId = this.getRequesterId();
		final Object other$requesterId = other.getRequesterId();
		if (this$requesterId == null ? other$requesterId != null : !this$requesterId.equals(other$requesterId)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$reviewedBy = this.getReviewedBy();
		final Object other$reviewedBy = other.getReviewedBy();
		if (this$reviewedBy == null ? other$reviewedBy != null : !this$reviewedBy.equals(other$reviewedBy)) return false;
		final Object this$reviewedAt = this.getReviewedAt();
		final Object other$reviewedAt = other.getReviewedAt();
		if (this$reviewedAt == null ? other$reviewedAt != null : !this$reviewedAt.equals(other$reviewedAt)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectCollaborationRequestEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isRequestedCanEditDraft() ? 79 : 97);
		result = result * PRIME + (this.isRequestedCanGenerate() ? 79 : 97);
		result = result * PRIME + (this.isRequestedCanManageCollaboration() ? 79 : 97);
		result = result * PRIME + (this.isGrantedCanEditDraft() ? 79 : 97);
		result = result * PRIME + (this.isGrantedCanGenerate() ? 79 : 97);
		result = result * PRIME + (this.isGrantedCanManageCollaboration() ? 79 : 97);
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $requesterId = this.getRequesterId();
		result = result * PRIME + ($requesterId == null ? 43 : $requesterId.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $reviewedBy = this.getReviewedBy();
		result = result * PRIME + ($reviewedBy == null ? 43 : $reviewedBy.hashCode());
		final Object $reviewedAt = this.getReviewedAt();
		result = result * PRIME + ($reviewedAt == null ? 43 : $reviewedAt.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectCollaborationRequestEntity(id=" + this.getId() + ", requesterId=" + this.getRequesterId() + ", status=" + this.getStatus() + ", requestedCanEditDraft=" + this.isRequestedCanEditDraft() + ", requestedCanGenerate=" + this.isRequestedCanGenerate() + ", requestedCanManageCollaboration=" + this.isRequestedCanManageCollaboration() + ", grantedCanEditDraft=" + this.isGrantedCanEditDraft() + ", grantedCanGenerate=" + this.isGrantedCanGenerate() + ", grantedCanManageCollaboration=" + this.isGrantedCanManageCollaboration() + ", reviewedBy=" + this.getReviewedBy() + ", reviewedAt=" + this.getReviewedAt() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
