package com.src.main.service;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.utils.AppConstants;
import com.src.main.utils.ProjectMetaDataConstants;

/**
 * State executor for MODEL_GENERATION step.
 * Reads the same YAML used for DTOs and generates JPA model classes.
 */
@Component
public class ModelGenerationExecutor implements StepExecutor {

    private final TemplateEngine tpl;

    public ModelGenerationExecutor(TemplateEngine tpl) {
        this.tpl = tpl;
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
            ModelGenerator generator = new ModelGenerator(tpl, basePkg);
            generator.generate(yaml, root);
            Map<String, Object> output = Map.of("status", "Success");
    		return StepResult.ok(output);
        } catch (Exception ex) {
            return StepResult.error("MODEL_GENERATION",ex.getMessage());
        }
    }
}
