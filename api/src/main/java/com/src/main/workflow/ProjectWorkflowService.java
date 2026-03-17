package com.src.main.workflow;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.service.ProjectEventStreamService;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.util.ProjectRunStatus;
import com.src.main.workflow.generation.ProjectGenerationStrategySelector;

@Service
public class ProjectWorkflowService {

	private static final Logger log = LoggerFactory.getLogger(ProjectWorkflowService.class);

	private final ProjectRunRepository runRepository;
	private final ProjectEventStreamService projectEventStreamService;
	private final ProjectGenerationStrategySelector projectGenerationStrategySelector;

	public ProjectWorkflowService(
			ProjectRunRepository runRepository,
			ProjectEventStreamService projectEventStreamService,
			ProjectGenerationStrategySelector projectGenerationStrategySelector) {
		this.runRepository = runRepository;
		this.projectEventStreamService = projectEventStreamService;
		this.projectGenerationStrategySelector = projectGenerationStrategySelector;
	}
	
	public void runFullWorkflow(ProjectRunEntity run){
		try {
			log.info("Starting full workflow for run {}", run.getId());
			runStateMachine(run);
			log.info("Completed full workflow for run {}", run.getId());
        } catch (Exception ex) {
            log.error("Workflow failed for project {} with runID {}", run.getProject().getId(),run.getId(), ex);
			run.setStatus(ProjectRunStatus.ERROR);
			run.setErrorMessage(ex.getMessage());
			runRepository.saveAndFlush(run);
			projectEventStreamService.publish(run.getProject().getId(), "generation",
					Map.of("projectId", run.getProject().getId().toString(), "runId", run.getId().toString(), "status", "ERROR",
							"hasZip", false,
							"message", ex.getMessage() == null ? "Generation failed." : ex.getMessage()));
        }
	}

	private void runStateMachine(ProjectRunEntity run) throws Exception {
		Map<String, Object> yaml = (Map<String, Object>) new Yaml().load(run.getProject().getYaml());
		run(run,run.getProject(), yaml);
	}
	
	@SuppressWarnings("unchecked")
	public void run(ProjectRunEntity run, ProjectEntity project, Map<String, Object> yaml) throws IOException {
		GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);
		projectGenerationStrategySelector.select(language).run(run, project, yaml);
	}
	

    @Transactional
    protected void markCompleted(ProjectRunEntity run) {
        run.setStatus(ProjectRunStatus.DONE);
        run.setErrorMessage(null);
        runRepository.saveAndFlush(run);
    }

    @Transactional
    protected void markFailed(ProjectRunEntity run,String reason) {
    	run.setStatus(ProjectRunStatus.ERROR);
    	run.setErrorMessage(reason);
    	runRepository.saveAndFlush(run);
    }
}
