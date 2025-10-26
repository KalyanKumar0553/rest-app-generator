package com.src.main.service;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.States;

@Component
public class StepExecutorFactory {

 private final Map<States, StepExecutor> registry = new EnumMap<>(States.class);

 public StepExecutorFactory(DtoGenerationExecutor dtoGenerationExecutor,ScaffoldExecutor scaffoldExecutor,ApplicationFileGenerationExecutor appFileExecutor) {
     registry.put(States.DTO_GENERATION, dtoGenerationExecutor);
     registry.put(States.SCAFFOLD, scaffoldExecutor);
     registry.put(States.APPLICATION_FILES, appFileExecutor);
 }

 public StepExecutor forState(States state) {
     StepExecutor ex = registry.get(state);
     if (ex == null) {
         throw new IllegalArgumentException("No executor registered for state: " + state);
     }
     return ex;
 }
}
