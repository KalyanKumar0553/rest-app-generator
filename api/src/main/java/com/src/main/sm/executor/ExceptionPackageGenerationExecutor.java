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
import com.src.main.sm.executor.common.BoilerplateStyle;
import com.src.main.sm.executor.common.BoilerplateStyleResolver;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.exceptiongen.ExceptionPackageGenerationService;
import com.src.main.sm.executor.exceptiongen.ExceptionPackageGenerationSupport;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class ExceptionPackageGenerationExecutor implements StepExecutor {

	private final ExceptionPackageGenerationService exceptionPackageGenerationService;
	private final ObjectMapper mapper = new ObjectMapper();

	public ExceptionPackageGenerationExecutor(ExceptionPackageGenerationService exceptionPackageGenerationService) {
		this.exceptionPackageGenerationService = exceptionPackageGenerationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("EXCEPTION_PACKAGE_GENERATION", "YAML not found in extended state.");
			}

			if (!ExceptionPackageGenerationSupport.isExceptionPackageRequired(yaml)) {
				return StepResult.ok(Map.of("status", "Success", "exceptionPackageGenerated", false));
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			String basePackage = StringUtils.firstNonBlank(
					str(yaml.get("basePackage")),
					spec.getBasePackage(),
					(String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID),
					ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), spec.getPackages(), "technical");
			String exceptionPackage = ExceptionPackageGenerationSupport.resolveExceptionPackage(basePackage, packageStructure);
			boolean useLombok = BoilerplateStyleResolver.resolveFromYaml(yaml, true) == BoilerplateStyle.LOMBOK;
			GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);

			exceptionPackageGenerationService.generate(root, exceptionPackage, useLombok, language);
			return StepResult.ok(Map.of(
					"status", "Success",
					"exceptionPackageGenerated", true,
					"exceptionPackage", exceptionPackage,
					"exceptionWithLombok", useLombok));
		} catch (Exception ex) {
			return StepResult.error("EXCEPTION_PACKAGE_GENERATION", ex.getMessage());
		}
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
