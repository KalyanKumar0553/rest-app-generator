package com.src.main.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.AI_LABS_JOB_HISTORY)
public class AiLabsJobHistoryEntity {
	@Id
	private UUID id;
	@Column(name = "owner_user_id", nullable = false, length = 100)
	private String ownerUserId;
	@Column(name = "status", nullable = false, length = 32)
	private String status;
	@Column(name = "generator", length = 32)
	private String generator;
	@Column(name = "project_id")
	private UUID projectId;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "prompt", nullable = false, columnDefinition = "text")
	private String prompt;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "steps_json", nullable = false, columnDefinition = "text")
	private String stepsJson;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "stream_preview", columnDefinition = "text")
	private String streamPreview;
	@JdbcTypeCode(SqlTypes.LONGVARCHAR)
	@Column(name = "error_message", columnDefinition = "text")
	private String errorMessage;
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

	public AiLabsJobHistoryEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public String getOwnerUserId() {
		return this.ownerUserId;
	}

	public String getStatus() {
		return this.status;
	}

	public String getGenerator() {
		return this.generator;
	}

	public UUID getProjectId() {
		return this.projectId;
	}

	public String getPrompt() {
		return this.prompt;
	}

	public String getStepsJson() {
		return this.stepsJson;
	}

	public String getStreamPreview() {
		return this.streamPreview;
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

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setOwnerUserId(final String ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}

	public void setProjectId(final UUID projectId) {
		this.projectId = projectId;
	}

	public void setPrompt(final String prompt) {
		this.prompt = prompt;
	}

	public void setStepsJson(final String stepsJson) {
		this.stepsJson = stepsJson;
	}

	public void setStreamPreview(final String streamPreview) {
		this.streamPreview = streamPreview;
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
}
