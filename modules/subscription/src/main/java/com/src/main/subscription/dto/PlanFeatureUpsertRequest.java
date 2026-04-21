package com.src.main.subscription.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;

public class PlanFeatureUpsertRequest {
	@NotBlank
	private String featureCode;
	private Boolean isEnabled = Boolean.FALSE;
	private Long limitValue;
	private BigDecimal decimalValue;
	private String stringValue;
	private String metadataJson;

	public PlanFeatureUpsertRequest() {
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

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof PlanFeatureUpsertRequest)) return false;
		final PlanFeatureUpsertRequest other = (PlanFeatureUpsertRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$isEnabled = this.getIsEnabled();
		final Object other$isEnabled = other.getIsEnabled();
		if (this$isEnabled == null ? other$isEnabled != null : !this$isEnabled.equals(other$isEnabled)) return false;
		final Object this$limitValue = this.getLimitValue();
		final Object other$limitValue = other.getLimitValue();
		if (this$limitValue == null ? other$limitValue != null : !this$limitValue.equals(other$limitValue)) return false;
		final Object this$featureCode = this.getFeatureCode();
		final Object other$featureCode = other.getFeatureCode();
		if (this$featureCode == null ? other$featureCode != null : !this$featureCode.equals(other$featureCode)) return false;
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
		return other instanceof PlanFeatureUpsertRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $isEnabled = this.getIsEnabled();
		result = result * PRIME + ($isEnabled == null ? 43 : $isEnabled.hashCode());
		final Object $limitValue = this.getLimitValue();
		result = result * PRIME + ($limitValue == null ? 43 : $limitValue.hashCode());
		final Object $featureCode = this.getFeatureCode();
		result = result * PRIME + ($featureCode == null ? 43 : $featureCode.hashCode());
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
		return "PlanFeatureUpsertRequest(featureCode=" + this.getFeatureCode() + ", isEnabled=" + this.getIsEnabled() + ", limitValue=" + this.getLimitValue() + ", decimalValue=" + this.getDecimalValue() + ", stringValue=" + this.getStringValue() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
