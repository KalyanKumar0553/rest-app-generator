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

/**
 * Drives execution of a {@link WorkflowPlan}: resolves the next step, invokes
 * the corresponding {@link StepExecutor} on its dedicated thread pool, applies
 * retry policies, and streams lifecycle events back to subscribed clients.
 */
@Service
public class WorkflowEngineService {

	private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

	private static final long DEFAULT_TIMEOUT_MS = 300_000L;
	private static final String ERROR_VARIABLE_KEY = "error";

	// Stage lifecycle statuses streamed over {@link ProjectEventStreamService}.
	private static final String STAGE_SKIPPED = "SKIPPED";
	private static final String STAGE_IN_PROGRESS = "INPROGRESS";
	private static final String STAGE_RETRYING = "RETRYING";
	private static final String STAGE_DONE = "DONE";
	private static final String STAGE_ERROR = "ERROR";
	private static final String STAGE_EVENT = "stage";

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
						String.valueOf(state.getVariables().getOrDefault(ERROR_VARIABLE_KEY, "Workflow execution failed.")));
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
			publishStage(plan, run, step, STAGE_SKIPPED, "Condition evaluated to false", 0);
			return WorkflowExecutionStatus.SKIP;
		}
		validateInputs(step, state);
		RetryPolicy retryPolicy = RetryPolicy.from(step);

		long currentBackoff = retryPolicy.initialBackoffMs();
		for (int attempt = 1; attempt <= retryPolicy.maxAttempts(); attempt++) {
			publishStage(plan, run, step, STAGE_IN_PROGRESS, null, attempt);
			StepResult result = invoke(step, state);

			if (result.isSuccess()) {
				applyStepOutputs(step, state, result);
				publishStage(plan, run, step, STAGE_DONE, result.getMessage(), attempt);
				return WorkflowExecutionStatus.SUCCESS;
			}

			log.warn("Workflow step {} failed on attempt {} with code {}: {}",
					step.getStepCode(), attempt, result.getCode(), result.getMessage());

			if (attempt < retryPolicy.maxAttempts()) {
				publishStage(plan, run, step, STAGE_RETRYING, result.getMessage(), attempt);
				sleep(currentBackoff);
				currentBackoff = Math.round(currentBackoff * retryPolicy.multiplier());
				continue;
			}

			state.getVariables().put(ERROR_VARIABLE_KEY, result.getMessage());
			publishStage(plan, run, step, STAGE_ERROR, result.getMessage(), attempt);
			return WorkflowExecutionStatus.FAILURE;
		}
		return WorkflowExecutionStatus.FAILURE;
	}

	private void applyStepOutputs(WorkflowStepEntity step, DefaultExtendedState state, StepResult result) {
		if (result.getDetails() != null) {
			result.getDetails().forEach(state.getVariables()::put);
		}
		validateOutputs(step, result);
	}

	private StepResult invoke(WorkflowStepEntity step, DefaultExtendedState state) {
		StepExecutor executor = workflowExecutorRegistry.resolve(step.getExecutorKey());
		long timeoutMs = resolveTimeoutMs(step);
		try {
			Future<StepResult> future = workflowExecutorPoolRegistry.submit(step.getPoolCode(),
					() -> executor.execute(state));
			return future.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException ex) {
			log.warn("Workflow step {} timed out after {} ms", step.getStepCode(), timeoutMs);
			return StepResult.error(step.getStepCode(), "Timed out after " + timeoutMs + " ms");
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			log.warn("Workflow step {} was interrupted", step.getStepCode(), ex);
			return StepResult.error(step.getStepCode(), ex.getMessage());
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause() == null ? ex : ex.getCause();
			log.warn("Workflow step {} threw {}: {}", step.getStepCode(), cause.getClass().getSimpleName(),
					cause.getMessage(), cause);
			return StepResult.error(step.getStepCode(), cause.getMessage());
		} catch (Exception ex) {
			log.warn("Workflow step {} failed unexpectedly", step.getStepCode(), ex);
			return StepResult.error(step.getStepCode(), ex.getMessage());
		}
	}

	private static long resolveTimeoutMs(WorkflowStepEntity step) {
		Long configured = step.getTimeoutMs();
		return (configured == null || configured <= 0) ? DEFAULT_TIMEOUT_MS : configured;
	}

	private void validateInputs(WorkflowStepEntity step, DefaultExtendedState state) {
		List<String> requiredInputs = workflowJsonHelper.readStringList(step.getRequiredInputsJson());
		Map<Object, Object> variables = state.getVariables();
		for (String inputKey : requiredInputs) {
			if (!variables.containsKey(inputKey) || variables.get(inputKey) == null) {
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

	private WorkflowStepEntity resolveNextStep(WorkflowPlan plan, WorkflowStepEntity currentStep,
			WorkflowExecutionStatus status, DefaultExtendedState state) {
		WorkflowTransitionType transitionType = toTransitionType(status);
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

	private static WorkflowTransitionType toTransitionType(WorkflowExecutionStatus status) {
		return switch (status) {
		case SUCCESS -> WorkflowTransitionType.SUCCESS;
		case FAILURE -> WorkflowTransitionType.FAILURE;
		case SKIP -> WorkflowTransitionType.SKIP;
		};
	}

	private void publishStage(WorkflowPlan plan, ProjectRunEntity run, WorkflowStepEntity step, String status,
			String message, int attempt) {
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
		projectEventStreamService.publish(run.getProject().getId(), STAGE_EVENT, payload);
	}

	private static void sleep(long millis) {
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Value object encapsulating a step's retry configuration with safe defaults.
	 */
	private record RetryPolicy(int maxAttempts, long initialBackoffMs, double multiplier) {

		static RetryPolicy from(WorkflowStepEntity step) {
			int maxAttempts = 1;
			if (step.isRetryEnabled()) {
				Integer configured = step.getRetryMaxAttempts();
				maxAttempts = Math.max(1, configured == null ? 1 : configured);
			}
			Long backoff = step.getRetryBackoffMs();
			long initialBackoffMs = backoff == null ? 0L : Math.max(0L, backoff);
			Double configuredMultiplier = step.getRetryBackoffMultiplier();
			double multiplier = configuredMultiplier == null ? 1.0d : Math.max(1.0d, configuredMultiplier);
			return new RetryPolicy(maxAttempts, initialBackoffMs, multiplier);
		}
	}
}
