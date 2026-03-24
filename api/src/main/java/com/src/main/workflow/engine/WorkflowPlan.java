package com.src.main.workflow.engine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.src.main.model.workflow.WorkflowDefinitionEntity;
import com.src.main.model.workflow.WorkflowStepEntity;
import com.src.main.model.workflow.WorkflowTransitionEntity;

public record WorkflowPlan(
		WorkflowDefinitionEntity definition,
		List<WorkflowStepEntity> steps,
		Map<String, WorkflowStepEntity> stepsByCode,
		Map<UUID, List<WorkflowTransitionEntity>> transitionsByStepId) {

	public static WorkflowPlan of(WorkflowDefinitionEntity definition, List<WorkflowStepEntity> steps,
			List<WorkflowTransitionEntity> transitions) {
		List<WorkflowStepEntity> orderedSteps = steps.stream()
				.sorted(Comparator.comparingInt(WorkflowStepEntity::getStepOrder))
				.toList();
		Map<String, WorkflowStepEntity> stepsByCode = orderedSteps.stream()
				.collect(Collectors.toMap(WorkflowStepEntity::getStepCode, Function.identity()));
		Map<UUID, List<WorkflowTransitionEntity>> transitionsByStepId = transitions.stream()
				.collect(Collectors.groupingBy(transition -> transition.getWorkflowStep().getId()));
		return new WorkflowPlan(definition, orderedSteps, stepsByCode, transitionsByStepId);
	}

	public WorkflowStepEntity firstStep() {
		if (steps.isEmpty()) {
			throw new IllegalStateException("Workflow has no enabled steps for language " + definition.getLanguage());
		}
		return steps.get(0);
	}

	public WorkflowStepEntity nextOrderedStep(WorkflowStepEntity currentStep) {
		for (WorkflowStepEntity step : steps) {
			if (step.getStepOrder() > currentStep.getStepOrder()) {
				return step;
			}
		}
		return null;
	}

	public List<WorkflowTransitionEntity> transitionsFor(WorkflowStepEntity step) {
		return transitionsByStepId.getOrDefault(step.getId(), List.of());
	}
}
