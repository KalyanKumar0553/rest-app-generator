package com.src.main.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.src.main.dto.AiLabsJobStatusDTO;

@Service
public class AiLabsEventStreamService {

	private static final long TIMEOUT_MS = 30L * 60L * 1000L;
	private final Map<UUID, List<SseEmitter>> emittersByJob = new ConcurrentHashMap<>();

	public SseEmitter subscribe(UUID jobId, AiLabsJobStatusDTO currentState) {
		SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
		emittersByJob.computeIfAbsent(jobId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

		emitter.onCompletion(() -> remove(jobId, emitter));
		emitter.onTimeout(() -> remove(jobId, emitter));
		emitter.onError(error -> remove(jobId, emitter));

		try {
			emitter.send(SseEmitter.event().name("connected").data(Map.of("jobId", jobId.toString())));
			if (currentState != null) {
				emitter.send(SseEmitter.event().name("status").data(currentState));
			}
		} catch (IOException ex) {
			remove(jobId, emitter);
		}

		return emitter;
	}

	public void publish(UUID jobId, AiLabsJobStatusDTO payload) {
		List<SseEmitter> emitters = emittersByJob.get(jobId);
		if (emitters == null || emitters.isEmpty()) {
			return;
		}

		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().name("status").data(payload));
			} catch (IOException ex) {
				remove(jobId, emitter);
			}
		}
	}

	private void remove(UUID jobId, SseEmitter emitter) {
		List<SseEmitter> emitters = emittersByJob.get(jobId);
		if (emitters == null) {
			return;
		}
		emitters.remove(emitter);
		if (emitters.isEmpty()) {
			emittersByJob.remove(jobId);
		}
	}
}
