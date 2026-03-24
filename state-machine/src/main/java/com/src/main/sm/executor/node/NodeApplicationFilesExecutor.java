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
			boolean includePrisma = ShippableModuleSupport.requiresNodePrisma(selectedModules);

			NodeGenerationSupport.writeFile(context.root(), "src/config/node-config.ts", renderConfig(context, includePrisma));
			NodeGenerationSupport.writeFile(context.root(), "src/middleware/validate-request.ts", renderValidationMiddleware());
			NodeGenerationSupport.writeFile(context.root(), "src/generated/module-manifest.json",
					renderModuleManifest(selectedModules, moduleConfigs));
			NodeGenerationSupport.writeFile(context.root(), "src/generated/module-bootstrap.ts",
					renderModuleBootstrap(selectedModules));
			if (includePrisma) {
				NodeGenerationSupport.writeFile(context.root(), ".env.example", renderPrismaEnvExample());
				NodeGenerationSupport.writeFile(context.root(), "src/lib/prisma.ts", renderPrismaClient());
				NodeGenerationSupport.writeFile(context.root(), "prisma/schema.prisma", renderPrismaSchema(selectedModules));
			}
			NodeGenerationSupport.writeFile(context.root(), "src/app.ts", renderApp(selectedModules));
			NodeGenerationSupport.writeFile(context.root(), "src/main.ts", renderMain());
			return NodeGenerationSupport.success("Node application files generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_APP_FILES", ex.getMessage());
		}
	}

	private String renderConfig(NodeProjectContext context, boolean includePrisma) {
		String databaseLine = includePrisma
				? "  databaseUrl: process.env.DATABASE_URL || 'postgresql://postgres:postgres@localhost:5432/%s',\n"
						.formatted(context.artifactId().replace("'", "\\'"))
				: "";
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

	private String renderMain() {
		return """
				import 'dotenv/config';
				
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

	private String renderPrismaEnvExample() {
		return """
				DATABASE_URL="postgresql://postgres:postgres@localhost:5432/appdb?schema=public"
				""";
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

	private String renderPrismaSchema(List<String> selectedModules) {
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
					  rolePermissions RolePermission[]
					  userRoles       UserRole[]
					}
					
					model Permission {
					  id              String           @id @default(cuid())
					  name            String           @unique
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
					  id           String               @id @default(cuid())
					  code         String               @unique
					  name         String
					  monthlyPrice Int
					  subscriptions TenantSubscription[]
					}
					
					model TenantSubscription {
					  tenantId  String @id
					  planCode  String
					  status    String
					  createdAt DateTime @default(now())
					  updatedAt DateTime @updatedAt
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
					
					""");
		}
		return builder.toString();
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
