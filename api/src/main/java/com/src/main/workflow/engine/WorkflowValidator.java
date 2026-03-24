package com.src.main.workflow.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.src.main.model.workflow.WorkflowStepEntity;
import com.src.main.model.workflow.WorkflowTransitionEntity;

import jakarta.annotation.PostConstruct;

@Component
public class WorkflowValidator {

	private final WorkflowDefinitionService workflowDefinitionService;
	private final WorkflowExecutorRegistry workflowExecutorRegistry;
	private final WorkflowExecutorPoolRegistry workflowExecutorPoolRegistry;
	private final WorkflowJsonHelper workflowJsonHelper;
	private final WorkflowConditionEvaluator workflowConditionEvaluator;

	public WorkflowValidator(
			WorkflowDefinitionService workflowDefinitionService,
			WorkflowExecutorRegistry workflowExecutorRegistry,
			WorkflowExecutorPoolRegistry workflowExecutorPoolRegistry,
			WorkflowJsonHelper workflowJsonHelper,
			WorkflowConditionEvaluator workflowConditionEvaluator) {
		this.workflowDefinitionService = workflowDefinitionService;
		this.workflowExecutorRegistry = workflowExecutorRegistry;
		this.workflowExecutorPoolRegistry = workflowExecutorPoolRegistry;
		this.workflowJsonHelper = workflowJsonHelper;
		this.workflowConditionEvaluator = workflowConditionEvaluator;
	}

	@PostConstruct
	public void validateAll() {
		for (WorkflowPlan plan : workflowDefinitionService.loadAllActivePlans()) {
			validate(plan);
		}
	}

	public void validate(WorkflowPlan plan) {
		if (plan.steps().isEmpty()) {
			throw new IllegalStateException("Workflow " + plan.definition().getCode() + " has no enabled steps");
		}
		Set<String> stepCodes = new HashSet<>();
		Set<Integer> orders = new HashSet<>();
		for (WorkflowStepEntity step : plan.steps()) {
			if (!stepCodes.add(step.getStepCode())) {
				throw new IllegalStateException("Duplicate step code " + step.getStepCode() + " in workflow " + plan.definition().getCode());
			}
			if (!orders.add(step.getStepOrder())) {
				throw new IllegalStateException("Duplicate step order " + step.getStepOrder() + " in workflow " + plan.definition().getCode());
			}
			if (!workflowExecutorRegistry.exists(step.getExecutorKey())) {
				throw new IllegalStateException("Missing StepExecutor bean " + step.getExecutorKey() + " for workflow " + plan.definition().getCode());
			}
			if (!workflowExecutorPoolRegistry.exists(step.getPoolCode())) {
				throw new IllegalStateException("Missing executor pool " + step.getPoolCode() + " for workflow " + plan.definition().getCode());
			}
			workflowJsonHelper.readStringList(step.getRequiredInputsJson());
			workflowJsonHelper.readStringList(step.getOptionalInputsJson());
			workflowJsonHelper.readStringList(step.getDeclaredOutputsJson());
			workflowConditionEvaluator.matches(step.getRunConditionJson(), new org.springframework.statemachine.support.DefaultExtendedState());
		}
		if (!workflowExecutorPoolRegistry.exists(plan.definition().getDispatchPoolCode())) {
			throw new IllegalStateException("Missing dispatch pool " + plan.definition().getDispatchPoolCode() + " for workflow " + plan.definition().getCode());
		}
		validateTransitions(plan);
	}

	private void validateTransitions(WorkflowPlan plan) {
		for (WorkflowStepEntity step : plan.steps()) {
			List<WorkflowTransitionEntity> transitions = plan.transitionsFor(step);
			for (WorkflowTransitionEntity transition : transitions) {
				if (!plan.stepsByCode().containsKey(transition.getTargetStepCode())) {
					throw new IllegalStateException("Workflow " + plan.definition().getCode()
							+ " has transition to missing step " + transition.getTargetStepCode());
				}
				workflowConditionEvaluator.matches(transition.getConditionJson(), new org.springframework.statemachine.support.DefaultExtendedState());
			}
		}
	}
}
