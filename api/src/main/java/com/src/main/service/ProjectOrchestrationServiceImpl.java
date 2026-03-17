package com.src.main.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.auth.service.RbacService;
import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectContributorRepository;
import com.src.main.repository.ProjectRepository;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.util.AppConstants;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

@Service
public class ProjectOrchestrationServiceImpl implements ProjectOrchestrationService {

	private final ProjectRepository projectRepository;
	private final ProjectRunRepository projectRunRepository;
	private final ProjectContributorRepository projectContributorRepository;
	private final ProjectUserIdentityService projectUserIdentityService;
	private final ProjectYamlService projectYamlService;
	private final ProjectNameValidationService projectNameValidationService;
	private final RbacService rbacService;

	@Value("${app.project.max-generates-per-user-per-day:200}")
	private int maxGeneratesPerUserPerDay;

	private ZoneId zoneId = ZoneId.of("Asia/Kolkata");

	public ProjectOrchestrationServiceImpl(ProjectRepository projectRepository, ProjectRunRepository projectRunRepository,
			ProjectContributorRepository projectContributorRepository,
			ProjectUserIdentityService projectUserIdentityService,
			ProjectYamlService projectYamlService,
			ProjectNameValidationService projectNameValidationService,
			RbacService rbacService) {
		this.projectRepository = projectRepository;
		this.projectRunRepository = projectRunRepository;
		this.projectContributorRepository = projectContributorRepository;
		this.projectUserIdentityService = projectUserIdentityService;
		this.projectYamlService = projectYamlService;
		this.projectNameValidationService = projectNameValidationService;
		this.rbacService = rbacService;
	}

	@Override
	@Transactional
	public ProjectEntity updateSpec(UUID projectId, String ownerId, String yamlContent) {
		if (!rbacService.currentUserHasPermission("project.update")) {
			throw new SecurityException("User not allowed to update projects");
		}
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		ProjectEntity project = getEditableProject(projectId, currentUser.userId());
		String updatedProjectName = projectYamlService.extractProjectName(yamlContent);
		String currentProjectName = project.getName() == null ? "" : project.getName().trim();
		String normalizedUpdatedProjectName = updatedProjectName == null ? "" : updatedProjectName.trim();
		if (!currentProjectName.equalsIgnoreCase(normalizedUpdatedProjectName)) {
			projectNameValidationService.ensureUniqueProjectName(updatedProjectName, currentUser, projectId);
		}
		project.setOwnerId(currentUser.userId());
		project.setName(updatedProjectName);
		project.setYaml(yamlContent);
		return project;
	}

	@Override
	@Transactional
	public ProjectRunEntity updateSpecAndGenerate(UUID projectId, String ownerId, String yamlContent) {
		ProjectEntity project = updateSpec(projectId, ownerId, yamlContent);

		ProjectRunEntity latest = projectRunRepository.findTopByProjectIdAndTypeOrderByCreatedAtDesc(projectId, ProjectRunType.GENERATE_CODE);
		if (latest != null && latest.getStatus() == ProjectRunStatus.QUEUED) {
			latest.setStatus(ProjectRunStatus.CANCELLED);
		}
		return createGenerateRun(project, ownerId);
	}

	@Override
	@Transactional
	public ProjectRunEntity generateCode(UUID projectId, String ownerId) {
		if (!rbacService.currentUserHasPermission("project.generate")) {
			throw new SecurityException("User not allowed to generate projects");
		}
		ProjectEntity project = getEditableProject(projectId, ownerId);
		ProjectRunEntity latest = projectRunRepository.findTopByProjectIdAndTypeOrderByCreatedAtDesc(projectId, ProjectRunType.GENERATE_CODE);
		if (latest != null && (latest.getStatus() == ProjectRunStatus.QUEUED || latest.getStatus() == ProjectRunStatus.INPROGRESS)) {
			return latest;
		}
		return createGenerateRun(project, ownerId);
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectEntity getOwnedProject(UUID projectId, String ownerId) {
		if (!rbacService.currentUserHasPermission("project.read")) {
			throw new SecurityException("User not allowed to access projects");
		}
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		ProjectEntity project = projectRepository.findWithContributorsById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (!canReadAllProjects()
				&& !currentUser.keys().contains(project.getOwnerId())
				&& !projectContributorRepository.existsByProjectIdAndUserIdIn(projectId, currentUser.keys())) {
			throw new SecurityException("User not allowed to access this project");
		}
		return project;
	}

	@Transactional(readOnly = true)
	protected ProjectEntity getEditableProject(UUID projectId, String ownerId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		ProjectEntity project = projectRepository.findWithContributorsById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (!canUpdateAllProjects()
				&& !currentUser.keys().contains(project.getOwnerId())
				&& !projectContributorRepository.existsByProjectIdAndUserIdIn(projectId, currentUser.keys())) {
			throw new SecurityException("User not allowed to modify this project");
		}
		return project;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProjectRunEntity> getRunsForProject(UUID projectId, String ownerId) {
		ProjectEntity project = getOwnedProject(projectId, ownerId);
		return projectRunRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectRunEntity getRun(UUID runId, String ownerId) {
		ProjectRunEntity run = projectRunRepository.findById(runId)
				.orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));
		getOwnedProject(run.getProject().getId(), ownerId);
		return run;
	}

	@Transactional
	protected ProjectRunEntity createGenerateRun(ProjectEntity project, String ownerId) {
		String canonicalUserId = projectUserIdentityService.resolve(ownerId).userId();
		enforceDailyLimit(canonicalUserId);
		long existingForProject = projectRunRepository.countByProjectIdAndType(project.getId(), ProjectRunType.GENERATE_CODE);
		ProjectRunEntity run = new ProjectRunEntity();
		run.setProject(project);
		run.setOwnerId(canonicalUserId);
		run.setType(ProjectRunType.GENERATE_CODE);
		run.setStatus(ProjectRunStatus.QUEUED);
		run.setRunNumber((int) existingForProject + 1);
		return projectRunRepository.save(run);
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
		projectRepository.save(project);
	}

	protected void enforceDailyLimit(String ownerId) {
		LocalDate today = LocalDate.now(zoneId);
		OffsetDateTime from = today.atStartOfDay(zoneId).toOffsetDateTime();
		OffsetDateTime to = today.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
		long countToday = projectRunRepository.countUserRunsInPeriod(ownerId, ProjectRunType.GENERATE_CODE, from, to);
		if (countToday >= maxGeneratesPerUserPerDay) {
			throw new IllegalStateException("Daily generate limit reached (" + maxGeneratesPerUserPerDay + " per day)");
		}
	}

	@Override
	public ResponseEntity<byte[]> download(UUID id) {
		ProjectRunEntity p = projectRunRepository.findById(id)
				.orElseThrow(() -> new java.util.NoSuchElementException("Project Run Not found"));
		if (p.getZip() == null) {
			return ResponseEntity.status(202).build();
		}
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format(AppConstants.DISP_ATTACHMENT_FMT, p.getProject().getArtifact()))
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(p.getZip());
	}

	private boolean canReadAllProjects() {
		return rbacService.currentUserHasPermission("project.read.all");
	}

	private boolean canUpdateAllProjects() {
		return rbacService.currentUserHasPermission("project.update.all");
	}
}
