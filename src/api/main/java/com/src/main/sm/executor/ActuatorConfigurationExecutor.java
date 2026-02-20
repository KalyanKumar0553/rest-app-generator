package com.src.main.sm.executor;

import java.util.List;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.actuator.ActuatorConfigurationService;
import com.src.main.sm.executor.actuator.ActuatorConfigurationSupport;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class ActuatorConfigurationExecutor implements StepExecutor {

	private final ActuatorConfigurationService actuatorConfigurationService;

	public ActuatorConfigurationExecutor(ActuatorConfigurationService actuatorConfigurationService) {
		this.actuatorConfigurationService = actuatorConfigurationService;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("ACTUATOR_CONFIGURATION", "YAML not found in extended state.");
			}
			if (!ActuatorConfigurationSupport.isActuatorEnabled(yaml)) {
				return StepResult.ok(Map.of("status", "Success", "actuatorConfigured", false));
			}

			List<String> includedEndpoints = ActuatorConfigurationSupport.resolveIncludedEndpoints(yaml);
			actuatorConfigurationService.applyConfiguration(yaml, includedEndpoints);
			return StepResult.ok(Map.of(
					"status", "Success",
					"actuatorConfigured", true,
					"actuatorEndpointCount", includedEndpoints.size()));
		} catch (Exception ex) {
			return StepResult.error("ACTUATOR_CONFIGURATION", ex.getMessage());
		}
	}
}
