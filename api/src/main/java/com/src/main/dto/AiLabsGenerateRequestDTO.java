package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;

public class AiLabsGenerateRequestDTO {
	@NotBlank
	private String prompt;

	public AiLabsGenerateRequestDTO() {
	}

	public String getPrompt() {
		return this.prompt;
	}

	public void setPrompt(final String prompt) {
		this.prompt = prompt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AiLabsGenerateRequestDTO)) return false;
		final AiLabsGenerateRequestDTO other = (AiLabsGenerateRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$prompt = this.getPrompt();
		final Object other$prompt = other.getPrompt();
		if (this$prompt == null ? other$prompt != null : !this$prompt.equals(other$prompt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AiLabsGenerateRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $prompt = this.getPrompt();
		result = result * PRIME + ($prompt == null ? 43 : $prompt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AiLabsGenerateRequestDTO(prompt=" + this.getPrompt() + ")";
	}
}
