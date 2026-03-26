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

@Entity
@Table(name = AppDbTables.WORKFLOW_STEPS, indexes = {@Index(name = "idx_workflow_step_workflow_order", columnList = "workflow_id, step_order"), @Index(name = "idx_workflow_step_workflow_code", columnList = "workflow_id, step_code")})
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

	public WorkflowStepEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public WorkflowDefinitionEntity getWorkflow() {
		return this.workflow;
	}

	public String getStepCode() {
		return this.stepCode;
	}

	public String getStepName() {
		return this.stepName;
	}

	public String getExecutorKey() {
		return this.executorKey;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public boolean isTerminal() {
		return this.terminal;
	}

	public int getStepOrder() {
		return this.stepOrder;
	}

	public String getPoolCode() {
		return this.poolCode;
	}

	public boolean isAsyncExecution() {
		return this.asyncExecution;
	}

	public Long getTimeoutMs() {
		return this.timeoutMs;
	}

	public String getRunConditionJson() {
		return this.runConditionJson;
	}

	public String getRequiredInputsJson() {
		return this.requiredInputsJson;
	}

	public String getOptionalInputsJson() {
		return this.optionalInputsJson;
	}

	public String getDeclaredOutputsJson() {
		return this.declaredOutputsJson;
	}

	public boolean isRetryEnabled() {
		return this.retryEnabled;
	}

	public Integer getRetryMaxAttempts() {
		return this.retryMaxAttempts;
	}

	public Long getRetryBackoffMs() {
		return this.retryBackoffMs;
	}

	public Double getRetryBackoffMultiplier() {
		return this.retryBackoffMultiplier;
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

	public void setWorkflow(final WorkflowDefinitionEntity workflow) {
		this.workflow = workflow;
	}

	public void setStepCode(final String stepCode) {
		this.stepCode = stepCode;
	}

	public void setStepName(final String stepName) {
		this.stepName = stepName;
	}

	public void setExecutorKey(final String executorKey) {
		this.executorKey = executorKey;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setTerminal(final boolean terminal) {
		this.terminal = terminal;
	}

	public void setStepOrder(final int stepOrder) {
		this.stepOrder = stepOrder;
	}

	public void setPoolCode(final String poolCode) {
		this.poolCode = poolCode;
	}

	public void setAsyncExecution(final boolean asyncExecution) {
		this.asyncExecution = asyncExecution;
	}

	public void setTimeoutMs(final Long timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	public void setRunConditionJson(final String runConditionJson) {
		this.runConditionJson = runConditionJson;
	}

	public void setRequiredInputsJson(final String requiredInputsJson) {
		this.requiredInputsJson = requiredInputsJson;
	}

	public void setOptionalInputsJson(final String optionalInputsJson) {
		this.optionalInputsJson = optionalInputsJson;
	}

	public void setDeclaredOutputsJson(final String declaredOutputsJson) {
		this.declaredOutputsJson = declaredOutputsJson;
	}

	public void setRetryEnabled(final boolean retryEnabled) {
		this.retryEnabled = retryEnabled;
	}

	public void setRetryMaxAttempts(final Integer retryMaxAttempts) {
		this.retryMaxAttempts = retryMaxAttempts;
	}

	public void setRetryBackoffMs(final Long retryBackoffMs) {
		this.retryBackoffMs = retryBackoffMs;
	}

	public void setRetryBackoffMultiplier(final Double retryBackoffMultiplier) {
		this.retryBackoffMultiplier = retryBackoffMultiplier;
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
		if (!(o instanceof WorkflowStepEntity)) return false;
		final WorkflowStepEntity other = (WorkflowStepEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isEnabled() != other.isEnabled()) return false;
		if (this.isTerminal() != other.isTerminal()) return false;
		if (this.getStepOrder() != other.getStepOrder()) return false;
		if (this.isAsyncExecution() != other.isAsyncExecution()) return false;
		if (this.isRetryEnabled() != other.isRetryEnabled()) return false;
		final Object this$timeoutMs = this.getTimeoutMs();
		final Object other$timeoutMs = other.getTimeoutMs();
		if (this$timeoutMs == null ? other$timeoutMs != null : !this$timeoutMs.equals(other$timeoutMs)) return false;
		final Object this$retryMaxAttempts = this.getRetryMaxAttempts();
		final Object other$retryMaxAttempts = other.getRetryMaxAttempts();
		if (this$retryMaxAttempts == null ? other$retryMaxAttempts != null : !this$retryMaxAttempts.equals(other$retryMaxAttempts)) return false;
		final Object this$retryBackoffMs = this.getRetryBackoffMs();
		final Object other$retryBackoffMs = other.getRetryBackoffMs();
		if (this$retryBackoffMs == null ? other$retryBackoffMs != null : !this$retryBackoffMs.equals(other$retryBackoffMs)) return false;
		final Object this$retryBackoffMultiplier = this.getRetryBackoffMultiplier();
		final Object other$retryBackoffMultiplier = other.getRetryBackoffMultiplier();
		if (this$retryBackoffMultiplier == null ? other$retryBackoffMultiplier != null : !this$retryBackoffMultiplier.equals(other$retryBackoffMultiplier)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$workflow = this.getWorkflow();
		final Object other$workflow = other.getWorkflow();
		if (this$workflow == null ? other$workflow != null : !this$workflow.equals(other$workflow)) return false;
		final Object this$stepCode = this.getStepCode();
		final Object other$stepCode = other.getStepCode();
		if (this$stepCode == null ? other$stepCode != null : !this$stepCode.equals(other$stepCode)) return false;
		final Object this$stepName = this.getStepName();
		final Object other$stepName = other.getStepName();
		if (this$stepName == null ? other$stepName != null : !this$stepName.equals(other$stepName)) return false;
		final Object this$executorKey = this.getExecutorKey();
		final Object other$executorKey = other.getExecutorKey();
		if (this$executorKey == null ? other$executorKey != null : !this$executorKey.equals(other$executorKey)) return false;
		final Object this$poolCode = this.getPoolCode();
		final Object other$poolCode = other.getPoolCode();
		if (this$poolCode == null ? other$poolCode != null : !this$poolCode.equals(other$poolCode)) return false;
		final Object this$runConditionJson = this.getRunConditionJson();
		final Object other$runConditionJson = other.getRunConditionJson();
		if (this$runConditionJson == null ? other$runConditionJson != null : !this$runConditionJson.equals(other$runConditionJson)) return false;
		final Object this$requiredInputsJson = this.getRequiredInputsJson();
		final Object other$requiredInputsJson = other.getRequiredInputsJson();
		if (this$requiredInputsJson == null ? other$requiredInputsJson != null : !this$requiredInputsJson.equals(other$requiredInputsJson)) return false;
		final Object this$optionalInputsJson = this.getOptionalInputsJson();
		final Object other$optionalInputsJson = other.getOptionalInputsJson();
		if (this$optionalInputsJson == null ? other$optionalInputsJson != null : !this$optionalInputsJson.equals(other$optionalInputsJson)) return false;
		final Object this$declaredOutputsJson = this.getDeclaredOutputsJson();
		final Object other$declaredOutputsJson = other.getDeclaredOutputsJson();
		if (this$declaredOutputsJson == null ? other$declaredOutputsJson != null : !this$declaredOutputsJson.equals(other$declaredOutputsJson)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof WorkflowStepEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isEnabled() ? 79 : 97);
		result = result * PRIME + (this.isTerminal() ? 79 : 97);
		result = result * PRIME + this.getStepOrder();
		result = result * PRIME + (this.isAsyncExecution() ? 79 : 97);
		result = result * PRIME + (this.isRetryEnabled() ? 79 : 97);
		final Object $timeoutMs = this.getTimeoutMs();
		result = result * PRIME + ($timeoutMs == null ? 43 : $timeoutMs.hashCode());
		final Object $retryMaxAttempts = this.getRetryMaxAttempts();
		result = result * PRIME + ($retryMaxAttempts == null ? 43 : $retryMaxAttempts.hashCode());
		final Object $retryBackoffMs = this.getRetryBackoffMs();
		result = result * PRIME + ($retryBackoffMs == null ? 43 : $retryBackoffMs.hashCode());
		final Object $retryBackoffMultiplier = this.getRetryBackoffMultiplier();
		result = result * PRIME + ($retryBackoffMultiplier == null ? 43 : $retryBackoffMultiplier.hashCode());
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $workflow = this.getWorkflow();
		result = result * PRIME + ($workflow == null ? 43 : $workflow.hashCode());
		final Object $stepCode = this.getStepCode();
		result = result * PRIME + ($stepCode == null ? 43 : $stepCode.hashCode());
		final Object $stepName = this.getStepName();
		result = result * PRIME + ($stepName == null ? 43 : $stepName.hashCode());
		final Object $executorKey = this.getExecutorKey();
		result = result * PRIME + ($executorKey == null ? 43 : $executorKey.hashCode());
		final Object $poolCode = this.getPoolCode();
		result = result * PRIME + ($poolCode == null ? 43 : $poolCode.hashCode());
		final Object $runConditionJson = this.getRunConditionJson();
		result = result * PRIME + ($runConditionJson == null ? 43 : $runConditionJson.hashCode());
		final Object $requiredInputsJson = this.getRequiredInputsJson();
		result = result * PRIME + ($requiredInputsJson == null ? 43 : $requiredInputsJson.hashCode());
		final Object $optionalInputsJson = this.getOptionalInputsJson();
		result = result * PRIME + ($optionalInputsJson == null ? 43 : $optionalInputsJson.hashCode());
		final Object $declaredOutputsJson = this.getDeclaredOutputsJson();
		result = result * PRIME + ($declaredOutputsJson == null ? 43 : $declaredOutputsJson.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "WorkflowStepEntity(id=" + this.getId() + ", workflow=" + this.getWorkflow() + ", stepCode=" + this.getStepCode() + ", stepName=" + this.getStepName() + ", executorKey=" + this.getExecutorKey() + ", enabled=" + this.isEnabled() + ", terminal=" + this.isTerminal() + ", stepOrder=" + this.getStepOrder() + ", poolCode=" + this.getPoolCode() + ", asyncExecution=" + this.isAsyncExecution() + ", timeoutMs=" + this.getTimeoutMs() + ", runConditionJson=" + this.getRunConditionJson() + ", requiredInputsJson=" + this.getRequiredInputsJson() + ", optionalInputsJson=" + this.getOptionalInputsJson() + ", declaredOutputsJson=" + this.getDeclaredOutputsJson() + ", retryEnabled=" + this.isRetryEnabled() + ", retryMaxAttempts=" + this.getRetryMaxAttempts() + ", retryBackoffMs=" + this.getRetryBackoffMs() + ", retryBackoffMultiplier=" + this.getRetryBackoffMultiplier() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}
}
