package com.src.main.agent.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AgentMessageResponseDto {

	private UUID messageId;
	private String role;
	private String content;
	private int sequenceNumber;
	private OffsetDateTime createdAt;

	public AgentMessageResponseDto() {
	}

	public AgentMessageResponseDto(UUID messageId, String role, String content,
			int sequenceNumber, OffsetDateTime createdAt) {
		this.messageId = messageId;
		this.role = role;
		this.content = content;
		this.sequenceNumber = sequenceNumber;
		this.createdAt = createdAt;
	}

	public UUID getMessageId() {
		return messageId;
	}

	public void setMessageId(UUID messageId) {
		this.messageId = messageId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
