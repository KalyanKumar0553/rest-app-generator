package com.src.main.service;

import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.src.main.dto.ProjectCollaborationActionDTO;
import com.src.main.dto.ProjectCollaborationActionRequestDTO;
import com.src.main.dto.ProjectCollaborationEditorDTO;
import com.src.main.dto.ProjectCollaborationPresenceResponseDTO;
import com.src.main.dto.ProjectCollaborationStateDTO;

@Service
public class ProjectCollaborationService {

	private static final long SESSION_TTL_SECONDS = 45L;
	private static final int MAX_ACTIONS = 25;

	private final ProjectEventStreamService projectEventStreamService;
	private final Map<UUID, Map<String, SessionPresence>> sessionsByProject = new ConcurrentHashMap<>();
	private final Map<UUID, Deque<ProjectCollaborationActionDTO>> actionsByProject = new ConcurrentHashMap<>();

	public ProjectCollaborationService(ProjectEventStreamService projectEventStreamService) {
		this.projectEventStreamService = projectEventStreamService;
	}

	public ProjectCollaborationPresenceResponseDTO register(UUID projectId, String userId, String requestedSessionId) {
		cleanup(projectId);
		String sessionId = normalizedSessionId(requestedSessionId);
		OffsetDateTime now = OffsetDateTime.now();
		sessionsByProject
				.computeIfAbsent(projectId, ignored -> new ConcurrentHashMap<>())
				.put(sessionId, new SessionPresence(sessionId, userId, now));
		ProjectCollaborationStateDTO state = snapshot(projectId);
		publishPresence(projectId, state);
		return new ProjectCollaborationPresenceResponseDTO(sessionId, state.getActiveEditors(), state.getEditors(), state.getRecentActions());
	}

	public ProjectCollaborationStateDTO heartbeat(UUID projectId, String userId, String sessionId) {
		cleanup(projectId);
		SessionPresence presence = sessionsByProject
				.computeIfAbsent(projectId, ignored -> new ConcurrentHashMap<>())
				.computeIfAbsent(normalizedSessionId(sessionId), key -> new SessionPresence(key, userId, OffsetDateTime.now()));
		presence.userId = userId;
		presence.lastSeenAt = OffsetDateTime.now();
		ProjectCollaborationStateDTO state = snapshot(projectId);
		publishPresence(projectId, state);
		return state;
	}

	public void leave(UUID projectId, String sessionId) {
		Map<String, SessionPresence> sessions = sessionsByProject.get(projectId);
		if (sessions == null) {
			return;
		}
		sessions.remove(normalizedSessionId(sessionId));
		if (sessions.isEmpty()) {
			sessionsByProject.remove(projectId);
		}
		publishPresence(projectId, snapshot(projectId));
	}

	public void clearUserSessions(UUID projectId, String userId) {
		Map<String, SessionPresence> sessions = sessionsByProject.get(projectId);
		if (sessions == null || sessions.isEmpty()) {
			return;
		}
		sessions.entrySet().removeIf(entry -> userId != null && userId.equals(entry.getValue().userId));
		if (sessions.isEmpty()) {
			sessionsByProject.remove(projectId);
		}
		publishPresence(projectId, snapshot(projectId));
	}

	public ProjectCollaborationStateDTO recordAction(UUID projectId, String userId, ProjectCollaborationActionRequestDTO request) {
		touchExistingSession(projectId, userId, request.getSessionId());
		ProjectCollaborationActionDTO action = new ProjectCollaborationActionDTO(
				UUID.randomUUID().toString(),
				projectId.toString(),
				normalizedSessionId(request.getSessionId()),
				userId,
				trimmed(request.getTabKey()),
				trimmed(request.getActionType()),
				request.getDraftVersion(),
				trimmed(request.getMessage()),
				OffsetDateTime.now());
		Deque<ProjectCollaborationActionDTO> actions = actionsByProject.computeIfAbsent(projectId, ignored -> new ArrayDeque<>());
		actions.addFirst(action);
		while (actions.size() > MAX_ACTIONS) {
			actions.removeLast();
		}
		projectEventStreamService.publish(projectId, "collaboration-action", action);
		return snapshot(projectId);
	}

	private void touchExistingSession(UUID projectId, String userId, String sessionId) {
		cleanup(projectId);
		Map<String, SessionPresence> sessions = sessionsByProject.get(projectId);
		if (sessions == null) {
			return;
		}
		SessionPresence presence = sessions.get(normalizedSessionId(sessionId));
		if (presence == null) {
			return;
		}
		presence.userId = userId;
		presence.lastSeenAt = OffsetDateTime.now();
	}

	public ProjectCollaborationStateDTO getState(UUID projectId) {
		cleanup(projectId);
		return snapshot(projectId);
	}

	private void cleanup(UUID projectId) {
		Map<String, SessionPresence> sessions = sessionsByProject.get(projectId);
		if (sessions == null || sessions.isEmpty()) {
			return;
		}
		OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(SESSION_TTL_SECONDS);
		sessions.entrySet().removeIf(entry -> entry.getValue().lastSeenAt.isBefore(cutoff));
		if (sessions.isEmpty()) {
			sessionsByProject.remove(projectId);
		}
	}

	private ProjectCollaborationStateDTO snapshot(UUID projectId) {
		List<ProjectCollaborationEditorDTO> editors = sessionsByProject.getOrDefault(projectId, Map.of()).values().stream()
				.sorted(Comparator.comparing((SessionPresence presence) -> presence.lastSeenAt).reversed())
				.map(presence -> new ProjectCollaborationEditorDTO(
						presence.sessionId,
						presence.userId,
						presence.userId,
						presence.lastSeenAt))
				.toList();
		List<ProjectCollaborationActionDTO> recentActions = new ArrayList<>(actionsByProject.getOrDefault(projectId, new ArrayDeque<>()));
		return new ProjectCollaborationStateDTO(editors.size(), editors, recentActions);
	}

	private void publishPresence(UUID projectId, ProjectCollaborationStateDTO state) {
		projectEventStreamService.publish(projectId, "presence", state);
	}

	private String normalizedSessionId(String requestedSessionId) {
		String trimmed = trimmed(requestedSessionId);
		return trimmed.isBlank() ? UUID.randomUUID().toString() : trimmed;
	}

	private String trimmed(String value) {
		return value == null ? "" : value.trim();
	}

	private static final class SessionPresence {
		private final String sessionId;
		private String userId;
		private OffsetDateTime lastSeenAt;

		private SessionPresence(String sessionId, String userId, OffsetDateTime lastSeenAt) {
			this.sessionId = sessionId;
			this.userId = userId;
			this.lastSeenAt = lastSeenAt;
		}
	}
}
