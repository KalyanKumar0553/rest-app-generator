package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.src.main.config.AppDbTables;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.PROJECT_RUNS, indexes = {@Index(name = "idx_runs_owner_type_created", columnList = "owner_id, type, created_at"), @Index(name = "idx_runs_project", columnList = "project_id")})
public class ProjectRunEntity {
	@Id
	@GeneratedValue
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private ProjectEntity project;
	@Column(name = "owner_id", nullable = false, length = 100)
	private String ownerId;
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ProjectRunType type;
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ProjectRunStatus status;
	@Column(name = "run_number", nullable = false)
	private int runNumber;
	@Column(name = "error_message")
	private String errorMessage;
	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;
	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;
	@JdbcTypeCode(SqlTypes.LONGVARBINARY)
	@Column(columnDefinition = "bytea")
	private byte[] zip;

	@PrePersist
	public void onCreate() {
		this.createdAt = OffsetDateTime.now();
		this.updatedAt = this.createdAt;
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public ProjectRunEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public ProjectEntity getProject() {
		return this.project;
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

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public byte[] getZip() {
		return this.zip;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setProject(final ProjectEntity project) {
		this.project = project;
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

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setZip(final byte[] zip) {
		this.zip = zip;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectRunEntity)) return false;
		final ProjectRunEntity other = (ProjectRunEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getRunNumber() != other.getRunNumber()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$project = this.getProject();
		final Object other$project = other.getProject();
		if (this$project == null ? other$project != null : !this$project.equals(other$project)) return false;
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
		if (!java.util.Arrays.equals(this.getZip(), other.getZip())) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectRunEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getRunNumber();
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $project = this.getProject();
		result = result * PRIME + ($project == null ? 43 : $project.hashCode());
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
		result = result * PRIME + java.util.Arrays.hashCode(this.getZip());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectRunEntity(id=" + this.getId() + ", project=" + this.getProject() + ", ownerId=" + this.getOwnerId() + ", type=" + this.getType() + ", status=" + this.getStatus() + ", runNumber=" + this.getRunNumber() + ", errorMessage=" + this.getErrorMessage() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ", zip=" + java.util.Arrays.toString(this.getZip()) + ")";
	}
}
