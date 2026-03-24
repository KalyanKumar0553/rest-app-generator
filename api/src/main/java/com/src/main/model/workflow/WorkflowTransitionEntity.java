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
import lombok.Data;

@Entity
@Table(name = AppDbTables.WORKFLOW_TRANSITIONS, indexes = {
		@Index(name = "idx_workflow_transition_step_type", columnList = "workflow_step_id, transition_type, priority") })
@Data
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
}
