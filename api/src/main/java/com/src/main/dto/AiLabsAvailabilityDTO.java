package com.src.main.dto;

public class AiLabsAvailabilityDTO {
	private boolean enabled;
	private Integer usageLimit;
	private int usedCount;
	private Integer remainingCount;
	private boolean limitReached;

	public boolean isEnabled() {
		return this.enabled;
	}

	public Integer getUsageLimit() {
		return this.usageLimit;
	}

	public int getUsedCount() {
		return this.usedCount;
	}

	public Integer getRemainingCount() {
		return this.remainingCount;
	}

	public boolean isLimitReached() {
		return this.limitReached;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	public void setUsageLimit(final Integer usageLimit) {
		this.usageLimit = usageLimit;
	}

	public void setUsedCount(final int usedCount) {
		this.usedCount = usedCount;
	}

	public void setRemainingCount(final Integer remainingCount) {
		this.remainingCount = remainingCount;
	}

	public void setLimitReached(final boolean limitReached) {
		this.limitReached = limitReached;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AiLabsAvailabilityDTO)) return false;
		final AiLabsAvailabilityDTO other = (AiLabsAvailabilityDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isEnabled() != other.isEnabled()) return false;
		if (this.getUsedCount() != other.getUsedCount()) return false;
		if (this.isLimitReached() != other.isLimitReached()) return false;
		final Object this$usageLimit = this.getUsageLimit();
		final Object other$usageLimit = other.getUsageLimit();
		if (this$usageLimit == null ? other$usageLimit != null : !this$usageLimit.equals(other$usageLimit)) return false;
		final Object this$remainingCount = this.getRemainingCount();
		final Object other$remainingCount = other.getRemainingCount();
		if (this$remainingCount == null ? other$remainingCount != null : !this$remainingCount.equals(other$remainingCount)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AiLabsAvailabilityDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isEnabled() ? 79 : 97);
		result = result * PRIME + this.getUsedCount();
		result = result * PRIME + (this.isLimitReached() ? 79 : 97);
		final Object $usageLimit = this.getUsageLimit();
		result = result * PRIME + ($usageLimit == null ? 43 : $usageLimit.hashCode());
		final Object $remainingCount = this.getRemainingCount();
		result = result * PRIME + ($remainingCount == null ? 43 : $remainingCount.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AiLabsAvailabilityDTO(enabled=" + this.isEnabled() + ", usageLimit=" + this.getUsageLimit() + ", usedCount=" + this.getUsedCount() + ", remainingCount=" + this.getRemainingCount() + ", limitReached=" + this.isLimitReached() + ")";
	}

	public AiLabsAvailabilityDTO() {
	}

	public AiLabsAvailabilityDTO(final boolean enabled, final Integer usageLimit, final int usedCount, final Integer remainingCount, final boolean limitReached) {
		this.enabled = enabled;
		this.usageLimit = usageLimit;
		this.usedCount = usedCount;
		this.remainingCount = remainingCount;
		this.limitReached = limitReached;
	}
}
