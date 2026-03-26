package com.src.main.dto;

import java.time.LocalDateTime;

public class RunSummaryDTO {
	private Long executionId;
	private String status;
	private LocalDateTime startTime;
	private LocalDateTime endTime;

	public Long getExecutionId() {
		return this.executionId;
	}

	public String getStatus() {
		return this.status;
	}

	public LocalDateTime getStartTime() {
		return this.startTime;
	}

	public LocalDateTime getEndTime() {
		return this.endTime;
	}

	public void setExecutionId(final Long executionId) {
		this.executionId = executionId;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setStartTime(final LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(final LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof RunSummaryDTO)) return false;
		final RunSummaryDTO other = (RunSummaryDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$executionId = this.getExecutionId();
		final Object other$executionId = other.getExecutionId();
		if (this$executionId == null ? other$executionId != null : !this$executionId.equals(other$executionId)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$startTime = this.getStartTime();
		final Object other$startTime = other.getStartTime();
		if (this$startTime == null ? other$startTime != null : !this$startTime.equals(other$startTime)) return false;
		final Object this$endTime = this.getEndTime();
		final Object other$endTime = other.getEndTime();
		if (this$endTime == null ? other$endTime != null : !this$endTime.equals(other$endTime)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof RunSummaryDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $executionId = this.getExecutionId();
		result = result * PRIME + ($executionId == null ? 43 : $executionId.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $startTime = this.getStartTime();
		result = result * PRIME + ($startTime == null ? 43 : $startTime.hashCode());
		final Object $endTime = this.getEndTime();
		result = result * PRIME + ($endTime == null ? 43 : $endTime.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "RunSummaryDTO(executionId=" + this.getExecutionId() + ", status=" + this.getStatus() + ", startTime=" + this.getStartTime() + ", endTime=" + this.getEndTime() + ")";
	}

	public RunSummaryDTO() {
	}

	public RunSummaryDTO(final Long executionId, final String status, final LocalDateTime startTime, final LocalDateTime endTime) {
		this.executionId = executionId;
		this.status = status;
		this.startTime = startTime;
		this.endTime = endTime;
	}
}
