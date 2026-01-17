package com.src.main.sm.config;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.ApplicationFileGenerationExecutor;
import com.src.main.sm.executor.DtoGenerationExecutor;
import com.src.main.sm.executor.ModelGenerationExecutor;
import com.src.main.sm.executor.ScaffoldExecutor;

@Component
public class StepExecutorFactory {

 private final Map<States, StepExecutor> registry = new EnumMap<>(States.class);

 public StepExecutorFactory(DtoGenerationExecutor dtoGenerationExecutor,ScaffoldExecutor scaffoldExecutor,
		 ApplicationFileGenerationExecutor appFileExecutor,
		 ModelGenerationExecutor modelExecutor
		 ) {
     registry.put(States.DTO_GENERATION, dtoGenerationExecutor);
     registry.put(States.SCAFFOLD, scaffoldExecutor);
     registry.put(States.APPLICATION_FILES, appFileExecutor);
     registry.put(States.MODEL_GENERATION, modelExecutor);
 }

 public StepExecutor forState(States state) {
     StepExecutor ex = registry.get(state);
     if (ex == null) {
         throw new IllegalArgumentException("No executor registered for state: " + state);
     }
     return ex;
 }
}
