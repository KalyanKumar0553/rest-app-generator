package com.src.main.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ArchivedProjectCollaborationDTO;
import com.src.main.dto.ProjectContributorDTO;
import com.src.main.dto.ProjectContributorUpsertRequestDTO;
import com.src.main.dto.ProjectCollaborationActionRequestDTO;
import com.src.main.dto.ProjectCollaborationInviteDTO;
import com.src.main.dto.ProjectCollaborationPresenceResponseDTO;
import com.src.main.dto.ProjectCollaborationRequestCreateDTO;
import com.src.main.dto.ProjectCollaborationRequestDTO;
import com.src.main.dto.ProjectCollaborationRequestReviewDTO;
import com.src.main.dto.ProjectCollaborationStateDTO;
import com.src.main.dto.ProjectDraftResponseDTO;
import com.src.main.dto.ProjectDraftVersionDetailsDTO;
import com.src.main.dto.ProjectDraftVersionDiffDTO;
import com.src.main.dto.ProjectDraftVersionSummaryDTO;
import com.src.main.dto.ProjectDraftTabDataDTO;
import com.src.main.dto.ProjectDraftTabPatchRequestDTO;
import com.src.main.dto.ProjectDraftUpsertRequestDTO;
import com.src.main.dto.ProjectDetailsDTO;
import com.src.main.dto.ProjectEditorPresenceRequestDTO;
import com.src.main.dto.ProjectImportRequestDTO;
import com.src.main.dto.ProjectContributorPermissionUpdateDTO;
import com.src.main.dto.ProjectRunDetailsResponseDTO;
import com.src.main.dto.ProjectStageRetryRequestDTO;
import com.src.main.dto.ProjectSummaryDTO;
import com.src.main.dto.ProjectTabDefinitionDTO;
import com.src.main.mapper.ProjectRunMapper;
import com.src.main.model.ProjectRunEntity;
import com.src.main.service.ProjectCollaborationService;
import com.src.main.service.ProjectEventStreamService;
import com.src.main.service.ProjectOrchestrationService;
import com.src.main.service.ProjectService;
import com.src.main.service.ProjectUserIdentityService;
import com.src.main.util.AppConstants;

@RestController
@RequestMapping(AppConstants.API_PROJECTS)
@Validated
public class ProjectController {
	private static final Logger log = LoggerFactory.getLogger(ProjectController.class);
	private final ProjectService service;
	private final ProjectOrchestrationService orchestrationService;
	private final ProjectEventStreamService projectEventStreamService;
	private final ProjectCollaborationService projectCollaborationService;
	private final ProjectUserIdentityService projectUserIdentityService;

