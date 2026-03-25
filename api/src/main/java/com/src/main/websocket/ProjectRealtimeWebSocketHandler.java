package com.src.main.websocket;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.auth.repository.InvalidatedTokenRepository;
import com.src.main.auth.util.JwtClaims;
import com.src.main.auth.util.JwtUtils;
import com.src.main.dto.ProjectCollaborationActionRequestDTO;
import com.src.main.dto.ProjectCollaborationPresenceResponseDTO;
import com.src.main.dto.ProjectEditorPresenceRequestDTO;
import com.src.main.service.ProjectCollaborationService;
import com.src.main.service.ProjectRealtimeSocketService;
import com.src.main.service.ProjectService;

@Component
public class ProjectRealtimeWebSocketHandler extends TextWebSocketHandler {

	private static final String ATTR_PROJECT_ID = "projectId";
	private static final String ATTR_USER_ID = "userId";
	private static final String ATTR_SESSION_ID = "collaborationSessionId";

	private final ObjectMapper objectMapper;
	private final JwtUtils jwtUtils;
	private final InvalidatedTokenRepository invalidatedTokenRepository;
	private final ProjectService projectService;
	private final ProjectCollaborationService projectCollaborationService;
	private final ProjectRealtimeSocketService projectRealtimeSocketService;
	private final Map<String, OffsetDateTime> connectedAtBySocket = new ConcurrentHashMap<>();

	public ProjectRealtimeWebSocketHandler(
			ObjectMapper objectMapper,
			JwtUtils jwtUtils,
			InvalidatedTokenRepository invalidatedTokenRepository,
			ProjectService projectService,
			ProjectCollaborationService projectCollaborationService,
			ProjectRealtimeSocketService projectRealtimeSocketService) {
		this.objectMapper = objectMapper;
		this.jwtUtils = jwtUtils;
		this.invalidatedTokenRepository = invalidatedTokenRepository;
		this.projectService = projectService;
		this.projectCollaborationService = projectCollaborationService;
		this.projectRealtimeSocketService = projectRealtimeSocketService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		UUID projectId = resolveProjectId(session);
		String userId = resolveUserId(session);
		if (projectId == null || userId == null || userId.isBlank()) {
			session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Authentication required"));
			return;
		}
		try {
			projectService.getAccessibleProject(projectId, userId);
		} catch (RuntimeException ex) {
			session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Project access denied"));
			return;
		}
		session.getAttributes().put(ATTR_PROJECT_ID, projectId);
		session.getAttributes().put(ATTR_USER_ID, userId);
		projectRealtimeSocketService.register(projectId, session);
		connectedAtBySocket.put(session.getId(), OffsetDateTime.now());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		JsonNode root = objectMapper.readTree(message.getPayload());
		String type = text(root.get("type"));
		JsonNode payload = root.get("payload");
		UUID projectId = (UUID) session.getAttributes().get(ATTR_PROJECT_ID);
		String userId = (String) session.getAttributes().get(ATTR_USER_ID);
		if (projectId == null || userId == null || userId.isBlank()) {
			session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Session not initialized"));
			return;
		}

		switch (type) {
			case "presence.register" -> {
				ProjectEditorPresenceRequestDTO request = new ProjectEditorPresenceRequestDTO();
				request.setSessionId(text(payload == null ? null : payload.get("sessionId")));
				ProjectCollaborationPresenceResponseDTO response = projectCollaborationService
						.register(projectId, userId, request.getSessionId());
				session.getAttributes().put(ATTR_SESSION_ID, response.getSessionId());
				projectRealtimeSocketService.send(session, "presence.registered", response);
			}
			case "presence.heartbeat" -> {
				String sessionId = text(payload == null ? null : payload.get("sessionId"));
				if (sessionId.isBlank()) {
					sessionId = textValue(session, ATTR_SESSION_ID);
				}
				if (!sessionId.isBlank()) {
					session.getAttributes().put(ATTR_SESSION_ID, sessionId);
					projectCollaborationService.heartbeat(projectId, userId, sessionId);
				}
			}
			case "presence.leave" -> {
				String sessionId = text(payload == null ? null : payload.get("sessionId"));
				if (sessionId.isBlank()) {
					sessionId = textValue(session, ATTR_SESSION_ID);
				}
				if (!sessionId.isBlank()) {
					projectCollaborationService.leave(projectId, sessionId);
				}
			}
			case "collaboration.action" -> {
				ProjectCollaborationActionRequestDTO request = new ProjectCollaborationActionRequestDTO();
				request.setSessionId(text(payload == null ? null : payload.get("sessionId")));
				request.setTabKey(text(payload == null ? null : payload.get("tabKey")));
				request.setActionType(text(payload == null ? null : payload.get("actionType")));
				if (payload != null && payload.hasNonNull("draftVersion")) {
					request.setDraftVersion(payload.get("draftVersion").asInt());
				}
				request.setMessage(text(payload == null ? null : payload.get("message")));
				projectCollaborationService.recordAction(projectId, userId, request);
			}
			default -> projectRealtimeSocketService.send(session, "error", Map.of("message", "Unsupported project realtime event"));
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		cleanupSession(session);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		cleanupSession(session);
		if (session.isOpen()) {
			session.close(CloseStatus.SERVER_ERROR);
		}
	}

	private void cleanupSession(WebSocketSession session) {
		UUID projectId = (UUID) session.getAttributes().get(ATTR_PROJECT_ID);
		String sessionId = textValue(session, ATTR_SESSION_ID);
		if (projectId != null) {
			projectRealtimeSocketService.unregister(projectId, session);
			if (!sessionId.isBlank()) {
				projectCollaborationService.leave(projectId, sessionId);
			}
		}
		connectedAtBySocket.remove(session.getId());
	}

	private UUID resolveProjectId(WebSocketSession session) {
		URI uri = session.getUri();
		if (uri == null || uri.getPath() == null) {
			return null;
		}
		String[] segments = uri.getPath().split("/");
		if (segments.length == 0) {
			return null;
		}
		String candidate = segments[segments.length - 1];
		try {
			return UUID.fromString(candidate);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private String resolveUserId(WebSocketSession session) {
		if (session.getPrincipal() != null && session.getPrincipal().getName() != null
				&& !session.getPrincipal().getName().isBlank()) {
			return session.getPrincipal().getName();
		}
		String token = resolveQueryParam(session.getUri(), "access_token");
		if (token == null || token.isBlank()) {
			token = resolveQueryParam(session.getUri(), "accessToken");
		}
		if (token == null || token.isBlank() || invalidatedTokenRepository.existsByToken(token)) {
			return null;
		}
		try {
			JwtClaims claims = jwtUtils.parse(token);
			return claims.getSub();
		} catch (Exception ex) {
			return null;
		}
	}

	private String resolveQueryParam(URI uri, String key) {
		if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
			return null;
		}
		for (String pair : uri.getQuery().split("&")) {
			String[] parts = pair.split("=", 2);
			if (parts.length == 2 && key.equals(parts[0])) {
				return java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8);
			}
		}
		return null;
	}

	private String text(JsonNode node) {
		return node == null || node.isNull() ? "" : node.asText("").trim();
	}

	private String textValue(WebSocketSession session, String key) {
		Object value = session.getAttributes().get(key);
		return value == null ? "" : String.valueOf(value).trim();
	}
}
