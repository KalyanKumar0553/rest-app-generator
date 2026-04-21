package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.BillingCycle;

public class PlanPriceResponse {
	private Long id;
	private Long planId;
	private String planCode;
	private BillingCycle billingCycle;
	private String currencyCode;
	private BigDecimal amount;
	private BigDecimal discountPercent;
	private LocalDateTime effectiveFrom;
	private LocalDateTime effectiveTo;
	private Boolean isActive;
	private String displayLabel;
	private String metadataJson;

	PlanPriceResponse(final Long id, final Long planId, final String planCode, final BillingCycle billingCycle, final String currencyCode, final BigDecimal amount, final BigDecimal discountPercent, final LocalDateTime effectiveFrom, final LocalDateTime effectiveTo, final Boolean isActive, final String displayLabel, final String metadataJson) {
		this.id = id;
		this.planId = planId;
		this.planCode = planCode;
		this.billingCycle = billingCycle;
		this.currencyCode = currencyCode;
		this.amount = amount;
		this.discountPercent = discountPercent;
		this.effectiveFrom = effectiveFrom;
		this.effectiveTo = effectiveTo;
		this.isActive = isActive;
		this.displayLabel = displayLabel;
		this.metadataJson = metadataJson;
	}


	public static class PlanPriceResponseBuilder {
		private Long id;
		private Long planId;
		private String planCode;
		private BillingCycle billingCycle;
		private String currencyCode;
		private BigDecimal amount;
		private BigDecimal discountPercent;
		private LocalDateTime effectiveFrom;
		private LocalDateTime effectiveTo;
		private Boolean isActive;
		private String displayLabel;
		private String metadataJson;

		PlanPriceResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder id(final Long id) {
			this.id = id;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder planId(final Long planId) {
			this.planId = planId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder planCode(final String planCode) {
			this.planCode = planCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder billingCycle(final BillingCycle billingCycle) {
			this.billingCycle = billingCycle;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder currencyCode(final String currencyCode) {
			this.currencyCode = currencyCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder amount(final BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder discountPercent(final BigDecimal discountPercent) {
			this.discountPercent = discountPercent;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder effectiveFrom(final LocalDateTime effectiveFrom) {
			this.effectiveFrom = effectiveFrom;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder effectiveTo(final LocalDateTime effectiveTo) {
			this.effectiveTo = effectiveTo;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder isActive(final Boolean isActive) {
			this.isActive = isActive;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder displayLabel(final String displayLabel) {
			this.displayLabel = displayLabel;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public PlanPriceResponse.PlanPriceResponseBuilder metadataJson(final String metadataJson) {
			this.metadataJson = metadataJson;
			return this;
		}

		public PlanPriceResponse build() {
			return new PlanPriceResponse(this.id, this.planId, this.planCode, this.billingCycle, this.currencyCode, this.amount, this.discountPercent, this.effectiveFrom, this.effectiveTo, this.isActive, this.displayLabel, this.metadataJson);
		}

		@Override
		public String toString() {
			return "PlanPriceResponse.PlanPriceResponseBuilder(id=" + this.id + ", planId=" + this.planId + ", planCode=" + this.planCode + ", billingCycle=" + this.billingCycle + ", currencyCode=" + this.currencyCode + ", amount=" + this.amount + ", discountPercent=" + this.discountPercent + ", effectiveFrom=" + this.effectiveFrom + ", effectiveTo=" + this.effectiveTo + ", isActive=" + this.isActive + ", displayLabel=" + this.displayLabel + ", metadataJson=" + this.metadataJson + ")";
		}
	}

	public static PlanPriceResponse.PlanPriceResponseBuilder builder() {
		return new PlanPriceResponse.PlanPriceResponseBuilder();
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

	public void setId(final Long id) {
		this.id = id;
	}

	public void setPlanId(final Long planId) {
		this.planId = planId;
	}

	public void setPlanCode(final String planCode) {
		this.planCode = planCode;
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
		if (!(o instanceof PlanPriceResponse)) return false;
		final PlanPriceResponse other = (PlanPriceResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$planId = this.getPlanId();
		final Object other$planId = other.getPlanId();
		if (this$planId == null ? other$planId != null : !this$planId.equals(other$planId)) return false;
		final Object this$isActive = this.getIsActive();
		final Object other$isActive = other.getIsActive();
		if (this$isActive == null ? other$isActive != null : !this$isActive.equals(other$isActive)) return false;
		final Object this$planCode = this.getPlanCode();
		final Object other$planCode = other.getPlanCode();
		if (this$planCode == null ? other$planCode != null : !this$planCode.equals(other$planCode)) return false;
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
		return other instanceof PlanPriceResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $planId = this.getPlanId();
		result = result * PRIME + ($planId == null ? 43 : $planId.hashCode());
		final Object $isActive = this.getIsActive();
		result = result * PRIME + ($isActive == null ? 43 : $isActive.hashCode());
		final Object $planCode = this.getPlanCode();
		result = result * PRIME + ($planCode == null ? 43 : $planCode.hashCode());
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
		return "PlanPriceResponse(id=" + this.getId() + ", planId=" + this.getPlanId() + ", planCode=" + this.getPlanCode() + ", billingCycle=" + this.getBillingCycle() + ", currencyCode=" + this.getCurrencyCode() + ", amount=" + this.getAmount() + ", discountPercent=" + this.getDiscountPercent() + ", effectiveFrom=" + this.getEffectiveFrom() + ", effectiveTo=" + this.getEffectiveTo() + ", isActive=" + this.getIsActive() + ", displayLabel=" + this.getDisplayLabel() + ", metadataJson=" + this.getMetadataJson() + ")";
	}
}
