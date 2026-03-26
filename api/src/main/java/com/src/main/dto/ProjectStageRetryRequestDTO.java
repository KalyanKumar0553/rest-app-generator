package com.src.main.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public class ProjectStageRetryRequestDTO {
	private UUID runId;
	@NotBlank(message = "stage is required")
	private String stage;

	public ProjectStageRetryRequestDTO() {
	}

	public UUID getRunId() {
		return this.runId;
	}

	public String getStage() {
		return this.stage;
	}

	public void setRunId(final UUID runId) {
		this.runId = runId;
	}

	public void setStage(final String stage) {
		this.stage = stage;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ProjectStageRetryRequestDTO)) return false;
		final ProjectStageRetryRequestDTO other = (ProjectStageRetryRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$runId = this.getRunId();
		final Object other$runId = other.getRunId();
		if (this$runId == null ? other$runId != null : !this$runId.equals(other$runId)) return false;
		final Object this$stage = this.getStage();
		final Object other$stage = other.getStage();
		if (this$stage == null ? other$stage != null : !this$stage.equals(other$stage)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ProjectStageRetryRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $runId = this.getRunId();
		result = result * PRIME + ($runId == null ? 43 : $runId.hashCode());
		final Object $stage = this.getStage();
		result = result * PRIME + ($stage == null ? 43 : $stage.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ProjectStageRetryRequestDTO(runId=" + this.getRunId() + ", stage=" + this.getStage() + ")";
	}
}
