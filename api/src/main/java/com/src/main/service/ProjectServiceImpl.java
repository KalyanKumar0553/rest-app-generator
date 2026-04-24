package com.src.main.service;

import java.util.Base64;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.auth.service.RbacService;
import com.src.main.dto.ArchivedProjectCollaborationDTO;
import com.src.main.dto.ProjectCollaborationInviteDTO;
import com.src.main.dto.ProjectCollaborationRequestCreateDTO;
import com.src.main.dto.ProjectCollaborationRequestDTO;
import com.src.main.dto.ProjectCollaborationRequestReviewDTO;
import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectContributorDTO;
import com.src.main.dto.ProjectContributorPermissionUpdateDTO;
import com.src.main.dto.ProjectContributorPermissionsDTO;
import com.src.main.dto.ProjectContributorUpsertRequestDTO;
import com.src.main.dto.ProjectDraftResponseDTO;
import com.src.main.dto.ProjectDraftVersionDetailsDTO;
import com.src.main.dto.ProjectDraftVersionDiffDTO;
import com.src.main.dto.ProjectDraftVersionSummaryDTO;
import com.src.main.dto.ProjectDraftTabDataDTO;
import com.src.main.dto.ProjectDraftTabPatchRequestDTO;
import com.src.main.dto.ProjectDraftUpsertRequestDTO;
import com.src.main.dto.ProjectDetailsDTO;
import com.src.main.dto.ProjectImportRequestDTO;
import com.src.main.dto.ProjectSummaryDTO;
import com.src.main.dto.ProjectTabDefinitionDTO;
import com.src.main.exception.GenericException;
import com.src.main.model.ProjectCollaborationRequestEntity;
import com.src.main.model.ProjectContributorEntity;
import com.src.main.model.ProjectDraftVersionEntity;
import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectCollaborationRequestRepository;
import com.src.main.repository.ProjectContributorRepository;
import com.src.main.repository.ProjectDraftVersionRepository;
import com.src.main.repository.PluginModuleRepository;
import com.src.main.repository.ProjectRepository;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.util.ProjectMetaDataConstants;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;
import com.src.main.common.util.JsonYamlConverterUtil;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;

