package com.src.main.agent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgentMessageRequestDto {

	@NotBlank(message = "Message content is required")
	@Size(min = 1, max = 10000, message = "Message must be between 1 and 10000 characters")
	private String content;

	public AgentMessageRequestDto() {
	}

	public AgentMessageRequestDto(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
