package com.src.main.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProjectRealtimeSocketService {

	private final Map<UUID, List<WebSocketSession>> sessionsByProject = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper;

	public ProjectRealtimeSocketService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public void register(UUID projectId, WebSocketSession session) {
		sessionsByProject.computeIfAbsent(projectId, ignored -> new CopyOnWriteArrayList<>()).add(session);
		send(session, "connected", Map.of("projectId", projectId.toString()));
	}

	public void unregister(UUID projectId, WebSocketSession session) {
		List<WebSocketSession> sessions = sessionsByProject.get(projectId);
		if (sessions == null) {
			return;
		}
		sessions.remove(session);
		if (sessions.isEmpty()) {
			sessionsByProject.remove(projectId);
		}
	}

	public void publish(UUID projectId, String eventName, Object payload) {
		List<WebSocketSession> sessions = sessionsByProject.get(projectId);
		if (sessions == null || sessions.isEmpty()) {
			return;
		}
		for (WebSocketSession session : sessions) {
			send(session, eventName, payload);
		}
	}

	public void send(WebSocketSession session, String eventName, Object payload) {
		if (session == null || !session.isOpen()) {
			return;
		}
		try {
			String json = objectMapper.writeValueAsString(Map.of(
					"event", eventName,
					"payload", payload));
			synchronized (session) {
				session.sendMessage(new TextMessage(json));
			}
		} catch (IOException ignored) {
		}
	}
}