	@PostMapping(consumes = {"text/yaml", "application/x-yaml", MediaType.TEXT_PLAIN_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCreateResponseDTO create(@RequestBody String yamlText, Principal principal) {
		return service.create(yamlText, currentUserId(principal));
	}

	@PostMapping(value = "/draft", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectDraftResponseDTO createDraft(@jakarta.validation.Valid @RequestBody ProjectDraftUpsertRequestDTO request, Principal principal) {
		return service.createDraft(request, currentUserId(principal));
	}

	@GetMapping
	public java.util.List<ProjectSummaryDTO> list(Principal principal, Authentication authentication) {
		String principalName = principal == null ? null : principal.getName();
		String jwtSubject = authentication == null ? null : authentication.getName();
		String resolvedUserId = currentUserId(principal);
		log.debug("Project list request: jwtSubject=\'{}\', principalName=\'{}\', resolvedUserId=\'{}\'", jwtSubject, principalName, resolvedUserId);
		List<ProjectSummaryDTO> projects = service.list(resolvedUserId);
		log.debug("Project list response: resolvedUserId=\'{}\', matchedProjectsCount={}", resolvedUserId, projects.size());
		return projects;
	}

	@PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectSummaryDTO importProject(@jakarta.validation.Valid @RequestBody ProjectImportRequestDTO request, Principal principal) {
		return service.importProject(request, currentUserId(principal));
	}

	@GetMapping("/{projectId}")
	public ProjectDetailsDTO get(@PathVariable("projectId") UUID projectId, Principal principal) {
		return service.getDetails(projectId, currentUserId(principal));
	}

	@GetMapping("/{projectId}/draft-tab")
	public ProjectDraftTabDataDTO getDraftTab(@PathVariable("projectId") UUID projectId, @RequestParam("tabKey") String tabKey, Principal principal) {
		return service.getDraftTabData(projectId, tabKey, currentUserId(principal));
	}

	@GetMapping("/{projectId}/draft-versions")
	public List<ProjectDraftVersionSummaryDTO> getDraftVersions(@PathVariable("projectId") UUID projectId, Principal principal) {
		return service.getDraftVersions(projectId, currentUserId(principal));
	}

	@GetMapping("/{projectId}/draft-versions/{versionId}")
	public ProjectDraftVersionDetailsDTO getDraftVersion(@PathVariable("projectId") UUID projectId, @PathVariable("versionId") UUID versionId, Principal principal) {
		return service.getDraftVersion(projectId, versionId, currentUserId(principal));
	}

	@GetMapping("/{projectId}/draft-versions/{versionId}/diff")
	public ProjectDraftVersionDiffDTO diffDraftVersion(@PathVariable("projectId") UUID projectId, @PathVariable("versionId") UUID versionId, @RequestParam(value = "compareToVersionId", required = false) UUID compareToVersionId, Principal principal) {
		return service.diffDraftVersion(projectId, versionId, compareToVersionId, currentUserId(principal));
	}

	@PostMapping("/{projectId}/draft-versions/{versionId}/restore")
	public ProjectDraftResponseDTO restoreDraftVersion(@PathVariable("projectId") UUID projectId, @PathVariable("versionId") UUID versionId, Principal principal) {
		return service.restoreDraftVersion(projectId, versionId, currentUserId(principal));
	}

	@GetMapping("/tab-details")
	public List<ProjectTabDefinitionDTO> getTabDetails(
			@RequestParam(value = "generator", required = false) String generator,
			@RequestParam(value = "dependency", required = false) List<String> dependencies,
			@RequestParam(value = "tabKey", required = false) String tabKey) {
		return service.getTabDetails(generator, dependencies, tabKey);
	}

	@GetMapping("/{projectId}/contributors")
	public List<ProjectContributorDTO> getContributors(@PathVariable("projectId") UUID projectId, Principal principal) {
		return service.getContributors(projectId, currentUserId(principal));
	}

	@PostMapping("/{projectId}/contributors")
	public List<ProjectContributorDTO> addContributor(@PathVariable("projectId") UUID projectId, @jakarta.validation.Valid @RequestBody ProjectContributorUpsertRequestDTO request, Principal principal) {
		return service.addContributor(projectId, currentUserId(principal), request);
	}

	@PatchMapping("/{projectId}/contributors/{contributorId}/permissions")
	public List<ProjectContributorDTO> updateContributorPermissions(@PathVariable("projectId") UUID projectId, @PathVariable("contributorId") UUID contributorId, @jakarta.validation.Valid @RequestBody ProjectContributorPermissionUpdateDTO request, Principal principal) {
		return service.updateContributorPermissions(projectId, contributorId, currentUserId(principal), request);
	}

	@DeleteMapping("/{projectId}/contributors")
	public ResponseEntity<Void> removeContributor(@PathVariable("projectId") UUID projectId, @RequestParam("userId") String contributorUserId, Principal principal) {
		service.removeContributor(projectId, currentUserId(principal), contributorUserId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{projectId}/contributors/detach")
	public ResponseEntity<Void> detachContributor(@PathVariable("projectId") UUID projectId, Principal principal) {
		service.detachContributor(projectId, currentUserId(principal));
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{projectId}")
	public ResponseEntity<Void> deleteProject(@PathVariable("projectId") UUID projectId, Principal principal) {
		service.deleteProject(projectId, currentUserId(principal));
		return ResponseEntity.noContent().build();
	}

	private String currentUserId(Principal principal) {
		return projectUserIdentityService.currentUserId(principal);
	}

	@PutMapping("/{projectId}/spec")
	public ResponseEntity<Void> updateSpec(@PathVariable("projectId") UUID projectId, @RequestBody String yaml, Principal principal) {
		String userId = currentUserId(principal);
		orchestrationService.updateSpec(projectId, userId, yaml);
		return ResponseEntity.noContent().build();
	}

	@PutMapping(value = "/{projectId}/draft", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProjectDraftResponseDTO> updateDraft(@PathVariable("projectId") UUID projectId, @jakarta.validation.Valid @RequestBody ProjectDraftUpsertRequestDTO request, Principal principal) {
		return ResponseEntity.ok(service.updateDraft(projectId, request, currentUserId(principal)));
	}

	@PatchMapping(value = "/{projectId}/draft-tab", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProjectDraftResponseDTO> patchDraftTab(@PathVariable("projectId") UUID projectId, @jakarta.validation.Valid @RequestBody ProjectDraftTabPatchRequestDTO request, Principal principal) {
		return ResponseEntity.ok(service.patchDraftTab(projectId, request, currentUserId(principal)));
	}

	@GetMapping(value = "/{projectId}/collaboration", produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationStateDTO getCollaborationState(@PathVariable("projectId") UUID projectId, Principal principal) {
		service.getAccessibleProject(projectId, currentUserId(principal));
		return projectCollaborationService.getState(projectId);
	}

	@GetMapping(value = "/{projectId}/collaboration/requests", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ProjectCollaborationRequestDTO> getCollaborationRequests(@PathVariable("projectId") UUID projectId, Principal principal) {
		return service.getCollaborationRequests(projectId, currentUserId(principal));
	}

	@PatchMapping(value = "/{projectId}/collaboration/requests/{requestId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationRequestDTO reviewCollaborationRequest(@PathVariable("projectId") UUID projectId, @PathVariable("requestId") UUID requestId, @jakarta.validation.Valid @RequestBody ProjectCollaborationRequestReviewDTO request, Principal principal) {
		return service.reviewCollaborationRequest(projectId, requestId, currentUserId(principal), request);
	}

	@GetMapping(value = "/collaboration/invites/{inviteToken}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationInviteDTO getCollaborationInvite(@PathVariable("inviteToken") String inviteToken, Principal principal) {
		return service.getCollaborationInvite(inviteToken, currentUserId(principal));
	}

	@PostMapping(value = "/collaboration/invites/{inviteToken}/requests", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationRequestDTO requestCollaboration(@PathVariable("inviteToken") String inviteToken, @RequestBody(required = false) ProjectCollaborationRequestCreateDTO request, Principal principal) {
		return service.requestCollaboration(inviteToken, currentUserId(principal), request == null ? new ProjectCollaborationRequestCreateDTO() : request);
	}

	@GetMapping(value = "/collaboration/archived", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ArchivedProjectCollaborationDTO> getArchivedCollaborations(Principal principal) {
		return service.getArchivedCollaborations(currentUserId(principal));
	}

	@PostMapping(value = "/collaboration/archived/{contributorId}/resubscribe", produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationRequestDTO resubscribeArchivedCollaboration(@PathVariable("contributorId") UUID contributorId, Principal principal) {
		return service.resubscribeArchivedCollaboration(contributorId, currentUserId(principal));
	}

	@PostMapping(value = "/{projectId}/collaboration/presence", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationPresenceResponseDTO registerPresence(@PathVariable("projectId") UUID projectId, @RequestBody(required = false) ProjectEditorPresenceRequestDTO request, Principal principal) {
		service.getAccessibleProject(projectId, currentUserId(principal));
		return projectCollaborationService.register(projectId, currentUserId(principal), request == null ? null : request.getSessionId());
	}

	@PutMapping(value = "/{projectId}/collaboration/presence/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationStateDTO heartbeatPresence(@PathVariable("projectId") UUID projectId, @PathVariable("sessionId") String sessionId, Principal principal) {
		service.getAccessibleProject(projectId, currentUserId(principal));
		return projectCollaborationService.heartbeat(projectId, currentUserId(principal), sessionId);
	}

	@DeleteMapping("/{projectId}/collaboration/presence/{sessionId}")
	public ResponseEntity<Void> leavePresence(@PathVariable("projectId") UUID projectId, @PathVariable("sessionId") String sessionId, Principal principal) {
		service.getAccessibleProject(projectId, currentUserId(principal));
		projectCollaborationService.leave(projectId, sessionId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{projectId}/collaboration/actions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ProjectCollaborationStateDTO recordCollaborationAction(@PathVariable("projectId") UUID projectId, @jakarta.validation.Valid @RequestBody ProjectCollaborationActionRequestDTO request, Principal principal) {
		service.getAccessibleProject(projectId, currentUserId(principal));
		return projectCollaborationService.recordAction(projectId, currentUserId(principal), request);
	}

	@PostMapping(value = "/{projectId}/retry-stage", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProjectRunDetailsResponseDTO> retryStage(@PathVariable("projectId") UUID projectId, @jakarta.validation.Valid @RequestBody ProjectStageRetryRequestDTO request, Principal principal) {
		String userId = currentUserId(principal);
		ProjectRunEntity run = orchestrationService.generateCode(projectId, userId);
		projectCollaborationService.recordAction(projectId, userId, buildRetryAction(request, run));
		return ResponseEntity.accepted().body(ProjectRunMapper.toDto(run));
	}

	private ProjectCollaborationActionRequestDTO buildRetryAction(ProjectStageRetryRequestDTO request, ProjectRunEntity run) {
		ProjectCollaborationActionRequestDTO actionRequest = new ProjectCollaborationActionRequestDTO();
		actionRequest.setSessionId("stage-retry-" + run.getId());
		actionRequest.setTabKey("explore");
		actionRequest.setActionType("STAGE_RETRY_REQUESTED");
		actionRequest.setMessage("Queued a fresh generation run after failure in " + request.getStage().trim());
		return actionRequest;
	}

	@PostMapping("/{projectId}/save-and-generate")
	public ResponseEntity<ProjectRunDetailsResponseDTO> saveAndGenerate(@PathVariable("projectId") UUID projectId, @RequestBody String yaml, Principal principal) {
		String userId = currentUserId(principal);
		ProjectRunEntity run = orchestrationService.updateSpecAndGenerate(projectId, userId, yaml);
		return ResponseEntity.accepted().body(ProjectRunMapper.toDto(run));
	}

	@PostMapping("/{projectId}/generate")
	public ResponseEntity<ProjectRunDetailsResponseDTO> generate(@PathVariable("projectId") UUID projectId, Principal principal) {
		String userId = currentUserId(principal);
		ProjectRunEntity run = orchestrationService.generateCode(projectId, userId);
		return ResponseEntity.accepted().body(ProjectRunMapper.toDto(run));
	}

	@GetMapping(value = "/{projectId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamProjectEvents(@PathVariable("projectId") UUID projectId, Principal principal) {
		orchestrationService.getOwnedProject(projectId, currentUserId(principal));
		return projectEventStreamService.subscribe(projectId);
	}

	public ProjectController(final ProjectService service, final ProjectOrchestrationService orchestrationService, final ProjectEventStreamService projectEventStreamService, final ProjectCollaborationService projectCollaborationService, final ProjectUserIdentityService projectUserIdentityService) {
		this.service = service;
		this.orchestrationService = orchestrationService;
		this.projectEventStreamService = projectEventStreamService;
		this.projectCollaborationService = projectCollaborationService;
		this.projectUserIdentityService = projectUserIdentityService;
	}
}
