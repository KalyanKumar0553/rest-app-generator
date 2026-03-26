package com.src.main.agent.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AgentSessionSummaryDto {

	private UUID sessionId;
	private String title;
	private String status;
	private UUID projectId;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public AgentSessionSummaryDto() {
	}

	public AgentSessionSummaryDto(UUID sessionId, String title, String status, UUID projectId,
			OffsetDateTime createdAt, OffsetDateTime updatedAt) {
		this.sessionId = sessionId;
		this.title = title;
		this.status = status;
		this.projectId = projectId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(UUID projectId) {
		this.projectId = projectId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
