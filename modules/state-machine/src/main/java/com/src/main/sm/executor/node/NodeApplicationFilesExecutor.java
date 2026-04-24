package com.src.main.sm.executor.node;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.util.ShippableModuleSupport;

@Component("nodeApplicationFilesExecutor")
public class NodeApplicationFilesExecutor implements StepExecutor {

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			Map<String, Object> yaml = (Map<String, Object>) state.getVariables().get("yaml");
			List<String> selectedModules = NodeGenerationSupport.extractSelectedShippedModules(yaml);
			Map<String, Object> moduleConfigs = NodeGenerationSupport.extractModuleConfigs(yaml);
			boolean includePrisma = "prisma".equals(context.orm()) || ShippableModuleSupport.requiresNodePrisma(selectedModules);
			boolean useSequelize = "sequelize".equals(context.orm());

			NodeGenerationSupport.writeFile(context.root(), "src/config/node-config.ts", renderConfig(context, includePrisma, useSequelize));
			NodeGenerationSupport.writeFile(context.root(), "src/middleware/validate-request.ts", renderValidationMiddleware());
			NodeGenerationSupport.writeFile(context.root(), "src/generated/module-manifest.json",
					renderModuleManifest(selectedModules, moduleConfigs));
			NodeGenerationSupport.writeFile(context.root(), "src/generated/module-bootstrap.ts",
					renderModuleBootstrap(selectedModules));
			if (includePrisma) {
				NodeGenerationSupport.writeFile(context.root(), ".env.example", renderPrismaEnvExample());
				NodeGenerationSupport.writeFile(context.root(), "src/lib/prisma.ts", renderPrismaClient());
				NodeGenerationSupport.writeFile(context.root(), "prisma/schema.prisma", renderPrismaSchema(context, selectedModules));
			}
			if (useSequelize) {
				NodeGenerationSupport.writeFile(context.root(), ".env.example", renderSequelizeEnvExample(context));
				NodeGenerationSupport.writeFile(context.root(), "src/lib/sequelize.ts", renderSequelizeClient(context));
				NodeGenerationSupport.writeFile(context.root(), "src/scripts/sync-db.ts", renderSequelizeSyncScript());
			}
			NodeGenerationSupport.writeFile(context.root(), "src/app.ts", renderApp(selectedModules));
			NodeGenerationSupport.writeFile(context.root(), "src/main.ts", renderMain(useSequelize));
			return NodeGenerationSupport.success("Node application files generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_APP_FILES", ex.getMessage());
		}
	}

	private String renderConfig(NodeProjectContext context, boolean includePrisma, boolean useSequelize) {
		String databaseLine = "";
		if (includePrisma) {
			databaseLine = "  databaseUrl: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/%s',\n"
					.formatted(context.artifactId().replace("'", "\\'"));
		} else if (useSequelize) {
			databaseLine = """
				  database: {
				    host: process.env.DB_HOST || 'localhost',
				    port: Number(process.env.DB_PORT || 5432),
				    name: process.env.DB_NAME || '%s',
				    user: process.env.DB_USER || 'postgres',
				    password: process.env.DB_PASSWORD || 'postgres'
				  },
				""".formatted(context.artifactId().replace("'", "\\'"));
		}
		return """
				export const nodeConfig = {
				  appName: '%s',
				  port: Number(process.env.PORT || %d),
				%s};
				""".formatted(context.appName().replace("'", "\\'"), context.port(), databaseLine);
	}

	private String renderApp(List<String> selectedModules) {
		String bootstrapImport = selectedModules.isEmpty() ? ""
				: "import { configureGeneratedModules } from './generated/module-bootstrap';\n";
		String bootstrapCall = selectedModules.isEmpty() ? "" : "  configureGeneratedModules(app);\n";
		return """
				import cors from 'cors';
				import express from 'express';
				%s
				import { registerRoutes } from './routes';
				
				export const createApp = () => {
				  const app = express();
				  app.use(cors());
				  app.use(express.json());
				%s  registerRoutes(app);
				  return app;
				};
				""".formatted(bootstrapImport, bootstrapCall);
	}

	private String renderMain(boolean useSequelize) {
		String databaseImport = useSequelize ? "\nimport { initializeDatabase } from './lib/sequelize';" : "";
		String databaseInit = useSequelize ? "\ninitializeDatabase()\n  .then(() => {\n" : "";
		String databaseClose = useSequelize ? "\n  })\n  .catch((error) => {\n    console.error('Database initialization failed', error);\n    process.exit(1);\n  });" : "";
		return """
				import 'dotenv/config';
				
				import { createApp } from './app';
				import { nodeConfig } from './config/node-config';%s
				
				const app = createApp();
				%s  app.listen(nodeConfig.port, () => {
				    console.log(`${nodeConfig.appName} listening on port ${nodeConfig.port}`);
				  });%s
				""".formatted(databaseImport, databaseInit, databaseClose);
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

	private String renderPrismaEnvExample() {
		return """
				DATABASE_URL="postgresql://postgres:postgres@localhost:5432/appdb?schema=public"
				""";
	}

	private String renderSequelizeEnvExample(NodeProjectContext context) {
		return """
				DB_HOST=localhost
				DB_PORT=5432
				DB_NAME=%s
				DB_USER=postgres
				DB_PASSWORD=postgres
				""".formatted(context.artifactId());
	}

	private String renderPrismaClient() {
		return """
				import { PrismaClient } from '@prisma/client';
				
				const globalForPrisma = globalThis as typeof globalThis & {
				  prisma?: PrismaClient;
				};
				
				export const prisma = globalForPrisma.prisma ?? new PrismaClient();
				
				if (process.env.NODE_ENV !== 'production') {
				  globalForPrisma.prisma = prisma;
				}
				""";
	}

	private String renderSequelizeClient(NodeProjectContext context) {
		return """
				import { Sequelize } from 'sequelize';
				
				import { nodeConfig } from '../config/node-config';
				
				export const sequelize = new Sequelize(
				  nodeConfig.database.name,
				  nodeConfig.database.user,
				  nodeConfig.database.password,
				  {
				    host: nodeConfig.database.host,
				    port: nodeConfig.database.port,
				    dialect: 'postgres',
				    logging: false
				  }
				);
				
				export const initializeDatabase = async (): Promise<void> => {
				  await sequelize.authenticate();
				};
				""";
	}

	private String renderSequelizeSyncScript() {
		return """
				import 'dotenv/config';
				
				import { sequelize } from '../lib/sequelize';
				import '../models';
				
				sequelize.sync({ alter: true })
				  .then(async () => {
				    await sequelize.close();
				    console.log('Database schema synchronized');
				  })
				  .catch(async (error) => {
				    console.error('Failed to synchronize database schema', error);
				    await sequelize.close();
				    process.exitCode = 1;
				  });
				""";
	}

	private String renderPrismaSchema(NodeProjectContext context, List<String> selectedModules) {
		StringBuilder builder = new StringBuilder();
		builder.append("""
				generator client {
				  provider = "prisma-client-js"
				}
				
				datasource db {
				  provider = "postgresql"
				  url      = env("DATABASE_URL")
				}
				
				""");
		if (selectedModules.contains("auth")) {
			builder.append("""
					model User {
					  id            String         @id @default(cuid())
					  username      String         @unique
					  email         String         @unique
					  passwordHash  String
					  status        String
					  timezone      String?
					  createdAt     DateTime       @default(now())
					  updatedAt     DateTime       @updatedAt
					  refreshTokens RefreshToken[]
					}
					
					model RefreshToken {
					  id        String   @id @default(cuid())
					  token     String   @unique
					  subject   String
					  issuedAt  DateTime
					  expiresAt DateTime
					  userId    String
					  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
					}
					
					model OAuthProviderConfig {
					  id        String   @id @default(cuid())
					  provider  String   @unique
					  enabled   Boolean  @default(false)
					  clientId  String?
					  createdAt DateTime @default(now())
					  updatedAt DateTime @updatedAt
					}
					
					""");
		}
		if (selectedModules.contains("rbac")) {
			builder.append("""
					model Role {
					  id              String           @id @default(cuid())
					  name            String           @unique
					  displayName     String?
					  description     String?
					  systemRole      Boolean          @default(false)
					  active          Boolean          @default(true)
					  rolePermissions RolePermission[]
					  userRoles       UserRole[]
					}
					
					model Permission {
					  id              String           @id @default(cuid())
					  name            String           @unique
					  displayName     String?
					  description     String?
					  category        String           @default("RBAC")
					  active          Boolean          @default(true)
					  rolePermissions RolePermission[]
					}
					
					model RolePermission {
					  roleId       String
					  permissionId String
					  role         Role       @relation(fields: [roleId], references: [id], onDelete: Cascade)
					  permission   Permission @relation(fields: [permissionId], references: [id], onDelete: Cascade)
					
					  @@id([roleId, permissionId])
					}
					
					model UserRole {
					  userId String @id
					  roleId String
					  role   Role   @relation(fields: [roleId], references: [id], onDelete: Cascade)
					}
					
					""");
		}
		if (selectedModules.contains("subscription")) {
			builder.append("""
					model SubscriptionPlan {
					  id                 String                @id @default(cuid())
					  code               String                @unique
					  name               String
					  description        String?
					  currency           String                @default("INR")
					  monthlyPrice       Int
					  yearlyPrice        Int?
					  trialDays          Int                  @default(0)
					  isActive           Boolean              @default(true)
					  isDefault          Boolean              @default(false)
					  sortOrder          Int                  @default(0)
					  subscriptions      TenantSubscription[]
					  prices             PlanPrice[]
					  featureMappings    PlanFeatureMapping[]
					  roleMappings       SubscriptionPlanRoleMapping[]
					  couponMappings     SubscriptionCouponPlanMapping[]
					}
					
					model SubscriptionFeature {
					  id          String               @id @default(cuid())
					  code        String               @unique
					  name        String
					  description String?
					  featureType String
					  isActive    Boolean              @default(true)
					  mappings    PlanFeatureMapping[]
					}
					
					model PlanFeatureMapping {
					  id          String              @id @default(cuid())
					  planId      String
					  featureId   String
					  isEnabled   Boolean             @default(false)
					  limitValue  Int?
					  stringValue String?
					  plan        SubscriptionPlan    @relation(fields: [planId], references: [id], onDelete: Cascade)
					  feature     SubscriptionFeature @relation(fields: [featureId], references: [id], onDelete: Cascade)
					
					  @@unique([planId, featureId])
					}
					
					model PlanPrice {
					  id           String           @id @default(cuid())
					  planId        String
					  billingCycle  String
					  currencyCode  String
					  amount        Decimal        @db.Decimal(19, 2)
					  displayLabel  String?
					  isActive      Boolean        @default(true)
					  plan          SubscriptionPlan @relation(fields: [planId], references: [id], onDelete: Cascade)
					}
					
					model SubscriptionCoupon {
					  id                   String                           @id @default(cuid())
					  code                 String                           @unique
					  name                 String
					  discountType         String
					  discountValue        Decimal                          @db.Decimal(19, 4)
					  isActive             Boolean                          @default(true)
					  planMappings         SubscriptionCouponPlanMapping[]
					  subscriptions        TenantSubscription[]
					}
					
					model SubscriptionCouponPlanMapping {
					  id       String             @id @default(cuid())
					  couponId String
					  planId   String
					  coupon   SubscriptionCoupon @relation(fields: [couponId], references: [id], onDelete: Cascade)
					  plan     SubscriptionPlan   @relation(fields: [planId], references: [id], onDelete: Cascade)
					
					  @@unique([couponId, planId])
					}
					
					model SubscriptionPlanRoleMapping {
					  id       String           @id @default(cuid())
					  planId   String
					  roleName String
					  plan     SubscriptionPlan @relation(fields: [planId], references: [id], onDelete: Cascade)
					
					  @@unique([planId, roleName])
					}
					
					model TenantSubscription {
					  tenantId              String             @id
					  subscriberUserId      String?
					  planCode              String
					  status                String
					  billingCycle          String             @default("MONTHLY")
					  currency              String             @default("INR")
					  trialEndsAt           DateTime?
					  autoRenew             Boolean            @default(false)
					  appliedCouponCode     String?
					  createdAt             DateTime           @default(now())
					  updatedAt             DateTime           @updatedAt
					  plan                  SubscriptionPlan?  @relation(fields: [planCode], references: [code], onDelete: SetNull)
					  auditLogs             SubscriptionAuditLog[]
					}
					
					model SubscriptionAuditLog {
					  id               String              @id @default(cuid())
					  tenantId         String
					  subscriptionId   String?
					  eventType        String
					  actorId          String?
					  reason           String?
					  createdAt        DateTime            @default(now())
					  subscription     TenantSubscription? @relation(fields: [subscriptionId], references: [tenantId], onDelete: SetNull)
					}
					
					""");
		}
		if (selectedModules.contains("state-machine")) {
			builder.append("""
					model WorkflowState {
					  id           String   @id @default(cuid())
					  workflowName String
					  stateId      String
					  label        String
					  createdAt    DateTime @default(now())
					  updatedAt    DateTime @updatedAt
					
					  @@unique([workflowName, stateId])
					}
					
					model WorkflowTransition {
					  id           String   @id @default(cuid())
					  workflowName String
					  fromState    String
					  eventName    String
					  toState      String
					  createdAt    DateTime @default(now())
					  updatedAt    DateTime @updatedAt
					
					  @@unique([workflowName, fromState, eventName])
					}
					
					model WorkflowInstance {
					  id           String   @id @default(cuid())
					  workflowName String
					  entityId     String
					  currentState String
					  createdAt    DateTime @default(now())
					  updatedAt    DateTime @updatedAt
					
					  @@unique([workflowName, entityId])
					}
					
					""");
		}
		for (NodeModelDefinition model : context.models()) {
			builder.append(renderPrismaModel(model));
		}
		return builder.toString();
	}

	private String renderPrismaModel(NodeModelDefinition model) {
		StringBuilder builder = new StringBuilder();
		builder.append("model ").append(model.name()).append(" {\n");
		for (NodeFieldDefinition field : model.fields()) {
			builder.append("  ").append(field.name()).append(" ").append(toPrismaType(field));
			if ("id".equals(field.name())) {
				builder.append(" @id @default(uuid())");
			} else if (field.optional()) {
				builder.append("?");
			}
			builder.append("\n");
		}
		builder.append("}\n\n");
		return builder.toString();
	}

	private String toPrismaType(NodeFieldDefinition field) {
		String normalized = String.valueOf(field.rawType()).trim().toLowerCase();
		return switch (normalized) {
		case "int", "integer", "long", "short", "byte" -> "Int";
		case "double", "float", "bigdecimal" -> "Float";
		case "boolean" -> "Boolean";
		case "localdate", "localdatetime", "offsetdatetime", "instant", "date" -> "DateTime";
		default -> "String";
		};
	}

	private String renderModuleManifest(List<String> selectedModules, Map<String, Object> moduleConfigs) {
		List<String> expandedModules = ShippableModuleSupport.expandSelectedModules(selectedModules, GenerationLanguage.NODE);
		String modulesJson = expandedModules.stream()
				.map(moduleId -> "    \"" + escapeJson(moduleId) + "\"")
				.collect(Collectors.joining(",\n"));
		String configJson = renderConfigObject(moduleConfigs, 2);
		return """
				{
				  "generator": "node",
				  "selectedModules": [
				%s
				  ],
				  "moduleConfigs": %s
				}
				""".formatted(modulesJson, configJson);
	}

	private String renderModuleBootstrap(List<String> selectedModules) {
		List<String> expandedModules = ShippableModuleSupport.expandSelectedModules(selectedModules, GenerationLanguage.NODE);
		if (expandedModules.isEmpty()) {
			return """
					import type { Express } from 'express';
					
					export const configureGeneratedModules = (_app: Express): void => {};
					""";
		}

		String imports = expandedModules.stream()
				.map(moduleId -> "import { registerModule as register%sModule } from '../../modules/%s/src';"
						.formatted(toPascalCase(moduleId), moduleId))
				.collect(Collectors.joining("\n"));
		String registryEntries = expandedModules.stream()
				.map(moduleId -> "  %s: register%sModule".formatted(toQuotedKey(moduleId), toPascalCase(moduleId)))
				.collect(Collectors.joining(",\n"));
		return """
				import type { Express } from 'express';
				import moduleManifest from './module-manifest.json';
				%s
				
				const moduleRegistrars: Record<string, (app: Express, config: Record<string, unknown>, manifest: typeof moduleManifest) => void> = {
				%s
				};
				
				export const configureGeneratedModules = (app: Express): void => {
				  for (const moduleId of moduleManifest.selectedModules) {
				    const registerModule = moduleRegistrars[moduleId];
				    if (!registerModule) {
				      continue;
				    }
				    const config = (moduleManifest.moduleConfigs?.[moduleId] ?? {}) as Record<string, unknown>;
				    registerModule(app, config, moduleManifest);
				  }
				};
				""".formatted(imports, registryEntries);
	}

	private String renderConfigObject(Object value, int indentLevel) {
		String indent = " ".repeat(Math.max(0, indentLevel) * 2);
		String childIndent = " ".repeat(Math.max(0, indentLevel + 1) * 2);
		if (value instanceof Map<?, ?> map) {
			if (map.isEmpty()) {
				return "{}";
			}
			return map.entrySet().stream()
					.map(entry -> childIndent + "\"" + escapeJson(String.valueOf(entry.getKey())) + "\": "
							+ renderConfigObject(entry.getValue(), indentLevel + 1))
					.collect(Collectors.joining(",\n", "{\n", "\n" + indent + "}"));
		}
		if (value instanceof List<?> list) {
			if (list.isEmpty()) {
				return "[]";
			}
			return list.stream()
					.map(item -> childIndent + renderConfigObject(item, indentLevel + 1))
					.collect(Collectors.joining(",\n", "[\n", "\n" + indent + "]"));
		}
		if (value instanceof Number || value instanceof Boolean) {
			return String.valueOf(value);
		}
		if (value == null) {
			return "null";
		}
		return "\"" + escapeJson(String.valueOf(value)) + "\"";
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private String toQuotedKey(String value) {
		return "\"" + escapeJson(value) + "\"";
	}

	private String toPascalCase(String raw) {
		StringBuilder builder = new StringBuilder();
		for (String token : raw.split("[^a-zA-Z0-9]+")) {
			if (token.isBlank()) {
				continue;
			}
			builder.append(Character.toUpperCase(token.charAt(0)));
			if (token.length() > 1) {
				builder.append(token.substring(1));
			}
		}
		return builder.length() == 0 ? "Module" : builder.toString();
	}
}
