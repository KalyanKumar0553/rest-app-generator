package com.src.main.workflow.generation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import com.src.main.exception.GenericException;
import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.service.ProjectEventStreamService;
import com.src.main.sm.config.Events;
import com.src.main.sm.config.States;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ProjectRunStatus;
import com.src.main.workflow.ProjectArchiveService;

@Component
public class JavaProjectGenerationStrategy implements ProjectGenerationStrategy {

	private final StateMachineFactory<States, Events> stateMachineFactory;
	private final ProjectRunRepository runRepository;
	private final ProjectEventStreamService projectEventStreamService;
	private final ProjectArchiveService projectArchiveService;

	public JavaProjectGenerationStrategy(
			@Qualifier("stateMachineFactory") StateMachineFactory<States, Events> stateMachineFactory,
			ProjectRunRepository runRepository,
			ProjectEventStreamService projectEventStreamService,
			ProjectArchiveService projectArchiveService) {
		this.stateMachineFactory = stateMachineFactory;
		this.runRepository = runRepository;
		this.projectEventStreamService = projectEventStreamService;
		this.projectArchiveService = projectArchiveService;
	}

	@Override
	public boolean supports(GenerationLanguage language) {
		return language == GenerationLanguage.JAVA || language == GenerationLanguage.KOTLIN;
	}

	@Override
	public byte[] generatePreviewZip(Map<String, Object> yaml, Map<String, Object> app) {
		Path tempDir = null;
		StateMachine<States, Events> stateMachine = null;
		try {
			tempDir = Files.createTempDirectory("project_view_");
			stateMachine = stateMachineFactory.getStateMachine();

			CountDownLatch latch = new CountDownLatch(1);
			AtomicReference<States> endState = new AtomicReference<>();
			stateMachine.addStateListener(new StateMachineListenerAdapter<>() {
				@Override
				public void stateChanged(State<States, Events> from, State<States, Events> to) {
					if (to != null && (to.getId() == States.DONE || to.getId() == States.ERROR)) {
						endState.set(to.getId());
						latch.countDown();
					}
				}
			});

			populatePreviewVariables(stateMachine.getExtendedState().getVariables(), tempDir, yaml, app);
			stateMachine.start();
			boolean finished = latch.await(Duration.ofMinutes(2).toMillis(), TimeUnit.MILLISECONDS);
			if (!finished) {
				throw new GenericException(HttpStatus.REQUEST_TIMEOUT, "Project preview generation timed out.");
			}
			if (endState.get() != States.DONE) {
				String errorMessage = String.valueOf(stateMachine.getExtendedState().getVariables().getOrDefault("error", "Project preview generation failed."));
				throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
			}
			return projectArchiveService.zipDirectory(tempDir);
		} catch (GenericException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		} finally {
			if (stateMachine != null) {
				try {
					stateMachine.stop();
				} catch (Exception ignored) {
				}
			}
			projectArchiveService.deleteDirectoryQuietly(tempDir);
		}
	}

	@Override
	public void run(ProjectRunEntity run, ProjectEntity project, Map<String, Object> yaml) {
		StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();
		try {
			Path tempDir = Files.createTempDirectory("genp_");
			populateProjectVariables(stateMachine.getExtendedState().getVariables(), tempDir, project, yaml);
			stateMachine.addStateListener(new StateMachineListenerAdapter<>() {
				@Override
				public void stateChanged(State<States, Events> from, State<States, Events> to) {
					if (to == null) {
						return;
					}
					handleTerminalState(run, tempDir, to.getId() == States.DONE, to.getId() == States.ERROR);
				}
			});
			stateMachine.start();
		} catch (Exception ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
		} finally {
			try {
				stateMachine.stop();
			} catch (Exception ignored) {
			}
		}
	}

	private void populatePreviewVariables(Map<Object, Object> variables, Path tempDir, Map<String, Object> yaml, Map<String, Object> app) {
		variables.put("autostart", Boolean.TRUE);
		variables.put(ProjectMetaDataConstants.ROOT_DIR, tempDir.toString());
		variables.put(ProjectMetaDataConstants.YAML, yaml);
		variables.put(ProjectMetaDataConstants.GROUP_ID, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP)));
		variables.put(ProjectMetaDataConstants.ARTIFACT_ID, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT)));
		variables.put(ProjectMetaDataConstants.VERSION, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION)));
		variables.put(ProjectMetaDataConstants.BUILD_TOOL, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.BUILD_TOOL, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL)));
		variables.put(ProjectMetaDataConstants.PACKAGING, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.PACKAGING, ProjectMetaDataConstants.DEFAULT_PACKAGING)));
		variables.put(ProjectMetaDataConstants.GENERATOR, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GENERATOR, ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR)));
		variables.put(ProjectMetaDataConstants.NAME, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME)));
		variables.put(ProjectMetaDataConstants.DESCRIPTION, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.DESCRIPTION, ProjectMetaDataConstants.DEFAULT_DESCRIPTION)));
		variables.put(ProjectMetaDataConstants.JDK_VERSION, String.valueOf(app.getOrDefault(ProjectMetaDataConstants.JDK_VERSION, ProjectMetaDataConstants.DEFAULT_JDK)));
	}

	private void populateProjectVariables(Map<Object, Object> variables, Path tempDir, ProjectEntity project, Map<String, Object> yaml) {
		variables.put("autostart", Boolean.TRUE);
		variables.put(ProjectMetaDataConstants.ROOT_DIR, tempDir.toString());
		variables.put("id", project.getId());
		variables.put(ProjectMetaDataConstants.YAML, yaml);
		variables.put(ProjectMetaDataConstants.GROUP_ID, project.getGroupId());
		variables.put(ProjectMetaDataConstants.ARTIFACT_ID, project.getArtifact());
		variables.put(ProjectMetaDataConstants.VERSION, project.getVersion());
		variables.put(ProjectMetaDataConstants.BUILD_TOOL, project.getBuildTool());
		variables.put(ProjectMetaDataConstants.PACKAGING, project.getPackaging());
		variables.put(ProjectMetaDataConstants.GENERATOR, project.getGenerator());
		variables.put(ProjectMetaDataConstants.NAME, project.getName());
		variables.put(ProjectMetaDataConstants.DESCRIPTION, project.getDescription());
		variables.put(ProjectMetaDataConstants.JDK_VERSION, project.getJdkVersion());
	}

	private void handleTerminalState(ProjectRunEntity run, Path tempDir, boolean done, boolean error) {
		if (done) {
			try {
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
				throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
			} finally {
				projectArchiveService.deleteDirectoryQuietly(tempDir);
			}
		}
		if (error) {
			run.setStatus(ProjectRunStatus.ERROR);
			if (run.getErrorMessage() == null || run.getErrorMessage().isBlank()) {
				run.setErrorMessage("Generation failed.");
			}
			runRepository.saveAndFlush(run);
			projectEventStreamService.publish(run.getProject().getId(), "generation", Map.of(
					"projectId", run.getProject().getId().toString(),
					"runId", run.getId().toString(),
					"status", "ERROR",
					"hasZip", false,
					"message", run.getErrorMessage()));
			projectArchiveService.deleteDirectoryQuietly(tempDir);
		}
	}
}
