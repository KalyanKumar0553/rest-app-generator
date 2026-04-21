package com.src.main.sm.executor.node;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component("nodeDockerExecutor")
public class NodeDockerExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			if (!context.dockerEnabled()) {
				return NodeGenerationSupport.success("Node docker generation skipped");
			}
			NodeGenerationSupport.writeFile(context.root(), "Dockerfile", renderDockerfile());
			NodeGenerationSupport.writeFile(context.root(), ".dockerignore", renderDockerIgnore());
			return NodeGenerationSupport.success("Node docker files generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_DOCKER_GENERATION", ex.getMessage());
		}
	}

	private String renderDockerfile() {
		return """
				FROM node:22-alpine
				WORKDIR /app
				COPY package.json tsconfig.json ./
				RUN npm install
				COPY src ./src
				EXPOSE 3000
				CMD ["npm", "start"]
				""";
	}

	private String renderDockerIgnore() {
		return """
				node_modules
				dist
				.git
				""";
	}
}
