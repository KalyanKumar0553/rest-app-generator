package com.src.main.agent.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.agent.dto.request.AgentMessageRequestDto;
import com.src.main.agent.dto.request.AgentSessionCreateRequestDto;
import com.src.main.agent.dto.request.AgentSpecSaveRequestDto;
import com.src.main.agent.dto.response.AgentMessageResponseDto;
import com.src.main.agent.dto.response.AgentSessionResponseDto;
import com.src.main.agent.dto.response.AgentSessionSummaryDto;
import com.src.main.agent.dto.response.AgentSpecSaveResponseDto;
import com.src.main.agent.model.AgentMessageEntity;
import com.src.main.agent.model.AgentMessageRole;
import com.src.main.agent.model.AgentSessionEntity;
import com.src.main.agent.model.AgentSessionStatus;
import com.src.main.agent.repository.AgentMessageRepository;
import com.src.main.agent.repository.AgentSessionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AgentService {

	private static final Logger log = LoggerFactory.getLogger(AgentService.class);

	private final AgentSessionRepository sessionRepository;
	private final AgentMessageRepository messageRepository;
	private final AgentPromptService promptService;
	private final AgentSpecGeneratorService specGeneratorService;
	private final AgentSpecParserService specParserService;

	public AgentService(AgentSessionRepository sessionRepository,
			AgentMessageRepository messageRepository,
			AgentPromptService promptService,
			AgentSpecGeneratorService specGeneratorService,
			AgentSpecParserService specParserService) {
		this.sessionRepository = sessionRepository;
		this.messageRepository = messageRepository;
		this.promptService = promptService;
		this.specGeneratorService = specGeneratorService;
		this.specParserService = specParserService;
	}

	@Transactional
	public AgentSessionResponseDto createSession(AgentSessionCreateRequestDto request, String ownerUserId) {
		String title = resolveTitle(request.getTitle(), request.getInputText());

		AgentSessionEntity session = new AgentSessionEntity();
		session.setOwnerUserId(ownerUserId);
		session.setTitle(title);
		session.setStatus(AgentSessionStatus.ACTIVE);
		session = sessionRepository.save(session);

		AgentMessageEntity userMessage = createMessage(session.getId(), AgentMessageRole.USER,
				request.getInputText(), 1);
		messageRepository.save(userMessage);

		String systemPrompt = promptService.buildSystemPrompt();
		List<AgentMessageEntity> history = List.of(userMessage);
		String agentReply = specGeneratorService.generateAgentReply(systemPrompt, history, request.getInputText());

		AgentMessageEntity agentMessage = createMessage(session.getId(), AgentMessageRole.AGENT, agentReply, 2);
		messageRepository.save(agentMessage);

		log.info("Created agent session {} for user {}", session.getId(), ownerUserId);
		return AgentSessionMapper.toResponseDto(session);
	}

	@Transactional(readOnly = true)
	public AgentSessionResponseDto getSession(UUID sessionId, String ownerUserId) {
		AgentSessionEntity session = findOwnedSession(sessionId, ownerUserId);
		return AgentSessionMapper.toResponseDto(session);
	}

	@Transactional(readOnly = true)
	public List<AgentSessionSummaryDto> listSessions(String ownerUserId) {
		return sessionRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId).stream()
				.map(AgentSessionMapper::toSummaryDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AgentMessageResponseDto> getMessages(UUID sessionId, String ownerUserId) {
		findOwnedSession(sessionId, ownerUserId);
		return messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId).stream()
				.map(AgentSessionMapper::toMessageDto)
				.toList();
	}

	@Transactional
	public AgentMessageResponseDto sendMessage(UUID sessionId, AgentMessageRequestDto request, String ownerUserId) {
		AgentSessionEntity session = findOwnedSession(sessionId, ownerUserId);
		validateSessionActive(session);

		int nextSequence = messageRepository.countBySessionId(sessionId) + 1;

		AgentMessageEntity userMessage = createMessage(sessionId, AgentMessageRole.USER,
				request.getContent(), nextSequence);
		messageRepository.save(userMessage);

		List<AgentMessageEntity> history = messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId);
		String systemPrompt = promptService.buildSystemPrompt();
		String agentReply = specGeneratorService.generateAgentReply(systemPrompt, history, request.getContent());

		AgentMessageEntity agentMessage = createMessage(sessionId, AgentMessageRole.AGENT,
				agentReply, nextSequence + 1);
		messageRepository.save(agentMessage);

		return AgentSessionMapper.toMessageDto(agentMessage);
	}

	@Transactional
	public AgentSessionResponseDto generateSpec(UUID sessionId, String ownerUserId) {
		AgentSessionEntity session = findOwnedSession(sessionId, ownerUserId);
		validateSessionActive(session);

		session.setStatus(AgentSessionStatus.GENERATING);
		sessionRepository.save(session);

		try {
			List<AgentMessageEntity> history = messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId);
			String systemPrompt = promptService.buildSystemPrompt();
			Map<String, Object> spec = specGeneratorService.generateSpec(systemPrompt, history);
			String specJson = specParserService.serializeSpec(spec);

			session.setGeneratedSpec(specJson);
			session.setStatus(AgentSessionStatus.COMPLETED);
			sessionRepository.save(session);

			int nextSequence = messageRepository.countBySessionId(sessionId) + 1;
			AgentMessageEntity systemMessage = createMessage(sessionId, AgentMessageRole.SYSTEM,
					"Project specification generated successfully.", nextSequence);
			messageRepository.save(systemMessage);

			log.info("Generated spec for session {}", sessionId);
			return AgentSessionMapper.toResponseDto(session);
		} catch (Exception ex) {
			log.error("Failed to generate spec for session {}: {}", sessionId, ex.getMessage());
			session.setStatus(AgentSessionStatus.FAILED);
			session.setErrorMessage(ex.getMessage());
			sessionRepository.save(session);
			throw new IllegalStateException("Failed to generate project specification: " + ex.getMessage(), ex);
		}
	}

	@Transactional
	public AgentSpecSaveResponseDto saveAsProject(UUID sessionId, AgentSpecSaveRequestDto request,
			String ownerUserId) {
		AgentSessionEntity session = findOwnedSession(sessionId, ownerUserId);
		if (session.getGeneratedSpec() == null || session.getGeneratedSpec().isBlank()) {
			throw new IllegalArgumentException(
					"No specification has been generated for this session. Generate the spec first.");
		}

		Map<String, Object> spec = specParserService.parseSpec(session.getGeneratedSpec());
		if (request != null && request.getSpecOverrides() != null) {
			spec = specParserService.applyOverrides(spec, request.getSpecOverrides());
			session.setGeneratedSpec(specParserService.serializeSpec(spec));
		}

		UUID projectId = UUID.randomUUID();
		session.setProjectId(projectId);
		session.setStatus(AgentSessionStatus.COMPLETED);
		sessionRepository.save(session);

		log.info("Saved session {} as project {}", sessionId, projectId);
		return new AgentSpecSaveResponseDto(sessionId, projectId, session.getStatus().name());
	}

	@Transactional
	public void cancelSession(UUID sessionId, String ownerUserId) {
		AgentSessionEntity session = findOwnedSession(sessionId, ownerUserId);
		if (session.getStatus() == AgentSessionStatus.COMPLETED) {
			throw new IllegalArgumentException("Cannot cancel a completed session.");
		}
		session.setStatus(AgentSessionStatus.CANCELLED);
		sessionRepository.save(session);
		log.info("Cancelled session {}", sessionId);
	}

	@Transactional
	public void deleteSession(UUID sessionId, String ownerUserId) {
		AgentSessionEntity session = findOwnedSession(sessionId, ownerUserId);
		messageRepository.deleteBySessionId(sessionId);
		sessionRepository.delete(session);
		log.info("Deleted session {}", sessionId);
	}

	private AgentSessionEntity findOwnedSession(UUID sessionId, String ownerUserId) {
		return sessionRepository.findByIdAndOwnerUserId(sessionId, ownerUserId)
				.orElseThrow(() -> new EntityNotFoundException("Agent session not found: " + sessionId));
	}

	private void validateSessionActive(AgentSessionEntity session) {
		AgentSessionStatus status = session.getStatus();
		if (status != AgentSessionStatus.ACTIVE && status != AgentSessionStatus.FAILED) {
			throw new IllegalArgumentException(
					"Session is not in an active state. Current status: " + status);
		}
	}

	private AgentMessageEntity createMessage(UUID sessionId, AgentMessageRole role, String content,
			int sequenceNumber) {
		AgentMessageEntity message = new AgentMessageEntity();
		message.setSessionId(sessionId);
		message.setRole(role);
		message.setContent(content);
		message.setSequenceNumber(sequenceNumber);
		return message;
	}

	private String resolveTitle(String title, String inputText) {
		if (title != null && !title.isBlank()) {
			return title.trim();
		}
		String base = inputText.trim();
		return base.length() > 100 ? base.substring(0, 100) + "..." : base;
	}
}
