package com.src.main.workflow;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;
import com.src.main.workflow.engine.WorkflowEngineService;

@Component
public class ProjectQueueWorker {

	private static final Logger log = LoggerFactory.getLogger(ProjectQueueWorker.class);

	private static final int BATCH_SIZE = 10;

	private final ProjectRunRepository projectRunRepository;
	private final ProjectWorkflowService workflowService;
	private final WorkflowEngineService workflowEngineService;

	public ProjectQueueWorker(
			ProjectRunRepository projectRunRepository,
			ProjectWorkflowService workflowService,
			WorkflowEngineService workflowEngineService) {
		this.projectRunRepository = projectRunRepository;
		this.workflowService = workflowService;
		this.workflowEngineService = workflowEngineService;
	}

	@Transactional
	@Scheduled(fixedDelay = 2000)
	public void pollQueue() {
		try {
			List<UUID> picked = pickAndSubmitBatch();
			if (!picked.isEmpty()) {
				log.info("Picked {} projects from DB queue: {}", picked.size(), picked);
			}
		} catch (Exception ex) {
			log.error("Error while polling project queue", ex);
		}
	}

	protected List<UUID> pickAndSubmitBatch() {
		List<ProjectRunEntity> queued = projectRunRepository.findNextBatchForProcessing(
				ProjectRunStatus.QUEUED.name(),
				ProjectRunType.GENERATE_CODE.name(),
				BATCH_SIZE);
		if (queued.isEmpty()) {
			return List.of();
		}
		for (ProjectRunEntity run : queued) {
			UUID runId = run.getId();
			try {
				@SuppressWarnings("unchecked")
				GenerationLanguage language = GenerationLanguageResolver
						.resolveFromYaml((java.util.Map<String, Object>) new Yaml().load(run.getProject().getYaml()));
				workflowEngineService.dispatch(language, () -> workflowService.runFullWorkflow(run));
				run.setStatus(ProjectRunStatus.INPROGRESS);
			} catch (RuntimeException ex) {
				log.warn("Executor busy, could not submit run {}: {}", runId, ex.getMessage());
			}
		}
		return queued.stream().map(ProjectRunEntity::getId).toList();
	}
}
