package com.src.main.dto;

import java.time.LocalDateTime;

public class StepSummaryDTO {
	private String stepName;
	private String status;
	private long readCount;
	private long writeCount;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String exitStatus;

	public String getStepName() {
		return this.stepName;
	}

	public String getStatus() {
		return this.status;
	}

	public long getReadCount() {
		return this.readCount;
	}

	public long getWriteCount() {
		return this.writeCount;
	}

	public LocalDateTime getStartTime() {
		return this.startTime;
	}

	public LocalDateTime getEndTime() {
		return this.endTime;
	}

	public String getExitStatus() {
		return this.exitStatus;
	}

	public void setStepName(final String stepName) {
		this.stepName = stepName;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public void setReadCount(final long readCount) {
		this.readCount = readCount;
	}

	public void setWriteCount(final long writeCount) {
		this.writeCount = writeCount;
	}

	public void setStartTime(final LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(final LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public void setExitStatus(final String exitStatus) {
		this.exitStatus = exitStatus;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof StepSummaryDTO)) return false;
		final StepSummaryDTO other = (StepSummaryDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getReadCount() != other.getReadCount()) return false;
		if (this.getWriteCount() != other.getWriteCount()) return false;
		final Object this$stepName = this.getStepName();
		final Object other$stepName = other.getStepName();
		if (this$stepName == null ? other$stepName != null : !this$stepName.equals(other$stepName)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$startTime = this.getStartTime();
		final Object other$startTime = other.getStartTime();
		if (this$startTime == null ? other$startTime != null : !this$startTime.equals(other$startTime)) return false;
		final Object this$endTime = this.getEndTime();
		final Object other$endTime = other.getEndTime();
		if (this$endTime == null ? other$endTime != null : !this$endTime.equals(other$endTime)) return false;
		final Object this$exitStatus = this.getExitStatus();
		final Object other$exitStatus = other.getExitStatus();
		if (this$exitStatus == null ? other$exitStatus != null : !this$exitStatus.equals(other$exitStatus)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof StepSummaryDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final long $readCount = this.getReadCount();
		result = result * PRIME + (int) ($readCount >>> 32 ^ $readCount);
		final long $writeCount = this.getWriteCount();
		result = result * PRIME + (int) ($writeCount >>> 32 ^ $writeCount);
		final Object $stepName = this.getStepName();
		result = result * PRIME + ($stepName == null ? 43 : $stepName.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $startTime = this.getStartTime();
		result = result * PRIME + ($startTime == null ? 43 : $startTime.hashCode());
		final Object $endTime = this.getEndTime();
		result = result * PRIME + ($endTime == null ? 43 : $endTime.hashCode());
		final Object $exitStatus = this.getExitStatus();
		result = result * PRIME + ($exitStatus == null ? 43 : $exitStatus.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "StepSummaryDTO(stepName=" + this.getStepName() + ", status=" + this.getStatus() + ", readCount=" + this.getReadCount() + ", writeCount=" + this.getWriteCount() + ", startTime=" + this.getStartTime() + ", endTime=" + this.getEndTime() + ", exitStatus=" + this.getExitStatus() + ")";
	}

	public StepSummaryDTO() {
	}

	public StepSummaryDTO(final String stepName, final String status, final long readCount, final long writeCount, final LocalDateTime startTime, final LocalDateTime endTime, final String exitStatus) {
		this.stepName = stepName;
		this.status = status;
		this.readCount = readCount;
		this.writeCount = writeCount;
		this.startTime = startTime;
		this.endTime = endTime;
		this.exitStatus = exitStatus;
	}
}
