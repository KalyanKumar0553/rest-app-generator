package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.mapper.MapperGenerationService;
import com.src.main.sm.executor.mapper.MapperGenerationSupport;
import com.src.main.sm.executor.mapper.MapperGenerationUnit;
import com.src.main.util.ProjectMetaDataConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class MapperGenerationExecutor implements StepExecutor {

	private final MapperGenerationService mapperGenerationService;
	private final ObjectMapper mapper = new ObjectMapper();

	public MapperGenerationExecutor(MapperGenerationService mapperGenerationService) {
		this.mapperGenerationService = mapperGenerationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("MAPPER_GENERATION", "YAML not found in extended state.");
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			String basePackage = StringUtils.firstNonBlank(
					str(yaml.get("basePackage")),
					spec.getBasePackage(),
					(String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID),
					ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), spec.getPackages(), "technical");
			GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);
			List<MapperGenerationUnit> units = MapperGenerationSupport.resolveUnits(yaml, basePackage, packageStructure);
			if (units.isEmpty()) {
				return StepResult.ok(Map.of("status", "Success", "mapperGeneratedCount", 0));
			}

			int generated = mapperGenerationService.generate(root, units, language);
			return StepResult.ok(Map.of("status", "Success", "mapperGeneratedCount", generated));
		} catch (Exception ex) {
			return StepResult.error("MAPPER_GENERATION", ex.getMessage());
		}
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
