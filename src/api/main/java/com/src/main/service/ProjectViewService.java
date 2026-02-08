package com.src.main.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.src.main.exception.GenericException;
import com.src.main.sm.config.Events;
import com.src.main.sm.config.States;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.workflow.ProjectWorkflowService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProjectViewService {

	private final StateMachineFactory<States, Events> stateMachineFactory;
	private final ProjectWorkflowService projectWorkflowService;

	public byte[] generateZip(String yamlText) {
		Map<String, Object> spec = parseYaml(yamlText);
		Map<String, Object> app = extractApp(spec);

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

			var vars = stateMachine.getExtendedState().getVariables();
			vars.put("autostart", Boolean.TRUE);
			vars.put(ProjectMetaDataConstants.ROOT_DIR, tempDir.toString());
			vars.put(ProjectMetaDataConstants.YAML, spec);
			vars.put(ProjectMetaDataConstants.GROUP_ID,
					String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP)));
			vars.put(ProjectMetaDataConstants.ARTIFACT_ID, String
					.valueOf(app.getOrDefault(ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT)));
			vars.put(ProjectMetaDataConstants.VERSION,
					String.valueOf(app.getOrDefault(ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION)));
			vars.put(ProjectMetaDataConstants.BUILD_TOOL, String
					.valueOf(app.getOrDefault(ProjectMetaDataConstants.BUILD_TOOL, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL)));
			vars.put(ProjectMetaDataConstants.PACKAGING, String
					.valueOf(app.getOrDefault(ProjectMetaDataConstants.PACKAGING, ProjectMetaDataConstants.DEFAULT_PACKAGING)));
			vars.put(ProjectMetaDataConstants.GENERATOR, String
					.valueOf(app.getOrDefault(ProjectMetaDataConstants.GENERATOR, ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR)));
			vars.put(ProjectMetaDataConstants.NAME,
					String.valueOf(app.getOrDefault(ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME)));
			vars.put(ProjectMetaDataConstants.DESCRIPTION, String
					.valueOf(app.getOrDefault(ProjectMetaDataConstants.DESCRIPTION, ProjectMetaDataConstants.DEFAULT_DESCRIPTION)));
			vars.put(ProjectMetaDataConstants.JDK_VERSION,
					String.valueOf(app.getOrDefault(ProjectMetaDataConstants.JDK_VERSION, ProjectMetaDataConstants.DEFAULT_JDK)));

			stateMachine.start();
			boolean finished = latch.await(Duration.ofMinutes(2).toMillis(), TimeUnit.MILLISECONDS);
			if (!finished) {
				throw new GenericException(HttpStatus.REQUEST_TIMEOUT, "Project preview generation timed out.");
			}

			if (endState.get() != States.DONE) {
				String errorMessage = String.valueOf(vars.getOrDefault("error", "Project preview generation failed."));
				throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
			}

			return projectWorkflowService.getZipData(tempDir);
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
			cleanupTempDir(tempDir);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseYaml(String yamlText) {
		try {
			Object data = new Yaml().load(yamlText);
			if (!(data instanceof Map<?, ?> map)) {
				throw new GenericException(HttpStatus.BAD_REQUEST, "YAML root must be an object.");
			}
			return (Map<String, Object>) map;
		} catch (GenericException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Invalid YAML: " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractApp(Map<String, Object> spec) {
		Object appRaw = spec.get("app");
		if (!(appRaw instanceof Map<?, ?> app)) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Missing required 'app' section.");
		}
		return (Map<String, Object>) app;
	}

	private void cleanupTempDir(Path tempDir) {
		if (tempDir == null) {
			return;
		}
		try {
			Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ignored) {
				}
			});
		} catch (IOException ignored) {
		}
	}
}
