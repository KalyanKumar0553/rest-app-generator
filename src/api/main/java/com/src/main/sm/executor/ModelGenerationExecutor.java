package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.model.ModelGenerationService;
import com.src.main.util.ProjectMetaDataConstants;

/**
 * State executor for MODEL_GENERATION step.
 * Reads the same YAML used for DTOs and generates JPA model classes.
 */
@Component
public class ModelGenerationExecutor implements StepExecutor {

    private final ModelGenerationService modelGenerationService;

    public ModelGenerationExecutor(ModelGenerationService modelGenerationService) {
        this.modelGenerationService = modelGenerationService;
    }

	private static String str(Object o) {
		return (o == null) ? null : String.valueOf(o);
	}

    @Override
    @SuppressWarnings("unchecked")
    public StepResult execute(ExtendedState data) {
        try {
    		Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
    		Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get("yaml");
    		String basePkg = (yaml != null) ? str(yaml.get("basePackage")) : null;
			GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);
            modelGenerationService.generate(yaml, root, basePkg, language);
            Map<String, Object> output = Map.of("status", "Success");
    		return StepResult.ok(output);
        } catch (Exception ex) {
            return StepResult.error("MODEL_GENERATION",ex.getMessage());
        }
    }
}
