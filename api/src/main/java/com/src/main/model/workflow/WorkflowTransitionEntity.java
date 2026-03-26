package com.src.main.model.workflow;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.WORKFLOW_TRANSITIONS, indexes = {@Index(name = "idx_workflow_transition_step_type", columnList = "workflow_step_id, transition_type, priority")})
public class WorkflowTransitionEntity {
	@Id
	@UuidGenerator
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "workflow_step_id", nullable = false)
	private WorkflowStepEntity workflowStep;
	@Enumerated(EnumType.STRING)
	@Column(name = "transition_type", nullable = false, length = 30)
	private WorkflowTransitionType transitionType;
	@Column(name = "target_step_code", nullable = false, length = 120)
	private String targetStepCode;
	@Column(name = "condition_json", columnDefinition = "TEXT")
	private String conditionJson;
	@Column(name = "priority", nullable = false)
	private int priority;

	public WorkflowTransitionEntity() {
	}

	public UUID getId() {
		return this.id;
	}

	public WorkflowStepEntity getWorkflowStep() {
		return this.workflowStep;
	}

	public WorkflowTransitionType getTransitionType() {
		return this.transitionType;
	}

	public String getTargetStepCode() {
		return this.targetStepCode;
	}

	public String getConditionJson() {
		return this.conditionJson;
	}

	public int getPriority() {
		return this.priority;
	}

	public void setId(final UUID id) {
		this.id = id;
	}

	public void setWorkflowStep(final WorkflowStepEntity workflowStep) {
		this.workflowStep = workflowStep;
	}

	public void setTransitionType(final WorkflowTransitionType transitionType) {
		this.transitionType = transitionType;
	}

	public void setTargetStepCode(final String targetStepCode) {
		this.targetStepCode = targetStepCode;
	}

	public void setConditionJson(final String conditionJson) {
		this.conditionJson = conditionJson;
	}

	public void setPriority(final int priority) {
		this.priority = priority;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof WorkflowTransitionEntity)) return false;
		final WorkflowTransitionEntity other = (WorkflowTransitionEntity) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getPriority() != other.getPriority()) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$workflowStep = this.getWorkflowStep();
		final Object other$workflowStep = other.getWorkflowStep();
		if (this$workflowStep == null ? other$workflowStep != null : !this$workflowStep.equals(other$workflowStep)) return false;
		final Object this$transitionType = this.getTransitionType();
		final Object other$transitionType = other.getTransitionType();
		if (this$transitionType == null ? other$transitionType != null : !this$transitionType.equals(other$transitionType)) return false;
		final Object this$targetStepCode = this.getTargetStepCode();
		final Object other$targetStepCode = other.getTargetStepCode();
		if (this$targetStepCode == null ? other$targetStepCode != null : !this$targetStepCode.equals(other$targetStepCode)) return false;
		final Object this$conditionJson = this.getConditionJson();
		final Object other$conditionJson = other.getConditionJson();
		if (this$conditionJson == null ? other$conditionJson != null : !this$conditionJson.equals(other$conditionJson)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof WorkflowTransitionEntity;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getPriority();
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $workflowStep = this.getWorkflowStep();
		result = result * PRIME + ($workflowStep == null ? 43 : $workflowStep.hashCode());
		final Object $transitionType = this.getTransitionType();
		result = result * PRIME + ($transitionType == null ? 43 : $transitionType.hashCode());
		final Object $targetStepCode = this.getTargetStepCode();
		result = result * PRIME + ($targetStepCode == null ? 43 : $targetStepCode.hashCode());
		final Object $conditionJson = this.getConditionJson();
		result = result * PRIME + ($conditionJson == null ? 43 : $conditionJson.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "WorkflowTransitionEntity(id=" + this.getId() + ", workflowStep=" + this.getWorkflowStep() + ", transitionType=" + this.getTransitionType() + ", targetStepCode=" + this.getTargetStepCode() + ", conditionJson=" + this.getConditionJson() + ", priority=" + this.getPriority() + ")";
	}
}
