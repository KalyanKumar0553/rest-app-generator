package com.src.main.workflow.generation;

import java.util.Map;

import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.sm.executor.common.GenerationLanguage;

public interface ProjectGenerationStrategy {

	boolean supports(GenerationLanguage language);

	byte[] generatePreviewZip(Map<String, Object> yaml, Map<String, Object> app);

	void run(ProjectRunEntity run, ProjectEntity project, Map<String, Object> yaml);
}
