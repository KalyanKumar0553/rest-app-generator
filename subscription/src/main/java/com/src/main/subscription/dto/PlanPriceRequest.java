package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlanPriceRequest {
	@NotNull
	private BillingCycle billingCycle;
	@NotBlank
	private String currencyCode;
	@NotNull
	private BigDecimal amount;
	private BigDecimal discountPercent;
	@NotNull
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive = Boolean.TRUE;
	private String displayLabel;
	private String metadataJson;

	public PlanPriceRequest() {
	}

	public BillingCycle getBillingCycle() {
		return this.billingCycle;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public BigDecimal getDiscountPercent() {
		return this.discountPercent;
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

	public String getDisplayLabel() {
		return this.displayLabel;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setBillingCycle(final BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public void setDiscountPercent(final BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
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

	public void setDisplayLabel(final String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof PlanPriceRequest)) return false;
		final PlanPriceRequest other = (PlanPriceRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$isActive = this.getIsActive();
		final Object other$isActive = other.getIsActive();
		if (this$isActive == null ? other$isActive != null : !this$isActive.equals(other$isActive)) return false;
		final Object this$billingCycle = this.getBillingCycle();
		final Object other$billingCycle = other.getBillingCycle();
		if (this$billingCycle == null ? other$billingCycle != null : !this$billingCycle.equals(other$billingCycle)) return false;
		final Object this$currencyCode = this.getCurrencyCode();
		final Object other$currencyCode = other.getCurrencyCode();
		if (this$currencyCode == null ? other$currencyCode != null : !this$currencyCode.equals(other$currencyCode)) return false;
		final Object this$amount = this.getAmount();
		final Object other$amount = other.getAmount();
		if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) return false;
		final Object this$discountPercent = this.getDiscountPercent();
		final Object other$discountPercent = other.getDiscountPercent();
		if (this$discountPercent == null ? other$discountPercent != null : !this$discountPercent.equals(other$discountPercent)) return false;
		final Object this$effectiveFrom = this.getEffectiveFrom();
		final Object other$effectiveFrom = other.getEffectiveFrom();
		if (this$effectiveFrom == null ? other$effectiveFrom != null : !this$effectiveFrom.equals(other$effectiveFrom)) return false;
		final Object this$effectiveTo = this.getEffectiveTo();
		final Object other$effectiveTo = other.getEffectiveTo();
		if (this$effectiveTo == null ? other$effectiveTo != null : !this$effectiveTo.equals(other$effectiveTo)) return false;
		final Object this$displayLabel = this.getDisplayLabel();
		final Object other$displayLabel = other.getDisplayLabel();
		if (this$displayLabel == null ? other$displayLabel != null : !this$displayLabel.equals(other$displayLabel)) return false;
		final Object this$metadataJson = this.getMetadataJson();
		final Object other$metadataJson = other.getMetadataJson();
		if (this$metadataJson == null ? other$metadataJson != null : !this$metadataJson.equals(other$metadataJson)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof PlanPriceRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $isActive = this.getIsActive();
		result = result * PRIME + ($isActive == null ? 43 : $isActive.hashCode());
		final Object $billingCycle = this.getBillingCycle();
		result = result * PRIME + ($billingCycle == null ? 43 : $billingCycle.hashCode());
		final Object $currencyCode = this.getCurrencyCode();
		result = result * PRIME + ($currencyCode == null ? 43 : $currencyCode.hashCode());
		final Object $amount = this.getAmount();
		result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
		final Object $discountPercent = this.getDiscountPercent();
		result = result * PRIME + ($discountPercent == null ? 43 : $discountPercent.hashCode());
		final Object $effectiveFrom = this.getEffectiveFrom();
		result = result * PRIME + ($effectiveFrom == null ? 43 : $effectiveFrom.hashCode());
		final Object $effectiveTo = this.getEffectiveTo();
		result = result * PRIME + ($effectiveTo == null ? 43 : $effectiveTo.hashCode());
		final Object $displayLabel = this.getDisplayLabel();
		result = result * PRIME + ($displayLabel == null ? 43 : $displayLabel.hashCode());
		final Object $metadataJson = this.getMetadataJson();
		result = result * PRIME + ($metadataJson == null ? 43 : $metadataJson.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "PlanPriceRequest(billingCycle=" + this.getBillingCycle() + ", currencyCode=" + this.getCurrencyCode() + ", amount=" + this.getAmount() + ", discountPercent=" + this.getDiscountPercent() + ", effectiveFrom=" + this.getEffectiveFrom() + ", effectiveTo=" + this.getEffectiveTo() + ", isActive=" + this.getIsActive() + ", displayLabel=" + this.getDisplayLabel() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
