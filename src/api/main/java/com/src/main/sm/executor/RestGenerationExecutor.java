package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.rest.RestControllerGenerator;
import com.src.main.sm.executor.rest.RestGenerationSupport;
import com.src.main.sm.executor.rest.RestGenerationUnit;
import com.src.main.sm.executor.rest.RestRepositoryGenerator;
import com.src.main.sm.executor.rest.RestSharedSupportGenerator;
import com.src.main.sm.executor.rest.RestServiceGenerator;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class RestGenerationExecutor implements StepExecutor {

	private final RestControllerGenerator controllerGenerator;
	private final RestServiceGenerator serviceGenerator;
	private final RestRepositoryGenerator repositoryGenerator;
	private final RestSharedSupportGenerator sharedSupportGenerator;
	private final ObjectMapper mapper = new ObjectMapper();

	public RestGenerationExecutor(RestControllerGenerator controllerGenerator, RestServiceGenerator serviceGenerator,
			RestRepositoryGenerator repositoryGenerator, RestSharedSupportGenerator sharedSupportGenerator) {
		this.controllerGenerator = controllerGenerator;
		this.serviceGenerator = serviceGenerator;
		this.repositoryGenerator = repositoryGenerator;
		this.sharedSupportGenerator = sharedSupportGenerator;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("REST_GENERATION", "YAML not found in extended state.");
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			List<ModelSpecDTO> models = spec.getModels();
			if (models == null || models.isEmpty()) {
				return StepResult.ok(Map.of("status", "Success", "restGeneratedCount", 0));
			}

			List<ModelSpecDTO> restEnabledModels = models.stream()
					.filter(model -> Boolean.TRUE.equals(model.getAddRestEndpoints()))
					.collect(Collectors.toList());
			if (restEnabledModels.isEmpty()) {
				return StepResult.ok(Map.of("status", "Success", "restGeneratedCount", 0));
			}

			String basePackage = StringUtils.firstNonBlank(str(yaml.get("basePackage")),
					(String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID),
					ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), spec.getPackages(), "technical");
			String utilPackage = RestGenerationSupport.resolveUtilPackage(basePackage, packageStructure);
			sharedSupportGenerator.generate(root, utilPackage);

			final int[] generatedCount = { 0 };
			try {
				restEnabledModels.forEach(model -> {
					try {
						RestGenerationUnit unit = RestGenerationSupport.buildUnit(model, basePackage, packageStructure);
						repositoryGenerator.generate(root, unit);
						serviceGenerator.generate(root, unit);
						controllerGenerator.generate(root, unit);
						generatedCount[0] += 1;
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				});
			} catch (RuntimeException ex) {
				if (ex.getCause() instanceof Exception cause) {
					throw cause;
				}
				throw ex;
			}

			return StepResult.ok(Map.of("status", "Success", "restGeneratedCount", generatedCount[0]));
		} catch (Exception ex) {
			return StepResult.error("REST_GENERATION", ex.getMessage());
		}
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
