package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.enumgen.EnumGenerationService;
import com.src.main.sm.executor.enumgen.EnumGenerationSupport;
import com.src.main.sm.executor.enumgen.EnumSpecResolved;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class EnumGenerationExecutor implements StepExecutor {

	private final EnumGenerationService enumGenerationService;
	private final ObjectMapper mapper = new ObjectMapper();

	public EnumGenerationExecutor(EnumGenerationService enumGenerationService) {
		this.enumGenerationService = enumGenerationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("ENUM_GENERATION", "YAML not found in extended state.");
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			List<EnumSpecResolved> enums = EnumGenerationSupport.resolveEnums(spec.getEnums());
			if (enums.isEmpty()) {
				return StepResult.ok(Map.of("status", "Success", "enumGenerated", false, "enumCount", 0));
			}

			String basePackage = StringUtils.firstNonBlank(str(yaml.get("basePackage")), spec.getBasePackage(),
					(String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID), ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), spec.getPackages(), "technical");
			String enumPackage = EnumGenerationSupport.resolveEnumPackage(basePackage, packageStructure);
			GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);

			enumGenerationService.generate(root, enumPackage, enums, language);
			return StepResult.ok(Map.of("status", "Success", "enumGenerated", true, "enumCount", enums.size()));
		} catch (Exception ex) {
			return StepResult.error("ENUM_GENERATION", ex.getMessage());
		}
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
