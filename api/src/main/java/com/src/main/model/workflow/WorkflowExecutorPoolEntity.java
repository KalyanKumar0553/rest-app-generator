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
@Table(name = AppDbTables.WORKFLOW_EXECUTOR_POOLS, indexes = {
		@Index(name = "idx_workflow_pool_code_active", columnList = "pool_code, active") })
@Data
public class WorkflowExecutorPoolEntity {

	@Id
	@UuidGenerator
	private UUID id;

	@Column(name = "pool_code", nullable = false, unique = true, length = 120)
	private String poolCode;

	@Column(name = "pool_name", nullable = false, length = 200)
	private String poolName;

	@Column(name = "core_pool_size", nullable = false)
	private int corePoolSize;

	@Column(name = "max_pool_size", nullable = false)
	private int maxPoolSize;

	@Column(name = "queue_capacity", nullable = false)
	private int queueCapacity;

	@Column(name = "keep_alive_seconds", nullable = false)
	private int keepAliveSeconds;

	@Column(name = "active", nullable = false)
	private boolean active;

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
