package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.dto.DtoGenerationService;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class DtoGenerationExecutor implements StepExecutor {

	private final DtoGenerationService dtoGenerationService;

	public DtoGenerationExecutor(DtoGenerationService dtoGenerationService) {
		this.dtoGenerationService = dtoGenerationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			String groupId = (String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID);
			String artifact = (String) data.getVariables().get(ProjectMetaDataConstants.ARTIFACT_ID);
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);

			if (yaml == null) {
				return StepResult.error("DTO_GENERATION", "YAML not found in extended state.");
			}

			dtoGenerationService.generate(root, yaml, groupId, artifact);
			return StepResult.ok(Map.of("status", "Success"));
		} catch (Exception ex) {
			return StepResult.error("DTO_GENERATION", ex.getMessage());
		}
	}
}
