package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.LayeredSpecSupport;
import com.src.main.sm.executor.docker.DockerGenerationService;
import com.src.main.sm.executor.docker.DockerGenerationSupport;
import com.src.main.util.ProjectMetaDataConstants;

@Component("dockerGenerationExecutor")
public class DockerGenerationExecutor implements StepExecutor {

	private final DockerGenerationService dockerGenerationService;
	private final ObjectMapper mapper = new ObjectMapper();

	public DockerGenerationExecutor(DockerGenerationService dockerGenerationService) {
		this.dockerGenerationService = dockerGenerationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("DOCKER_GENERATION", "YAML not found in extended state.");
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			Object enabledRaw = firstNonNull(yaml.get("useDockerCompose"), spec.getUseDockerCompose(),
					yaml.get(ProjectMetaDataConstants.EXTRAS_DOCKER_COMPOSE), extractPreferenceValue(yaml, "useDockerCompose"), false);
			if (!DockerGenerationSupport.isDockerComposeEnabled(enabledRaw)) {
				return StepResult.ok(Map.of("status", "Success", "dockerGenerated", false));
			}

			String artifactId = resolveArtifactId(data, yaml);
			String buildTool = resolveBuildTool(data, yaml);
			String database = resolveDatabase(yaml);
			String serviceName = DockerGenerationSupport.toServiceName(artifactId);

			dockerGenerationService.generate(root, artifactId, serviceName, buildTool, database);
			return StepResult.ok(Map.of("status", "Success", "dockerGenerated", true));
		} catch (Exception ex) {
			return StepResult.error("DOCKER_GENERATION", ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private static Object extractPreferenceValue(Map<String, Object> yaml, String key) {
		if (yaml.get("preferences") instanceof Map<?, ?> preferencesRaw) {
			return ((Map<String, Object>) preferencesRaw).get(key);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static String resolveArtifactId(ExtendedState data, Map<String, Object> yaml) {
		return StringUtils.firstNonBlank(
				LayeredSpecSupport.resolveArtifactId(yaml, null),
				str(data.getVariables().get(ProjectMetaDataConstants.ARTIFACT_ID)),
				ProjectMetaDataConstants.DEFAULT_ARTIFACT);
	}

	private static String resolveBuildTool(ExtendedState data, Map<String, Object> yaml) {
		return StringUtils.firstNonBlank(
				LayeredSpecSupport.resolveBuildTool(yaml, null),
				str(data.getVariables().get(ProjectMetaDataConstants.BUILD_TOOL)),
				ProjectMetaDataConstants.DEFAULT_BUILD_TOOL);
	}

	private static String resolveDatabase(Map<String, Object> yaml) {
		return LayeredSpecSupport.resolveDatabaseCode(yaml);
	}

	private static Object firstNonNull(Object... values) {
		return java.util.Arrays.stream(values).filter(java.util.Objects::nonNull).findFirst().orElse(null);
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
