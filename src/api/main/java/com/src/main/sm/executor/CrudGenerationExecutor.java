package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.crud.CrudGenerationService;
import com.src.main.sm.executor.crud.CrudGenerationSupport;
import com.src.main.sm.executor.crud.CrudGenerationUnit;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class CrudGenerationExecutor implements StepExecutor {

	private final CrudGenerationService crudGenerationService;
	private final ObjectMapper mapper = new ObjectMapper();

	public CrudGenerationExecutor(CrudGenerationService crudGenerationService) {
		this.crudGenerationService = crudGenerationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("CRUD_GENERATION", "YAML not found in extended state.");
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			List<ModelSpecDTO> models = spec.getModels();
			if (models == null || models.isEmpty()) {
				return StepResult.ok(Map.of("status", "Success", "crudGeneratedCount", 0));
			}

			List<ModelSpecDTO> crudEnabledModels = models.stream()
					.filter(model -> Boolean.TRUE.equals(model.getAddCrudOperations()))
					.filter(model -> !Boolean.TRUE.equals(model.getAddRestEndpoints()))
					.toList();
			if (crudEnabledModels.isEmpty()) {
				return StepResult.ok(Map.of("status", "Success", "crudGeneratedCount", 0));
			}

			String basePackage = StringUtils.firstNonBlank(str(yaml.get("basePackage")), spec.getBasePackage(),
					(String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID),
					ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), spec.getPackages(),
					"technical");

			List<CrudGenerationUnit> units = crudEnabledModels.stream()
					.map(model -> CrudGenerationSupport.buildUnit(model, basePackage, packageStructure))
					.toList();
			crudGenerationService.generate(root, units);
			return StepResult.ok(Map.of("status", "Success", "crudGeneratedCount", units.size()));
		} catch (Exception ex) {
			return StepResult.error("CRUD_GENERATION", ex.getMessage());
		}
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}
}
