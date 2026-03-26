package com.src.main.subscription.dto;

public class UsageStatusResponse {
	private Long tenantId;
	private String featureCode;
	private Long usedValue;
	private Long reservedValue;
	private Long remainingValue;
	private String periodKey;

	UsageStatusResponse(final Long tenantId, final String featureCode, final Long usedValue, final Long reservedValue, final Long remainingValue, final String periodKey) {
		this.tenantId = tenantId;
		this.featureCode = featureCode;
		this.usedValue = usedValue;
		this.reservedValue = reservedValue;
		this.remainingValue = remainingValue;
		this.periodKey = periodKey;
	}


	public static class UsageStatusResponseBuilder {
		private Long tenantId;
		private String featureCode;
		private Long usedValue;
		private Long reservedValue;
		private Long remainingValue;
		private String periodKey;

		UsageStatusResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public UsageStatusResponse.UsageStatusResponseBuilder tenantId(final Long tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageStatusResponse.UsageStatusResponseBuilder featureCode(final String featureCode) {
			this.featureCode = featureCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageStatusResponse.UsageStatusResponseBuilder usedValue(final Long usedValue) {
			this.usedValue = usedValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageStatusResponse.UsageStatusResponseBuilder reservedValue(final Long reservedValue) {
			this.reservedValue = reservedValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageStatusResponse.UsageStatusResponseBuilder remainingValue(final Long remainingValue) {
			this.remainingValue = remainingValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public UsageStatusResponse.UsageStatusResponseBuilder periodKey(final String periodKey) {
			this.periodKey = periodKey;
			return this;
		}

		public UsageStatusResponse build() {
			return new UsageStatusResponse(this.tenantId, this.featureCode, this.usedValue, this.reservedValue, this.remainingValue, this.periodKey);
		}

		@Override
		public String toString() {
			return "UsageStatusResponse.UsageStatusResponseBuilder(tenantId=" + this.tenantId + ", featureCode=" + this.featureCode + ", usedValue=" + this.usedValue + ", reservedValue=" + this.reservedValue + ", remainingValue=" + this.remainingValue + ", periodKey=" + this.periodKey + ")";
		}
	}

	public static UsageStatusResponse.UsageStatusResponseBuilder builder() {
		return new UsageStatusResponse.UsageStatusResponseBuilder();
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getFeatureCode() {
		return this.featureCode;
	}

	public Long getUsedValue() {
		return this.usedValue;
	}

	public Long getReservedValue() {
		return this.reservedValue;
	}

	public Long getRemainingValue() {
		return this.remainingValue;
	}

	public String getPeriodKey() {
		return this.periodKey;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setFeatureCode(final String featureCode) {
		this.featureCode = featureCode;
	}

	public void setUsedValue(final Long usedValue) {
		this.usedValue = usedValue;
	}

	public void setReservedValue(final Long reservedValue) {
		this.reservedValue = reservedValue;
	}

	public void setRemainingValue(final Long remainingValue) {
		this.remainingValue = remainingValue;
	}

	public void setPeriodKey(final String periodKey) {
		this.periodKey = periodKey;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof UsageStatusResponse)) return false;
		final UsageStatusResponse other = (UsageStatusResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$usedValue = this.getUsedValue();
		final Object other$usedValue = other.getUsedValue();
		if (this$usedValue == null ? other$usedValue != null : !this$usedValue.equals(other$usedValue)) return false;
		final Object this$reservedValue = this.getReservedValue();
		final Object other$reservedValue = other.getReservedValue();
		if (this$reservedValue == null ? other$reservedValue != null : !this$reservedValue.equals(other$reservedValue)) return false;
		final Object this$remainingValue = this.getRemainingValue();
		final Object other$remainingValue = other.getRemainingValue();
		if (this$remainingValue == null ? other$remainingValue != null : !this$remainingValue.equals(other$remainingValue)) return false;
		final Object this$featureCode = this.getFeatureCode();
		final Object other$featureCode = other.getFeatureCode();
		if (this$featureCode == null ? other$featureCode != null : !this$featureCode.equals(other$featureCode)) return false;
		final Object this$periodKey = this.getPeriodKey();
		final Object other$periodKey = other.getPeriodKey();
		if (this$periodKey == null ? other$periodKey != null : !this$periodKey.equals(other$periodKey)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof UsageStatusResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $usedValue = this.getUsedValue();
		result = result * PRIME + ($usedValue == null ? 43 : $usedValue.hashCode());
		final Object $reservedValue = this.getReservedValue();
		result = result * PRIME + ($reservedValue == null ? 43 : $reservedValue.hashCode());
		final Object $remainingValue = this.getRemainingValue();
		result = result * PRIME + ($remainingValue == null ? 43 : $remainingValue.hashCode());
		final Object $featureCode = this.getFeatureCode();
		result = result * PRIME + ($featureCode == null ? 43 : $featureCode.hashCode());
		final Object $periodKey = this.getPeriodKey();
		result = result * PRIME + ($periodKey == null ? 43 : $periodKey.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "UsageStatusResponse(tenantId=" + this.getTenantId() + ", featureCode=" + this.getFeatureCode() + ", usedValue=" + this.getUsedValue() + ", reservedValue=" + this.getReservedValue() + ", remainingValue=" + this.getRemainingValue() + ", periodKey=" + this.getPeriodKey() + ")";
	}
}
