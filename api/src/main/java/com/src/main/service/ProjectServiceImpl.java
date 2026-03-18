package com.src.main.service;

import java.util.Base64;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.auth.service.RbacService;
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
	private final ProjectUserIdentityService projectUserIdentityService;
	private final ProjectYamlService projectYamlService;
	private final ProjectNameValidationService projectNameValidationService;
	private final RbacService rbacService;
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
			if (!rbacService.currentUserHasPermission("project.create")) {
				throw new SecurityException("User not allowed to create projects");
			}
			var violations = validator.validate(new Input(yamlText));
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}

			Map<String, Object> spec = projectYamlService.parseSpec(yamlText);
			Map<String, Object> app = projectYamlService.getRequiredAppSection(spec);
			String artifact = projectYamlService.getString(app, ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT);
			String groupId = projectYamlService.getString(app, ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP);
			String version = projectYamlService.getString(app, ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION);
			String buildTool = projectYamlService.getString(app, ProjectMetaDataConstants.BUILD_TOOL, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL);
			String packaging = projectYamlService.getString(app, ProjectMetaDataConstants.PACKAGING, ProjectMetaDataConstants.DEFAULT_PACKAGING);
			String generator = projectYamlService.getString(app, ProjectMetaDataConstants.GENERATOR, ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR);
			String name = projectYamlService.getString(app, ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME);
			String description = projectYamlService.getString(app, ProjectMetaDataConstants.DESCRIPTION, ProjectMetaDataConstants.DEFAULT_DESCRIPTION);
			String springBootVersion = projectYamlService.getString(app, ProjectMetaDataConstants.SPRING_BOOT_VERSION, ProjectMetaDataConstants.DEFAULT_BOOT_VERSION);
			String jdkVersion = projectYamlService.getString(app, ProjectMetaDataConstants.JDK_VERSION, ProjectMetaDataConstants.DEFAULT_JDK);
			if (artifact == null || artifact.isBlank()) {
				throw new IllegalArgumentException("app.artifact must be provided");
			}
			if (groupId == null || groupId.isBlank()) {
				throw new IllegalArgumentException("app.groupId must be provided");
			}
			if (version == null || version.isBlank()) {
				throw new IllegalArgumentException("app.version must be provided");
			}
			ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
			projectNameValidationService.ensureUniqueProjectName(name, currentUser, null);
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
			p.setOwnerId(currentUser.userId());
			p.setCreatedAt(OffsetDateTime.now());
			p.setUpdatedAt(OffsetDateTime.now());
			ProjectEntity savedProject = repo.saveAndFlush(p);

			ProjectRunEntity run = new ProjectRunEntity();
			run.setProject(savedProject);
			run.setOwnerId(currentUser.userId());
			run.setType(ProjectRunType.GENERATE_CODE);
			run.setStatus(ProjectRunStatus.QUEUED);
			run.setRunNumber((int) projectRunRepository.countByProjectIdAndType(savedProject.getId(), ProjectRunType.GENERATE_CODE) + 1);
			projectRunRepository.save(run);

			return new ProjectCreateResponseDTO(savedProject.getId().toString(), ProjectRunStatus.QUEUED.name());
		} catch (GenericException e) {
			throw e;
		} catch (IllegalArgumentException | ConstraintViolationException e) {
			throw new GenericException(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<ProjectSummaryDTO> list(String userId) {
		if (!rbacService.currentUserHasPermission("project.read")) {
			throw new SecurityException("User not allowed to view projects");
		}
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		List<ProjectEntity> all = canReadAllProjects()
				? repo.findAll()
				: findAccessibleProjectsForList(currentUser);
		List<ProjectSummaryDTO> out = new ArrayList<>();
		for (ProjectEntity p : all) {
			out.add(toSummary(p));
		}
		return out;
	}

	private List<ProjectEntity> findAccessibleProjectsForList(ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		List<ProjectEntity> accessible = repo.findAccessibleProjectsByUserKeys(currentUser.keys());
		if (!accessible.isEmpty()) {
			return accessible;
		}

		List<ProjectEntity> allProjects = repo.findAllWithContributors();
		List<ProjectEntity> matchedProjects = new ArrayList<>();
		for (ProjectEntity project : allProjects) {
			boolean ownerMatch = matchesUserKey(project.getOwnerId(), currentUser.keys());
			boolean contributorMatch = normalizeContributorAliases(project, currentUser);
			if (!ownerMatch && !contributorMatch) {
				continue;
			}
			if (ownerMatch && !currentUser.userId().equals(project.getOwnerId())) {
				project.setOwnerId(currentUser.userId());
				repo.save(project);
			}
			matchedProjects.add(project);
		}

		matchedProjects.sort(Comparator.comparing(ProjectEntity::getUpdatedAt,
				Comparator.nullsLast(Comparator.reverseOrder())));
		return matchedProjects;
	}

	private boolean normalizeContributorAliases(ProjectEntity project, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		if (project == null || project.getContributors() == null || project.getContributors().isEmpty()) {
			return false;
		}
		boolean matched = false;
		for (ProjectContributorEntity contributor : project.getContributors()) {
			if (!matchesUserKey(contributor.getUserId(), currentUser.keys())) {
				continue;
			}
			matched = true;
			if (!currentUser.userId().equals(contributor.getUserId())) {
				contributor.setUserId(currentUser.userId());
				projectContributorRepository.save(contributor);
			}
		}
		return matched;
	}

	private boolean matchesUserKey(String candidate, java.util.Set<String> keys) {
		if (candidate == null || candidate.isBlank() || keys == null || keys.isEmpty()) {
			return false;
		}
		String normalizedCandidate = candidate.trim();
		return keys.stream().anyMatch(key -> key != null && normalizedCandidate.equalsIgnoreCase(key.trim()));
	}

	@Override
	public ProjectDetailsDTO getDetails(UUID projectId, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectEntity project = getAccessibleProject(projectId, currentUser);
		boolean canManageAllContributors = canManageAllProjectContributors();
		boolean isOwner = currentUser.keys().contains(project.getOwnerId());
		List<ProjectRunEntity> runs = projectRunRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());
		ProjectRunEntity latestRun = runs.isEmpty() ? null : runs.get(runs.size() - 1);
		ProjectRunEntity latestRunWithZip = null;
		for (int i = runs.size() - 1; i >= 0; i--) {
			ProjectRunEntity candidate = runs.get(i);
			if (candidate != null && candidate.getZip() != null && candidate.getZip().length > 0) {
				latestRunWithZip = candidate;
				break;
			}
		}
		return new ProjectDetailsDTO(
				project.getId().toString(),
				project.getId(),
				project.getName(),
				project.getDescription(),
				project.getArtifact(),
				project.getYaml(),
				project.getOwnerId(),
				!isOwner && !canManageAllContributors,
				isOwner || canManageAllContributors,
				toContributorDtos(project.getId()),
				latestRun == null ? null : latestRun.getId(),
				latestRun == null || latestRun.getStatus() == null ? null : latestRun.getStatus().name(),
				latestRun == null ? null : latestRun.getRunNumber(),
				latestRunWithZip != null,
				encodeZipBase64(latestRunWithZip),
				buildZipFileName(project),
				project.getCreatedAt(),
				project.getUpdatedAt());
	}

	private String encodeZipBase64(ProjectRunEntity run) {
		if (run == null || run.getZip() == null || run.getZip().length == 0) {
			return null;
		}
		return Base64.getEncoder().encodeToString(run.getZip());
	}

	private String buildZipFileName(ProjectEntity project) {
		if (project == null || project.getArtifact() == null || project.getArtifact().isBlank()) {
			return "project.zip";
		}
		return project.getArtifact().trim() + ".zip";
	}

	@Override
	public List<ProjectContributorDTO> getContributors(UUID projectId, String userId) {
		getAccessibleProject(projectId, projectUserIdentityService.resolve(userId));
		return toContributorDtos(projectId);
	}

	@Override
	@Transactional
	public List<ProjectContributorDTO> addContributor(UUID projectId, String ownerId, ProjectContributorUpsertRequestDTO request) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		ProjectEntity project = getOwnedProject(projectId, currentUser);
		if (!rbacService.currentUserHasPermission("project.contributor.manage")) {
			throw new SecurityException("User not allowed to manage contributors");
		}
		String contributorUserId = request.getUserId() == null ? "" : request.getUserId().trim();
		if (contributorUserId.isEmpty()) {
			throw new IllegalArgumentException("userId is required");
		}
		if (currentUser.keys().contains(contributorUserId) || currentUser.userId().equals(contributorUserId)) {
			throw new IllegalArgumentException("Project owner already has access");
		}
		if (!projectContributorRepository.existsByProjectIdAndUserId(projectId, contributorUserId)) {
			ProjectContributorEntity contributor = new ProjectContributorEntity();
			contributor.setProject(project);
			contributor.setUserId(contributorUserId);
			projectContributorRepository.save(contributor);
		}
		return getContributors(projectId, currentUser.userId());
	}

	@Override
	@Transactional
	public void removeContributor(UUID projectId, String ownerId, String contributorUserId) {
		if (!rbacService.currentUserHasPermission("project.contributor.manage")) {
			throw new SecurityException("User not allowed to manage contributors");
		}
		getOwnedProject(projectId, projectUserIdentityService.resolve(ownerId));
		String normalizedUserId = contributorUserId == null ? "" : contributorUserId.trim();
		if (normalizedUserId.isEmpty()) {
			throw new IllegalArgumentException("contributorUserId is required");
		}
		projectContributorRepository.deleteByProjectIdAndUserId(projectId, normalizedUserId);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteProject(UUID projectId, String userId) {
		try {
			ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
			ProjectEntity project = getProjectForDelete(projectId, currentUser);
			projectRunRepository.deleteByProjectId(projectId);
			projectContributorRepository.deleteByProjectId(projectId);
			repo.delete(project);
			repo.flush();
		} catch (GenericException | IllegalArgumentException | SecurityException e) {
			throw e;
		} catch (Exception e) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete project");
		}
	}

	public ProjectEntity getAccessibleProject(UUID projectId, String userId) {
		return getAccessibleProject(projectId, projectUserIdentityService.resolve(userId));
	}

	private ProjectEntity getAccessibleProject(UUID projectId, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		if (!rbacService.currentUserHasPermission("project.read")) {
			throw new SecurityException("User not allowed to access projects");
		}
		ProjectEntity project = repo.findWithContributorsById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (canReadAllProjects()
				|| currentUser.keys().contains(project.getOwnerId())
				|| projectContributorRepository.existsByProjectIdAndUserIdIn(projectId, currentUser.keys())) {
			return project;
		}
		throw new SecurityException("User not allowed to access this project");
	}

	private ProjectEntity getOwnedProject(UUID projectId, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		ProjectEntity project = repo.findWithContributorsById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (!canManageAllProjectContributors() && !currentUser.keys().contains(project.getOwnerId())) {
			throw new SecurityException("Only the project owner can manage contributors");
		}
		return project;
	}

	private ProjectEntity getProjectForDelete(UUID projectId, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		ProjectEntity project = repo.findWithContributorsById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (canDeleteAllProjects()) {
			return project;
		}
		if (!rbacService.currentUserHasPermission("project.delete")) {
			throw new SecurityException("User not allowed to delete projects");
		}
		if (!currentUser.keys().contains(project.getOwnerId())) {
			throw new SecurityException("Only the project owner can delete this project");
		}
		return project;
	}

	private void reassignOwnerIfNeeded(ProjectEntity project, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		if (project == null || project.getOwnerId() == null || currentUser == null) {
			return;
		}
		if (!currentUser.keys().contains(project.getOwnerId())) {
			return;
		}
		if (project.getOwnerId().equals(currentUser.userId())) {
			return;
		}
		project.setOwnerId(currentUser.userId());
		repo.save(project);
	}

	private boolean canReadAllProjects() {
		return rbacService.currentUserHasPermission("project.read.all");
	}

	private boolean canManageAllProjectContributors() {
		return rbacService.currentUserHasPermission("project.contributor.manage.all");
	}

	private boolean canDeleteAllProjects() {
		return rbacService.currentUserHasPermission("project.delete.all");
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
