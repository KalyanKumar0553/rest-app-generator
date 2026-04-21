package com.src.main.subscription.dto;

import java.math.BigDecimal;
import com.src.main.subscription.enums.FeatureType;

public class PlanFeatureResponse {
	private Long id;
	private Long planId;
	private String planCode;
	private Long featureId;
	private String featureCode;
	private FeatureType featureType;
	private Boolean isEnabled;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	private String metadataJson;

	PlanFeatureResponse(final Long id, final Long planId, final String planCode, final Long featureId, final String featureCode, final FeatureType featureType, final Boolean isEnabled, final Long limitValue, final BigDecimal decimalValue, final String stringValue, final String metadataJson) {
		this.id = id;
		this.planId = planId;
		this.planCode = planCode;
		this.featureId = featureId;
		this.featureCode = featureCode;
		this.featureType = featureType;
		this.isEnabled = isEnabled;
		this.limitValue = limitValue;
		this.decimalValue = decimalValue;
		this.stringValue = stringValue;
		this.metadataJson = metadataJson;
	}


	public static class PlanFeatureResponseBuilder {
		private Long id;
		private Long planId;
		private String planCode;
		private Long featureId;
		private String featureCode;
		private FeatureType featureType;
		private Boolean isEnabled;
		private Long limitValue;
		private BigDecimal decimalValue;
		private String stringValue;
		private String metadataJson;

		PlanFeatureResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder id(final Long id) {
			this.id = id;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder planId(final Long planId) {
			this.planId = planId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder planCode(final String planCode) {
			this.planCode = planCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder featureId(final Long featureId) {
			this.featureId = featureId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder featureCode(final String featureCode) {
			this.featureCode = featureCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder featureType(final FeatureType featureType) {
			this.featureType = featureType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder isEnabled(final Boolean isEnabled) {
			this.isEnabled = isEnabled;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder limitValue(final Long limitValue) {
			this.limitValue = limitValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder decimalValue(final BigDecimal decimalValue) {
			this.decimalValue = decimalValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder stringValue(final String stringValue) {
			this.stringValue = stringValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanFeatureResponse.PlanFeatureResponseBuilder metadataJson(final String metadataJson) {
			this.metadataJson = metadataJson;
			return this;
		}

		public PlanFeatureResponse build() {
			return new PlanFeatureResponse(this.id, this.planId, this.planCode, this.featureId, this.featureCode, this.featureType, this.isEnabled, this.limitValue, this.decimalValue, this.stringValue, this.metadataJson);
		}

		@Override
		public String toString() {
			return "PlanFeatureResponse.PlanFeatureResponseBuilder(id=" + this.id + ", planId=" + this.planId + ", planCode=" + this.planCode + ", featureId=" + this.featureId + ", featureCode=" + this.featureCode + ", featureType=" + this.featureType + ", isEnabled=" + this.isEnabled + ", limitValue=" + this.limitValue + ", decimalValue=" + this.decimalValue + ", stringValue=" + this.stringValue + ", metadataJson=" + this.metadataJson + ")";
		}
	}

	public static PlanFeatureResponse.PlanFeatureResponseBuilder builder() {
		return new PlanFeatureResponse.PlanFeatureResponseBuilder();
	}

	public Long getId() {
		return this.id;
	}

	public Long getPlanId() {
		return this.planId;
	}

	public String getPlanCode() {
		return this.planCode;
	}

	public Long getFeatureId() {
		return this.featureId;
	}

	public String getFeatureCode() {
		return this.featureCode;
	}

	public FeatureType getFeatureType() {
		return this.featureType;
	}

	public Boolean getIsEnabled() {
		return this.isEnabled;
	}

	public Long getLimitValue() {
		return this.limitValue;
	}

	public BigDecimal getDecimalValue() {
		return this.decimalValue;
	}

	public String getStringValue() {
		return this.stringValue;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setPlanId(final Long planId) {
		this.planId = planId;
	}

	public void setPlanCode(final String planCode) {
		this.planCode = planCode;
	}

	public void setFeatureId(final Long featureId) {
		this.featureId = featureId;
	}

	public void setFeatureCode(final String featureCode) {
		this.featureCode = featureCode;
	}

	public void setFeatureType(final FeatureType featureType) {
		this.featureType = featureType;
	}

	public void setIsEnabled(final Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setLimitValue(final Long limitValue) {
		this.limitValue = limitValue;
	}

	public void setDecimalValue(final BigDecimal decimalValue) {
		this.decimalValue = decimalValue;
	}

	public void setStringValue(final String stringValue) {
		this.stringValue = stringValue;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof PlanFeatureResponse)) return false;
		final PlanFeatureResponse other = (PlanFeatureResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$planId = this.getPlanId();
		final Object other$planId = other.getPlanId();
		if (this$planId == null ? other$planId != null : !this$planId.equals(other$planId)) return false;
		final Object this$featureId = this.getFeatureId();
		final Object other$featureId = other.getFeatureId();
		if (this$featureId == null ? other$featureId != null : !this$featureId.equals(other$featureId)) return false;
		final Object this$isEnabled = this.getIsEnabled();
		final Object other$isEnabled = other.getIsEnabled();
		if (this$isEnabled == null ? other$isEnabled != null : !this$isEnabled.equals(other$isEnabled)) return false;
		final Object this$limitValue = this.getLimitValue();
		final Object other$limitValue = other.getLimitValue();
		if (this$limitValue == null ? other$limitValue != null : !this$limitValue.equals(other$limitValue)) return false;
		final Object this$planCode = this.getPlanCode();
		final Object other$planCode = other.getPlanCode();
		if (this$planCode == null ? other$planCode != null : !this$planCode.equals(other$planCode)) return false;
		final Object this$featureCode = this.getFeatureCode();
		final Object other$featureCode = other.getFeatureCode();
		if (this$featureCode == null ? other$featureCode != null : !this$featureCode.equals(other$featureCode)) return false;
		final Object this$featureType = this.getFeatureType();
		final Object other$featureType = other.getFeatureType();
		if (this$featureType == null ? other$featureType != null : !this$featureType.equals(other$featureType)) return false;
		final Object this$decimalValue = this.getDecimalValue();
		final Object other$decimalValue = other.getDecimalValue();
		if (this$decimalValue == null ? other$decimalValue != null : !this$decimalValue.equals(other$decimalValue)) return false;
		final Object this$stringValue = this.getStringValue();
		final Object other$stringValue = other.getStringValue();
		if (this$stringValue == null ? other$stringValue != null : !this$stringValue.equals(other$stringValue)) return false;
		final Object this$metadataJson = this.getMetadataJson();
		final Object other$metadataJson = other.getMetadataJson();
		if (this$metadataJson == null ? other$metadataJson != null : !this$metadataJson.equals(other$metadataJson)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof PlanFeatureResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $planId = this.getPlanId();
		result = result * PRIME + ($planId == null ? 43 : $planId.hashCode());
		final Object $featureId = this.getFeatureId();
		result = result * PRIME + ($featureId == null ? 43 : $featureId.hashCode());
		final Object $isEnabled = this.getIsEnabled();
		result = result * PRIME + ($isEnabled == null ? 43 : $isEnabled.hashCode());
		final Object $limitValue = this.getLimitValue();
		result = result * PRIME + ($limitValue == null ? 43 : $limitValue.hashCode());
		final Object $planCode = this.getPlanCode();
		result = result * PRIME + ($planCode == null ? 43 : $planCode.hashCode());
		final Object $featureCode = this.getFeatureCode();
		result = result * PRIME + ($featureCode == null ? 43 : $featureCode.hashCode());
		final Object $featureType = this.getFeatureType();
		result = result * PRIME + ($featureType == null ? 43 : $featureType.hashCode());
		final Object $decimalValue = this.getDecimalValue();
		result = result * PRIME + ($decimalValue == null ? 43 : $decimalValue.hashCode());
		final Object $stringValue = this.getStringValue();
		result = result * PRIME + ($stringValue == null ? 43 : $stringValue.hashCode());
		final Object $metadataJson = this.getMetadataJson();
		result = result * PRIME + ($metadataJson == null ? 43 : $metadataJson.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "PlanFeatureResponse(id=" + this.getId() + ", planId=" + this.getPlanId() + ", planCode=" + this.getPlanCode() + ", featureId=" + this.getFeatureId() + ", featureCode=" + this.getFeatureCode() + ", featureType=" + this.getFeatureType() + ", isEnabled=" + this.getIsEnabled() + ", limitValue=" + this.getLimitValue() + ", decimalValue=" + this.getDecimalValue() + ", stringValue=" + this.getStringValue() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
