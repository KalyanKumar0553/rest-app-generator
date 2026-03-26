package com.src.main.agent.dto.response;

import java.util.UUID;

public class AgentSpecSaveResponseDto {

	private UUID sessionId;
	private UUID projectId;
	private String status;

	public AgentSpecSaveResponseDto() {
	}

	public AgentSpecSaveResponseDto(UUID sessionId, UUID projectId, String status) {
		this.sessionId = sessionId;
		this.projectId = projectId;
		this.status = status;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(UUID projectId) {
		this.projectId = projectId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
