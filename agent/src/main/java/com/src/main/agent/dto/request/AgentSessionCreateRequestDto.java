package com.src.main.agent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AgentSessionCreateRequestDto {

	@NotBlank(message = "Input text is required")
	@Size(min = 3, max = 10000, message = "Input text must be between 3 and 10000 characters")
	private String inputText;

	@Size(max = 255, message = "Title must not exceed 255 characters")
	private String title;

	public AgentSessionCreateRequestDto() {
	}

	public AgentSessionCreateRequestDto(String inputText, String title) {
		this.inputText = inputText;
		this.title = title;
	}

	public String getInputText() {
		return inputText;
	}

	public void setInputText(String inputText) {
		this.inputText = inputText;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
