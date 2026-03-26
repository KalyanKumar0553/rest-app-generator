package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class AiLabsJobStatusDTO {
	private UUID jobId;
	private String status;
	private String prompt;
	private List<AiLabsStepDTO> steps;
	private String streamPreview;
	private String projectId;
	private String generator;
	private String errorMessage;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public UUID getJobId() {
		return this.jobId;
	}

	public String getStatus() {
		return this.status;
	}

	public String getPrompt() {
		return this.prompt;
	}

	public List<AiLabsStepDTO> getSteps() {
		return this.steps;
	}

	public String getStreamPreview() {
		return this.streamPreview;
	}

	public String getProjectId() {
		return this.projectId;
	}

	public String getGenerator() {
		return this.generator;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public OffsetDateTime getCreatedAt() {
		return this.createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return this.updatedAt;
	}

	public void setJobId(final UUID jobId) {
		this.jobId = jobId;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setPrompt(final String prompt) {
		this.prompt = prompt;
	}

	public void setSteps(final List<AiLabsStepDTO> steps) {
		this.steps = steps;
	}

	public void setStreamPreview(final String streamPreview) {
		this.streamPreview = streamPreview;
	}

	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	public void setGenerator(final String generator) {
		this.generator = generator;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void setCreatedAt(final OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setUpdatedAt(final OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AiLabsJobStatusDTO)) return false;
		final AiLabsJobStatusDTO other = (AiLabsJobStatusDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$jobId = this.getJobId();
		final Object other$jobId = other.getJobId();
		if (this$jobId == null ? other$jobId != null : !this$jobId.equals(other$jobId)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$prompt = this.getPrompt();
		final Object other$prompt = other.getPrompt();
		if (this$prompt == null ? other$prompt != null : !this$prompt.equals(other$prompt)) return false;
		final Object this$steps = this.getSteps();
		final Object other$steps = other.getSteps();
		if (this$steps == null ? other$steps != null : !this$steps.equals(other$steps)) return false;
		final Object this$streamPreview = this.getStreamPreview();
		final Object other$streamPreview = other.getStreamPreview();
		if (this$streamPreview == null ? other$streamPreview != null : !this$streamPreview.equals(other$streamPreview)) return false;
		final Object this$projectId = this.getProjectId();
		final Object other$projectId = other.getProjectId();
		if (this$projectId == null ? other$projectId != null : !this$projectId.equals(other$projectId)) return false;
		final Object this$generator = this.getGenerator();
		final Object other$generator = other.getGenerator();
		if (this$generator == null ? other$generator != null : !this$generator.equals(other$generator)) return false;
		final Object this$errorMessage = this.getErrorMessage();
		final Object other$errorMessage = other.getErrorMessage();
		if (this$errorMessage == null ? other$errorMessage != null : !this$errorMessage.equals(other$errorMessage)) return false;
		final Object this$createdAt = this.getCreatedAt();
		final Object other$createdAt = other.getCreatedAt();
		if (this$createdAt == null ? other$createdAt != null : !this$createdAt.equals(other$createdAt)) return false;
		final Object this$updatedAt = this.getUpdatedAt();
		final Object other$updatedAt = other.getUpdatedAt();
		if (this$updatedAt == null ? other$updatedAt != null : !this$updatedAt.equals(other$updatedAt)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AiLabsJobStatusDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $jobId = this.getJobId();
		result = result * PRIME + ($jobId == null ? 43 : $jobId.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $prompt = this.getPrompt();
		result = result * PRIME + ($prompt == null ? 43 : $prompt.hashCode());
		final Object $steps = this.getSteps();
		result = result * PRIME + ($steps == null ? 43 : $steps.hashCode());
		final Object $streamPreview = this.getStreamPreview();
		result = result * PRIME + ($streamPreview == null ? 43 : $streamPreview.hashCode());
		final Object $projectId = this.getProjectId();
		result = result * PRIME + ($projectId == null ? 43 : $projectId.hashCode());
		final Object $generator = this.getGenerator();
		result = result * PRIME + ($generator == null ? 43 : $generator.hashCode());
		final Object $errorMessage = this.getErrorMessage();
		result = result * PRIME + ($errorMessage == null ? 43 : $errorMessage.hashCode());
		final Object $createdAt = this.getCreatedAt();
		result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
		final Object $updatedAt = this.getUpdatedAt();
		result = result * PRIME + ($updatedAt == null ? 43 : $updatedAt.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AiLabsJobStatusDTO(jobId=" + this.getJobId() + ", status=" + this.getStatus() + ", prompt=" + this.getPrompt() + ", steps=" + this.getSteps() + ", streamPreview=" + this.getStreamPreview() + ", projectId=" + this.getProjectId() + ", generator=" + this.getGenerator() + ", errorMessage=" + this.getErrorMessage() + ", createdAt=" + this.getCreatedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
	}

	public AiLabsJobStatusDTO() {
	}

	public AiLabsJobStatusDTO(final UUID jobId, final String status, final String prompt, final List<AiLabsStepDTO> steps, final String streamPreview, final String projectId, final String generator, final String errorMessage, final OffsetDateTime createdAt, final OffsetDateTime updatedAt) {
		this.jobId = jobId;
		this.status = status;
		this.prompt = prompt;
		this.steps = steps;
		this.streamPreview = streamPreview;
		this.projectId = projectId;
		this.generator = generator;
		this.errorMessage = errorMessage;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
