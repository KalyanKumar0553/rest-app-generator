package com.src.main.sm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import com.src.main.exceptions.GenericException;
import com.src.main.model.ProjectEntity;
import com.src.main.utils.ProjectMetaDataConstants;

@Component
public class WorkflowRunner {

	private final StateMachineFactory<States, Events> factory;
	
	public WorkflowRunner(StateMachineFactory<States, Events> f) {
		this.factory = f;
	}

	@SuppressWarnings("unchecked")
	public byte[] run(ProjectEntity project, Map<String, Object> yaml) throws Exception {
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
		CountDownLatch done = new CountDownLatch(1);
		sm.addStateListener(new StateMachineListenerAdapter<>() {
			@Override
			public void stateChanged(State<States, Events> from, State<States, Events> to) {
				if (to.getId() == States.DONE || to.getId() == States.ERROR) {
					done.countDown();
				}
			}
		});
		sm.startReactively().block();
		done.await(15, TimeUnit.SECONDS);
		if (sm.getExtendedState().getVariables().containsKey("error")) {
			String errorMsg = (String)sm.getExtendedState().getVariables().get("error");
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,errorMsg);
		}
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
}