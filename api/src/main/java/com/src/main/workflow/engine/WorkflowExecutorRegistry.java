package com.src.main.workflow.engine;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.src.main.sm.config.StepExecutor;

/**
 * Registry of Spring-managed {@link StepExecutor} beans, keyed by bean name.
 * Provides O(1) lookup for the workflow engine when resolving step executors.
 */
@Component
public class WorkflowExecutorRegistry {

	private static final Logger log = LoggerFactory.getLogger(WorkflowExecutorRegistry.class);

	private final Map<String, StepExecutor> executorsByBeanName;

	public WorkflowExecutorRegistry(Map<String, StepExecutor> executorsByBeanName) {
		this.executorsByBeanName = Map.copyOf(executorsByBeanName);
		if (log.isInfoEnabled()) {
			log.info("Registered {} StepExecutor bean(s): {}", this.executorsByBeanName.size(),
					this.executorsByBeanName.keySet());
		}
	}

	public StepExecutor resolve(String executorKey) {
		StepExecutor executor = executorsByBeanName.get(executorKey);
		if (executor == null) {
			throw new IllegalArgumentException("No StepExecutor bean registered with name " + executorKey);
		}
		return executor;
	}

	public boolean exists(String executorKey) {
		return executorsByBeanName.containsKey(executorKey);
	}
}
