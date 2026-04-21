package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.OverrideType;

public class SubscriptionOverrideResponse {
	private Long id;
	private Long tenantId;
	private String featureCode;
	private Boolean isEnabled;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	private OverrideType overrideType;
	private String reason;
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive;
	private String metadataJson;

	SubscriptionOverrideResponse(final Long id, final Long tenantId, final String featureCode, final Boolean isEnabled, final Long limitValue, final BigDecimal decimalValue, final String stringValue, final OverrideType overrideType, final String reason, final LocalDateTime effectiveFrom, final LocalDateTime effectiveTo, final Boolean isActive, final String metadataJson) {
		this.id = id;
		this.tenantId = tenantId;
		this.featureCode = featureCode;
		this.isEnabled = isEnabled;
		this.limitValue = limitValue;
		this.decimalValue = decimalValue;
		this.stringValue = stringValue;
		this.overrideType = overrideType;
		this.reason = reason;
		this.effectiveFrom = effectiveFrom;
		this.effectiveTo = effectiveTo;
		this.isActive = isActive;
		this.metadataJson = metadataJson;
	}


	public static class SubscriptionOverrideResponseBuilder {
		private Long id;
		private Long tenantId;
		private String featureCode;
		private Boolean isEnabled;
		private Long limitValue;
		private BigDecimal decimalValue;
		private String stringValue;
		private OverrideType overrideType;
		private String reason;
		private LocalDateTime effectiveFrom;
		private LocalDateTime effectiveTo;
		private Boolean isActive;
		private String metadataJson;

		SubscriptionOverrideResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder id(final Long id) {
			this.id = id;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder tenantId(final Long tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder featureCode(final String featureCode) {
			this.featureCode = featureCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder isEnabled(final Boolean isEnabled) {
			this.isEnabled = isEnabled;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder limitValue(final Long limitValue) {
			this.limitValue = limitValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder decimalValue(final BigDecimal decimalValue) {
			this.decimalValue = decimalValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder stringValue(final String stringValue) {
			this.stringValue = stringValue;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder overrideType(final OverrideType overrideType) {
			this.overrideType = overrideType;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder reason(final String reason) {
			this.reason = reason;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder effectiveFrom(final LocalDateTime effectiveFrom) {
			this.effectiveFrom = effectiveFrom;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder effectiveTo(final LocalDateTime effectiveTo) {
			this.effectiveTo = effectiveTo;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder isActive(final Boolean isActive) {
			this.isActive = isActive;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder metadataJson(final String metadataJson) {
			this.metadataJson = metadataJson;
			return this;
		}

		public SubscriptionOverrideResponse build() {
			return new SubscriptionOverrideResponse(this.id, this.tenantId, this.featureCode, this.isEnabled, this.limitValue, this.decimalValue, this.stringValue, this.overrideType, this.reason, this.effectiveFrom, this.effectiveTo, this.isActive, this.metadataJson);
		}

		@Override
		public String toString() {
			return "SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder(id=" + this.id + ", tenantId=" + this.tenantId + ", featureCode=" + this.featureCode + ", isEnabled=" + this.isEnabled + ", limitValue=" + this.limitValue + ", decimalValue=" + this.decimalValue + ", stringValue=" + this.stringValue + ", overrideType=" + this.overrideType + ", reason=" + this.reason + ", effectiveFrom=" + this.effectiveFrom + ", effectiveTo=" + this.effectiveTo + ", isActive=" + this.isActive + ", metadataJson=" + this.metadataJson + ")";
		}
	}

	public static SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder builder() {
		return new SubscriptionOverrideResponse.SubscriptionOverrideResponseBuilder();
	}

	public Long getId() {
		return this.id;
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getFeatureCode() {
		return this.featureCode;
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

	public OverrideType getOverrideType() {
		return this.overrideType;
	}

	public String getReason() {
		return this.reason;
	}

	public LocalDateTime getEffectiveFrom() {
		return this.effectiveFrom;
	}

	public LocalDateTime getEffectiveTo() {
		return this.effectiveTo;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setFeatureCode(final String featureCode) {
		this.featureCode = featureCode;
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

	public void setOverrideType(final OverrideType overrideType) {
		this.overrideType = overrideType;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	public void setEffectiveFrom(final LocalDateTime effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public void setEffectiveTo(final LocalDateTime effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionOverrideResponse)) return false;
		final SubscriptionOverrideResponse other = (SubscriptionOverrideResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$isEnabled = this.getIsEnabled();
		final Object other$isEnabled = other.getIsEnabled();
		if (this$isEnabled == null ? other$isEnabled != null : !this$isEnabled.equals(other$isEnabled)) return false;
		final Object this$limitValue = this.getLimitValue();
		final Object other$limitValue = other.getLimitValue();
		if (this$limitValue == null ? other$limitValue != null : !this$limitValue.equals(other$limitValue)) return false;
		final Object this$isActive = this.getIsActive();
		final Object other$isActive = other.getIsActive();
		if (this$isActive == null ? other$isActive != null : !this$isActive.equals(other$isActive)) return false;
		final Object this$featureCode = this.getFeatureCode();
		final Object other$featureCode = other.getFeatureCode();
		if (this$featureCode == null ? other$featureCode != null : !this$featureCode.equals(other$featureCode)) return false;
		final Object this$decimalValue = this.getDecimalValue();
		final Object other$decimalValue = other.getDecimalValue();
		if (this$decimalValue == null ? other$decimalValue != null : !this$decimalValue.equals(other$decimalValue)) return false;
		final Object this$stringValue = this.getStringValue();
		final Object other$stringValue = other.getStringValue();
		if (this$stringValue == null ? other$stringValue != null : !this$stringValue.equals(other$stringValue)) return false;
		final Object this$overrideType = this.getOverrideType();
		final Object other$overrideType = other.getOverrideType();
		if (this$overrideType == null ? other$overrideType != null : !this$overrideType.equals(other$overrideType)) return false;
		final Object this$reason = this.getReason();
		final Object other$reason = other.getReason();
		if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
		final Object this$effectiveFrom = this.getEffectiveFrom();
		final Object other$effectiveFrom = other.getEffectiveFrom();
		if (this$effectiveFrom == null ? other$effectiveFrom != null : !this$effectiveFrom.equals(other$effectiveFrom)) return false;
		final Object this$effectiveTo = this.getEffectiveTo();
		final Object other$effectiveTo = other.getEffectiveTo();
		if (this$effectiveTo == null ? other$effectiveTo != null : !this$effectiveTo.equals(other$effectiveTo)) return false;
		final Object this$metadataJson = this.getMetadataJson();
		final Object other$metadataJson = other.getMetadataJson();
		if (this$metadataJson == null ? other$metadataJson != null : !this$metadataJson.equals(other$metadataJson)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionOverrideResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $isEnabled = this.getIsEnabled();
		result = result * PRIME + ($isEnabled == null ? 43 : $isEnabled.hashCode());
		final Object $limitValue = this.getLimitValue();
		result = result * PRIME + ($limitValue == null ? 43 : $limitValue.hashCode());
		final Object $isActive = this.getIsActive();
		result = result * PRIME + ($isActive == null ? 43 : $isActive.hashCode());
		final Object $featureCode = this.getFeatureCode();
		result = result * PRIME + ($featureCode == null ? 43 : $featureCode.hashCode());
		final Object $decimalValue = this.getDecimalValue();
		result = result * PRIME + ($decimalValue == null ? 43 : $decimalValue.hashCode());
		final Object $stringValue = this.getStringValue();
		result = result * PRIME + ($stringValue == null ? 43 : $stringValue.hashCode());
		final Object $overrideType = this.getOverrideType();
		result = result * PRIME + ($overrideType == null ? 43 : $overrideType.hashCode());
		final Object $reason = this.getReason();
		result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
		final Object $effectiveFrom = this.getEffectiveFrom();
		result = result * PRIME + ($effectiveFrom == null ? 43 : $effectiveFrom.hashCode());
		final Object $effectiveTo = this.getEffectiveTo();
		result = result * PRIME + ($effectiveTo == null ? 43 : $effectiveTo.hashCode());
		final Object $metadataJson = this.getMetadataJson();
		result = result * PRIME + ($metadataJson == null ? 43 : $metadataJson.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionOverrideResponse(id=" + this.getId() + ", tenantId=" + this.getTenantId() + ", featureCode=" + this.getFeatureCode() + ", isEnabled=" + this.getIsEnabled() + ", limitValue=" + this.getLimitValue() + ", decimalValue=" + this.getDecimalValue() + ", stringValue=" + this.getStringValue() + ", overrideType=" + this.getOverrideType() + ", reason=" + this.getReason() + ", effectiveFrom=" + this.getEffectiveFrom() + ", effectiveTo=" + this.getEffectiveTo() + ", isActive=" + this.getIsActive() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
