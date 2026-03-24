package com.src.main.model.workflow;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.src.main.config.AppDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = AppDbTables.WORKFLOW_DEFINITIONS, indexes = {
		@Index(name = "idx_workflow_def_language_active", columnList = "language, active"),
		@Index(name = "idx_workflow_def_code_version", columnList = "code, version") })
@Data
public class WorkflowDefinitionEntity {

	@Id
	@UuidGenerator
	private UUID id;

	@Column(name = "code", nullable = false, length = 120)
	private String code;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "language", nullable = false, length = 50)
	private String language;

	@Column(name = "version", nullable = false)
	private int version;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "dispatch_pool_code", nullable = false, length = 120)
	private String dispatchPoolCode;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	void onCreate() {
		createdAt = OffsetDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = OffsetDateTime.now();
	}
}
