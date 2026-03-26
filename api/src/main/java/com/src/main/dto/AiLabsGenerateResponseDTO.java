package com.src.main.dto;

import java.util.UUID;

public class AiLabsGenerateResponseDTO {
	private UUID jobId;
	private String status;

	public UUID getJobId() {
		return this.jobId;
	}

	public String getStatus() {
		return this.status;
	}

	public void setJobId(final UUID jobId) {
		this.jobId = jobId;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AiLabsGenerateResponseDTO)) return false;
		final AiLabsGenerateResponseDTO other = (AiLabsGenerateResponseDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$jobId = this.getJobId();
		final Object other$jobId = other.getJobId();
		if (this$jobId == null ? other$jobId != null : !this$jobId.equals(other$jobId)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AiLabsGenerateResponseDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $jobId = this.getJobId();
		result = result * PRIME + ($jobId == null ? 43 : $jobId.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AiLabsGenerateResponseDTO(jobId=" + this.getJobId() + ", status=" + this.getStatus() + ")";
	}

	public AiLabsGenerateResponseDTO() {
	}

	public AiLabsGenerateResponseDTO(final UUID jobId, final String status) {
		this.jobId = jobId;
		this.status = status;
	}
}
