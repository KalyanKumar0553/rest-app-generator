package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ProjectCollaborationRequestDTO {
	private UUID id;
	private String requesterId;
	private String status;
	private ProjectContributorPermissionsDTO requestedPermissions;
	private ProjectContributorPermissionsDTO grantedPermissions;
	private String reviewedBy;
	private OffsetDateTime reviewedAt;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public UUID getId() {
		return this.id;
	}

	public String getRequesterId() {
		return this.requesterId;
	}

	public String getStatus() {
		return this.status;
	}

	public ProjectContributorPermissionsDTO getRequestedPermissions() {
		return this.requestedPermissions;
	}

	public ProjectContributorPermissionsDTO getGrantedPermissions() {
		return this.grantedPermissions;
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

	public void setRequesterId(final String requesterId) {
		this.requesterId = requesterId;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setRequestedPermissions(final ProjectContributorPermissionsDTO requestedPermissions) {
		this.requestedPermissions = requestedPermissions;
	}

	public void setGrantedPermissions(final ProjectContributorPermissionsDTO grantedPermissions) {
		this.grantedPermissions = grantedPermissions;
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
		if (!(o instanceof ProjectCollaborationRequestDTO)) return false;
		final ProjectCollaborationRequestDTO other = (ProjectCollaborationRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$requesterId = this.getRequesterId();
		final Object other$requesterId = other.getRequesterId();
		if (this$requesterId == null ? other$requesterId != null : !this$requesterId.equals(other$requesterId)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$requestedPermissions = this.getRequestedPermissions();
		final Object other$requestedPermissions = other.getRequestedPermissions();
		if (this$requestedPermissions == null ? other$requestedPermissions != null : !this$requestedPermissions.equals(other$requestedPermissions)) return false;
		final Object this$grantedPermissions = this.getGrantedPermissions();
		final Object other$grantedPermissions = other.getGrantedPermissions();
		if (this$grantedPermissions == null ? other$grantedPermissions != null : !this$grantedPermissions.equals(other$grantedPermissions)) return false;
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
		return other instanceof ProjectCollaborationRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $requesterId = this.getRequesterId();
		result = result * PRIME + ($requesterId == null ? 43 : $requesterId.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $requestedPermissions = this.getRequestedPermissions();
		result = result * PRIME + ($requestedPermissions == null ? 43 : $requestedPermissions.hashCode());
		final Object $grantedPermissions = this.getGrantedPermissions();
		result = result * PRIME + ($grantedPermissions == null ? 43 : $grantedPermissions.hashCode());
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
		return "ProjectCollaborationRequestDTO(id=" + this.getId() + ", requesterId=" + this.getRequesterId() + ", status=" + this.getStatus() + ", requestedPermissions=" + this.getRequestedPermissions() + ", grantedPermissions=" + this.getGrantedPermissions() + ", reviewedBy=" + this.getReviewedBy() + ", reviewedAt=" + this.getReviewedAt() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}

	public ProjectCollaborationRequestDTO() {
	}

	public ProjectCollaborationRequestDTO(final UUID id, final String requesterId, final String status, final ProjectContributorPermissionsDTO requestedPermissions, final ProjectContributorPermissionsDTO grantedPermissions, final String reviewedBy, final OffsetDateTime reviewedAt, final OffsetDateTime createdAt, final OffsetDateTime updatedAt) {
		this.id = id;
		this.requesterId = requesterId;
		this.status = status;
		this.requestedPermissions = requestedPermissions;
		this.grantedPermissions = grantedPermissions;
		this.reviewedBy = reviewedBy;
		this.reviewedAt = reviewedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
