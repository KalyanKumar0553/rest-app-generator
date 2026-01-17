package com.src.main.workflow;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

@Component
public class ProjectQueueWorker {

	private static final Logger log = LoggerFactory.getLogger(ProjectQueueWorker.class);

	private static final int BATCH_SIZE = 10;

	private final ProjectRunRepository projectRunRepository;
	private final TaskExecutor projectExecutor;
	private final ProjectWorkflowService workflowService;

	public ProjectQueueWorker(
			ProjectRunRepository projectRunRepository,
			@Qualifier("projectTaskExecutor") TaskExecutor projectExecutor,
			ProjectWorkflowService workflowService) {
		this.projectRunRepository = projectRunRepository;
		this.projectExecutor = projectExecutor;
		this.workflowService = workflowService;
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
		List<ProjectRunEntity> queued = projectRunRepository.findNextBatchForProcessing(ProjectRunStatus.QUEUED,ProjectRunType.GENERATE_CODE, PageRequest.of(0, BATCH_SIZE));
		if (queued.isEmpty()) {
			return List.of();
		}
		for (ProjectRunEntity run : queued) {
			UUID runId = run.getId();
			try {
				projectExecutor.execute(() -> workflowService.runFullWorkflow(run));
				run.setStatus(ProjectRunStatus.INPROGRESS);
			} catch (RuntimeException ex) {
				log.warn("Executor busy, could not submit run {}: {}", runId, ex.getMessage());
			}
		}
		return queued.stream().map(ProjectRunEntity::getId).toList();
	}
}
