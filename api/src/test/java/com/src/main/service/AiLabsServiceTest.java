package com.src.main.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.src.main.repository.AiLabsJobHistoryRepository;

class AiLabsServiceTest {

	@Test
	void getJob_rejectsAccessFromDifferentUser() {
		ConfigMetadataService configMetadataService = mock(ConfigMetadataService.class);
		when(configMetadataService.isPropertyEnabled("app.feature.ai-labs.enabled", false)).thenReturn(true);

		AiLabsQuotaService aiLabsQuotaService = mock(AiLabsQuotaService.class);
		AiLabsEventStreamService eventStreamService = mock(AiLabsEventStreamService.class);
		ProjectService projectService = mock(ProjectService.class);
		ProjectDraftSpecMapperService projectDraftSpecMapperService = mock(ProjectDraftSpecMapperService.class);
		ProjectNameValidationService projectNameValidationService = mock(ProjectNameValidationService.class);
		ProjectUserIdentityService projectUserIdentityService = mock(ProjectUserIdentityService.class);
		AiLabsJobHistoryRepository aiLabsJobHistoryRepository = mock(AiLabsJobHistoryRepository.class);
		ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
		@SuppressWarnings("unchecked")
		ObjectProvider<ChatClient.Builder> chatClientBuilderProvider = mock(ObjectProvider.class);
		when(chatClientBuilderProvider.getIfAvailable()).thenReturn(chatClientBuilder);

		AiLabsService service = new AiLabsService(
				eventStreamService,
				projectService,
				projectDraftSpecMapperService,
				projectNameValidationService,
				projectUserIdentityService,
				configMetadataService,
				aiLabsQuotaService,
				aiLabsJobHistoryRepository,
				chatClientBuilderProvider,
				new ObjectMapper().registerModule(new JavaTimeModule()));

		UUID jobId = service.createJob("build a crm", "owner-user").getJobId();

		assertThatThrownBy(() -> service.getJob(jobId, "another-user"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("AI Labs job not found");
	}
}
