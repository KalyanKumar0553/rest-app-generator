package com.src.main.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ProjectEventStreamService {

	private static final long TIMEOUT_MS = 30L * 60L * 1000L;
	private final Map<UUID, List<SseEmitter>> emittersByProject = new ConcurrentHashMap<>();

	public SseEmitter subscribe(UUID projectId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
		emittersByProject.computeIfAbsent(projectId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

		emitter.onCompletion(() -> remove(projectId, emitter));
		emitter.onTimeout(() -> remove(projectId, emitter));
		emitter.onError(error -> remove(projectId, emitter));

		try {
			emitter.send(SseEmitter.event().name("connected").data(Map.of("projectId", projectId.toString())));
		} catch (IOException ex) {
			remove(projectId, emitter);
		}

		return emitter;
	}

	public void publish(UUID projectId, String eventName, Object payload) {
		List<SseEmitter> emitters = emittersByProject.get(projectId);
		if (emitters == null || emitters.isEmpty()) {
			return;
		}

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().name(eventName).data(payload));
			} catch (IOException ex) {
				remove(projectId, emitter);
			}
		}
	}

	private void remove(UUID projectId, SseEmitter emitter) {
		List<SseEmitter> emitters = emittersByProject.get(projectId);
		if (emitters == null) {
			return;
		}
		emitters.remove(emitter);
		if (emitters.isEmpty()) {
			emittersByProject.remove(projectId);
		}
	}
}
