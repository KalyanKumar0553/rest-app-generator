package com.src.main.sm.executor.node;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component
public class NodeApplicationFilesExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			NodeGenerationSupport.writeFile(context.root(), "src/config/node-config.ts", renderConfig(context));
			NodeGenerationSupport.writeFile(context.root(), "src/middleware/validate-request.ts", renderValidationMiddleware());
			NodeGenerationSupport.writeFile(context.root(), "src/app.ts", renderApp(context));
			NodeGenerationSupport.writeFile(context.root(), "src/main.ts", renderMain());
			return NodeGenerationSupport.success("Node application files generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_APP_FILES", ex.getMessage());
		}
	}

	private String renderConfig(NodeProjectContext context) {
		return """
				export const nodeConfig = {
				  appName: '%s',
				  port: Number(process.env.PORT || %d)
				};
				""".formatted(context.appName().replace("'", "\\'"), context.port());
	}

	private String renderApp(NodeProjectContext _context) {
		return """
				import cors from 'cors';
				import express from 'express';
				
				import { registerRoutes } from './routes';
				
				export const createApp = () => {
				  const app = express();
				  app.use(cors());
				  app.use(express.json());
				  registerRoutes(app);
				  return app;
				};
				""";
	}

	private String renderMain() {
		return """
				import { createApp } from './app';
				import { nodeConfig } from './config/node-config';
				
				const app = createApp();
				app.listen(nodeConfig.port, () => {
				  console.log(`${nodeConfig.appName} listening on port ${nodeConfig.port}`);
				});
				""";
	}

	private String renderValidationMiddleware() {
		return """
				import { NextFunction, Request, Response } from 'express';
				import { ZodTypeAny } from 'zod';
				
				export const validateRequest = (schema: ZodTypeAny) => {
				  return (request: Request, response: Response, next: NextFunction): void => {
				    const result = schema.safeParse(request.body ?? {});
				    if (!result.success) {
				      response.status(400).json({
				        message: 'Validation failed',
				        errors: result.error.issues.map((issue) => ({
				          path: issue.path.join('.'),
				          message: issue.message
				        }))
				      });
				      return;
				    }
				
				    request.body = result.data;
				    next();
				  };
				};
				""";
	}
}
