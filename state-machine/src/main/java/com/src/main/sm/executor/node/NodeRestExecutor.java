package com.src.main.sm.executor.node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component("nodeRestExecutor")
public class NodeRestExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			for (NodeModelDefinition modelDefinition : context.models()) {
				NodeGenerationSupport.writeFile(context.root(), "src/controllers/" + modelDefinition.name() + "Controller.ts", renderController(modelDefinition));
				NodeGenerationSupport.writeFile(context.root(), "src/routes/" + modelDefinition.name() + "Routes.ts", renderRoute(context, modelDefinition));
			}
			NodeGenerationSupport.writeFile(context.root(), "src/routes/index.ts", renderRouteRegistry(context));
			return NodeGenerationSupport.success("Node routes generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_REST_GENERATION", ex.getMessage());
		}
	}

	private String renderController(NodeModelDefinition modelDefinition) {
		String name = modelDefinition.name();
		String camel = NodeGenerationSupport.toCamelCase(name);
		return """
				import { Request, Response } from 'express';
				
				import { %2$sService } from '../services/%1$sService';
				
				export const list%1$s = (_request: Request, response: Response): void => {
				  response.json(%2$sService.list());
				};
				
				export const get%1$s = (request: Request, response: Response): void => {
				  const item = %2$sService.getById(request.params.id);
				  if (!item) {
				    response.status(404).json({ message: '%1$s not found' });
				    return;
				  }
				  response.json(item);
				};
				
				export const create%1$s = (request: Request, response: Response): void => {
				  const item = %2$sService.create(request.body ?? {});
				  response.status(201).json(item);
				};
				
				export const update%1$s = (request: Request, response: Response): void => {
				  const item = %2$sService.update(request.params.id, request.body ?? {});
				  if (!item) {
				    response.status(404).json({ message: '%1$s not found' });
				    return;
				  }
				  response.json(item);
				};
				
				export const delete%1$s = (request: Request, response: Response): void => {
				  const deleted = %2$sService.delete(request.params.id);
				  response.status(deleted ? 204 : 404).send();
				};
				""".formatted(name, camel);
	}

	private String renderRoute(NodeProjectContext context, NodeModelDefinition modelDefinition) {
		String name = modelDefinition.name();
		String routeVariable = NodeGenerationSupport.toCamelCase(NodeGenerationSupport.toKebabCase(name)) + "Routes";
		NodeRestConfig restConfig = modelDefinition.restConfig();
		List<String> imports = new ArrayList<>();
		imports.add("import { Router } from 'express';");
		imports.add("import { validateRequest } from '../middleware/validate-request';");
		imports.add("import { create" + name + ", delete" + name + ", get" + name + ", list" + name + ", update" + name + " } from '../controllers/" + name + "Controller';");

		String createSchemaName = resolveCreateSchemaName(context, modelDefinition);
		String updateSchemaName = resolveUpdateSchemaName(context, modelDefinition);
		if (!createSchemaName.isBlank()) {
			imports.add(createSchemaImport(context, modelDefinition, createSchemaName));
		}
		if (!updateSchemaName.isBlank()) {
			imports.add(updateSchemaImport(context, modelDefinition, updateSchemaName));
		}

		List<String> routes = new ArrayList<>();
		if (restConfig.listEnabled()) {
			routes.add("router.get('/', list" + name + ");");
		}
		if (restConfig.getEnabled()) {
			routes.add("router.get('/:id', get" + name + ");");
		}
		if (restConfig.createEnabled()) {
			String validation = createSchemaName.isBlank() ? "" : "validateRequest(" + createSchemaName + "), ";
			routes.add("router.post('/', " + validation + "create" + name + ");");
		}
		if (restConfig.updateEnabled()) {
			String validation = updateSchemaName.isBlank() ? "" : "validateRequest(" + updateSchemaName + "), ";
			routes.add("router.put('/:id', " + validation + "update" + name + ");");
		}
		if (restConfig.deleteEnabled()) {
			routes.add("router.delete('/:id', delete" + name + ");");
		}

		return String.join("\n", imports.stream().distinct().toList())
				+ "\n\nconst router = Router();\n\n"
				+ String.join("\n", routes)
				+ "\n\nexport const " + routeVariable + " = router;\n";
	}

	private String renderRouteRegistry(NodeProjectContext context) {
		String imports = context.models().stream()
				.map(model -> "import { " + routeVariable(model) + " } from './" + model.name() + "Routes';")
				.collect(Collectors.joining("\n"));
		String mounts = context.models().stream()
				.map(model -> "  app.use('" + normalizeRoutePath(model.restConfig().basePath(), model.name()) + "', " + routeVariable(model) + ");")
				.collect(Collectors.joining("\n"));
		return """
				import { Express } from 'express';
				%1$s
				
				export const registerRoutes = (app: Express): void => {
				  app.get('/health', (_request, response) => {
				    response.json({ status: 'UP' });
				  });
				%2$s
				};
				""".formatted(imports.isBlank() ? "" : "\n" + imports, mounts.isBlank() ? "" : "\n" + mounts);
	}

	private String routeVariable(NodeModelDefinition modelDefinition) {
		return NodeGenerationSupport.toCamelCase(NodeGenerationSupport.toKebabCase(modelDefinition.name())) + "Routes";
	}

	private String createSchemaImport(NodeProjectContext context, NodeModelDefinition modelDefinition, String schemaName) {
		String currentPath = "routes";
		String dtoName = modelDefinition.restConfig().createDtoName();
		if (!dtoName.isBlank() && NodeValidationSupport.hasDto(context, dtoName)) {
			return "import { " + schemaName + " } from '" + NodeValidationSupport.dtoSchemaImportPath(context, currentPath, dtoName) + "';";
		}
		return "import { " + schemaName + " } from '../validation/models/" + modelDefinition.name() + ".schema';";
	}

	private String updateSchemaImport(NodeProjectContext context, NodeModelDefinition modelDefinition, String schemaName) {
		String currentPath = "routes";
		String dtoName = resolveUpdateDtoName(modelDefinition);
		if (!dtoName.isBlank() && NodeValidationSupport.hasDto(context, dtoName)) {
			return "import { " + schemaName + " } from '" + NodeValidationSupport.dtoSchemaImportPath(context, currentPath, dtoName) + "';";
		}
		return "import { " + schemaName + " } from '../validation/models/" + modelDefinition.name() + ".schema';";
	}

	private String resolveCreateSchemaName(NodeProjectContext context, NodeModelDefinition modelDefinition) {
		String dtoName = modelDefinition.restConfig().createDtoName();
		if (!dtoName.isBlank() && NodeValidationSupport.hasDto(context, dtoName)) {
			return NodeGenerationSupport.toPascalCase(dtoName) + "Schema";
		}
		return "Create" + modelDefinition.name() + "InputSchema";
	}

	private String resolveUpdateSchemaName(NodeProjectContext context, NodeModelDefinition modelDefinition) {
		String dtoName = resolveUpdateDtoName(modelDefinition);
		if (!dtoName.isBlank() && NodeValidationSupport.hasDto(context, dtoName)) {
			return NodeGenerationSupport.toPascalCase(dtoName) + "Schema";
		}
		return "Update" + modelDefinition.name() + "InputSchema";
	}

	private String resolveUpdateDtoName(NodeModelDefinition modelDefinition) {
		if (!modelDefinition.restConfig().updateDtoName().isBlank()) {
			return modelDefinition.restConfig().updateDtoName();
		}
		if (!modelDefinition.restConfig().patchDtoName().isBlank()) {
			return modelDefinition.restConfig().patchDtoName();
		}
		return modelDefinition.restConfig().createDtoName();
	}

	private String normalizeRoutePath(String basePath, String modelName) {
		String path = basePath == null || basePath.isBlank() ? "/api/" + NodeGenerationSupport.toKebabCase(modelName) : basePath.trim();
		return path.startsWith("/") ? path : "/" + path;
	}
}
