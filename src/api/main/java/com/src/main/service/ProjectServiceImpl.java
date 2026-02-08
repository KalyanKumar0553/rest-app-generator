package com.src.main.service;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectSummaryDTO;
import com.src.main.exception.GenericException;
import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRepository;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository repo;
	private final ProjectRunRepository projectRunRepository;
	private final Validator validator;

	static class Input {
		@NotBlank
		String yaml;

		Input(String yaml) {
			this.yaml = yaml;
		}

		public String getYaml() {
			return yaml;
		}
	}

	@Override
	@Transactional
	public ProjectCreateResponseDTO create(String yamlText) {
		try {
			var violations = validator.validate(new Input(yamlText));
			if (!violations.isEmpty())
				throw new ConstraintViolationException(violations);

			Map<String, Object> spec;
			try {
				spec = new Yaml().load(yamlText);
				if (spec == null || !(spec instanceof Map))
					throw new IllegalArgumentException("YAML must be a mapping at the root");
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid YAML: " + e.getMessage());
			}
			Map<String, Object> app = (Map<String, Object>) spec.get("app");
			if (app == null)
				throw new IllegalArgumentException("Missing required 'app' section");
			String artifact = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT));
			String groupId = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP));
			String version = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION));
			String buildTool = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.BUILD_TOOL, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL));
			String packaging = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.PACKAGING, ProjectMetaDataConstants.DEFAULT_PACKAGING));
			String generator = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GENERATOR, ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR));
			String name = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME));
			String description = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.DESCRIPTION, ProjectMetaDataConstants.DEFAULT_DESCRIPTION));
			String springBootVersion = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.SPRING_BOOT_VERSION, ProjectMetaDataConstants.DEFAULT_BOOT_VERSION));
			String jdkVersion = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.JDK_VERSION, ProjectMetaDataConstants.DEFAULT_JDK));
			if (artifact == null || artifact.isBlank())
				throw new IllegalArgumentException("app.artifact must be provided");
			if (groupId == null || groupId.isBlank())
				throw new IllegalArgumentException("app.groupId must be provided");
			if (version == null || version.isBlank())
				throw new IllegalArgumentException("app.version must be provided");
			ProjectEntity p = new ProjectEntity();
			p.setId(java.util.UUID.randomUUID());
			p.setArtifact(artifact);
			p.setGroupId(groupId);
			p.setVersion(version);
			p.setBuildTool(buildTool);
			p.setPackaging(packaging);
			p.setGenerator(generator);
			p.setName(name);
			p.setDescription(description);
			p.setSpringBootVersion(springBootVersion);
			p.setJdkVersion(jdkVersion);
			p.setYaml(yamlText);
			p.setOwnerId("public");
			p.setCreatedAt(OffsetDateTime.now());
			p.setUpdatedAt(OffsetDateTime.now());
			ProjectEntity savedProject = repo.saveAndFlush(p);

			ProjectRunEntity run = new ProjectRunEntity();
			run.setProject(savedProject);
			run.setOwnerId("public");
			run.setType(ProjectRunType.GENERATE_CODE);
			run.setStatus(ProjectRunStatus.QUEUED);
			run.setRunNumber((int) projectRunRepository.countByProjectIdAndType(savedProject.getId(), ProjectRunType.GENERATE_CODE) + 1);
			projectRunRepository.save(run);

			return new ProjectCreateResponseDTO(savedProject.getId().toString(), ProjectRunStatus.QUEUED.name());	
		} catch (Exception e) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
		}
	}

	@Override
	public java.util.List<ProjectSummaryDTO> list() {
		java.util.List<ProjectEntity> all = repo.findAll();
		java.util.List<ProjectSummaryDTO> out = new java.util.ArrayList<>();
		for (ProjectEntity p : all) {
			out.add(new ProjectSummaryDTO(p.getId().toString(), p.getArtifact(), p.getId()));
		}
		return out;
	}
}
