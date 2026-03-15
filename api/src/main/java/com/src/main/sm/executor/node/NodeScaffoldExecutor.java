package com.src.main.sm.executor.node;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component
public class NodeScaffoldExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			NodeGenerationSupport.writeFile(context.root(), "package.json", renderPackageJson(context));
			NodeGenerationSupport.writeFile(context.root(), "tsconfig.json", renderTsConfig());
			NodeGenerationSupport.writeFile(context.root(), ".gitignore", renderGitIgnore());
			NodeGenerationSupport.writeFile(context.root(), "README.md", renderReadme(context));
			return NodeGenerationSupport.success("Node scaffold generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_SCAFFOLD", ex.getMessage());
		}
	}

	private String renderPackageJson(NodeProjectContext context) {
		return """
				{
				  \"name\": \"%s\",
				  \"version\": \"%s\",
				  \"private\": true,
				  \"description\": \"%s\",
				  \"scripts\": {
				    \"dev\": \"tsx watch src/main.ts\",
				    \"start\": \"tsx src/main.ts\",
				    \"build\": \"tsc -p tsconfig.json\"
				  },
				  \"dependencies\": {
				    \"cors\": \"^2.8.5\",
				    \"express\": \"^4.21.2\",
				    \"zod\": \"^3.24.1\"
				  },
				  \"devDependencies\": {
				    \"@types/cors\": \"^2.8.17\",
				    \"@types/express\": \"^5.0.1\",
				    \"@types/node\": \"^22.10.2\",
				    \"tsx\": \"^4.19.2\",
				    \"typescript\": \"^5.7.2\"
				  }
				}
				""".formatted(context.artifactId(), context.version(), escapeJson(context.description()));
	}

	private String renderTsConfig() {
		return """
				{
				  \"compilerOptions\": {
				    \"target\": \"ES2022\",
				    \"module\": \"CommonJS\",
				    \"moduleResolution\": \"Node\",
				    \"rootDir\": \"src\",
				    \"outDir\": \"dist\",
				    \"strict\": true,
				    \"esModuleInterop\": true,
				    \"forceConsistentCasingInFileNames\": true,
				    \"skipLibCheck\": true,
				    \"resolveJsonModule\": true
				  },
				  \"include\": [\"src/**/*.ts\"],
				  \"exclude\": [\"dist\", \"node_modules\"]
				}
				""";
	}

	private String renderGitIgnore() {
		return """
				node_modules
				dist
				.env
				""";
	}

	private String renderReadme(NodeProjectContext context) {
		return """
				# %s
				
				%s
				
				## Run
				
				```bash
				%s install
				%s start
				```
				
				The server starts on port `%d`.
				""".formatted(context.appName(), context.description(), context.packageManager(), context.packageManager(), context.port());
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
