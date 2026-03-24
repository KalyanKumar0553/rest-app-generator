package com.src.main.workflow.engine;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.config.StepExecutor;

@Component
public class WorkflowExecutorRegistry {

	private final Map<String, StepExecutor> executorsByBeanName;

	public WorkflowExecutorRegistry(Map<String, StepExecutor> executorsByBeanName) {
		this.executorsByBeanName = executorsByBeanName;
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
