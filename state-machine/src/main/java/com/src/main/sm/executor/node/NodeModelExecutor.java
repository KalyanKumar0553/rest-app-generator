package com.src.main.sm.executor.node;

import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;

@Component("nodeModelExecutor")
public class NodeModelExecutor implements StepExecutor {

	@Override
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			for (NodeModelDefinition modelDefinition : context.models()) {
				if ("sequelize".equals(context.orm())) {
					NodeGenerationSupport.writeFile(context.root(), "src/models/" + modelDefinition.name() + ".model.ts", renderSequelizeModel(modelDefinition));
				} else {
					NodeGenerationSupport.writeFile(context.root(), "src/models/" + modelDefinition.name() + ".ts", renderPrismaModel(modelDefinition));
				}
				NodeGenerationSupport.writeFile(context.root(), "src/services/" + modelDefinition.name() + "Service.ts", renderService(context, modelDefinition));
				NodeGenerationSupport.writeFile(context.root(),
						"src/validation/models/" + modelDefinition.name() + ".schema.ts",
						NodeValidationSupport.renderModelSchema(context, modelDefinition));
			}
			if ("sequelize".equals(context.orm())) {
				NodeGenerationSupport.writeFile(context.root(), "src/models/index.ts", renderSequelizeIndex(context));
			}
			return NodeGenerationSupport.success("Node models generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_MODEL_GENERATION", ex.getMessage());
		}
	}

	private String renderPrismaModel(NodeModelDefinition modelDefinition) {
		String fields = modelDefinition.fields().stream()
				.map(field -> "  " + field.name() + (field.optional() ? "?: " : ": ") + field.tsType() + ";")
				.collect(Collectors.joining("\n"));
		return "export interface " + modelDefinition.name() + " {\n" + fields + "\n}\n\n"
				+ "export type Create" + modelDefinition.name() + "Input = Omit<" + modelDefinition.name() + ", 'id'>;\n"
				+ "export type Update" + modelDefinition.name() + "Input = Partial<Create" + modelDefinition.name() + "Input>;\n";
	}

	private String renderSequelizeModel(NodeModelDefinition modelDefinition) {
		String initFields = modelDefinition.fields().stream()
				.map(field -> "  " + field.name() + ": { type: " + sequelizeType(field) + ", allowNull: " + Boolean.toString(field.optional()) + (field.name().equals("id") ? ", primaryKey: true" : "") + " }")
				.collect(Collectors.joining(",\n"));
		String declaredFields = modelDefinition.fields().stream()
				.map(field -> "  declare " + field.name() + ": " + field.tsType() + ";")
				.collect(Collectors.joining("\n"));
		return """
				import { DataTypes, InferAttributes, InferCreationAttributes, Model } from 'sequelize';
				
				import { sequelize } from '../lib/sequelize';
				
				export class %1$s extends Model<InferAttributes<%1$s>, InferCreationAttributes<%1$s>> {
				%2$s
				}
				
				%1$s.init({
				%3$s
				}, {
				  sequelize,
				  modelName: '%4$s',
				  tableName: '%4$ss',
				  timestamps: false
				});
				""".formatted(modelDefinition.name(), declaredFields, initFields, NodeGenerationSupport.toCamelCase(modelDefinition.name()));
	}

	private String sequelizeType(NodeFieldDefinition field) {
		String normalized = String.valueOf(field.rawType()).trim().toLowerCase();
		return switch (normalized) {
		case "int", "integer", "long", "short", "byte" -> "DataTypes.INTEGER";
		case "double", "float", "bigdecimal" -> "DataTypes.DOUBLE";
		case "boolean" -> "DataTypes.BOOLEAN";
		case "localdate" -> "DataTypes.DATEONLY";
		case "localdatetime", "offsetdatetime", "instant", "date" -> "DataTypes.DATE";
		default -> "DataTypes.STRING";
		};
	}

	private String renderService(NodeProjectContext context, NodeModelDefinition modelDefinition) {
		String name = modelDefinition.name();
		String camel = NodeGenerationSupport.toCamelCase(name);
		if ("sequelize".equals(context.orm())) {
			return """
					import { randomUUID } from 'node:crypto';
					
					import { Create%1$sInput, Update%1$sInput } from '../validation/models/%1$s.schema';
					import { %1$s } from '../models/%1$s.model';
					
					class %1$sService {
					  async list(): Promise<%1$s[]> {
					    return %1$s.findAll();
					  }
					
					  async getById(id: string): Promise<%1$s | null> {
					    return %1$s.findByPk(id);
					  }
					
					  async create(input: Create%1$sInput): Promise<%1$s> {
					    return %1$s.create({ ...input, id: randomUUID() } as never);
					  }
					
					  async update(id: string, input: Update%1$sInput): Promise<%1$s | null> {
					    const current = await %1$s.findByPk(id);
					    if (!current) {
					      return null;
					    }
					    await current.update(input);
					    return current;
					  }
					
					  async delete(id: string): Promise<boolean> {
					    const deleted = await %1$s.destroy({ where: { id } });
					    return deleted > 0;
					  }
					}
					
					export const %2$sService = new %1$sService();
					""".formatted(name, camel);
		}
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

	private String renderSequelizeIndex(NodeProjectContext context) {
		return context.models().stream()
				.map(model -> "export * from './" + model.name() + ".model';")
				.collect(Collectors.joining("\n", "", "\n"));
	}
}
