package com.src.main.workflow.generation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ProjectRunStatus;
import com.src.main.workflow.ProjectArchiveService;
import com.src.main.workflow.engine.WorkflowEngineService;
import com.src.main.service.PluginModuleService;

@Component
public class DatabaseWorkflowProjectGenerationStrategy implements ProjectGenerationStrategy {

	private final WorkflowEngineService workflowEngineService;
	private final ProjectRunRepository runRepository;
	private final ProjectEventStreamService projectEventStreamService;
	private final ProjectArchiveService projectArchiveService;
	private final PluginModuleService pluginModuleService;

	public DatabaseWorkflowProjectGenerationStrategy(
			WorkflowEngineService workflowEngineService,
			ProjectRunRepository runRepository,
			ProjectEventStreamService projectEventStreamService,
			ProjectArchiveService projectArchiveService,
			PluginModuleService pluginModuleService) {
		this.workflowEngineService = workflowEngineService;
		this.runRepository = runRepository;
		this.projectEventStreamService = projectEventStreamService;
		this.projectArchiveService = projectArchiveService;
		this.pluginModuleService = pluginModuleService;
	}

	@Override
	public boolean supports(GenerationLanguage language) {
		return true;
	}

	@Override
	public byte[] generatePreviewZip(Map<String, Object> yaml, Map<String, Object> app) {
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("project_view_");
			DefaultExtendedState state = new DefaultExtendedState();
			populatePreviewVariables(state.getVariables(), tempDir, yaml, app);
			workflowEngineService.execute(resolveLanguage(yaml), state, null);
			pluginModuleService.applyPluginsToProject(tempDir, resolveSelectedPlugins(yaml, app));
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
			tempDir = Files.createTempDirectory("gen_dbwf_");
			DefaultExtendedState state = new DefaultExtendedState();
			populateProjectVariables(state.getVariables(), tempDir, project, yaml);
			workflowEngineService.execute(resolveLanguage(yaml), state, run);
			pluginModuleService.applyPluginsToProject(tempDir, resolveSelectedPlugins(yaml, project));
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

	private GenerationLanguage resolveLanguage(Map<String, Object> yaml) {
		return com.src.main.sm.executor.common.GenerationLanguageResolver.resolveFromYaml(yaml);
	}

	private void populatePreviewVariables(Map<Object, Object> variables, Path tempDir, Map<String, Object> yaml,
			Map<String, Object> app) {
		variables.put(ProjectMetaDataConstants.ROOT_DIR, tempDir.toString());
		variables.put(ProjectMetaDataConstants.YAML, yaml);
		variables.put(ProjectMetaDataConstants.GROUP_ID,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP)));
		variables.put(ProjectMetaDataConstants.ARTIFACT_ID,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT)));
		variables.put(ProjectMetaDataConstants.VERSION,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION)));
		variables.put(ProjectMetaDataConstants.BUILD_TOOL,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.BUILD_TOOL, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL)));
		variables.put(ProjectMetaDataConstants.PACKAGING,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.PACKAGING, ProjectMetaDataConstants.DEFAULT_PACKAGING)));
		variables.put(ProjectMetaDataConstants.GENERATOR,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GENERATOR, ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR)));
		variables.put(ProjectMetaDataConstants.NAME,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME)));
		variables.put(ProjectMetaDataConstants.DESCRIPTION,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.DESCRIPTION, ProjectMetaDataConstants.DEFAULT_DESCRIPTION)));
		variables.put(ProjectMetaDataConstants.JDK_VERSION,
				String.valueOf(app.getOrDefault(ProjectMetaDataConstants.JDK_VERSION, ProjectMetaDataConstants.DEFAULT_JDK)));
	}

	private void populateProjectVariables(Map<Object, Object> variables, Path tempDir, ProjectEntity project,
			Map<String, Object> yaml) {
		variables.put(ProjectMetaDataConstants.ROOT_DIR, tempDir.toString());
		variables.put(ProjectMetaDataConstants.YAML, yaml);
		variables.put("id", project.getId());
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

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> resolveSelectedPlugins(Map<String, Object> yaml, Object source) {
		Object fromSpec = yaml == null ? null : ((Map<String, Object>) ((Map<String, Object>) yaml.getOrDefault("core", Collections.emptyMap())))
				.get("modules");
		if (fromSpec instanceof Map<?, ?> modules) {
			Object plugins = ((Map<String, Object>) modules).get("plugins");
			if (plugins instanceof List<?> pluginList) {
				return pluginList.stream()
						.filter(Map.class::isInstance)
						.map(plugin -> (Map<String, Object>) plugin)
						.toList();
			}
		}
		if (source instanceof Map<?, ?> previewApp) {
			Object plugins = ((Map<String, Object>) previewApp).get("selectedPlugins");
			if (plugins instanceof List<?> pluginList) {
				return pluginList.stream()
						.filter(Map.class::isInstance)
						.map(plugin -> (Map<String, Object>) plugin)
						.toList();
			}
		}
		if (source instanceof ProjectEntity project) {
			String draftData = project.getDraftData();
			if (draftData != null && draftData.contains("\"selectedPlugins\"")) {
				try {
					com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
					Map<String, Object> draft = mapper.readerForMapOf(Object.class).readValue(draftData);
					Object plugins = draft.get("selectedPlugins");
					if (plugins instanceof List<?> pluginList) {
						return pluginList.stream()
								.filter(Map.class::isInstance)
								.map(plugin -> (Map<String, Object>) plugin)
								.toList();
					}
				} catch (Exception ignored) {
					return Collections.emptyList();
				}
			}
		}
		return Collections.emptyList();
	}
}
