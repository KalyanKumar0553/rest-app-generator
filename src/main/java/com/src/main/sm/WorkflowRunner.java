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
		String groupId = project.getGroupId();
		String buildTool = project.getBuildTool();
		String artifact = project.getArtifact();
		String version = project.getVersion();
		sm.getExtendedState().getVariables().put("autostart", true);
		sm.getExtendedState().getVariables().put("root", temp.toString());
		sm.getExtendedState().getVariables().put("groupId", groupId);
		sm.getExtendedState().getVariables().put("artifact", artifact);
		sm.getExtendedState().getVariables().put("version", version);
		sm.getExtendedState().getVariables().put("buildTool", buildTool);
		sm.getExtendedState().getVariables().put("yaml", yaml);
		sm.getExtendedState().getVariables().put("autostart", Boolean.TRUE);
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
		done.await(10, TimeUnit.MINUTES);
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