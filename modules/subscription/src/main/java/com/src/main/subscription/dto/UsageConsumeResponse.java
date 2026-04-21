package com.src.main.subscription.dto;

public class UsageConsumeResponse {
	private Boolean allowed;
	private Long usedValue;
	private Long remainingValue;
	private String featureCode;

	UsageConsumeResponse(final Boolean allowed, final Long usedValue, final Long remainingValue, final String featureCode) {
		this.allowed = allowed;
		this.usedValue = usedValue;
		this.remainingValue = remainingValue;
		this.featureCode = featureCode;
	}


	public static class UsageConsumeResponseBuilder {
		private Boolean allowed;
		private Long usedValue;
		private Long remainingValue;
		private String featureCode;

		UsageConsumeResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public UsageConsumeResponse.UsageConsumeResponseBuilder allowed(final Boolean allowed) {
			this.allowed = allowed;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageConsumeResponse.UsageConsumeResponseBuilder usedValue(final Long usedValue) {
			this.usedValue = usedValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageConsumeResponse.UsageConsumeResponseBuilder remainingValue(final Long remainingValue) {
			this.remainingValue = remainingValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageConsumeResponse.UsageConsumeResponseBuilder featureCode(final String featureCode) {
			this.featureCode = featureCode;
			return this;
		}

		public UsageConsumeResponse build() {
			return new UsageConsumeResponse(this.allowed, this.usedValue, this.remainingValue, this.featureCode);
		}

		@Override
		public String toString() {
			return "UsageConsumeResponse.UsageConsumeResponseBuilder(allowed=" + this.allowed + ", usedValue=" + this.usedValue + ", remainingValue=" + this.remainingValue + ", featureCode=" + this.featureCode + ")";
		}
	}

	public static UsageConsumeResponse.UsageConsumeResponseBuilder builder() {
		return new UsageConsumeResponse.UsageConsumeResponseBuilder();
	}

	public Boolean getAllowed() {
		return this.allowed;
	}

	public Long getUsedValue() {
		return this.usedValue;
	}

	public Long getRemainingValue() {
		return this.remainingValue;
	}

	public String getFeatureCode() {
		return this.featureCode;
	}

	public void setAllowed(final Boolean allowed) {
		this.allowed = allowed;
	}

	public void setUsedValue(final Long usedValue) {
		this.usedValue = usedValue;
	}

	public void setRemainingValue(final Long remainingValue) {
		this.remainingValue = remainingValue;
	}

	public void setFeatureCode(final String featureCode) {
		this.featureCode = featureCode;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof UsageConsumeResponse)) return false;
		final UsageConsumeResponse other = (UsageConsumeResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$allowed = this.getAllowed();
		final Object other$allowed = other.getAllowed();
		if (this$allowed == null ? other$allowed != null : !this$allowed.equals(other$allowed)) return false;
		final Object this$usedValue = this.getUsedValue();
		final Object other$usedValue = other.getUsedValue();
		if (this$usedValue == null ? other$usedValue != null : !this$usedValue.equals(other$usedValue)) return false;
		final Object this$remainingValue = this.getRemainingValue();
		final Object other$remainingValue = other.getRemainingValue();
		if (this$remainingValue == null ? other$remainingValue != null : !this$remainingValue.equals(other$remainingValue)) return false;
		final Object this$featureCode = this.getFeatureCode();
		final Object other$featureCode = other.getFeatureCode();
		if (this$featureCode == null ? other$featureCode != null : !this$featureCode.equals(other$featureCode)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof UsageConsumeResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $allowed = this.getAllowed();
		result = result * PRIME + ($allowed == null ? 43 : $allowed.hashCode());
		final Object $usedValue = this.getUsedValue();
		result = result * PRIME + ($usedValue == null ? 43 : $usedValue.hashCode());
		final Object $remainingValue = this.getRemainingValue();
		result = result * PRIME + ($remainingValue == null ? 43 : $remainingValue.hashCode());
		final Object $featureCode = this.getFeatureCode();
		result = result * PRIME + ($featureCode == null ? 43 : $featureCode.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "UsageConsumeResponse(allowed=" + this.getAllowed() + ", usedValue=" + this.getUsedValue() + ", remainingValue=" + this.getRemainingValue() + ", featureCode=" + this.getFeatureCode() + ")";
	}
}
