package com.src.main.workflow.generation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.common.GenerationLanguage;

@Component
public class ProjectGenerationStrategySelector {

	private final List<ProjectGenerationStrategy> strategies;

	public ProjectGenerationStrategySelector(List<ProjectGenerationStrategy> strategies) {
		this.strategies = strategies;
	}

	public ProjectGenerationStrategy select(GenerationLanguage language) {
		return strategies.stream()
				.filter(strategy -> strategy.supports(language))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No generation strategy registered for language: " + language));
	}
}
