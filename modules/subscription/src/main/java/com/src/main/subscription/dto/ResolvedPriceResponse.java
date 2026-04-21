package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.BillingCycle;

public class ResolvedPriceResponse {
	private String planCode;
	private BillingCycle billingCycle;
	private String currencyCode;
	private BigDecimal baseAmount;
	private BigDecimal amount;
	private BigDecimal discountPercent;
	private String couponCode;
	private BigDecimal couponDiscountAmount;
	private String displayLabel;
	private LocalDateTime effectiveFrom;

	ResolvedPriceResponse(final String planCode, final BillingCycle billingCycle, final String currencyCode, final BigDecimal baseAmount, final BigDecimal amount, final BigDecimal discountPercent, final String couponCode, final BigDecimal couponDiscountAmount, final String displayLabel, final LocalDateTime effectiveFrom) {
		this.planCode = planCode;
		this.billingCycle = billingCycle;
		this.currencyCode = currencyCode;
		this.baseAmount = baseAmount;
		this.amount = amount;
		this.discountPercent = discountPercent;
		this.couponCode = couponCode;
		this.couponDiscountAmount = couponDiscountAmount;
		this.displayLabel = displayLabel;
		this.effectiveFrom = effectiveFrom;
	}


	public static class ResolvedPriceResponseBuilder {
		private String planCode;
		private BillingCycle billingCycle;
		private String currencyCode;
		private BigDecimal baseAmount;
		private BigDecimal amount;
		private BigDecimal discountPercent;
		private String couponCode;
		private BigDecimal couponDiscountAmount;
		private String displayLabel;
		private LocalDateTime effectiveFrom;

		ResolvedPriceResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder planCode(final String planCode) {
			this.planCode = planCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder billingCycle(final BillingCycle billingCycle) {
			this.billingCycle = billingCycle;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder currencyCode(final String currencyCode) {
			this.currencyCode = currencyCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder baseAmount(final BigDecimal baseAmount) {
			this.baseAmount = baseAmount;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder amount(final BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder discountPercent(final BigDecimal discountPercent) {
			this.discountPercent = discountPercent;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder couponCode(final String couponCode) {
			this.couponCode = couponCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder couponDiscountAmount(final BigDecimal couponDiscountAmount) {
			this.couponDiscountAmount = couponDiscountAmount;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder displayLabel(final String displayLabel) {
			this.displayLabel = displayLabel;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public ResolvedPriceResponse.ResolvedPriceResponseBuilder effectiveFrom(final LocalDateTime effectiveFrom) {
			this.effectiveFrom = effectiveFrom;
			return this;
		}

		public ResolvedPriceResponse build() {
			return new ResolvedPriceResponse(this.planCode, this.billingCycle, this.currencyCode, this.baseAmount, this.amount, this.discountPercent, this.couponCode, this.couponDiscountAmount, this.displayLabel, this.effectiveFrom);
		}

		@Override
		public String toString() {
			return "ResolvedPriceResponse.ResolvedPriceResponseBuilder(planCode=" + this.planCode + ", billingCycle=" + this.billingCycle + ", currencyCode=" + this.currencyCode + ", baseAmount=" + this.baseAmount + ", amount=" + this.amount + ", discountPercent=" + this.discountPercent + ", couponCode=" + this.couponCode + ", couponDiscountAmount=" + this.couponDiscountAmount + ", displayLabel=" + this.displayLabel + ", effectiveFrom=" + this.effectiveFrom + ")";
		}
	}

	public static ResolvedPriceResponse.ResolvedPriceResponseBuilder builder() {
		return new ResolvedPriceResponse.ResolvedPriceResponseBuilder();
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

	public BigDecimal getBaseAmount() {
		return this.baseAmount;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public BigDecimal getDiscountPercent() {
		return this.discountPercent;
	}

	public String getCouponCode() {
		return this.couponCode;
	}

	public BigDecimal getCouponDiscountAmount() {
		return this.couponDiscountAmount;
	}

	public String getDisplayLabel() {
		return this.displayLabel;
	}

	public LocalDateTime getEffectiveFrom() {
		return this.effectiveFrom;
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

	public void setBaseAmount(final BigDecimal baseAmount) {
		this.baseAmount = baseAmount;
	}

	public void setAmount(final BigDecimal amount) {
		this.amount = amount;
	}

	public void setDiscountPercent(final BigDecimal discountPercent) {
		this.discountPercent = discountPercent;
	}

	public void setCouponCode(final String couponCode) {
		this.couponCode = couponCode;
	}

	public void setCouponDiscountAmount(final BigDecimal couponDiscountAmount) {
		this.couponDiscountAmount = couponDiscountAmount;
	}

	public void setDisplayLabel(final String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public void setEffectiveFrom(final LocalDateTime effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ResolvedPriceResponse)) return false;
		final ResolvedPriceResponse other = (ResolvedPriceResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$planCode = this.getPlanCode();
		final Object other$planCode = other.getPlanCode();
		if (this$planCode == null ? other$planCode != null : !this$planCode.equals(other$planCode)) return false;
		final Object this$billingCycle = this.getBillingCycle();
		final Object other$billingCycle = other.getBillingCycle();
		if (this$billingCycle == null ? other$billingCycle != null : !this$billingCycle.equals(other$billingCycle)) return false;
		final Object this$currencyCode = this.getCurrencyCode();
		final Object other$currencyCode = other.getCurrencyCode();
		if (this$currencyCode == null ? other$currencyCode != null : !this$currencyCode.equals(other$currencyCode)) return false;
		final Object this$baseAmount = this.getBaseAmount();
		final Object other$baseAmount = other.getBaseAmount();
		if (this$baseAmount == null ? other$baseAmount != null : !this$baseAmount.equals(other$baseAmount)) return false;
		final Object this$amount = this.getAmount();
		final Object other$amount = other.getAmount();
		if (this$amount == null ? other$amount != null : !this$amount.equals(other$amount)) return false;
		final Object this$discountPercent = this.getDiscountPercent();
		final Object other$discountPercent = other.getDiscountPercent();
		if (this$discountPercent == null ? other$discountPercent != null : !this$discountPercent.equals(other$discountPercent)) return false;
		final Object this$couponCode = this.getCouponCode();
		final Object other$couponCode = other.getCouponCode();
		if (this$couponCode == null ? other$couponCode != null : !this$couponCode.equals(other$couponCode)) return false;
		final Object this$couponDiscountAmount = this.getCouponDiscountAmount();
		final Object other$couponDiscountAmount = other.getCouponDiscountAmount();
		if (this$couponDiscountAmount == null ? other$couponDiscountAmount != null : !this$couponDiscountAmount.equals(other$couponDiscountAmount)) return false;
		final Object this$displayLabel = this.getDisplayLabel();
		final Object other$displayLabel = other.getDisplayLabel();
		if (this$displayLabel == null ? other$displayLabel != null : !this$displayLabel.equals(other$displayLabel)) return false;
		final Object this$effectiveFrom = this.getEffectiveFrom();
		final Object other$effectiveFrom = other.getEffectiveFrom();
		if (this$effectiveFrom == null ? other$effectiveFrom != null : !this$effectiveFrom.equals(other$effectiveFrom)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ResolvedPriceResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $planCode = this.getPlanCode();
		result = result * PRIME + ($planCode == null ? 43 : $planCode.hashCode());
		final Object $billingCycle = this.getBillingCycle();
		result = result * PRIME + ($billingCycle == null ? 43 : $billingCycle.hashCode());
		final Object $currencyCode = this.getCurrencyCode();
		result = result * PRIME + ($currencyCode == null ? 43 : $currencyCode.hashCode());
		final Object $baseAmount = this.getBaseAmount();
		result = result * PRIME + ($baseAmount == null ? 43 : $baseAmount.hashCode());
		final Object $amount = this.getAmount();
		result = result * PRIME + ($amount == null ? 43 : $amount.hashCode());
		final Object $discountPercent = this.getDiscountPercent();
		result = result * PRIME + ($discountPercent == null ? 43 : $discountPercent.hashCode());
		final Object $couponCode = this.getCouponCode();
		result = result * PRIME + ($couponCode == null ? 43 : $couponCode.hashCode());
		final Object $couponDiscountAmount = this.getCouponDiscountAmount();
		result = result * PRIME + ($couponDiscountAmount == null ? 43 : $couponDiscountAmount.hashCode());
		final Object $displayLabel = this.getDisplayLabel();
		result = result * PRIME + ($displayLabel == null ? 43 : $displayLabel.hashCode());
		final Object $effectiveFrom = this.getEffectiveFrom();
		result = result * PRIME + ($effectiveFrom == null ? 43 : $effectiveFrom.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ResolvedPriceResponse(planCode=" + this.getPlanCode() + ", billingCycle=" + this.getBillingCycle() + ", currencyCode=" + this.getCurrencyCode() + ", baseAmount=" + this.getBaseAmount() + ", amount=" + this.getAmount() + ", discountPercent=" + this.getDiscountPercent() + ", couponCode=" + this.getCouponCode() + ", couponDiscountAmount=" + this.getCouponDiscountAmount() + ", displayLabel=" + this.getDisplayLabel() + ", effectiveFrom=" + this.getEffectiveFrom() + ")";
	}
}
