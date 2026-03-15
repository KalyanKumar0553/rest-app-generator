package com.src.main.sm.executor.node;

import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component
public class NodeModelExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			for (NodeModelDefinition modelDefinition : context.models()) {
				NodeGenerationSupport.writeFile(context.root(), "src/models/" + modelDefinition.name() + ".ts", renderModel(modelDefinition));
				NodeGenerationSupport.writeFile(context.root(), "src/services/" + modelDefinition.name() + "Service.ts", renderService(modelDefinition));
				NodeGenerationSupport.writeFile(context.root(),
						"src/validation/models/" + modelDefinition.name() + ".schema.ts",
						NodeValidationSupport.renderModelSchema(context, modelDefinition));
			}
			return NodeGenerationSupport.success("Node models generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_MODEL_GENERATION", ex.getMessage());
		}
	}

	private String renderModel(NodeModelDefinition modelDefinition) {
		String fields = modelDefinition.fields().stream()
				.map(field -> "  " + field.name() + (field.optional() ? "?: " : ": ") + field.tsType() + ";")
				.collect(Collectors.joining("\n"));
		return "export interface " + modelDefinition.name() + " {\n" + fields + "\n}\n\n"
				+ "export type Create" + modelDefinition.name() + "Input = Omit<" + modelDefinition.name() + ", 'id'>;\n"
				+ "export type Update" + modelDefinition.name() + "Input = Partial<Create" + modelDefinition.name() + "Input>;\n";
	}

	private String renderService(NodeModelDefinition modelDefinition) {
		String name = modelDefinition.name();
		String camel = NodeGenerationSupport.toCamelCase(name);
		return """
				import { randomUUID } from 'node:crypto';
				
				import { %1$s, Create%1$sInput, Update%1$sInput } from '../models/%1$s';
				
				class %1$sService {
				  private readonly items = new Map<string, %1$s>();
				
				  list(): %1$s[] {
				    return Array.from(this.items.values());
				  }
				
				  getById(id: string): %1$s | undefined {
				    return this.items.get(id);
				  }
				
				  create(input: Create%1$sInput): %1$s {
				    const %2$s: %1$s = { id: randomUUID(), ...input };
				    this.items.set(%2$s.id, %2$s);
				    return %2$s;
				  }
				
				  update(id: string, input: Update%1$sInput): %1$s | undefined {
				    const current = this.items.get(id);
				    if (!current) {
				      return undefined;
				    }
				    const updated: %1$s = { ...current, ...input, id };
				    this.items.set(id, updated);
				    return updated;
				  }
				
				  delete(id: string): boolean {
				    return this.items.delete(id);
				  }
				}
				
				export const %2$sService = new %1$sService();
				""".formatted(name, camel);
	}
}
