package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

public class ProjectRunDetailsResponseDTO {
	private UUID runId;
	private UUID projectId;
	private String ownerId;
	private ProjectRunType type;
	private ProjectRunStatus status;
	private int runNumber;
	private boolean hasZip;
	private String errorMessage;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public UUID getRunId() {
		return this.runId;
	}

	public UUID getProjectId() {
		return this.projectId;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public ProjectRunType getType() {
		return this.type;
	}

	public ProjectRunStatus getStatus() {
		return this.status;
	}

	public int getRunNumber() {
		return this.runNumber;
	}

	public boolean isHasZip() {
		return this.hasZip;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setRunId(final UUID runId) {
		this.runId = runId;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}

	public void setType(final ProjectRunType type) {
		this.type = type;
	}

	public void setStatus(final ProjectRunStatus status) {
		this.status = status;
	}

	public void setRunNumber(final int runNumber) {
		this.runNumber = runNumber;
	}

	public void setHasZip(final boolean hasZip) {
		this.hasZip = hasZip;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
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
		if (!(o instanceof ProjectRunDetailsResponseDTO)) return false;
		final ProjectRunDetailsResponseDTO other = (ProjectRunDetailsResponseDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getRunNumber() != other.getRunNumber()) return false;
		if (this.isHasZip() != other.isHasZip()) return false;
		final Object this$runId = this.getRunId();
		final Object other$runId = other.getRunId();
		if (this$runId == null ? other$runId != null : !this$runId.equals(other$runId)) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
		final Object this$ownerId = this.getOwnerId();
		final Object other$ownerId = other.getOwnerId();
		if (this$ownerId == null ? other$ownerId != null : !this$ownerId.equals(other$ownerId)) return false;
		final Object this$type = this.getType();
		final Object other$type = other.getType();
		if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$errorMessage = this.getErrorMessage();
		final Object other$errorMessage = other.getErrorMessage();
		if (this$errorMessage == null ? other$errorMessage != null : !this$errorMessage.equals(other$errorMessage)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectRunDetailsResponseDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getRunNumber();
		result = result * PRIME + (this.isHasZip() ? 79 : 97);
		final Object $runId = this.getRunId();
		result = result * PRIME + ($runId == null ? 43 : $runId.hashCode());
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		final Object $ownerId = this.getOwnerId();
		result = result * PRIME + ($ownerId == null ? 43 : $ownerId.hashCode());
		final Object $type = this.getType();
		result = result * PRIME + ($type == null ? 43 : $type.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $errorMessage = this.getErrorMessage();
		result = result * PRIME + ($errorMessage == null ? 43 : $errorMessage.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectRunDetailsResponseDTO(runId=" + this.getRunId() + ", projectId=" + this.getProjectId() + ", ownerId=" + this.getOwnerId() + ", type=" + this.getType() + ", status=" + this.getStatus() + ", runNumber=" + this.getRunNumber() + ", hasZip=" + this.isHasZip() + ", errorMessage=" + this.getErrorMessage() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}

	public ProjectRunDetailsResponseDTO(final UUID runId, final UUID projectId, final String ownerId, final ProjectRunType type, final ProjectRunStatus status, final int runNumber, final boolean hasZip, final String errorMessage, final OffsetDateTime createdAt, final OffsetDateTime updatedAt) {
		this.runId = runId;
		this.projectId = projectId;
		this.ownerId = ownerId;
		this.type = type;
		this.status = status;
		this.runNumber = runNumber;
		this.hasZip = hasZip;
		this.errorMessage = errorMessage;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public ProjectRunDetailsResponseDTO() {
	}
}
