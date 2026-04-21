package com.src.main.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.SubscriptionStatus;

public class SubscriptionResponse {
	private Long subscriptionId;
	private Long tenantId;
	private String subscriberUserId;
	private String planCode;
	private String planName;
	private BillingCycle billingCycle;
	private SubscriptionStatus status;
	private LocalDateTime startAt;
	private LocalDateTime endAt;
	private LocalDateTime trialStartAt;
	private LocalDateTime trialEndAt;
	private Boolean autoRenew;
	private BigDecimal priceSnapshot;
	private String currencyCode;
	private String appliedCouponCode;
	private BigDecimal appliedDiscountAmount;

	SubscriptionResponse(final Long subscriptionId, final Long tenantId, final String subscriberUserId, final String planCode, final String planName, final BillingCycle billingCycle, final SubscriptionStatus status, final LocalDateTime startAt, final LocalDateTime endAt, final LocalDateTime trialStartAt, final LocalDateTime trialEndAt, final Boolean autoRenew, final BigDecimal priceSnapshot, final String currencyCode, final String appliedCouponCode, final BigDecimal appliedDiscountAmount) {
		this.subscriptionId = subscriptionId;
		this.tenantId = tenantId;
		this.subscriberUserId = subscriberUserId;
		this.planCode = planCode;
		this.planName = planName;
		this.billingCycle = billingCycle;
		this.status = status;
		this.startAt = startAt;
		this.endAt = endAt;
		this.trialStartAt = trialStartAt;
		this.trialEndAt = trialEndAt;
		this.autoRenew = autoRenew;
		this.priceSnapshot = priceSnapshot;
		this.currencyCode = currencyCode;
		this.appliedCouponCode = appliedCouponCode;
		this.appliedDiscountAmount = appliedDiscountAmount;
	}


	public static class SubscriptionResponseBuilder {
		private Long subscriptionId;
		private Long tenantId;
		private String subscriberUserId;
		private String planCode;
		private String planName;
		private BillingCycle billingCycle;
		private SubscriptionStatus status;
		private LocalDateTime startAt;
		private LocalDateTime endAt;
		private LocalDateTime trialStartAt;
		private LocalDateTime trialEndAt;
		private Boolean autoRenew;
		private BigDecimal priceSnapshot;
		private String currencyCode;
		private String appliedCouponCode;
		private BigDecimal appliedDiscountAmount;

		SubscriptionResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder subscriptionId(final Long subscriptionId) {
			this.subscriptionId = subscriptionId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder tenantId(final Long tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder subscriberUserId(final String subscriberUserId) {
			this.subscriberUserId = subscriberUserId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder planCode(final String planCode) {
			this.planCode = planCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder planName(final String planName) {
			this.planName = planName;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder billingCycle(final BillingCycle billingCycle) {
			this.billingCycle = billingCycle;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder status(final SubscriptionStatus status) {
			this.status = status;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder startAt(final LocalDateTime startAt) {
			this.startAt = startAt;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder endAt(final LocalDateTime endAt) {
			this.endAt = endAt;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder trialStartAt(final LocalDateTime trialStartAt) {
			this.trialStartAt = trialStartAt;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder trialEndAt(final LocalDateTime trialEndAt) {
			this.trialEndAt = trialEndAt;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder autoRenew(final Boolean autoRenew) {
			this.autoRenew = autoRenew;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder priceSnapshot(final BigDecimal priceSnapshot) {
			this.priceSnapshot = priceSnapshot;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder currencyCode(final String currencyCode) {
			this.currencyCode = currencyCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder appliedCouponCode(final String appliedCouponCode) {
			this.appliedCouponCode = appliedCouponCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionResponse.SubscriptionResponseBuilder appliedDiscountAmount(final BigDecimal appliedDiscountAmount) {
			this.appliedDiscountAmount = appliedDiscountAmount;
			return this;
		}

		public SubscriptionResponse build() {
			return new SubscriptionResponse(this.subscriptionId, this.tenantId, this.subscriberUserId, this.planCode, this.planName, this.billingCycle, this.status, this.startAt, this.endAt, this.trialStartAt, this.trialEndAt, this.autoRenew, this.priceSnapshot, this.currencyCode, this.appliedCouponCode, this.appliedDiscountAmount);
		}

		@Override
		public String toString() {
			return "SubscriptionResponse.SubscriptionResponseBuilder(subscriptionId=" + this.subscriptionId + ", tenantId=" + this.tenantId + ", subscriberUserId=" + this.subscriberUserId + ", planCode=" + this.planCode + ", planName=" + this.planName + ", billingCycle=" + this.billingCycle + ", status=" + this.status + ", startAt=" + this.startAt + ", endAt=" + this.endAt + ", trialStartAt=" + this.trialStartAt + ", trialEndAt=" + this.trialEndAt + ", autoRenew=" + this.autoRenew + ", priceSnapshot=" + this.priceSnapshot + ", currencyCode=" + this.currencyCode + ", appliedCouponCode=" + this.appliedCouponCode + ", appliedDiscountAmount=" + this.appliedDiscountAmount + ")";
		}
	}

	public static SubscriptionResponse.SubscriptionResponseBuilder builder() {
		return new SubscriptionResponse.SubscriptionResponseBuilder();
	}

	public Long getSubscriptionId() {
		return this.subscriptionId;
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getSubscriberUserId() {
		return this.subscriberUserId;
	}

	public String getPlanCode() {
		return this.planCode;
	}

	public String getPlanName() {
		return this.planName;
	}

	public BillingCycle getBillingCycle() {
		return this.billingCycle;
	}

	public SubscriptionStatus getStatus() {
		return this.status;
	}

	public LocalDateTime getStartAt() {
		return this.startAt;
	}

	public LocalDateTime getEndAt() {
		return this.endAt;
	}

	public LocalDateTime getTrialStartAt() {
		return this.trialStartAt;
	}

	public LocalDateTime getTrialEndAt() {
		return this.trialEndAt;
	}

	public Boolean getAutoRenew() {
		return this.autoRenew;
	}

	public BigDecimal getPriceSnapshot() {
		return this.priceSnapshot;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public String getAppliedCouponCode() {
		return this.appliedCouponCode;
	}

	public BigDecimal getAppliedDiscountAmount() {
		return this.appliedDiscountAmount;
	}

	public void setSubscriptionId(final Long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setSubscriberUserId(final String subscriberUserId) {
		this.subscriberUserId = subscriberUserId;
	}

	public void setPlanCode(final String planCode) {
		this.planCode = planCode;
	}

	public void setPlanName(final String planName) {
		this.planName = planName;
	}

	public void setBillingCycle(final BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public void setStatus(final SubscriptionStatus status) {
		this.status = status;
	}

	public void setStartAt(final LocalDateTime startAt) {
		this.startAt = startAt;
	}

	public void setEndAt(final LocalDateTime endAt) {
		this.endAt = endAt;
	}

	public void setTrialStartAt(final LocalDateTime trialStartAt) {
		this.trialStartAt = trialStartAt;
	}

	public void setTrialEndAt(final LocalDateTime trialEndAt) {
		this.trialEndAt = trialEndAt;
	}

	public void setAutoRenew(final Boolean autoRenew) {
		this.autoRenew = autoRenew;
	}

	public void setPriceSnapshot(final BigDecimal priceSnapshot) {
		this.priceSnapshot = priceSnapshot;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setAppliedCouponCode(final String appliedCouponCode) {
		this.appliedCouponCode = appliedCouponCode;
	}

	public void setAppliedDiscountAmount(final BigDecimal appliedDiscountAmount) {
		this.appliedDiscountAmount = appliedDiscountAmount;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionResponse)) return false;
		final SubscriptionResponse other = (SubscriptionResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$subscriptionId = this.getSubscriptionId();
		final Object other$subscriptionId = other.getSubscriptionId();
		if (this$subscriptionId == null ? other$subscriptionId != null : !this$subscriptionId.equals(other$subscriptionId)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$autoRenew = this.getAutoRenew();
		final Object other$autoRenew = other.getAutoRenew();
		if (this$autoRenew == null ? other$autoRenew != null : !this$autoRenew.equals(other$autoRenew)) return false;
		final Object this$subscriberUserId = this.getSubscriberUserId();
		final Object other$subscriberUserId = other.getSubscriberUserId();
		if (this$subscriberUserId == null ? other$subscriberUserId != null : !this$subscriberUserId.equals(other$subscriberUserId)) return false;
		final Object this$planCode = this.getPlanCode();
		final Object other$planCode = other.getPlanCode();
		if (this$planCode == null ? other$planCode != null : !this$planCode.equals(other$planCode)) return false;
		final Object this$planName = this.getPlanName();
		final Object other$planName = other.getPlanName();
		if (this$planName == null ? other$planName != null : !this$planName.equals(other$planName)) return false;
		final Object this$billingCycle = this.getBillingCycle();
		final Object other$billingCycle = other.getBillingCycle();
		if (this$billingCycle == null ? other$billingCycle != null : !this$billingCycle.equals(other$billingCycle)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$startAt = this.getStartAt();
		final Object other$startAt = other.getStartAt();
		if (this$startAt == null ? other$startAt != null : !this$startAt.equals(other$startAt)) return false;
		final Object this$endAt = this.getEndAt();
		final Object other$endAt = other.getEndAt();
		if (this$endAt == null ? other$endAt != null : !this$endAt.equals(other$endAt)) return false;
		final Object this$trialStartAt = this.getTrialStartAt();
		final Object other$trialStartAt = other.getTrialStartAt();
		if (this$trialStartAt == null ? other$trialStartAt != null : !this$trialStartAt.equals(other$trialStartAt)) return false;
		final Object this$trialEndAt = this.getTrialEndAt();
		final Object other$trialEndAt = other.getTrialEndAt();
		if (this$trialEndAt == null ? other$trialEndAt != null : !this$trialEndAt.equals(other$trialEndAt)) return false;
		final Object this$priceSnapshot = this.getPriceSnapshot();
		final Object other$priceSnapshot = other.getPriceSnapshot();
		if (this$priceSnapshot == null ? other$priceSnapshot != null : !this$priceSnapshot.equals(other$priceSnapshot)) return false;
		final Object this$currencyCode = this.getCurrencyCode();
		final Object other$currencyCode = other.getCurrencyCode();
		if (this$currencyCode == null ? other$currencyCode != null : !this$currencyCode.equals(other$currencyCode)) return false;
		final Object this$appliedCouponCode = this.getAppliedCouponCode();
		final Object other$appliedCouponCode = other.getAppliedCouponCode();
		if (this$appliedCouponCode == null ? other$appliedCouponCode != null : !this$appliedCouponCode.equals(other$appliedCouponCode)) return false;
		final Object this$appliedDiscountAmount = this.getAppliedDiscountAmount();
		final Object other$appliedDiscountAmount = other.getAppliedDiscountAmount();
		if (this$appliedDiscountAmount == null ? other$appliedDiscountAmount != null : !this$appliedDiscountAmount.equals(other$appliedDiscountAmount)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $subscriptionId = this.getSubscriptionId();
		result = result * PRIME + ($subscriptionId == null ? 43 : $subscriptionId.hashCode());
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $autoRenew = this.getAutoRenew();
		result = result * PRIME + ($autoRenew == null ? 43 : $autoRenew.hashCode());
		final Object $subscriberUserId = this.getSubscriberUserId();
		result = result * PRIME + ($subscriberUserId == null ? 43 : $subscriberUserId.hashCode());
		final Object $planCode = this.getPlanCode();
		result = result * PRIME + ($planCode == null ? 43 : $planCode.hashCode());
		final Object $planName = this.getPlanName();
		result = result * PRIME + ($planName == null ? 43 : $planName.hashCode());
		final Object $billingCycle = this.getBillingCycle();
		result = result * PRIME + ($billingCycle == null ? 43 : $billingCycle.hashCode());
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $startAt = this.getStartAt();
		result = result * PRIME + ($startAt == null ? 43 : $startAt.hashCode());
		final Object $endAt = this.getEndAt();
		result = result * PRIME + ($endAt == null ? 43 : $endAt.hashCode());
		final Object $trialStartAt = this.getTrialStartAt();
		result = result * PRIME + ($trialStartAt == null ? 43 : $trialStartAt.hashCode());
		final Object $trialEndAt = this.getTrialEndAt();
		result = result * PRIME + ($trialEndAt == null ? 43 : $trialEndAt.hashCode());
		final Object $priceSnapshot = this.getPriceSnapshot();
		result = result * PRIME + ($priceSnapshot == null ? 43 : $priceSnapshot.hashCode());
		final Object $currencyCode = this.getCurrencyCode();
		result = result * PRIME + ($currencyCode == null ? 43 : $currencyCode.hashCode());
		final Object $appliedCouponCode = this.getAppliedCouponCode();
		result = result * PRIME + ($appliedCouponCode == null ? 43 : $appliedCouponCode.hashCode());
		final Object $appliedDiscountAmount = this.getAppliedDiscountAmount();
		result = result * PRIME + ($appliedDiscountAmount == null ? 43 : $appliedDiscountAmount.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionResponse(subscriptionId=" + this.getSubscriptionId() + ", tenantId=" + this.getTenantId() + ", subscriberUserId=" + this.getSubscriberUserId() + ", planCode=" + this.getPlanCode() + ", planName=" + this.getPlanName() + ", billingCycle=" + this.getBillingCycle() + ", status=" + this.getStatus() + ", startAt=" + this.getStartAt() + ", endAt=" + this.getEndAt() + ", trialStartAt=" + this.getTrialStartAt() + ", trialEndAt=" + this.getTrialEndAt() + ", autoRenew=" + this.getAutoRenew() + ", priceSnapshot=" + this.getPriceSnapshot() + ", currencyCode=" + this.getCurrencyCode() + ", appliedCouponCode=" + this.getAppliedCouponCode() + ", appliedDiscountAmount=" + this.getAppliedDiscountAmount() + ")";
	}
}
