package com.src.main.subscription.dto;

import com.src.main.subscription.enums.FeatureType;

public class EntitlementValueResponse {
	private String featureCode;
	private FeatureType featureType;
	private Boolean enabled;
	private Long limitValue;
	private Long usedValue;
	private Long remainingValue;
	private String unit;
	private String source;

	EntitlementValueResponse(final String featureCode, final FeatureType featureType, final Boolean enabled, final Long limitValue, final Long usedValue, final Long remainingValue, final String unit, final String source) {
		this.featureCode = featureCode;
		this.featureType = featureType;
		this.enabled = enabled;
		this.limitValue = limitValue;
		this.usedValue = usedValue;
		this.remainingValue = remainingValue;
		this.unit = unit;
		this.source = source;
	}


	public static class EntitlementValueResponseBuilder {
		private String featureCode;
		private FeatureType featureType;
		private Boolean enabled;
		private Long limitValue;
		private Long usedValue;
		private Long remainingValue;
		private String unit;
		private String source;

		EntitlementValueResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder featureCode(final String featureCode) {
			this.featureCode = featureCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder featureType(final FeatureType featureType) {
			this.featureType = featureType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder enabled(final Boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder limitValue(final Long limitValue) {
			this.limitValue = limitValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder usedValue(final Long usedValue) {
			this.usedValue = usedValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder remainingValue(final Long remainingValue) {
			this.remainingValue = remainingValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder unit(final String unit) {
			this.unit = unit;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public EntitlementValueResponse.EntitlementValueResponseBuilder source(final String source) {
			this.source = source;
			return this;
		}

		public EntitlementValueResponse build() {
			return new EntitlementValueResponse(this.featureCode, this.featureType, this.enabled, this.limitValue, this.usedValue, this.remainingValue, this.unit, this.source);
		}

		@Override
		public String toString() {
			return "EntitlementValueResponse.EntitlementValueResponseBuilder(featureCode=" + this.featureCode + ", featureType=" + this.featureType + ", enabled=" + this.enabled + ", limitValue=" + this.limitValue + ", usedValue=" + this.usedValue + ", remainingValue=" + this.remainingValue + ", unit=" + this.unit + ", source=" + this.source + ")";
		}
	}

	public static EntitlementValueResponse.EntitlementValueResponseBuilder builder() {
		return new EntitlementValueResponse.EntitlementValueResponseBuilder();
	}

	public String getFeatureCode() {
		return this.featureCode;
	}

	public FeatureType getFeatureType() {
		return this.featureType;
	}

	public Boolean getEnabled() {
		return this.enabled;
	}

	public Long getLimitValue() {
		return this.limitValue;
	}

	public Long getUsedValue() {
		return this.usedValue;
	}

	public Long getRemainingValue() {
		return this.remainingValue;
	}

	public String getUnit() {
		return this.unit;
	}

	public String getSource() {
		return this.source;
	}

	public void setFeatureCode(final String featureCode) {
		this.featureCode = featureCode;
	}

	public void setFeatureType(final FeatureType featureType) {
		this.featureType = featureType;
	}

	public void setEnabled(final Boolean enabled) {
		this.enabled = enabled;
	}

	public void setLimitValue(final Long limitValue) {
		this.limitValue = limitValue;
	}

	public void setUsedValue(final Long usedValue) {
		this.usedValue = usedValue;
	}

	public void setRemainingValue(final Long remainingValue) {
		this.remainingValue = remainingValue;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	public void setSource(final String source) {
		this.source = source;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof EntitlementValueResponse)) return false;
		final EntitlementValueResponse other = (EntitlementValueResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$enabled = this.getEnabled();
		final Object other$enabled = other.getEnabled();
		if (this$enabled == null ? other$enabled != null : !this$enabled.equals(other$enabled)) return false;
		final Object this$limitValue = this.getLimitValue();
		final Object other$limitValue = other.getLimitValue();
		if (this$limitValue == null ? other$limitValue != null : !this$limitValue.equals(other$limitValue)) return false;
		final Object this$usedValue = this.getUsedValue();
		final Object other$usedValue = other.getUsedValue();
		if (this$usedValue == null ? other$usedValue != null : !this$usedValue.equals(other$usedValue)) return false;
		final Object this$remainingValue = this.getRemainingValue();
		final Object other$remainingValue = other.getRemainingValue();
		if (this$remainingValue == null ? other$remainingValue != null : !this$remainingValue.equals(other$remainingValue)) return false;
		final Object this$featureCode = this.getFeatureCode();
		final Object other$featureCode = other.getFeatureCode();
		if (this$featureCode == null ? other$featureCode != null : !this$featureCode.equals(other$featureCode)) return false;
		final Object this$featureType = this.getFeatureType();
		final Object other$featureType = other.getFeatureType();
		if (this$featureType == null ? other$featureType != null : !this$featureType.equals(other$featureType)) return false;
		final Object this$unit = this.getUnit();
		final Object other$unit = other.getUnit();
		if (this$unit == null ? other$unit != null : !this$unit.equals(other$unit)) return false;
		final Object this$source = this.getSource();
		final Object other$source = other.getSource();
		if (this$source == null ? other$source != null : !this$source.equals(other$source)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof EntitlementValueResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $enabled = this.getEnabled();
		result = result * PRIME + ($enabled == null ? 43 : $enabled.hashCode());
		final Object $limitValue = this.getLimitValue();
		result = result * PRIME + ($limitValue == null ? 43 : $limitValue.hashCode());
		final Object $usedValue = this.getUsedValue();
		result = result * PRIME + ($usedValue == null ? 43 : $usedValue.hashCode());
		final Object $remainingValue = this.getRemainingValue();
		result = result * PRIME + ($remainingValue == null ? 43 : $remainingValue.hashCode());
		final Object $featureCode = this.getFeatureCode();
		result = result * PRIME + ($featureCode == null ? 43 : $featureCode.hashCode());
		final Object $featureType = this.getFeatureType();
		result = result * PRIME + ($featureType == null ? 43 : $featureType.hashCode());
		final Object $unit = this.getUnit();
		result = result * PRIME + ($unit == null ? 43 : $unit.hashCode());
		final Object $source = this.getSource();
		result = result * PRIME + ($source == null ? 43 : $source.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "EntitlementValueResponse(featureCode=" + this.getFeatureCode() + ", featureType=" + this.getFeatureType() + ", enabled=" + this.getEnabled() + ", limitValue=" + this.getLimitValue() + ", usedValue=" + this.getUsedValue() + ", remainingValue=" + this.getRemainingValue() + ", unit=" + this.getUnit() + ", source=" + this.getSource() + ")";
	}
}
