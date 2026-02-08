package com.src.main.workflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import com.src.main.exception.GenericException;
import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.service.ProjectEventStreamService;
import com.src.main.sm.config.Events;
import com.src.main.sm.config.States;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ProjectRunStatus;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProjectWorkflowService {

	private static final Logger log = LoggerFactory.getLogger(ProjectWorkflowService.class);

	private final StateMachineFactory<States, Events> factory;
	private final ProjectRunRepository runRepository;
	private final ProjectEventStreamService projectEventStreamService;
	
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
					Map.of("projectId", run.getProject().getId().toString(), "status", "ERROR",
							"message", ex.getMessage() == null ? "Generation failed." : ex.getMessage()));
        }
	}

	private void runStateMachine(ProjectRunEntity run) throws Exception {
		Map<String, Object> yaml = (Map<String, Object>) new Yaml().load(run.getProject().getYaml());
		run(run,run.getProject(), yaml);
	}
	
	@SuppressWarnings("unchecked")
	public void run(ProjectRunEntity run, ProjectEntity project, Map<String, Object> yaml) throws IOException {
		StateMachine<States, Events> sm = factory.getStateMachine();
		Path temp = Files.createTempDirectory("genp_");
		sm.getExtendedState().getVariables().put("autostart", Boolean.TRUE);
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.ROOT_DIR, temp.toString());
		sm.getExtendedState().getVariables().put("id", project.getId());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.YAML, yaml);
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.GROUP_ID, project.getGroupId());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.ARTIFACT_ID, project.getArtifact());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.VERSION, project.getVersion());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.BUILD_TOOL, project.getBuildTool());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.PACKAGING, project.getPackaging());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.BUILD_TOOL, project.getBuildTool());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.GENERATOR, project.getGenerator());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.NAME, project.getName());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.DESCRIPTION, project.getDescription());
		sm.getExtendedState().getVariables().put(ProjectMetaDataConstants.JDK_VERSION, project.getJdkVersion());
		sm.addStateListener(new StateMachineListenerAdapter<>() {
			@Override
			public void stateChanged(State<States, Events> from, State<States, Events> to) {
				if (to.getId() == States.DONE) {
					try {
						byte[] zipData = getZipData(temp);
						run.setZip(zipData);
						run.setStatus(ProjectRunStatus.SUCCESS);
						run.setErrorMessage(null);
						runRepository.saveAndFlush(run);
						projectEventStreamService.publish(run.getProject().getId(), "generation",
								Map.of("projectId", run.getProject().getId().toString(), "status", "SUCCESS",
										"fileName", run.getProject().getArtifact() + ".zip",
										"zipBase64", Base64.getEncoder().encodeToString(zipData)));
					} catch (IOException e) {
						throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
					}
				}
				if (to.getId() == States.ERROR) {
					run.setStatus(ProjectRunStatus.ERROR);
					if (run.getErrorMessage() == null || run.getErrorMessage().isBlank()) {
						run.setErrorMessage("Generation failed.");
					}
					runRepository.saveAndFlush(run);
					projectEventStreamService.publish(run.getProject().getId(), "generation",
							Map.of("projectId", run.getProject().getId().toString(), "status", "ERROR",
									"message", run.getErrorMessage()));
				}
			}
		});
		if (sm.getExtendedState().getVariables().containsKey("error")) {
			String errorMsg = (String)sm.getExtendedState().getVariables().get("error");
			
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,errorMsg);
		}
	}
	

	public byte[] getZipData(Path temp) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(out)) {
			Files.walk(temp).forEach(p -> {
				try {
					if (Files.isRegularFile(p)) {
						String name = temp.relativize(p).toString().replace("\\", "/");
						zos.putNextEntry(new ZipEntry(name));
						try (InputStream in = Files.newInputStream(p)) {
							in.transferTo(zos);
						}
						zos.closeEntry();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
		return out.toByteArray();
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
