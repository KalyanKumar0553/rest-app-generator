package com.src.main.sm.executor.model;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;

@Service
public class ModelGenerationService {

	private final TemplateEngine templateEngine;

	public ModelGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Map<String, Object> yaml, Path root, String basePackage, GenerationLanguage language) throws Exception {
		ModelGenerator generator = new ModelGenerator(templateEngine, basePackage, language);
		generator.generate(yaml, root);
	}
}
