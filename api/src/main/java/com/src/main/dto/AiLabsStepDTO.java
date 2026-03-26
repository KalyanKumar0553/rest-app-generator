package com.src.main.dto;

import java.time.OffsetDateTime;

public class AiLabsStepDTO {
	private String key;
	private String label;
	private String status;
	private String message;
	private OffsetDateTime updatedAt;

	public String getKey() {
		return this.key;
	}

	public String getLabel() {
		return this.label;
	}

	public String getStatus() {
		return this.status;
	}

	public String getMessage() {
		return this.message;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AiLabsStepDTO)) return false;
		final AiLabsStepDTO other = (AiLabsStepDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$key = this.getKey();
		final Object other$key = other.getKey();
		if (this$key == null ? other$key != null : !this$key.equals(other$key)) return false;
		final Object this$label = this.getLabel();
		final Object other$label = other.getLabel();
		if (this$label == null ? other$label != null : !this$label.equals(other$label)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$message = this.getMessage();
		final Object other$message = other.getMessage();
		if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AiLabsStepDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $key = this.getKey();
		result = result * PRIME + ($key == null ? 43 : $key.hashCode());
		final Object $label = this.getLabel();
		result = result * PRIME + ($label == null ? 43 : $label.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $message = this.getMessage();
		result = result * PRIME + ($message == null ? 43 : $message.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AiLabsStepDTO(key=" + this.getKey() + ", label=" + this.getLabel() + ", status=" + this.getStatus() + ", message=" + this.getMessage() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}

	public AiLabsStepDTO() {
	}

	public AiLabsStepDTO(final String key, final String label, final String status, final String message, final OffsetDateTime updatedAt) {
		this.key = key;
		this.label = label;
		this.status = status;
		this.message = message;
		this.updatedAt = updatedAt;
	}
}
