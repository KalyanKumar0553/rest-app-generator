package com.src.main.workflow.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.model.workflow.WorkflowDefinitionEntity;
import com.src.main.model.workflow.WorkflowStepEntity;
import com.src.main.model.workflow.WorkflowTransitionEntity;

import jakarta.annotation.PostConstruct;

/**
 * Validates that every active workflow plan is internally consistent and that
 * all referenced executors, pools and JSON payloads are resolvable. Runs once
 * at application start-up and may be invoked ad hoc when plans change.
 */
@Component
public class WorkflowValidator {

	private static final Logger log = LoggerFactory.getLogger(WorkflowValidator.class);

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
		List<WorkflowPlan> plans = workflowDefinitionService.loadAllActivePlans();
		log.info("Validating {} active workflow plan(s)", plans.size());
		plans.forEach(this::validate);
	}

	public void validate(WorkflowPlan plan) {
		WorkflowDefinitionEntity definition = plan.definition();
		if (plan.steps().isEmpty()) {
			throw new IllegalStateException("Workflow " + definition.getCode() + " has no enabled steps");
		}
		validateSteps(plan);
		if (!workflowExecutorPoolRegistry.exists(definition.getDispatchPoolCode())) {
			throw new IllegalStateException("Missing dispatch pool " + definition.getDispatchPoolCode()
					+ " for workflow " + definition.getCode());
		}
		validateTransitions(plan);
	}

	private void validateSteps(WorkflowPlan plan) {
		String workflowCode = plan.definition().getCode();
		Set<String> stepCodes = new HashSet<>();
		Set<Integer> orders = new HashSet<>();
		for (WorkflowStepEntity step : plan.steps()) {
			if (!stepCodes.add(step.getStepCode())) {
				throw new IllegalStateException("Duplicate step code " + step.getStepCode()
						+ " in workflow " + workflowCode);
			}
			if (!orders.add(step.getStepOrder())) {
				throw new IllegalStateException("Duplicate step order " + step.getStepOrder()
						+ " in workflow " + workflowCode);
			}
			if (!workflowExecutorRegistry.exists(step.getExecutorKey())) {
				throw new IllegalStateException("Missing StepExecutor bean " + step.getExecutorKey()
						+ " for workflow " + workflowCode);
			}
			if (!workflowExecutorPoolRegistry.exists(step.getPoolCode())) {
				throw new IllegalStateException("Missing executor pool " + step.getPoolCode()
						+ " for workflow " + workflowCode);
			}
			// Exercise JSON parsers so malformed definitions fail fast at start-up.
			workflowJsonHelper.readStringList(step.getRequiredInputsJson());
			workflowJsonHelper.readStringList(step.getOptionalInputsJson());
			workflowJsonHelper.readStringList(step.getDeclaredOutputsJson());
			workflowConditionEvaluator.matches(step.getRunConditionJson(), new DefaultExtendedState());
		}
	}

	private void validateTransitions(WorkflowPlan plan) {
		String workflowCode = plan.definition().getCode();
		for (WorkflowStepEntity step : plan.steps()) {
			List<WorkflowTransitionEntity> transitions = plan.transitionsFor(step);
			for (WorkflowTransitionEntity transition : transitions) {
				if (!plan.stepsByCode().containsKey(transition.getTargetStepCode())) {
					throw new IllegalStateException("Workflow " + workflowCode
							+ " has transition to missing step " + transition.getTargetStepCode());
				}
				workflowConditionEvaluator.matches(transition.getConditionJson(), new DefaultExtendedState());
			}
		}
	}
}
