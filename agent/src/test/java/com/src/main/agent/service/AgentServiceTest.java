package com.src.main.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

	@Mock
	private AgentSessionRepository sessionRepository;
	@Mock
	private AgentMessageRepository messageRepository;
	@Mock
	private AgentPromptService promptService;
	@Mock
	private AgentSpecGeneratorService specGeneratorService;
	@Mock
	private AgentSpecParserService specParserService;

	private AgentService agentService;

	private static final String OWNER_USER_ID = "user-123";
	private static final String SYSTEM_PROMPT = "system-prompt";

	@BeforeEach
	void setUp() {
		agentService = new AgentService(sessionRepository, messageRepository,
				promptService, specGeneratorService, specParserService);
	}

	private AgentSessionEntity buildSession(UUID id, String ownerUserId, AgentSessionStatus status) {
		AgentSessionEntity session = new AgentSessionEntity();
		session.setId(id);
		session.setOwnerUserId(ownerUserId);
		session.setTitle("Test Session");
		session.setStatus(status);
		session.setCreatedAt(OffsetDateTime.now());
		session.setUpdatedAt(OffsetDateTime.now());
		return session;
	}

	private AgentMessageEntity buildMessage(UUID sessionId, AgentMessageRole role, String content, int seq) {
		AgentMessageEntity message = new AgentMessageEntity();
		message.setId(UUID.randomUUID());
		message.setSessionId(sessionId);
		message.setRole(role);
		message.setContent(content);
		message.setSequenceNumber(seq);
		message.setCreatedAt(OffsetDateTime.now());
		return message;
	}

	@Nested
	class CreateSession {

		@Test
		void createsSessionWithUserMessageAndAgentReply() {
			AgentSessionCreateRequestDto request = new AgentSessionCreateRequestDto(
					"Build a CRM application", "My CRM");
			AgentSessionEntity savedSession = buildSession(UUID.randomUUID(), OWNER_USER_ID,
					AgentSessionStatus.ACTIVE);

			when(sessionRepository.save(any(AgentSessionEntity.class))).thenReturn(savedSession);
			when(messageRepository.save(any(AgentMessageEntity.class)))
					.thenAnswer(inv -> inv.getArgument(0));
			when(promptService.buildSystemPrompt()).thenReturn(SYSTEM_PROMPT);
			when(specGeneratorService.generateAgentReply(eq(SYSTEM_PROMPT), anyList(), anyString()))
					.thenReturn("I'll help you build a CRM.");

			AgentSessionResponseDto result = agentService.createSession(request, OWNER_USER_ID);

			assertThat(result).isNotNull();
			assertThat(result.getSessionId()).isEqualTo(savedSession.getId());
			assertThat(result.getStatus()).isEqualTo("ACTIVE");

			ArgumentCaptor<AgentMessageEntity> messageCaptor = ArgumentCaptor.forClass(AgentMessageEntity.class);
			verify(messageRepository, org.mockito.Mockito.times(2)).save(messageCaptor.capture());
			List<AgentMessageEntity> savedMessages = messageCaptor.getAllValues();
			assertThat(savedMessages.get(0).getRole()).isEqualTo(AgentMessageRole.USER);
			assertThat(savedMessages.get(0).getContent()).isEqualTo("Build a CRM application");
			assertThat(savedMessages.get(1).getRole()).isEqualTo(AgentMessageRole.AGENT);
		}

		@Test
		void usesInputTextAsTitleWhenTitleIsNull() {
			AgentSessionCreateRequestDto request = new AgentSessionCreateRequestDto(
					"Build a CRM application", null);

			ArgumentCaptor<AgentSessionEntity> sessionCaptor = ArgumentCaptor.forClass(AgentSessionEntity.class);
			AgentSessionEntity saved = buildSession(UUID.randomUUID(), OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.save(sessionCaptor.capture())).thenReturn(saved);
			when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
			when(promptService.buildSystemPrompt()).thenReturn(SYSTEM_PROMPT);
			when(specGeneratorService.generateAgentReply(anyString(), anyList(), anyString())).thenReturn("reply");

			agentService.createSession(request, OWNER_USER_ID);

			assertThat(sessionCaptor.getValue().getTitle()).isEqualTo("Build a CRM application");
		}

		@Test
		void truncatesTitleWhenInputTextExceeds100Characters() {
			String longInput = "A".repeat(150);
			AgentSessionCreateRequestDto request = new AgentSessionCreateRequestDto(longInput, null);

			ArgumentCaptor<AgentSessionEntity> sessionCaptor = ArgumentCaptor.forClass(AgentSessionEntity.class);
			AgentSessionEntity saved = buildSession(UUID.randomUUID(), OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.save(sessionCaptor.capture())).thenReturn(saved);
			when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
			when(promptService.buildSystemPrompt()).thenReturn(SYSTEM_PROMPT);
			when(specGeneratorService.generateAgentReply(anyString(), anyList(), anyString())).thenReturn("reply");

			agentService.createSession(request, OWNER_USER_ID);

			assertThat(sessionCaptor.getValue().getTitle()).hasSize(103);
			assertThat(sessionCaptor.getValue().getTitle()).endsWith("...");
		}
	}

	@Nested
	class GetSession {

		@Test
		void returnsSessionWhenOwnedByUser() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			AgentSessionResponseDto result = agentService.getSession(sessionId, OWNER_USER_ID);

			assertThat(result.getSessionId()).isEqualTo(sessionId);
		}

		@Test
		void throwsWhenSessionNotFound() {
			UUID sessionId = UUID.randomUUID();
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> agentService.getSession(sessionId, OWNER_USER_ID))
					.isInstanceOf(EntityNotFoundException.class)
					.hasMessageContaining("Agent session not found");
		}

		@Test
		void throwsWhenSessionOwnedByDifferentUser() {
			UUID sessionId = UUID.randomUUID();
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, "other-user"))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> agentService.getSession(sessionId, "other-user"))
					.isInstanceOf(EntityNotFoundException.class);
		}
	}

	@Nested
	class ListSessions {

		@Test
		void returnsSessionSummariesForUser() {
			AgentSessionEntity session1 = buildSession(UUID.randomUUID(), OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			AgentSessionEntity session2 = buildSession(UUID.randomUUID(), OWNER_USER_ID, AgentSessionStatus.COMPLETED);
			when(sessionRepository.findByOwnerUserIdOrderByCreatedAtDesc(OWNER_USER_ID))
					.thenReturn(List.of(session1, session2));

			List<AgentSessionSummaryDto> result = agentService.listSessions(OWNER_USER_ID);

			assertThat(result).hasSize(2);
			assertThat(result.get(0).getSessionId()).isEqualTo(session1.getId());
			assertThat(result.get(1).getSessionId()).isEqualTo(session2.getId());
		}

		@Test
		void returnsEmptyListWhenNoSessions() {
			when(sessionRepository.findByOwnerUserIdOrderByCreatedAtDesc(OWNER_USER_ID))
					.thenReturn(List.of());

			List<AgentSessionSummaryDto> result = agentService.listSessions(OWNER_USER_ID);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	class GetMessages {

		@Test
		void returnsOrderedMessagesForSession() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			AgentMessageEntity msg1 = buildMessage(sessionId, AgentMessageRole.USER, "Hello", 1);
			AgentMessageEntity msg2 = buildMessage(sessionId, AgentMessageRole.AGENT, "Hi there", 2);
			when(messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId))
					.thenReturn(List.of(msg1, msg2));

			List<AgentMessageResponseDto> result = agentService.getMessages(sessionId, OWNER_USER_ID);

			assertThat(result).hasSize(2);
			assertThat(result.get(0).getRole()).isEqualTo("USER");
			assertThat(result.get(1).getRole()).isEqualTo("AGENT");
			assertThat(result.get(0).getSequenceNumber()).isEqualTo(1);
			assertThat(result.get(1).getSequenceNumber()).isEqualTo(2);
		}

		@Test
		void throwsWhenSessionNotOwnedByUser() {
			UUID sessionId = UUID.randomUUID();
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> agentService.getMessages(sessionId, OWNER_USER_ID))
					.isInstanceOf(EntityNotFoundException.class);
		}
	}

	@Nested
	class SendMessage {

		@Test
		void savesUserMessageAndAgentReply() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));
			when(messageRepository.countBySessionId(sessionId)).thenReturn(2);
			when(messageRepository.save(any(AgentMessageEntity.class)))
					.thenAnswer(inv -> {
						AgentMessageEntity msg = inv.getArgument(0);
						msg.setId(UUID.randomUUID());
						msg.setCreatedAt(OffsetDateTime.now());
						return msg;
					});
			when(messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId))
					.thenReturn(List.of());
			when(promptService.buildSystemPrompt()).thenReturn(SYSTEM_PROMPT);
			when(specGeneratorService.generateAgentReply(anyString(), anyList(), anyString()))
					.thenReturn("Agent reply");

			AgentMessageRequestDto request = new AgentMessageRequestDto("Add a User entity");
			AgentMessageResponseDto result = agentService.sendMessage(sessionId, request, OWNER_USER_ID);

			assertThat(result.getRole()).isEqualTo("AGENT");
			assertThat(result.getContent()).isEqualTo("Agent reply");
			assertThat(result.getSequenceNumber()).isEqualTo(4);

			ArgumentCaptor<AgentMessageEntity> captor = ArgumentCaptor.forClass(AgentMessageEntity.class);
			verify(messageRepository, org.mockito.Mockito.times(2)).save(captor.capture());
			assertThat(captor.getAllValues().get(0).getRole()).isEqualTo(AgentMessageRole.USER);
			assertThat(captor.getAllValues().get(0).getSequenceNumber()).isEqualTo(3);
		}

		@Test
		void throwsWhenSessionIsCompleted() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.COMPLETED);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			AgentMessageRequestDto request = new AgentMessageRequestDto("Hello");

			assertThatThrownBy(() -> agentService.sendMessage(sessionId, request, OWNER_USER_ID))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("not in an active state");
		}

		@Test
		void throwsWhenSessionIsCancelled() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.CANCELLED);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			AgentMessageRequestDto request = new AgentMessageRequestDto("Hello");

			assertThatThrownBy(() -> agentService.sendMessage(sessionId, request, OWNER_USER_ID))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("not in an active state");
		}

		@Test
		void allowsMessageWhenSessionIsInFailedState() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.FAILED);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));
			when(messageRepository.countBySessionId(sessionId)).thenReturn(2);
			when(messageRepository.save(any())).thenAnswer(inv -> {
				AgentMessageEntity msg = inv.getArgument(0);
				msg.setId(UUID.randomUUID());
				msg.setCreatedAt(OffsetDateTime.now());
				return msg;
			});
			when(messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId)).thenReturn(List.of());
			when(promptService.buildSystemPrompt()).thenReturn(SYSTEM_PROMPT);
			when(specGeneratorService.generateAgentReply(anyString(), anyList(), anyString())).thenReturn("reply");

			AgentMessageRequestDto request = new AgentMessageRequestDto("Try again");
			AgentMessageResponseDto result = agentService.sendMessage(sessionId, request, OWNER_USER_ID);

			assertThat(result).isNotNull();
		}
	}

	@Nested
	class GenerateSpec {

		@Test
		void generatesSpecAndUpdatesSession() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));
			when(sessionRepository.save(any(AgentSessionEntity.class)))
					.thenAnswer(inv -> inv.getArgument(0));
			when(messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId))
					.thenReturn(List.of());
			when(messageRepository.countBySessionId(sessionId)).thenReturn(2);
			when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
			when(promptService.buildSystemPrompt()).thenReturn(SYSTEM_PROMPT);

			Map<String, Object> spec = Map.of("settings", Map.of("language", "java"));
			when(specGeneratorService.generateSpec(anyString(), anyList())).thenReturn(spec);
			when(specParserService.serializeSpec(spec)).thenReturn("{\"settings\":{\"language\":\"java\"}}");

			AgentSessionResponseDto result = agentService.generateSpec(sessionId, OWNER_USER_ID);

			assertThat(result.getStatus()).isEqualTo("COMPLETED");
			assertThat(result.getGeneratedSpec()).contains("java");
		}

		@Test
		void setsFailedStatusOnError() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));
			when(sessionRepository.save(any(AgentSessionEntity.class)))
					.thenAnswer(inv -> inv.getArgument(0));
			when(messageRepository.findBySessionIdOrderBySequenceNumberAsc(sessionId))
					.thenReturn(List.of());
			when(promptService.buildSystemPrompt()).thenReturn(SYSTEM_PROMPT);
			when(specGeneratorService.generateSpec(anyString(), anyList()))
					.thenThrow(new RuntimeException("AI service unavailable"));

			assertThatThrownBy(() -> agentService.generateSpec(sessionId, OWNER_USER_ID))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("Failed to generate project specification");

			assertThat(session.getStatus()).isEqualTo(AgentSessionStatus.FAILED);
			assertThat(session.getErrorMessage()).contains("AI service unavailable");
		}

		@Test
		void throwsWhenSessionIsGenerating() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.GENERATING);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			assertThatThrownBy(() -> agentService.generateSpec(sessionId, OWNER_USER_ID))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("not in an active state");
		}
	}

	@Nested
	class SaveAsProject {

		@Test
		void savesProjectWithGeneratedSpec() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.COMPLETED);
			session.setGeneratedSpec("{\"settings\":{\"language\":\"java\"}}");
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));
			when(specParserService.parseSpec(anyString()))
					.thenReturn(Map.of("settings", Map.of("language", "java")));
			when(sessionRepository.save(any(AgentSessionEntity.class)))
					.thenAnswer(inv -> inv.getArgument(0));

			AgentSpecSaveResponseDto result = agentService.saveAsProject(sessionId, null, OWNER_USER_ID);

			assertThat(result.getSessionId()).isEqualTo(sessionId);
			assertThat(result.getProjectId()).isNotNull();
			assertThat(result.getStatus()).isEqualTo("COMPLETED");
		}

		@Test
		void appliesOverridesBeforeSaving() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.COMPLETED);
			session.setGeneratedSpec("{\"settings\":{\"language\":\"java\"}}");
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			Map<String, Object> baseSpec = new java.util.LinkedHashMap<>(Map.of("settings", Map.of("language", "java")));
			Map<String, Object> overrides = Map.of("settings", Map.of("language", "node"));
			Map<String, Object> mergedSpec = Map.of("settings", Map.of("language", "node"));

			when(specParserService.parseSpec(anyString())).thenReturn(baseSpec);
			when(specParserService.applyOverrides(baseSpec, overrides)).thenReturn(mergedSpec);
			when(specParserService.serializeSpec(mergedSpec)).thenReturn("{\"settings\":{\"language\":\"node\"}}");
			when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			AgentSpecSaveRequestDto request = new AgentSpecSaveRequestDto(overrides);
			AgentSpecSaveResponseDto result = agentService.saveAsProject(sessionId, request, OWNER_USER_ID);

			assertThat(result.getProjectId()).isNotNull();
			verify(specParserService).applyOverrides(baseSpec, overrides);
		}

		@Test
		void throwsWhenNoSpecGenerated() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			assertThatThrownBy(() -> agentService.saveAsProject(sessionId, null, OWNER_USER_ID))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("No specification has been generated");
		}
	}

	@Nested
	class CancelSession {

		@Test
		void cancelsActiveSession() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));
			when(sessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

			agentService.cancelSession(sessionId, OWNER_USER_ID);

			assertThat(session.getStatus()).isEqualTo(AgentSessionStatus.CANCELLED);
			verify(sessionRepository).save(session);
		}

		@Test
		void throwsWhenCancellingCompletedSession() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.COMPLETED);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			assertThatThrownBy(() -> agentService.cancelSession(sessionId, OWNER_USER_ID))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("Cannot cancel a completed session");
		}
	}

	@Nested
	class DeleteSession {

		@Test
		void deletesSessionAndMessages() {
			UUID sessionId = UUID.randomUUID();
			AgentSessionEntity session = buildSession(sessionId, OWNER_USER_ID, AgentSessionStatus.ACTIVE);
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.of(session));

			agentService.deleteSession(sessionId, OWNER_USER_ID);

			verify(messageRepository).deleteBySessionId(sessionId);
			verify(sessionRepository).delete(session);
		}

		@Test
		void throwsWhenDeletingNonExistentSession() {
			UUID sessionId = UUID.randomUUID();
			when(sessionRepository.findByIdAndOwnerUserId(sessionId, OWNER_USER_ID))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> agentService.deleteSession(sessionId, OWNER_USER_ID))
					.isInstanceOf(EntityNotFoundException.class);
		}
	}
}
