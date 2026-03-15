package com.src.main.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectContributorDTO;
import com.src.main.dto.ProjectContributorUpsertRequestDTO;
import com.src.main.dto.ProjectDetailsDTO;
import com.src.main.dto.ProjectSummaryDTO;
import com.src.main.exception.GenericException;
import com.src.main.model.ProjectContributorEntity;
import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectContributorRepository;
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
	private final ProjectContributorRepository projectContributorRepository;
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
	public ProjectCreateResponseDTO create(String yamlText, String ownerId) {
		try {
			var violations = validator.validate(new Input(yamlText));
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}

			Map<String, Object> spec;
			try {
				spec = new Yaml().load(yamlText);
				if (spec == null || !(spec instanceof Map)) {
					throw new IllegalArgumentException("YAML must be a mapping at the root");
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid YAML: " + e.getMessage());
			}
			Map<String, Object> app = (Map<String, Object>) spec.get("app");
			if (app == null) {
				throw new IllegalArgumentException("Missing required 'app' section");
			}
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
			if (artifact == null || artifact.isBlank()) {
				throw new IllegalArgumentException("app.artifact must be provided");
			}
			if (groupId == null || groupId.isBlank()) {
				throw new IllegalArgumentException("app.groupId must be provided");
			}
			if (version == null || version.isBlank()) {
				throw new IllegalArgumentException("app.version must be provided");
			}
			ProjectEntity p = new ProjectEntity();
			p.setId(UUID.randomUUID());
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
			p.setOwnerId(ownerId);
			p.setCreatedAt(OffsetDateTime.now());
			p.setUpdatedAt(OffsetDateTime.now());
			ProjectEntity savedProject = repo.saveAndFlush(p);

			ProjectRunEntity run = new ProjectRunEntity();
			run.setProject(savedProject);
			run.setOwnerId(ownerId);
			run.setType(ProjectRunType.GENERATE_CODE);
			run.setStatus(ProjectRunStatus.QUEUED);
			run.setRunNumber((int) projectRunRepository.countByProjectIdAndType(savedProject.getId(), ProjectRunType.GENERATE_CODE) + 1);
			projectRunRepository.save(run);

			return new ProjectCreateResponseDTO(savedProject.getId().toString(), ProjectRunStatus.QUEUED.name());
		} catch (Exception e) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	public List<ProjectSummaryDTO> list(String userId) {
		List<ProjectEntity> all = isAdmin() ? repo.findAll() : repo.findAccessibleProjects(userId);
		List<ProjectSummaryDTO> out = new ArrayList<>();
		for (ProjectEntity p : all) {
			out.add(toSummary(p));
		}
		return out;
	}

	@Override
	public ProjectDetailsDTO getDetails(UUID projectId, String userId) {
		ProjectEntity project = getAccessibleProject(projectId, userId);
		boolean admin = isAdmin();
		boolean isOwner = userId.equals(project.getOwnerId());
		return new ProjectDetailsDTO(
				project.getId().toString(),
				project.getId(),
				project.getName(),
				project.getDescription(),
				project.getArtifact(),
				project.getYaml(),
				project.getOwnerId(),
				!isOwner && !admin,
				isOwner || admin,
				toContributorDtos(project.getId()),
				project.getCreatedAt(),
				project.getUpdatedAt());
	}

	@Override
	public List<ProjectContributorDTO> getContributors(UUID projectId, String userId) {
		getAccessibleProject(projectId, userId);
		return toContributorDtos(projectId);
	}

	@Override
	@Transactional
	public List<ProjectContributorDTO> addContributor(UUID projectId, String ownerId, ProjectContributorUpsertRequestDTO request) {
		ProjectEntity project = getOwnedProject(projectId, ownerId);
		String contributorUserId = request.getUserId() == null ? "" : request.getUserId().trim();
		if (contributorUserId.isEmpty()) {
			throw new IllegalArgumentException("userId is required");
		}
		if (ownerId.equals(contributorUserId)) {
			throw new IllegalArgumentException("Project owner already has access");
		}
		if (!projectContributorRepository.existsByProjectIdAndUserId(projectId, contributorUserId)) {
			ProjectContributorEntity contributor = new ProjectContributorEntity();
			contributor.setProject(project);
			contributor.setUserId(contributorUserId);
			projectContributorRepository.save(contributor);
		}
		return getContributors(projectId, ownerId);
	}

	@Override
	@Transactional
	public void removeContributor(UUID projectId, String ownerId, String contributorUserId) {
		getOwnedProject(projectId, ownerId);
		String normalizedUserId = contributorUserId == null ? "" : contributorUserId.trim();
		if (normalizedUserId.isEmpty()) {
			throw new IllegalArgumentException("contributorUserId is required");
		}
		projectContributorRepository.deleteByProjectIdAndUserId(projectId, normalizedUserId);
	}

	public ProjectEntity getAccessibleProject(UUID projectId, String userId) {
		ProjectEntity project = repo.findWithContributorsById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		if (isAdmin() || userId.equals(project.getOwnerId()) || projectContributorRepository.existsByProjectIdAndUserId(projectId, userId)) {
			return project;
		}
		throw new SecurityException("User not allowed to access this project");
	}

	private ProjectEntity getOwnedProject(UUID projectId, String ownerId) {
		ProjectEntity project = repo.findWithContributorsById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		if (!isAdmin() && !ownerId.equals(project.getOwnerId())) {
			throw new SecurityException("Only the project owner can manage contributors");
		}
		return project;
	}

	private boolean isAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return false;
		}
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
	}

	private List<ProjectContributorDTO> toContributorDtos(UUID projectId) {
		List<ProjectContributorEntity> contributors = projectContributorRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
		return contributors.stream()
				.map(contributor -> new ProjectContributorDTO(
						contributor.getId(),
						contributor.getUserId(),
						contributor.getCreatedAt()))
				.collect(Collectors.toList());
	}

	private ProjectSummaryDTO toSummary(ProjectEntity project) {
		return new ProjectSummaryDTO(
				project.getId().toString(),
				project.getArtifact(),
				project.getId(),
				project.getName(),
				project.getDescription(),
				project.getCreatedAt(),
				project.getUpdatedAt());
	}
}
