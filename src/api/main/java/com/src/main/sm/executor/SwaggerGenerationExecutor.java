package com.src.main.sm.executor;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.swagger.SwaggerGenerationService;
import com.src.main.sm.executor.swagger.SwaggerGenerationSupport;
import com.src.main.sm.executor.swagger.SwaggerGroupSpec;
import com.src.main.util.PathUtils;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class SwaggerGenerationExecutor implements StepExecutor {

	private final SwaggerGenerationService swaggerGenerationService;
	private final ObjectMapper mapper = new ObjectMapper();

	public SwaggerGenerationExecutor(SwaggerGenerationService swaggerGenerationService) {
		this.swaggerGenerationService = swaggerGenerationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("SWAGGER_GENERATION", "YAML not found in extended state.");
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			Object enabledRaw = firstNonNull(yaml.get("enableOpenapi"), spec.getEnableOpenapi(),
					yaml.get(ProjectMetaDataConstants.EXTRAS_OPENAPI), false);
			String basePackage = StringUtils.firstNonBlank(str(yaml.get("basePackage")), spec.getBasePackage(),
					(String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID), ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), spec.getPackages(),
					"technical");
			String swaggerPackage = SwaggerGenerationSupport.resolveSwaggerPackage(basePackage, packageStructure);
			if (!SwaggerGenerationSupport.isOpenApiEnabled(enabledRaw)) {
				deleteOpenApiConfigIfExists(root, swaggerPackage);
				return StepResult.ok(Map.of("status", "Success", "swaggerGenerated", false));
			}

			String appName = extractAppName(yaml);
			List<SwaggerGroupSpec> groups = SwaggerGenerationSupport.buildGroupsFromYaml(yaml, spec.getModels());
			if (groups.isEmpty()) {
				deleteOpenApiConfigIfExists(root, swaggerPackage);
				return StepResult.ok(Map.of("status", "Success", "swaggerGenerated", false, "swaggerGroupCount", 0));
			}

			swaggerGenerationService.generate(root, swaggerPackage, appName, groups);
			return StepResult.ok(Map.of("status", "Success", "swaggerGenerated", true, "swaggerGroupCount", groups.size()));
		} catch (Exception ex) {
			return StepResult.error("SWAGGER_GENERATION", ex.getMessage());
		}
	}

	private void deleteOpenApiConfigIfExists(Path root, String swaggerPackage) {
		try {
			Path file = root.resolve("src/main/java")
					.resolve(PathUtils.javaSrcPathFromPackage(swaggerPackage))
					.resolve("OpenApiConfig.java");
			Files.deleteIfExists(file);
		} catch (Exception ignored) {
			// Best-effort cleanup of stale swagger config from earlier generations.
		}
	}

	@SuppressWarnings("unchecked")
	private String extractAppName(Map<String, Object> yaml) {
		if (yaml.get("app") instanceof Map<?, ?> appRaw) {
			Map<String, Object> app = (Map<String, Object>) appRaw;
			return StringUtils.firstNonBlank(str(app.get("name")), "Generated API");
		}
		return "Generated API";
	}

	private static Object firstNonNull(Object... values) {
		return java.util.Arrays.stream(values).filter(java.util.Objects::nonNull).findFirst().orElse(null);
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
