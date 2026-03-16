package com.src.main.sm.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.node.NodeApplicationFilesExecutor;
import com.src.main.sm.executor.node.NodeDockerExecutor;
import com.src.main.sm.executor.node.NodeDtoExecutor;
import com.src.main.sm.executor.node.NodeEnumExecutor;
import com.src.main.sm.executor.node.NodeModelExecutor;
import com.src.main.sm.executor.node.NodeRestExecutor;
import com.src.main.sm.executor.node.NodeScaffoldExecutor;

@Component
public class NodeStepExecutorFactory {

	private final Map<NodeState, StepExecutor> registry = new EnumMap<>(NodeState.class);

	public NodeStepExecutorFactory(
			NodeScaffoldExecutor scaffoldExecutor,
			NodeEnumExecutor enumExecutor,
			NodeDtoExecutor dtoExecutor,
			NodeModelExecutor modelExecutor,
			NodeRestExecutor restExecutor,
			NodeApplicationFilesExecutor applicationFilesExecutor,
			NodeDockerExecutor dockerExecutor) {
		registry.put(NodeState.SCAFFOLD, scaffoldExecutor);
		registry.put(NodeState.ENUM_GENERATION, enumExecutor);
		registry.put(NodeState.DTO_GENERATION, dtoExecutor);
		registry.put(NodeState.MODEL_GENERATION, modelExecutor);
		registry.put(NodeState.REST_GENERATION, restExecutor);
		registry.put(NodeState.APPLICATION_FILES, applicationFilesExecutor);
		registry.put(NodeState.DOCKER_GENERATION, dockerExecutor);
	}

	public StepExecutor forState(NodeState state) {
		StepExecutor executor = registry.get(state);
		if (executor == null) {
			throw new IllegalArgumentException("No executor registered for node state: " + state);
		}
		return executor;
	}
}
