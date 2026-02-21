package com.src.main.sm.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.ActuatorConfigurationExecutor;
import com.src.main.sm.executor.ApplicationFileGenerationExecutor;
import com.src.main.sm.executor.CrudGenerationExecutor;
import com.src.main.sm.executor.DockerGenerationExecutor;
import com.src.main.sm.executor.DtoGenerationExecutor;
import com.src.main.sm.executor.EnumGenerationExecutor;
import com.src.main.sm.executor.ModelGenerationExecutor;
import com.src.main.sm.executor.RestGenerationExecutor;
import com.src.main.sm.executor.ScaffoldExecutor;
import com.src.main.sm.executor.SwaggerGenerationExecutor;

@Component
public class StepExecutorFactory {

 private final Map<States, StepExecutor> registry = new EnumMap<>(States.class);

	 public StepExecutorFactory(DtoGenerationExecutor dtoGenerationExecutor, EnumGenerationExecutor enumGenerationExecutor,
			 ScaffoldExecutor scaffoldExecutor,
			 ApplicationFileGenerationExecutor appFileExecutor,
			 ModelGenerationExecutor modelExecutor,
			 SwaggerGenerationExecutor swaggerGenerationExecutor,
			 DockerGenerationExecutor dockerGenerationExecutor,
			 RestGenerationExecutor restGenerationExecutor,
			 CrudGenerationExecutor crudGenerationExecutor,
			 ActuatorConfigurationExecutor actuatorConfigurationExecutor
			 ) {
     registry.put(States.DTO_GENERATION, dtoGenerationExecutor);
     registry.put(States.ENUM_GENERATION, enumGenerationExecutor);
     registry.put(States.SCAFFOLD, scaffoldExecutor);
     registry.put(States.APPLICATION_FILES, appFileExecutor);
     registry.put(States.DOCKER_GENERATION, dockerGenerationExecutor);
     registry.put(States.MODEL_GENERATION, modelExecutor);
	     registry.put(States.SWAGGER_GENERATION, swaggerGenerationExecutor);
	     registry.put(States.REST_GENERATION, restGenerationExecutor);
	     registry.put(States.CRUD_GENERATION, crudGenerationExecutor);
	     registry.put(States.ACTUATOR_CONFIGURATION, actuatorConfigurationExecutor);
	 }

 public StepExecutor forState(States state) {
     StepExecutor ex = registry.get(state);
     if (ex == null) {
         throw new IllegalArgumentException("No executor registered for state: " + state);
     }
     return ex;
 }
}
