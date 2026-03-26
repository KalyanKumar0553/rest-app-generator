package com.src.main.agent.service;

import com.src.main.agent.dto.response.AgentMessageResponseDto;
import com.src.main.agent.dto.response.AgentSessionResponseDto;
import com.src.main.agent.dto.response.AgentSessionSummaryDto;
import com.src.main.agent.model.AgentMessageEntity;
import com.src.main.agent.model.AgentSessionEntity;

public final class AgentSessionMapper {

	private AgentSessionMapper() {
	}

	public static AgentSessionResponseDto toResponseDto(AgentSessionEntity entity) {
		return new AgentSessionResponseDto(
				entity.getId(),
				entity.getTitle(),
				entity.getStatus().name(),
				entity.getProjectId(),
				entity.getGeneratedSpec(),
				entity.getErrorMessage(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	public static AgentSessionSummaryDto toSummaryDto(AgentSessionEntity entity) {
		return new AgentSessionSummaryDto(
				entity.getId(),
				entity.getTitle(),
				entity.getStatus().name(),
				entity.getProjectId(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	public static AgentMessageResponseDto toMessageDto(AgentMessageEntity entity) {
		return new AgentMessageResponseDto(
				entity.getId(),
				entity.getRole().name(),
				entity.getContent(),
				entity.getSequenceNumber(),
				entity.getCreatedAt());
	}
}
