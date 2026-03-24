package com.src.main.workflow.engine;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.stereotype.Service;

import com.src.main.dto.StepResult;
import com.src.main.exception.GenericException;
import com.src.main.model.ProjectRunEntity;
import com.src.main.model.workflow.WorkflowStepEntity;
import com.src.main.model.workflow.WorkflowTransitionEntity;
import com.src.main.model.workflow.WorkflowTransitionType;
import com.src.main.service.ProjectEventStreamService;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;

@Service
public class WorkflowEngineService {

	private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);
	private static final long DEFAULT_TIMEOUT_MS = 300_000L;

	private final WorkflowDefinitionService workflowDefinitionService;
	private final WorkflowExecutorRegistry workflowExecutorRegistry;
	private final WorkflowExecutorPoolRegistry workflowExecutorPoolRegistry;
	private final WorkflowConditionEvaluator workflowConditionEvaluator;
	private final WorkflowJsonHelper workflowJsonHelper;
	private final ProjectEventStreamService projectEventStreamService;

	public WorkflowEngineService(
			WorkflowDefinitionService workflowDefinitionService,
			WorkflowExecutorRegistry workflowExecutorRegistry,
			WorkflowExecutorPoolRegistry workflowExecutorPoolRegistry,
			WorkflowConditionEvaluator workflowConditionEvaluator,
			WorkflowJsonHelper workflowJsonHelper,
			ProjectEventStreamService projectEventStreamService) {
		this.workflowDefinitionService = workflowDefinitionService;
		this.workflowExecutorRegistry = workflowExecutorRegistry;
		this.workflowExecutorPoolRegistry = workflowExecutorPoolRegistry;
		this.workflowConditionEvaluator = workflowConditionEvaluator;
		this.workflowJsonHelper = workflowJsonHelper;
		this.projectEventStreamService = projectEventStreamService;
	}

	public DefaultExtendedState execute(GenerationLanguage language, DefaultExtendedState state, ProjectRunEntity run) {
		WorkflowPlan plan = workflowDefinitionService.loadActivePlan(language);
		WorkflowStepEntity currentStep = plan.firstStep();
		while (currentStep != null) {
			WorkflowExecutionStatus status = executeStep(plan, currentStep, state, run);
			WorkflowStepEntity nextStep = resolveNextStep(plan, currentStep, status, state);
			if (currentStep.isTerminal() && status == WorkflowExecutionStatus.SUCCESS) {
				return state;
			}
			if (status == WorkflowExecutionStatus.FAILURE && nextStep == null) {
				throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,
						String.valueOf(state.getVariables().getOrDefault("error", "Workflow execution failed.")));
			}
			currentStep = nextStep;
		}
		return state;
	}

	public Future<?> dispatch(GenerationLanguage language, Runnable task) {
		WorkflowPlan plan = workflowDefinitionService.loadActivePlan(language);
		return workflowExecutorPoolRegistry.submit(plan.definition().getDispatchPoolCode(), task);
	}

	private WorkflowExecutionStatus executeStep(WorkflowPlan plan, WorkflowStepEntity step, DefaultExtendedState state,
			ProjectRunEntity run) {
		if (!workflowConditionEvaluator.matches(step.getRunConditionJson(), state)) {
			publishStage(plan, run, step, "SKIPPED", "Condition evaluated to false", 0);
			return WorkflowExecutionStatus.SKIP;
		}
		validateInputs(step, state);
		int maxAttempts = step.isRetryEnabled() ? Math.max(1, step.getRetryMaxAttempts() == null ? 1 : step.getRetryMaxAttempts()) : 1;
		long backoff = step.getRetryBackoffMs() == null ? 0L : Math.max(0L, step.getRetryBackoffMs());
		double multiplier = step.getRetryBackoffMultiplier() == null ? 1.0d : Math.max(1.0d, step.getRetryBackoffMultiplier());

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			publishStage(plan, run, step, "INPROGRESS", null, attempt);
			StepResult result = invoke(step, state);
			if (result.isSuccess()) {
				if (result.getDetails() != null) {
					result.getDetails().forEach(state.getVariables()::put);
				}
				validateOutputs(step, result);
				publishStage(plan, run, step, "DONE", result.getMessage(), attempt);
				return WorkflowExecutionStatus.SUCCESS;
			}
			log.warn("Workflow step {} failed on attempt {} with code {}: {}", step.getStepCode(), attempt, result.getCode(), result.getMessage());
			if (attempt < maxAttempts) {
				publishStage(plan, run, step, "RETRYING", result.getMessage(), attempt);
				sleep(backoff);
				backoff = Math.round(backoff * multiplier);
				continue;
			}
			state.getVariables().put("error", result.getMessage());
			publishStage(plan, run, step, "ERROR", result.getMessage(), attempt);
			return WorkflowExecutionStatus.FAILURE;
		}
		return WorkflowExecutionStatus.FAILURE;
	}

	private StepResult invoke(WorkflowStepEntity step, DefaultExtendedState state) {
		StepExecutor executor = workflowExecutorRegistry.resolve(step.getExecutorKey());
		long timeoutMs = step.getTimeoutMs() == null || step.getTimeoutMs() <= 0 ? DEFAULT_TIMEOUT_MS : step.getTimeoutMs();
		try {
			Future<StepResult> future = workflowExecutorPoolRegistry.submit(step.getPoolCode(), () -> executor.execute(state));
			return future.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {
			return StepResult.error(step.getStepCode(), "Timed out after " + timeoutMs + " ms");
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return StepResult.error(step.getStepCode(), ex.getMessage());
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause() == null ? ex : ex.getCause();
			return StepResult.error(step.getStepCode(), cause.getMessage());
		} catch (Exception ex) {
			return StepResult.error(step.getStepCode(), ex.getMessage());
		}
	}

	private void validateInputs(WorkflowStepEntity step, DefaultExtendedState state) {
		List<String> requiredInputs = workflowJsonHelper.readStringList(step.getRequiredInputsJson());
		for (String inputKey : requiredInputs) {
			if (!state.getVariables().containsKey(inputKey) || state.getVariables().get(inputKey) == null) {
				throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Missing required workflow input '" + inputKey + "' for step " + step.getStepCode());
			}
		}
	}

	private void validateOutputs(WorkflowStepEntity step, StepResult result) {
		List<String> declaredOutputs = workflowJsonHelper.readStringList(step.getDeclaredOutputsJson());
		if (declaredOutputs.isEmpty()) {
			return;
		}
		Map<String, Object> details = result.getDetails() == null ? Map.of() : result.getDetails();
		for (String outputKey : declaredOutputs) {
			if (!details.containsKey(outputKey)) {
				throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Workflow step " + step.getStepCode() + " did not produce declared output '" + outputKey + "'");
			}
		}
	}

	private WorkflowStepEntity resolveNextStep(WorkflowPlan plan, WorkflowStepEntity currentStep, WorkflowExecutionStatus status,
			DefaultExtendedState state) {
		WorkflowTransitionType transitionType = switch (status) {
		case SUCCESS -> WorkflowTransitionType.SUCCESS;
		case FAILURE -> WorkflowTransitionType.FAILURE;
		case SKIP -> WorkflowTransitionType.SKIP;
		};
		for (WorkflowTransitionEntity transition : plan.transitionsFor(currentStep)) {
			if (transition.getTransitionType() != transitionType) {
				continue;
			}
			if (workflowConditionEvaluator.matches(transition.getConditionJson(), state)) {
				return plan.stepsByCode().get(transition.getTargetStepCode());
			}
		}
		if (status == WorkflowExecutionStatus.FAILURE) {
			return null;
		}
		return plan.nextOrderedStep(currentStep);
	}

	private void publishStage(WorkflowPlan plan, ProjectRunEntity run, WorkflowStepEntity step, String status, String message, int attempt) {
		if (run == null || run.getProject() == null) {
			return;
		}
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("projectId", run.getProject().getId().toString());
		payload.put("runId", run.getId().toString());
		payload.put("stage", step.getStepCode());
		payload.put("stepName", step.getStepName());
		payload.put("stepOrder", step.getStepOrder());
		payload.put("totalSteps", plan.steps().size());
		payload.put("retryEnabled", step.isRetryEnabled());
		payload.put("status", status);
		payload.put("message", message == null ? "" : message);
		payload.put("attempt", attempt);
		payload.put("executorKey", step.getExecutorKey());
		payload.put("timestamp", OffsetDateTime.now().toString());
		projectEventStreamService.publish(run.getProject().getId(), "stage", payload);
	}

	private void sleep(long millis) {
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
