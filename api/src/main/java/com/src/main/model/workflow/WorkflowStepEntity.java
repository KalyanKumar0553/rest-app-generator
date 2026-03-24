package com.src.main.model.workflow;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.src.main.config.AppDbTables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = AppDbTables.WORKFLOW_STEPS, indexes = {
		@Index(name = "idx_workflow_step_workflow_order", columnList = "workflow_id, step_order"),
		@Index(name = "idx_workflow_step_workflow_code", columnList = "workflow_id, step_code") })
@Data
public class WorkflowStepEntity {

	@Id
	@UuidGenerator
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_id", nullable = false)
	private WorkflowDefinitionEntity workflow;

	@Column(name = "step_code", nullable = false, length = 120)
	private String stepCode;

	@Column(name = "step_name", nullable = false, length = 200)
	private String stepName;

	@Column(name = "executor_key", nullable = false, length = 120)
	private String executorKey;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@Column(name = "terminal", nullable = false)
	private boolean terminal;

	@Column(name = "step_order", nullable = false)
	private int stepOrder;

	@Column(name = "pool_code", nullable = false, length = 120)
	private String poolCode;

	@Column(name = "async_execution", nullable = false)
	private boolean asyncExecution;

	@Column(name = "timeout_ms")
	private Long timeoutMs;

	@Column(name = "run_condition_json", columnDefinition = "TEXT")
	private String runConditionJson;

	@Column(name = "required_inputs_json", columnDefinition = "TEXT")
	private String requiredInputsJson;

	@Column(name = "optional_inputs_json", columnDefinition = "TEXT")
	private String optionalInputsJson;

	@Column(name = "declared_outputs_json", columnDefinition = "TEXT")
	private String declaredOutputsJson;

	@Column(name = "retry_enabled", nullable = false)
	private boolean retryEnabled;

	@Column(name = "retry_max_attempts")
	private Integer retryMaxAttempts;

	@Column(name = "retry_backoff_ms")
	private Long retryBackoffMs;

	@Column(name = "retry_backoff_multiplier")
	private Double retryBackoffMultiplier;

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