@Service
public class ProjectServiceImpl implements ProjectService {
	private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);
	private static final String REQUEST_STATUS_PENDING = "PENDING";
	private static final String REQUEST_STATUS_ACCEPTED = "ACCEPTED";
	private static final String REQUEST_STATUS_REJECTED = "REJECTED";
	private static final String REQUEST_STATUS_ARCHIVED = "ARCHIVED";
	private static final Set<String> SHIPPABLE_MODULE_KEYS = Set.of("rbac", "auth", "state-machine", "subscription", "swagger", "cdn");
	private final ProjectRepository repo;
	private final ProjectRunRepository projectRunRepository;
	private final ProjectCollaborationRequestRepository projectCollaborationRequestRepository;
	private final ProjectContributorRepository projectContributorRepository;
	private final ProjectDraftVersionRepository projectDraftVersionRepository;
	private final PluginModuleRepository pluginModuleRepository;
	private final ProjectUserIdentityService projectUserIdentityService;
	private final ProjectYamlService projectYamlService;
	private final ProjectDraftService projectDraftService;
	private final ProjectDraftSpecMapperService projectDraftSpecMapperService;
	private final ProjectNameValidationService projectNameValidationService;
	private final ProjectCollaborationService projectCollaborationService;
	private final RbacService rbacService;
	private final Validator validator;


	record Input(@NotBlank String yaml) {
	}

	private record ResolvedProjectDraft(
			Map<String, Object> draftData,
			Map<String, Object> spec,
			String yamlText,
			String artifact,
			String groupId,
			String version,
			String buildTool,
			String packaging,
			String generator,
			String name,
			String description,
			String springBootVersion,
			String jdkVersion) {
	}


	private record DraftDiffResult(List<String> addedPaths, List<String> removedPaths, List<String> changedPaths) {
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
			throw new GenericException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (Exception e) {
			log.error("Unexpected error creating project", e);
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create project.", e);
		}
	}

	@Override
	@Transactional
	public ProjectDraftResponseDTO createDraft(ProjectDraftUpsertRequestDTO request, String ownerId) {
		try {
			if (!rbacService.currentUserHasPermission("project.create")) {
				throw new SecurityException("User not allowed to create projects");
			}
			ResolvedProjectDraft resolvedDraft = resolveProjectDraft(request, ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR);
			ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
			projectNameValidationService.ensureUniqueProjectName(resolvedDraft.name(), currentUser, null);
			ProjectEntity p = new ProjectEntity();
			p.setId(UUID.randomUUID());
			applyResolvedProjectDraft(p, resolvedDraft, request.getDraftVersion());
			p.setOwnerId(currentUser.userId());
			p.setCreatedAt(OffsetDateTime.now());
			p.setUpdatedAt(OffsetDateTime.now());
			ProjectEntity savedProject = repo.saveAndFlush(p);
			saveDraftSnapshot(savedProject, currentUser.userId(), null);
			return new ProjectDraftResponseDTO(savedProject.getId().toString(), savedProject.getDraftVersion());
		} catch (GenericException e) {
			throw e;
		} catch (IllegalArgumentException | ConstraintViolationException e) {
			throw new GenericException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (Exception e) {
			log.error("Unexpected error creating project draft", e);
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create project draft.", e);
		}
	}

	@Override
	@Transactional
	public ProjectDraftResponseDTO updateDraft(UUID projectId, ProjectDraftUpsertRequestDTO request, String ownerId) {
		if (!rbacService.currentUserHasPermission("project.update")) {
			throw new SecurityException("User not allowed to update projects");
		}
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		ProjectEntity project = getProjectForDraftUpdate(projectId, currentUser, true);
		ResolvedProjectDraft resolvedDraft = resolveProjectDraft(request, project.getGenerator());
		String updatedProjectName = resolvedDraft.name();
		String currentProjectName = project.getName() == null ? "" : project.getName().trim();
		String normalizedUpdatedProjectName = updatedProjectName == null ? "" : updatedProjectName.trim();
		if (!currentProjectName.equalsIgnoreCase(normalizedUpdatedProjectName)) {
			projectNameValidationService.ensureUniqueProjectName(updatedProjectName, currentUser, projectId);
		}
		project.setOwnerId(currentUser.userId());
		Integer currentDraftVersion = project.getDraftVersion() == null ? 1 : project.getDraftVersion();
		Integer requestedDraftVersion = request.getDraftVersion() == null ? currentDraftVersion : request.getDraftVersion();
		assertExactDraftVersionMatch(currentDraftVersion, requestedDraftVersion);
		applyResolvedProjectDraft(project, resolvedDraft, currentDraftVersion + 1);
		project.setUpdatedAt(OffsetDateTime.now());
		ProjectEntity savedProject = repo.saveAndFlush(project);
		saveDraftSnapshot(savedProject, currentUser.userId(), null);
		return new ProjectDraftResponseDTO(savedProject.getId().toString(), savedProject.getDraftVersion());
	}

	@Override
	@Transactional
	public ProjectDraftResponseDTO patchDraftTab(UUID projectId, ProjectDraftTabPatchRequestDTO request, String ownerId) {
		if (!rbacService.currentUserHasPermission("project.update")) {
			throw new SecurityException("User not allowed to update projects");
		}
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		ProjectEntity project = getProjectForDraftUpdate(projectId, currentUser, true);
		Integer currentDraftVersion = project.getDraftVersion() == null ? 1 : project.getDraftVersion();
		assertExactDraftVersionMatch(currentDraftVersion, request.getDraftVersion());
		Map<String, Object> existingDraftData = projectDraftService.deserialize(project.getDraftData());
		Map<String, Object> mergedDraftData = projectDraftService.mergeTabData(existingDraftData, request.getTabKey(), request.getTabData(), project.getGenerator());
		ProjectDraftUpsertRequestDTO fullRequest = new ProjectDraftUpsertRequestDTO();
		fullRequest.setDraftData(mergedDraftData);
		fullRequest.setDraftVersion(currentDraftVersion);
		return updateDraft(projectId, fullRequest, ownerId);
	}

	private ResolvedProjectDraft resolveProjectDraft(ProjectDraftUpsertRequestDTO request, String fallbackGenerator) {
		Map<String, Object> draftData = request.getDraftData();
		if (draftData == null) {
			throw new IllegalArgumentException("draftData must be provided");
		}
		validateDraftData(draftData);
		Map<String, Object> spec = projectDraftSpecMapperService.buildSpec(draftData);
		String yamlText = JsonYamlConverterUtil.mapToYaml(spec);
		Map<String, Object> app = projectYamlService.getRequiredAppSection(spec);
		String artifact = projectYamlService.getString(app, ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT);
		String groupId = projectYamlService.getString(app, ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP);
		String version = projectYamlService.getString(app, ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION);
		String buildTool = projectYamlService.getString(app, ProjectMetaDataConstants.BUILD_TOOL, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL);
		String packaging = projectYamlService.getString(app, ProjectMetaDataConstants.PACKAGING, ProjectMetaDataConstants.DEFAULT_PACKAGING);
		String generator = projectDraftService.resolveGenerator(draftData, projectYamlService.getString(app, ProjectMetaDataConstants.GENERATOR, fallbackGenerator));
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
		return new ResolvedProjectDraft(draftData, spec, yamlText, artifact, groupId, version, buildTool, packaging, generator, name, description, springBootVersion, jdkVersion);
	}

	private void validateDraftData(Map<String, Object> draftData) {
		Map<String, Object> settings = asSettingsMap(draftData.get("settings"));
		if (settings.containsKey("projectName") && trimmed(settings.get("projectName")).isBlank()) {
			throw new IllegalArgumentException("Project name is required.");
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asSettingsMap(Object value) {
		return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Collections.emptyMap();
	}

	private String trimmed(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	private void assertExactDraftVersionMatch(Integer currentDraftVersion, Integer requestedDraftVersion) {
		if (requestedDraftVersion == null || !requestedDraftVersion.equals(currentDraftVersion)) {
			throw new GenericException(HttpStatus.CONFLICT, "Project draft is outdated. Reload the latest project state and try again.");
		}
	}

	private void applyResolvedProjectDraft(ProjectEntity project, ResolvedProjectDraft resolvedDraft, Integer draftVersion) {
		project.setArtifact(resolvedDraft.artifact());
		project.setGroupId(resolvedDraft.groupId());
		project.setVersion(resolvedDraft.version());
		project.setBuildTool(resolvedDraft.buildTool());
		project.setPackaging(resolvedDraft.packaging());
		project.setGenerator(resolvedDraft.generator());
		project.setName(resolvedDraft.name());
		project.setDescription(resolvedDraft.description());
		project.setSpringBootVersion(resolvedDraft.springBootVersion());
		project.setJdkVersion(resolvedDraft.jdkVersion());
		project.setYaml(resolvedDraft.yamlText());
		project.setDraftData(projectDraftService.serialize(resolvedDraft.draftData()));
		project.setDraftVersion(draftVersion);
	}

	private String resolveProjectDescription(ProjectEntity project) {
		String description = project.getDescription() == null ? "" : project.getDescription().trim();
		if (!description.isBlank()) {
			return description;
		}
		String draftDataJson = project.getDraftData();
		if (draftDataJson == null || draftDataJson.isBlank()) {
			return description;
		}
		try {
			Map<String, Object> draftData = projectDraftService.deserialize(draftDataJson);
			Map<String, Object> settings = asSettingsMap(draftData.get("settings"));
			String draftDescription = trimmed(settings.get("projectDescription"));
			return draftDescription.isBlank() ? description : draftDescription;
		} catch (Exception ignored) {
			return description;
		}
	}

	@Override
	@Transactional
	public List<ProjectSummaryDTO> list(String userId) {
		if (!rbacService.currentUserHasPermission("project.read")) {
			throw new SecurityException("User not allowed to view projects");
		}
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		log.debug("Listing projects for resolved user: userId=\'{}\', keys={}", currentUser.userId(), currentUser.keys());
		List<ProjectEntity> all = canReadAllProjects() ? repo.findAll() : findAccessibleProjectsForList(currentUser);
		log.debug("Matched accessible projects for resolved user: userId=\'{}\', count={}", currentUser.userId(), all.size());
		List<ProjectSummaryDTO> out = new ArrayList<>();
		for (ProjectEntity p : all) {
			out.add(toSummary(p, currentUser));
		}
		return out;
	}

	private List<ProjectEntity> findAccessibleProjectsForList(ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		List<ProjectEntity> accessible = repo.findAccessibleProjectsByUserKeys(currentUser.keys());
		log.debug("Project list fast query result: userId=\'{}\', keys={}, count={}", currentUser.userId(), currentUser.keys(), accessible.size());
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
		matchedProjects.sort(Comparator.comparing(ProjectEntity::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
		log.debug("Project list fallback match result: userId=\'{}\', keys={}, count={}", currentUser.userId(), currentUser.keys(), matchedProjects.size());
		return matchedProjects;
	}

	private boolean normalizeContributorAliases(ProjectEntity project, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		if (project == null || project.getContributors() == null || project.getContributors().isEmpty()) {
			return false;
		}
		boolean matched = false;
		for (ProjectContributorEntity contributor : project.getContributors()) {
			if (contributor.isDisabled()) {
				continue;
			}
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
		String description = resolveProjectDescription(project);
		boolean canManageAllContributors = canManageAllProjectContributors();
		boolean isOwner = currentUser.keys().contains(project.getOwnerId());
		Optional<ProjectContributorEntity> currentContributor = findContributorAccess(project.getId(), currentUser);
		boolean canManageContributors = isOwner || canManageAllContributors || currentContributor.map(ProjectContributorEntity::isCanManageCollaboration).orElse(false);
		String inviteToken = canManageContributors ? ensureInviteToken(project) : null;
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
		Map<String, Object> draftData = projectDraftService.deserialize(project.getDraftData());
		String generator = projectDraftService.resolveGenerator(draftData, project.getGenerator());
		List<ProjectTabDefinitionDTO> tabDetails = projectDraftService.getTabDetails(
				generator, projectDraftService.resolveSelectedDependencies(draftData), resolveConfigEnabledShippableModuleKeys());
		boolean isContributorAccess = !isOwner && !canManageAllContributors;
		List<ProjectCollaborationRequestDTO> collaborationRequests = canManageContributors
				? toCollaborationRequestDtos(project.getId()) : Collections.emptyList();
		UUID latestRunId = latestRun == null ? null : latestRun.getId();
		String latestRunStatus = latestRun == null || latestRun.getStatus() == null ? null : latestRun.getStatus().name();
		Integer latestRunNumber = latestRun == null ? null : latestRun.getRunNumber();
		return new ProjectDetailsDTO(
				project.getId().toString(), project.getId(), project.getName(), description,
				generator, project.getArtifact(), null, null, project.getDraftVersion(), tabDetails,
				project.getOwnerId(), isContributorAccess, canManageContributors, inviteToken,
				toContributorDtos(project.getId()), collaborationRequests,
				latestRunId, latestRunStatus, latestRunNumber,
				latestRunWithZip != null, encodeZipBase64(latestRunWithZip), buildZipFileName(project),
				project.getCreatedAt(), project.getUpdatedAt());
	}

	@Override
	public ProjectDraftTabDataDTO getDraftTabData(UUID projectId, String tabKey, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectEntity project = getAccessibleProject(projectId, currentUser);
		Map<String, Object> draftData = projectDraftService.deserialize(project.getDraftData());
		String generator = projectDraftService.resolveGenerator(draftData, project.getGenerator());
		Map<String, Object> tabData = projectDraftService.extractTabData(draftData, tabKey, generator);
		return new ProjectDraftTabDataDTO(tabKey, tabData);
	}

	@Override
	public List<ProjectTabDefinitionDTO> getTabDetails(String generator, List<String> dependencies) {
		return projectDraftService.getTabDetails(generator, dependencies, resolveConfigEnabledShippableModuleKeys());
	}

	@Override
	public List<ProjectTabDefinitionDTO> getTabDetails(String generator, List<String> dependencies, String tabKey) {
		return projectDraftService.getTabDetails(generator, dependencies, resolveConfigEnabledShippableModuleKeys(), tabKey);
	}

	private Set<String> resolveConfigEnabledShippableModuleKeys() {
		List<com.src.main.model.PluginModuleEntity> modules = pluginModuleRepository.findAllByOrderByNameAsc();
		if (modules == null) {
			return Set.of();
		}
		return modules.stream()
				.filter(module -> module != null && module.isEnabled() && module.isEnableConfig())
				.map(module -> module.getCode() == null ? "" : module.getCode().trim().toLowerCase())
				.filter(SHIPPABLE_MODULE_KEYS::contains)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProjectDraftVersionSummaryDTO> getDraftVersions(UUID projectId, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		getAccessibleProject(projectId, currentUser);
		return projectDraftVersionRepository.findByProjectIdOrderByDraftVersionDesc(projectId).stream().map(this::toDraftVersionSummary).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectDraftVersionDetailsDTO getDraftVersion(UUID projectId, UUID versionId, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		getAccessibleProject(projectId, currentUser);
		ProjectDraftVersionEntity version = getDraftVersionEntity(projectId, versionId);
		return toDraftVersionDetails(version);
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectDraftVersionDiffDTO diffDraftVersion(UUID projectId, UUID versionId, UUID compareToVersionId, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectEntity project = getAccessibleProject(projectId, currentUser);
		ProjectDraftVersionEntity targetVersion = getDraftVersionEntity(projectId, versionId);
		ProjectDraftVersionEntity baseVersion = compareToVersionId == null ? null : getDraftVersionEntity(projectId, compareToVersionId);
		Map<String, Object> baseDraft = baseVersion == null ? projectDraftService.deserialize(project.getDraftData()) : projectDraftService.deserialize(baseVersion.getDraftData());
		Map<String, Object> targetDraft = projectDraftService.deserialize(targetVersion.getDraftData());
		DraftDiffResult diff = diffDraftData(baseDraft, targetDraft);
		return new ProjectDraftVersionDiffDTO(baseVersion == null ? null : baseVersion.getId(), baseVersion == null ? project.getDraftVersion() : baseVersion.getDraftVersion(), targetVersion.getId(), targetVersion.getDraftVersion(), diff.addedPaths(), diff.removedPaths(), diff.changedPaths());
	}

	@Override
	@Transactional
	public ProjectDraftResponseDTO restoreDraftVersion(UUID projectId, UUID versionId, String userId) {
		if (!rbacService.currentUserHasPermission("project.update")) {
			throw new SecurityException("User not allowed to update projects");
		}
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectEntity project = getProjectForDraftUpdate(projectId, currentUser, true);
		ProjectDraftVersionEntity version = getDraftVersionEntity(projectId, versionId);
		Map<String, Object> draftData = projectDraftService.deserialize(version.getDraftData());
		ProjectDraftUpsertRequestDTO request = new ProjectDraftUpsertRequestDTO();
		request.setDraftData(draftData);
		ResolvedProjectDraft resolvedDraft = resolveProjectDraft(request, project.getGenerator());
		String updatedProjectName = resolvedDraft.name();
		String currentProjectName = project.getName() == null ? "" : project.getName().trim();
		String normalizedUpdatedProjectName = updatedProjectName == null ? "" : updatedProjectName.trim();
		if (!currentProjectName.equalsIgnoreCase(normalizedUpdatedProjectName)) {
			projectNameValidationService.ensureUniqueProjectName(updatedProjectName, currentUser, projectId);
		}
		project.setOwnerId(currentUser.userId());
		Integer currentDraftVersion = project.getDraftVersion() == null ? 1 : project.getDraftVersion();
		applyResolvedProjectDraft(project, resolvedDraft, currentDraftVersion + 1);
		project.setUpdatedAt(OffsetDateTime.now());
		ProjectEntity savedProject = repo.saveAndFlush(project);
		saveDraftSnapshot(savedProject, currentUser.userId(), version.getId());
		return new ProjectDraftResponseDTO(savedProject.getId().toString(), savedProject.getDraftVersion());
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

	private void saveDraftSnapshot(ProjectEntity project, String userId, UUID restoredFromVersionId) {
		if (project == null || project.getId() == null || project.getDraftVersion() == null) {
			return;
		}
		ProjectDraftVersionEntity snapshot = new ProjectDraftVersionEntity();
		snapshot.setProject(project);
		snapshot.setDraftVersion(project.getDraftVersion());
		snapshot.setDraftData(project.getDraftData() == null ? "{}" : project.getDraftData());
		snapshot.setYaml(project.getYaml() == null ? "" : project.getYaml());
		snapshot.setGenerator(project.getGenerator());
		snapshot.setCreatedByUserId(userId == null || userId.isBlank() ? "system" : userId.trim());
		snapshot.setRestoredFromVersionId(restoredFromVersionId);
		snapshot.setCreatedAt(OffsetDateTime.now());
		projectDraftVersionRepository.save(snapshot);
	}

	private ProjectDraftVersionEntity getDraftVersionEntity(UUID projectId, UUID versionId) {
		return projectDraftVersionRepository.findByIdAndProjectId(versionId, projectId).orElseThrow(() -> new IllegalArgumentException("Draft version not found"));
	}

	private ProjectDraftVersionSummaryDTO toDraftVersionSummary(ProjectDraftVersionEntity version) {
		return new ProjectDraftVersionSummaryDTO(version.getId(), version.getDraftVersion(), version.getGenerator(), version.getCreatedByUserId(), version.getRestoredFromVersionId(), version.getCreatedAt());
	}

	private ProjectDraftVersionDetailsDTO toDraftVersionDetails(ProjectDraftVersionEntity version) {
		return new ProjectDraftVersionDetailsDTO(version.getId(), version.getDraftVersion(), version.getGenerator(), version.getCreatedByUserId(), version.getRestoredFromVersionId(), version.getCreatedAt(), version.getYaml(), projectDraftService.deserialize(version.getDraftData()));
	}

	private DraftDiffResult diffDraftData(Map<String, Object> baseDraft, Map<String, Object> targetDraft) {
		Set<String> addedPaths = new LinkedHashSet<>();
		Set<String> removedPaths = new LinkedHashSet<>();
		Set<String> changedPaths = new LinkedHashSet<>();
		compareNodes("", baseDraft, targetDraft, addedPaths, removedPaths, changedPaths);
		return new DraftDiffResult(List.copyOf(addedPaths), List.copyOf(removedPaths), List.copyOf(changedPaths));
	}

	@SuppressWarnings("unchecked")
	private void compareNodes(String path, Object baseValue, Object targetValue, Set<String> addedPaths, Set<String> removedPaths, Set<String> changedPaths) {
		if (baseValue == null && targetValue == null) {
			return;
		}
		String normalizedPath = path == null || path.isBlank() ? "$" : path;
		if (baseValue == null) {
			addedPaths.add(normalizedPath);
			return;
		}
		if (targetValue == null) {
			removedPaths.add(normalizedPath);
			return;
		}
		if (baseValue instanceof Map<?, ?> baseMap && targetValue instanceof Map<?, ?> targetMap) {
			Set<String> keys = new LinkedHashSet<>();
			baseMap.keySet().forEach(key -> keys.add(String.valueOf(key)));
			targetMap.keySet().forEach(key -> keys.add(String.valueOf(key)));
			for (String key : keys) {
				String childPath = "$".equals(normalizedPath) ? key : normalizedPath + "." + key;
				compareNodes(childPath, ((Map<String, Object>) baseMap).get(key), ((Map<String, Object>) targetMap).get(key), addedPaths, removedPaths, changedPaths);
			}
			return;
		}
		if (baseValue instanceof List<?> baseList && targetValue instanceof List<?> targetList) {
			int maxSize = Math.max(baseList.size(), targetList.size());
			for (int index = 0; index < maxSize; index++) {
				Object baseItem = index < baseList.size() ? baseList.get(index) : null;
				Object targetItem = index < targetList.size() ? targetList.get(index) : null;
				String childPath = normalizedPath + "[" + index + "]";
				compareNodes(childPath, baseItem, targetItem, addedPaths, removedPaths, changedPaths);
			}
			return;
		}
		if (!java.util.Objects.equals(baseValue, targetValue)) {
			changedPaths.add(normalizedPath);
		}
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
		ProjectEntity project = getProjectForContributorManagement(projectId, currentUser, false);
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
			contributor.setCanEditDraft(true);
			contributor.setCanGenerate(true);
			contributor.setCanManageCollaboration(false);
			contributor.setDisabled(false);
			contributor.setDisabledAt(null);
			projectContributorRepository.save(contributor);
		} else {
			projectContributorRepository.findFirstByProjectIdAndUserId(projectId, contributorUserId).ifPresent(contributor -> {
				contributor.setDisabled(false);
				contributor.setDisabledAt(null);
				contributor.setCanEditDraft(true);
				contributor.setCanGenerate(true);
				contributor.setCanManageCollaboration(false);
				projectContributorRepository.save(contributor);
			});
		}
		return getContributors(projectId, currentUser.userId());
	}

	@Override
	@Transactional
	public List<ProjectContributorDTO> updateContributorPermissions(UUID projectId, UUID contributorId, String ownerId, ProjectContributorPermissionUpdateDTO request) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		getProjectForContributorManagement(projectId, currentUser, true);
		ProjectContributorEntity contributor = projectContributorRepository.findByIdAndProjectId(contributorId, projectId).orElseThrow(() -> new IllegalArgumentException("Contributor not found"));
		if (contributor.isDisabled()) {
			throw new IllegalArgumentException("Contributor is archived");
		}
		contributor.setCanEditDraft(request.isCanEditDraft());
		contributor.setCanGenerate(request.isCanGenerate());
		contributor.setCanManageCollaboration(request.isCanManageCollaboration());
		projectContributorRepository.save(contributor);
		return getContributors(projectId, currentUser.userId());
	}

	@Override
	@Transactional
	public void removeContributor(UUID projectId, String ownerId, String contributorUserId) {
		if (!rbacService.currentUserHasPermission("project.contributor.manage")) {
			throw new SecurityException("User not allowed to manage contributors");
		}
		getProjectForContributorManagement(projectId, projectUserIdentityService.resolve(ownerId), false);
		String normalizedUserId = contributorUserId == null ? "" : contributorUserId.trim();
		if (normalizedUserId.isEmpty()) {
			throw new IllegalArgumentException("contributorUserId is required");
		}
		projectContributorRepository.deleteByProjectIdAndUserId(projectId, normalizedUserId);
	}

	@Override
	@Transactional
	public void detachContributor(UUID projectId, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		getAccessibleProject(projectId, currentUser);
		ProjectContributorEntity contributor = findContributorAccess(projectId, currentUser).orElseThrow(() -> new SecurityException("Contributor access not found"));
		contributor.setDisabled(true);
		contributor.setDisabledAt(OffsetDateTime.now());
		projectContributorRepository.save(contributor);
		projectCollaborationService.clearUserSessions(projectId, currentUser.userId());
	}

	@Override
	@Transactional(readOnly = true)
	public List<ArchivedProjectCollaborationDTO> getArchivedCollaborations(String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		return projectContributorRepository.findByUserIdInAndDisabledTrueOrderByDisabledAtDesc(currentUser.keys())
				.stream()
				.map(contributor -> new ArchivedProjectCollaborationDTO(
						contributor.getId(), contributor.getProject().getId(),
						contributor.getProject().getName(), contributor.getProject().getOwnerId(),
						contributor.getProject().getGenerator(), contributor.getProject().getInviteToken(),
						contributor.getDisabledAt()))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public ProjectCollaborationRequestDTO resubscribeArchivedCollaboration(UUID contributorId, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectContributorEntity contributor = projectContributorRepository.findById(contributorId).orElseThrow(() -> new IllegalArgumentException("Archived collaboration not found"));
		if (!contributor.isDisabled() || !matchesUserKey(contributor.getUserId(), currentUser.keys())) {
			throw new SecurityException("User not allowed to resubscribe this collaboration");
		}
		ProjectCollaborationRequestCreateDTO request = new ProjectCollaborationRequestCreateDTO();
		request.setCanEditDraft(contributor.isCanEditDraft());
		request.setCanGenerate(contributor.isCanGenerate());
		request.setCanManageCollaboration(contributor.isCanManageCollaboration());
		return requestCollaboration(contributor.getProject().getInviteToken(), currentUser.userId(), request);
	}

	@Override
	public List<ProjectCollaborationRequestDTO> getCollaborationRequests(UUID projectId, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		getProjectForContributorManagement(projectId, currentUser, false);
		return toCollaborationRequestDtos(projectId);
	}

	@Override
	@Transactional
	public ProjectCollaborationRequestDTO reviewCollaborationRequest(UUID projectId, UUID requestId, String ownerId, ProjectCollaborationRequestReviewDTO request) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(ownerId);
		ProjectEntity project = getProjectForContributorManagement(projectId, currentUser, true);
		ProjectCollaborationRequestEntity collaborationRequest = projectCollaborationRequestRepository.findByIdAndProjectId(requestId, projectId).orElseThrow(() -> new IllegalArgumentException("Collaboration request not found"));
		String status = trimmed(request.getStatus()).toUpperCase();
		if (!REQUEST_STATUS_ACCEPTED.equals(status) && !REQUEST_STATUS_REJECTED.equals(status)) {
			throw new IllegalArgumentException("status must be ACCEPTED or REJECTED");
		}
		collaborationRequest.setStatus(status);
		collaborationRequest.setReviewedBy(currentUser.userId());
		collaborationRequest.setReviewedAt(OffsetDateTime.now());
		collaborationRequest.setGrantedCanEditDraft(REQUEST_STATUS_ACCEPTED.equals(status) && request.isCanEditDraft());
		collaborationRequest.setGrantedCanGenerate(REQUEST_STATUS_ACCEPTED.equals(status) && request.isCanGenerate());
		collaborationRequest.setGrantedCanManageCollaboration(REQUEST_STATUS_ACCEPTED.equals(status) && request.isCanManageCollaboration());
		if (REQUEST_STATUS_ACCEPTED.equals(status)) {
			ProjectContributorEntity contributor = projectContributorRepository.findFirstByProjectIdAndUserIdIn(projectId, List.of(collaborationRequest.getRequesterId())).orElseGet(() -> {
				ProjectContributorEntity created = new ProjectContributorEntity();
				created.setProject(project);
				created.setUserId(collaborationRequest.getRequesterId());
				return created;
			});
			contributor.setDisabled(false);
			contributor.setDisabledAt(null);
			contributor.setCanEditDraft(collaborationRequest.isGrantedCanEditDraft());
			contributor.setCanGenerate(collaborationRequest.isGrantedCanGenerate());
			contributor.setCanManageCollaboration(collaborationRequest.isGrantedCanManageCollaboration());
			projectContributorRepository.save(contributor);
		}
		return toCollaborationRequestDto(projectCollaborationRequestRepository.save(collaborationRequest));
	}

	@Override
	@Transactional
	public ProjectCollaborationInviteDTO getCollaborationInvite(String inviteToken, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectEntity project = getProjectByInviteToken(inviteToken);
		Optional<ProjectContributorEntity> contributor = findContributorAccess(project.getId(), currentUser);
		boolean isOwner = currentUser.keys().contains(project.getOwnerId());
		boolean hasAccess = isOwner || contributor.isPresent();
		boolean requestPending = projectCollaborationRequestRepository.findFirstByProjectIdAndRequesterIdOrderByCreatedAtDesc(project.getId(), currentUser.userId()).map(item -> REQUEST_STATUS_PENDING.equalsIgnoreCase(item.getStatus())).orElse(false);
		return new ProjectCollaborationInviteDTO(project.getInviteToken(), project.getId().toString(), project.getName(), project.getGenerator(), project.getOwnerId(), hasAccess, requestPending);
	}

	@Override
	@Transactional
	public ProjectCollaborationRequestDTO requestCollaboration(String inviteToken, String userId, ProjectCollaborationRequestCreateDTO request) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectEntity project = getProjectByInviteToken(inviteToken);
		if (currentUser.keys().contains(project.getOwnerId())) {
			throw new IllegalArgumentException("Project owner already has access");
		}
		if (findContributorAccess(project.getId(), currentUser).isPresent()) {
			throw new IllegalArgumentException("You already have contributor access to this project");
		}
		projectContributorRepository.findFirstByProjectIdAndUserIdIn(project.getId(), currentUser.keys()).filter(ProjectContributorEntity::isDisabled).ifPresent(contributor -> archivePendingRequests(project.getId(), contributor.getUserId()));
		ProjectCollaborationRequestEntity collaborationRequest = projectCollaborationRequestRepository.findFirstByProjectIdAndRequesterIdOrderByCreatedAtDesc(project.getId(), currentUser.userId()).filter(existing -> REQUEST_STATUS_PENDING.equalsIgnoreCase(existing.getStatus()) || REQUEST_STATUS_ARCHIVED.equalsIgnoreCase(existing.getStatus())).orElseGet(ProjectCollaborationRequestEntity::new);
		collaborationRequest.setProject(project);
		collaborationRequest.setRequesterId(currentUser.userId());
		collaborationRequest.setStatus(REQUEST_STATUS_PENDING);
		collaborationRequest.setRequestedCanEditDraft(request.isCanEditDraft());
		collaborationRequest.setRequestedCanGenerate(request.isCanGenerate());
		collaborationRequest.setRequestedCanManageCollaboration(request.isCanManageCollaboration());
		collaborationRequest.setGrantedCanEditDraft(false);
		collaborationRequest.setGrantedCanGenerate(false);
		collaborationRequest.setGrantedCanManageCollaboration(false);
		collaborationRequest.setReviewedBy(null);
		collaborationRequest.setReviewedAt(null);
		return toCollaborationRequestDto(projectCollaborationRequestRepository.save(collaborationRequest));
	}

	@Override
	@Transactional
	public ProjectSummaryDTO importProject(ProjectImportRequestDTO request, String userId) {
		ProjectUserIdentityService.ResolvedProjectUser currentUser = projectUserIdentityService.resolve(userId);
		ProjectEntity project = getProjectByInviteToken(extractInviteToken(request == null ? null : request.getProjectUrl()));
		if (currentUser.keys().contains(project.getOwnerId())) {
			return toSummary(project, currentUser);
		}
		if (findContributorAccess(project.getId(), currentUser).isEmpty()) {
			throw new SecurityException("You do not have contributor access to this project.");
		}
		return toSummary(project, currentUser);
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
		ProjectEntity project = repo.findWithContributorsById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (canReadAllProjects() || currentUser.keys().contains(project.getOwnerId()) || projectContributorRepository.existsByProjectIdAndUserIdInAndDisabledFalse(projectId, currentUser.keys())) {
			return project;
		}
		throw new SecurityException("User not allowed to access this project");
	}

	private ProjectEntity getProjectForContributorManagement(UUID projectId, ProjectUserIdentityService.ResolvedProjectUser currentUser, boolean forUpdate) {
		ProjectEntity project = (forUpdate ? repo.findWithContributorsByIdForUpdate(projectId) : repo.findWithContributorsById(projectId)).orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (!canManageAllProjectContributors() && !currentUser.keys().contains(project.getOwnerId()) && !findContributorAccess(project.getId(), currentUser).map(ProjectContributorEntity::isCanManageCollaboration).orElse(false)) {
			throw new SecurityException("Only the project owner can manage contributors");
		}
		return project;
	}

	private ProjectEntity getProjectForDraftUpdate(UUID projectId, ProjectUserIdentityService.ResolvedProjectUser currentUser, boolean forUpdate) {
		ProjectEntity project = (forUpdate ? repo.findWithContributorsByIdForUpdate(projectId) : repo.findWithContributorsById(projectId)).orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		reassignOwnerIfNeeded(project, currentUser);
		if (currentUser.keys().contains(project.getOwnerId()) || findContributorAccess(project.getId(), currentUser).map(ProjectContributorEntity::isCanEditDraft).orElse(false)) {
			return project;
		}
		throw new SecurityException("User not allowed to modify this project");
	}

	private ProjectEntity getProjectForDelete(UUID projectId, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		ProjectEntity project = repo.findWithContributorsById(projectId).orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
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

	private Optional<ProjectContributorEntity> findContributorAccess(UUID projectId, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		if (currentUser == null || currentUser.keys() == null || currentUser.keys().isEmpty()) {
			return Optional.empty();
		}
		return projectContributorRepository.findFirstByProjectIdAndUserIdInAndDisabledFalse(projectId, currentUser.keys());
	}

	private String ensureInviteToken(ProjectEntity project) {
		if (project == null) {
			return null;
		}
		String inviteToken = trimmed(project.getInviteToken());
		if (!inviteToken.isBlank()) {
			return inviteToken;
		}
		project.setInviteToken(UUID.randomUUID().toString());
		return repo.saveAndFlush(project).getInviteToken();
	}

	private ProjectEntity getProjectByInviteToken(String inviteToken) {
		String normalizedToken = trimmed(inviteToken);
		if (normalizedToken.isBlank()) {
			throw new IllegalArgumentException("inviteToken is required");
		}
		ProjectEntity project = repo.findByInviteToken(normalizedToken).orElseThrow(() -> new IllegalArgumentException("Invalid collaboration invite"));
		if (!rbacService.currentUserHasPermission("project.read")) {
			throw new SecurityException("User not allowed to access projects");
		}
		return project;
	}

	private String extractInviteToken(String projectUrl) {
		String normalizedUrl = trimmed(projectUrl);
		if (normalizedUrl.isBlank()) {
			throw new IllegalArgumentException("Enter a valid project URL.");
		}
		String tokenFromPath = extractInviteTokenFromLocation(normalizedUrl);
		if (!tokenFromPath.isBlank()) {
			return tokenFromPath;
		}
		try {
			URI uri = new URI(normalizedUrl);
			String tokenFromFragment = extractInviteTokenFromLocation(uri.getFragment());
			if (!tokenFromFragment.isBlank()) {
				return tokenFromFragment;
			}
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException("Enter a valid project URL.");
		}
		throw new IllegalArgumentException("Enter a valid project URL.");
	}

	private String extractInviteTokenFromLocation(String location) {
		String normalizedLocation = trimmed(location);
		if (normalizedLocation.isBlank()) {
			return "";
		}
		String[] locationPatterns = {"/project-collaboration/", "project-collaboration/"};
		for (String pattern : locationPatterns) {
			int patternIndex = normalizedLocation.indexOf(pattern);
			if (patternIndex < 0) {
				continue;
			}
			String tail = normalizedLocation.substring(patternIndex + pattern.length()).trim();
			if (tail.isBlank()) {
				return "";
			}
			int delimiterIndex = firstDelimiterIndex(tail);
			return (delimiterIndex >= 0 ? tail.substring(0, delimiterIndex) : tail).trim();
		}
		return "";
	}

	private int firstDelimiterIndex(String value) {
		int firstIndex = -1;
		for (char delimiter : new char[] {'/', '?', '#', '&'}) {
			int index = value.indexOf(delimiter);
			if (index < 0) {
				continue;
			}
			if (firstIndex < 0 || index < firstIndex) {
				firstIndex = index;
			}
		}
		return firstIndex;
	}

	private List<ProjectCollaborationRequestDTO> toCollaborationRequestDtos(UUID projectId) {
		return projectCollaborationRequestRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream().map(this::toCollaborationRequestDto).collect(Collectors.toList());
	}

	private ProjectCollaborationRequestDTO toCollaborationRequestDto(ProjectCollaborationRequestEntity request) {
		ProjectContributorPermissionsDTO requestedPermissions = new ProjectContributorPermissionsDTO(
				request.isRequestedCanEditDraft(), request.isRequestedCanGenerate(),
				request.isRequestedCanManageCollaboration());
		ProjectContributorPermissionsDTO grantedPermissions = new ProjectContributorPermissionsDTO(
				request.isGrantedCanEditDraft(), request.isGrantedCanGenerate(),
				request.isGrantedCanManageCollaboration());
		return new ProjectCollaborationRequestDTO(
				request.getId(), request.getRequesterId(), request.getStatus(),
				requestedPermissions, grantedPermissions,
				request.getReviewedBy(), request.getReviewedAt(),
				request.getCreatedAt(), request.getUpdatedAt());
	}

	private List<ProjectContributorDTO> toContributorDtos(UUID projectId) {
		List<ProjectContributorEntity> contributors = projectContributorRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
		return contributors.stream()
				.filter(contributor -> !contributor.isDisabled())
				.map(contributor -> new ProjectContributorDTO(
						contributor.getId(), contributor.getUserId(),
						contributor.isCanEditDraft(), contributor.isCanGenerate(),
						contributor.isCanManageCollaboration(), contributor.isDisabled(),
						contributor.getDisabledAt(), contributor.getCreatedAt()))
				.collect(Collectors.toList());
	}

	private void archivePendingRequests(UUID projectId, String requesterId) {
		projectCollaborationRequestRepository.findFirstByProjectIdAndRequesterIdOrderByCreatedAtDesc(projectId, requesterId).ifPresent(existing -> {
			if (REQUEST_STATUS_PENDING.equalsIgnoreCase(existing.getStatus())) {
				existing.setStatus(REQUEST_STATUS_ARCHIVED);
				existing.setReviewedBy(requesterId);
				existing.setReviewedAt(OffsetDateTime.now());
				projectCollaborationRequestRepository.save(existing);
			}
		});
	}

	private ProjectSummaryDTO toSummary(ProjectEntity project, ProjectUserIdentityService.ResolvedProjectUser currentUser) {
		boolean contributorAccess = findContributorAccess(project.getId(), currentUser).isPresent();
		return new ProjectSummaryDTO(
				project.getId().toString(), project.getArtifact(), project.getId(),
				project.getName(), project.getDescription(), project.getGenerator(),
				project.getCreatedAt(), project.getUpdatedAt(),
				project.getOwnerId(), contributorAccess);
	}

	public ProjectServiceImpl(final ProjectRepository repo, final ProjectRunRepository projectRunRepository, final ProjectCollaborationRequestRepository projectCollaborationRequestRepository, final ProjectContributorRepository projectContributorRepository, final ProjectDraftVersionRepository projectDraftVersionRepository, final PluginModuleRepository pluginModuleRepository, final ProjectUserIdentityService projectUserIdentityService, final ProjectYamlService projectYamlService, final ProjectDraftService projectDraftService, final ProjectDraftSpecMapperService projectDraftSpecMapperService, final ProjectNameValidationService projectNameValidationService, final ProjectCollaborationService projectCollaborationService, final RbacService rbacService, final Validator validator) {
		this.repo = repo;
		this.projectRunRepository = projectRunRepository;
		this.projectCollaborationRequestRepository = projectCollaborationRequestRepository;
		this.projectContributorRepository = projectContributorRepository;
		this.projectDraftVersionRepository = projectDraftVersionRepository;
		this.pluginModuleRepository = pluginModuleRepository;
		this.projectUserIdentityService = projectUserIdentityService;
		this.projectYamlService = projectYamlService;
		this.projectDraftService = projectDraftService;
		this.projectDraftSpecMapperService = projectDraftSpecMapperService;
		this.projectNameValidationService = projectNameValidationService;
		this.projectCollaborationService = projectCollaborationService;
		this.rbacService = rbacService;
		this.validator = validator;
	}
}
