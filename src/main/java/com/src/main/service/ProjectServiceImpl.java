package com.src.main.service;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.src.main.dto.ProjectCreateResponse;
import com.src.main.dto.ProjectStatusResponse;
import com.src.main.dto.ProjectSummary;
import com.src.main.exceptions.GenericException;
import com.src.main.model.ProjectEntity;
import com.src.main.sm.WorkflowRunner;
import com.src.main.status.ProjectRepository;
import com.src.main.utils.AppConstants;
import com.src.main.utils.ProjectStatus;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;

@Service
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository repo;
	private final Validator validator;
	private final WorkflowRunner runner;

	public ProjectServiceImpl(ProjectRepository repo, Validator validator,WorkflowRunner runner) {
		this.repo = repo;
		this.validator = validator;
		this.runner = runner;
	}

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
	public ProjectCreateResponse create(String yamlText) {
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
			String artifact = String.valueOf(app.getOrDefault("artifactId", AppConstants.DEFAULT_ARTIFACT));
			String groupId = String.valueOf(app.getOrDefault("groupId", AppConstants.DEFAULT_GROUP));
			String version = String.valueOf(app.getOrDefault("version", AppConstants.DEFAULT_VERSION));
			String buildTool = String.valueOf(app.getOrDefault("buildTool", AppConstants.DEFAULT_BUILD_TOOL));
			if (artifact == null || artifact.isBlank())
				throw new IllegalArgumentException("app.artifact must be provided");
			if (groupId == null || groupId.isBlank())
				throw new IllegalArgumentException("app.groupId must be provided");
			if (version == null || version.isBlank())
				throw new IllegalArgumentException("app.version must be provided");

			ProjectEntity p = new ProjectEntity();
			p.setId(java.util.UUID.randomUUID());
			p.setYaml(yamlText);
			p.setStatus(ProjectStatus.QUEUED);
			p.setCreatedAt(OffsetDateTime.now());
			p.setUpdatedAt(OffsetDateTime.now());
			p.setArtifact(artifact);
			p.setGroupId(groupId);
			p.setVersion(version);
			p.setBuildTool(buildTool);
			Map<String, Object> yaml = (Map<String, Object>) new Yaml().load(p.getYaml());
			repo.save(p);
			byte[] data = runner.run(p, yaml);
			p.setZip(data);
			p.setStatus(ProjectStatus.SUCCESS);
			p.setUpdatedAt(OffsetDateTime.now());
			repo.saveAndFlush(p);
			return new ProjectCreateResponse(p.getId().toString(), p.getStatus().name());	
		} catch (Exception e) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage());
		}
	}

	@Override
	public ProjectStatusResponse status(java.util.UUID id) {
		return repo.findById(id)
				.map(p -> new ProjectStatusResponse(p.getId().toString(), p.getArtifact(), p.getStatus().name(),
						p.getErrorMessage()))
				.orElseThrow(() -> new java.util.NoSuchElementException("Project not found"));
	}

	@Override
	public ResponseEntity<byte[]> download(java.util.UUID id) {
		ProjectEntity p = repo.findById(id)
				.orElseThrow(() -> new java.util.NoSuchElementException("Project not found"));
		if (p.getZip() == null)
			return ResponseEntity.status(202).build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						String.format(AppConstants.DISP_ATTACHMENT_FMT, p.getArtifact()))
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(p.getZip());
	}

	@Override
	public java.util.List<ProjectSummary> list() {
		java.util.List<ProjectEntity> all = repo.findAll();
		java.util.List<ProjectSummary> out = new java.util.ArrayList<>();
		for (ProjectEntity p : all) {
			out.add(new ProjectSummary(p.getId().toString(), p.getArtifact(), p.getStatus().name()));
		}
		return out;
	}
}
