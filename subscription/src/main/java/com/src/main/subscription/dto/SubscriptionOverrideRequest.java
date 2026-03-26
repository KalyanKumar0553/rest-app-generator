package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.OverrideType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubscriptionOverrideRequest {
	@NotBlank
	private String featureCode;
	private Boolean isEnabled;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	@NotNull
	private OverrideType overrideType;
	private String reason;
	@NotNull
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive = Boolean.TRUE;
	private String metadataJson;

	public SubscriptionOverrideRequest() {
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
		if (!(o instanceof SubscriptionOverrideRequest)) return false;
		final SubscriptionOverrideRequest other = (SubscriptionOverrideRequest) o;
		if (!other.canEqual((Object) this)) return false;
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
		return other instanceof SubscriptionOverrideRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
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
		return "SubscriptionOverrideRequest(featureCode=" + this.getFeatureCode() + ", isEnabled=" + this.getIsEnabled() + ", limitValue=" + this.getLimitValue() + ", decimalValue=" + this.getDecimalValue() + ", stringValue=" + this.getStringValue() + ", overrideType=" + this.getOverrideType() + ", reason=" + this.getReason() + ", effectiveFrom=" + this.getEffectiveFrom() + ", effectiveTo=" + this.getEffectiveTo() + ", isActive=" + this.getIsActive() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
