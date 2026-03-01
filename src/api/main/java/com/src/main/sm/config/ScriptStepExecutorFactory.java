package com.src.main.sm.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.NodeGenerationExecutor;
import com.src.main.sm.executor.PythonGenerationExecutor;

@Component
public class ScriptStepExecutorFactory {

	private final Map<ScriptStates, StepExecutor> registry = new EnumMap<>(ScriptStates.class);

	public ScriptStepExecutorFactory(
			NodeGenerationExecutor nodeGenerationExecutor,
			PythonGenerationExecutor pythonGenerationExecutor) {
		registry.put(ScriptStates.NODE_GENERATION, nodeGenerationExecutor);
		registry.put(ScriptStates.PYTHON_GENERATION, pythonGenerationExecutor);
	}

	public StepExecutor forState(ScriptStates state) {
		StepExecutor ex = registry.get(state);
		if (ex == null) {
			throw new IllegalArgumentException("No executor registered for script state: " + state);
		}
		return ex;
	}
}
