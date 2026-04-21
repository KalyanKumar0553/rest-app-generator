package com.src.main.sm.executor.node;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.util.ShippableModuleSupport;

@Component("nodeScaffoldExecutor")
public class NodeScaffoldExecutor implements StepExecutor {

	private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState state) {
		try {
			NodeProjectContext context = NodeGenerationSupport.resolveContext(state);
			Map<String, Object> yaml = (Map<String, Object>) state.getVariables().get("yaml");
			List<String> selectedModules = NodeGenerationSupport.extractSelectedShippedModules(yaml);
			boolean includePrisma = "prisma".equals(context.orm()) || ShippableModuleSupport.requiresNodePrisma(selectedModules);
			boolean useSequelize = "sequelize".equals(context.orm());

			NodeGenerationSupport.writeFile(context.root(), "package.json",
					renderPackageJson(context, selectedModules, includePrisma, useSequelize));
			NodeGenerationSupport.writeFile(context.root(), "tsconfig.json", renderTsConfig(selectedModules));
			NodeGenerationSupport.writeFile(context.root(), ".gitignore", renderGitIgnore());
			NodeGenerationSupport.writeFile(context.root(), "README.md", renderReadme(context, selectedModules, includePrisma, useSequelize));
			copyShippedModules(context.root(), selectedModules);
			return NodeGenerationSupport.success("Node scaffold generated");
		} catch (Exception ex) {
			return StepResult.error("NODE_SCAFFOLD", ex.getMessage());
		}
	}

	private String renderPackageJson(NodeProjectContext context, List<String> selectedModules, boolean includePrisma, boolean useSequelize) {
		Map<String, String> dependencies = new LinkedHashMap<>();
		dependencies.put("cors", "^2.8.5");
		dependencies.put("dotenv", "^16.4.7");
		dependencies.put("express", "^4.21.2");
		dependencies.put("zod", "^3.24.1");

		Map<String, String> devDependencies = new LinkedHashMap<>();
		devDependencies.put("@types/cors", "^2.8.17");
		devDependencies.put("@types/express", "^5.0.1");
		devDependencies.put("@types/node", "^22.10.2");
		devDependencies.put("tsx", "^4.19.2");
		devDependencies.put("typescript", "^5.7.2");
		if (includePrisma) {
			dependencies.putIfAbsent("@prisma/client", "^6.5.0");
			devDependencies.putIfAbsent("prisma", "^6.5.0");
		}
		if (useSequelize) {
			dependencies.putIfAbsent("sequelize", "^6.37.5");
			dependencies.putIfAbsent("pg", "^8.13.1");
		}

		for (ShippableModuleSupport.NodePackageDependency dependency : ShippableModuleSupport
				.resolveNodePackageDependencies(selectedModules)) {
			if (dependency.devDependency()) {
				devDependencies.putIfAbsent(dependency.packageName(), dependency.version());
			} else {
				dependencies.putIfAbsent(dependency.packageName(), dependency.version());
			}
		}

		String dependenciesJson = renderJsonSection(dependencies, 4);
		String devDependenciesJson = renderJsonSection(devDependencies, 4);
		String prismaScripts = includePrisma
				? """
				    "prisma:generate": "prisma generate",
				    "prisma:migrate": "prisma migrate dev",
				"""
				: "";
		String sequelizeScripts = useSequelize
				? """
				    "db:sync": "tsx src/scripts/sync-db.ts",
				"""
				: "";
		return """
				{
				  "name": "%s",
				  "version": "%s",
				  "private": true,
				  "description": "%s",
				  "scripts": {
				    "dev": "tsx watch src/main.ts",
				    "start": "tsx src/main.ts",
				%s%s    "build": "tsc -p tsconfig.json"
				  },
				  "dependencies": {
				%s
				  },
				  "devDependencies": {
				%s
				  }
				}
				""".formatted(escapeJson(context.artifactId()), escapeJson(context.version()),
				escapeJson(context.description()), prismaScripts, sequelizeScripts, dependenciesJson, devDependenciesJson);
	}

	private String renderTsConfig(List<String> selectedModules) {
		boolean hasModules = !ShippableModuleSupport.expandSelectedModules(selectedModules, GenerationLanguage.NODE).isEmpty();
		String includeBlock = hasModules
				? """
				  "include": ["src/**/*.ts", "modules/**/*.ts"],
				"""
				: """
				  "include": ["src/**/*.ts"],
				""";
		String rootDir = hasModules ? "." : "src";
		return """
				{
				  "compilerOptions": {
				    "target": "ES2022",
				    "module": "CommonJS",
				    "moduleResolution": "Node",
				    "rootDir": "%s",
				    "outDir": "dist",
				    "strict": true,
				    "esModuleInterop": true,
				    "forceConsistentCasingInFileNames": true,
				    "skipLibCheck": true,
				    "resolveJsonModule": true
				  },
				%s  "exclude": ["dist", "node_modules"]
				}
				""".formatted(rootDir, includeBlock);
	}

	private String renderGitIgnore() {
		return """
				node_modules
				dist
				.env
				""";
	}

	private String renderReadme(NodeProjectContext context, List<String> selectedModules, boolean includePrisma, boolean useSequelize) {
		String modulesSection = selectedModules.isEmpty()
				? ""
				: """

				## Included Modules

				%s
				"""
						.formatted(selectedModules.stream().map(module -> "- `" + module + "`")
								.collect(java.util.stream.Collectors.joining("\n")));
		String prismaSection = includePrisma
				? """

				## Database

				```bash
				cp .env.example .env
				%s prisma:migrate
				%s prisma:generate
				```
				"""
						.formatted(context.packageManager(), context.packageManager())
				: "";
		String sequelizeSection = useSequelize
				? """

				## Database

				```bash
				cp .env.example .env
				%s db:sync
				```
				"""
						.formatted(context.packageManager())
				: "";
		return """
				# %s
				
				%s
				
				## Run
				
				```bash
				%s install
				%s start
				```
				
				The server starts on port `%d`.%s%s%s
				""".formatted(context.appName(), context.description(), context.packageManager(), context.packageManager(),
				context.port(), modulesSection, prismaSection, sequelizeSection);
	}

	private void copyShippedModules(Path root, List<String> selectedModules) throws Exception {
		List<String> shippedModules = ShippableModuleSupport.expandSelectedModules(selectedModules, GenerationLanguage.NODE);
		for (String moduleId : shippedModules) {
			Resource[] resources = resourceResolver.getResources(
					ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
							+ ShippableModuleSupport.resourceSearchPattern(moduleId, GenerationLanguage.NODE));
			String prefix = ShippableModuleSupport.resourcePrefix(moduleId, GenerationLanguage.NODE) + "/";
			for (Resource resource : resources) {
				if (!resource.exists() || !resource.isReadable()) {
					continue;
				}
				String externalForm = resource.getURL().toExternalForm();
				int prefixIndex = externalForm.indexOf(prefix);
				if (prefixIndex < 0) {
					continue;
				}
				String relativePath = externalForm.substring(prefixIndex + prefix.length());
				if (relativePath.isBlank() || relativePath.endsWith("/")) {
					continue;
				}
				Path target = root.resolve("modules").resolve(moduleId).resolve(relativePath);
				Files.createDirectories(target.getParent());
				try (InputStream input = resource.getInputStream()) {
					Files.copy(input, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	private String renderJsonSection(Map<String, String> values, int indentSpaces) {
		String indent = " ".repeat(indentSpaces);
		return values.entrySet().stream()
				.map(entry -> indent + "\"%s\": \"%s\"".formatted(escapeJson(entry.getKey()), escapeJson(entry.getValue())))
				.collect(java.util.stream.Collectors.joining(",\n"));
	}

	private String escapeJson(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
