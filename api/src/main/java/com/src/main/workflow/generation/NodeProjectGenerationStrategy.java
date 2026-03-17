package com.src.main.workflow.generation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.exception.GenericException;
import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.service.ProjectEventStreamService;
import com.src.main.sm.config.NodeState;
import com.src.main.sm.config.NodeStepExecutorFactory;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ProjectRunStatus;
import com.src.main.workflow.ProjectArchiveService;

@Component
public class NodeProjectGenerationStrategy implements ProjectGenerationStrategy {

	private static final List<NodeState> WORKFLOW = List.of(
			NodeState.SCAFFOLD,
			NodeState.ENUM_GENERATION,
			NodeState.DTO_GENERATION,
			NodeState.MODEL_GENERATION,
			NodeState.REST_GENERATION,
			NodeState.APPLICATION_FILES,
			NodeState.DOCKER_GENERATION);

	private final NodeStepExecutorFactory executorFactory;
	private final ProjectRunRepository runRepository;
	private final ProjectEventStreamService projectEventStreamService;
	private final ProjectArchiveService projectArchiveService;

	public NodeProjectGenerationStrategy(
			NodeStepExecutorFactory executorFactory,
			ProjectRunRepository runRepository,
			ProjectEventStreamService projectEventStreamService,
			ProjectArchiveService projectArchiveService) {
		this.executorFactory = executorFactory;
		this.runRepository = runRepository;
		this.projectEventStreamService = projectEventStreamService;
		this.projectArchiveService = projectArchiveService;
	}

	@Override
	public boolean supports(GenerationLanguage language) {
		return language == GenerationLanguage.NODE;
	}

	@Override
	public byte[] generatePreviewZip(Map<String, Object> yaml, Map<String, Object> app) {
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("project_view_node_");
			DefaultExtendedState state = new DefaultExtendedState();
			populatePreviewVariables(state.getVariables(), tempDir, yaml, app);
			runWorkflow(null, state);
			return projectArchiveService.zipDirectory(tempDir);
		} catch (GenericException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		} finally {
			projectArchiveService.deleteDirectoryQuietly(tempDir);
		}
	}

	@Override
	public void run(ProjectRunEntity run, ProjectEntity project, Map<String, Object> yaml) {
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("node_gen_");
			DefaultExtendedState state = new DefaultExtendedState();
			populateProjectVariables(state.getVariables(), tempDir, project, yaml);
			runWorkflow(run, state);
			byte[] zipData = projectArchiveService.zipDirectory(tempDir);
			run.setZip(zipData);
			run.setStatus(ProjectRunStatus.SUCCESS);
			run.setErrorMessage(null);
			runRepository.saveAndFlush(run);
			projectEventStreamService.publish(run.getProject().getId(), "generation", Map.of(
					"projectId", run.getProject().getId().toString(),
					"runId", run.getId().toString(),
					"status", "SUCCESS",
					"fileName", run.getProject().getArtifact() + ".zip",
					"hasZip", true));
		} catch (Exception ex) {
			run.setStatus(ProjectRunStatus.ERROR);
			run.setErrorMessage(ex.getMessage());
			runRepository.saveAndFlush(run);
			projectEventStreamService.publish(run.getProject().getId(), "generation", Map.of(
					"projectId", run.getProject().getId().toString(),
					"runId", run.getId().toString(),
					"status", "ERROR",
					"hasZip", false,
					"message", ex.getMessage() == null ? "Generation failed." : ex.getMessage()));
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		} finally {
			projectArchiveService.deleteDirectoryQuietly(tempDir);
		}
	}

	private void runWorkflow(ProjectRunEntity run, DefaultExtendedState state) {
		for (NodeState nodeState : WORKFLOW) {
			publishStage(run, nodeState, "INPROGRESS", null);
			var result = executeStep(nodeState, state);
			if (!result.isSuccess()) {
				publishStage(run, nodeState, "ERROR", result.getMessage());
				throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, result.getMessage());
			}
			publishStage(run, nodeState, "DONE", result.getMessage());
			if (result.getDetails() != null) {
				state.getVariables().putAll(result.getDetails());
			}
		}
	}

	private com.src.main.dto.StepResult executeStep(NodeState nodeState, DefaultExtendedState state) {
		try {
			return executorFactory.forState(nodeState).execute(state);
		} catch (Exception ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	private void publishStage(ProjectRunEntity run, NodeState nodeState, String status, String message) {
		if (run == null || run.getProject() == null || nodeState == null) {
			return;
		}
		projectEventStreamService.publish(run.getProject().getId(), "stage", Map.of(
				"projectId", run.getProject().getId().toString(),
				"runId", run.getId().toString(),
				"stage", nodeState.name(),
				"status", status,
				"message", message == null ? "" : message,
				"timestamp", OffsetDateTime.now().toString()));
	}

	private void populatePreviewVariables(Map<Object, Object> variables, Path tempDir, Map<String, Object> yaml, Map<String, Object> app) {
		variables.put(ProjectMetaDataConstants.ROOT_DIR, tempDir.toString());
		variables.put(ProjectMetaDataConstants.YAML, yaml);
		variables.put(ProjectMetaDataConstants.ARTIFACT_ID, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT)));
		variables.put(ProjectMetaDataConstants.GROUP_ID, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP)));
		variables.put(ProjectMetaDataConstants.VERSION, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION)));
		variables.put(ProjectMetaDataConstants.NAME, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME)));
		variables.put(ProjectMetaDataConstants.DESCRIPTION, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.DESCRIPTION, ProjectMetaDataConstants.DEFAULT_DESCRIPTION)));
		variables.put(ProjectMetaDataConstants.GENERATOR, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GENERATOR, "node")));
		variables.put(ProjectMetaDataConstants.JDK_VERSION, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.JDK_VERSION, ProjectMetaDataConstants.DEFAULT_JDK)));
	}

	private void populateProjectVariables(Map<Object, Object> variables, Path tempDir, ProjectEntity project, Map<String, Object> yaml) {
		variables.put(ProjectMetaDataConstants.ROOT_DIR, tempDir.toString());
		variables.put(ProjectMetaDataConstants.YAML, yaml);
		variables.put(ProjectMetaDataConstants.ARTIFACT_ID, project.getArtifact());
		variables.put(ProjectMetaDataConstants.GROUP_ID, project.getGroupId());
		variables.put(ProjectMetaDataConstants.VERSION, project.getVersion());
		variables.put(ProjectMetaDataConstants.NAME, project.getName());
		variables.put(ProjectMetaDataConstants.DESCRIPTION, project.getDescription());
		variables.put(ProjectMetaDataConstants.GENERATOR, project.getGenerator());
		variables.put(ProjectMetaDataConstants.JDK_VERSION, project.getJdkVersion());
	}
}
