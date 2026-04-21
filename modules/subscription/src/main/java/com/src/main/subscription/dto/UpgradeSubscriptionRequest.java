package com.src.main.subscription.dto;

import com.src.main.subscription.enums.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpgradeSubscriptionRequest {
	@NotNull
	private Long tenantId;
	private String userId;
	@NotBlank
	private String targetPlanCode;
	@NotNull
	private BillingCycle billingCycle;
	@NotBlank
	private String currencyCode;
	private String couponCode;
	private String reason;

	public UpgradeSubscriptionRequest() {
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getTargetPlanCode() {
		return this.targetPlanCode;
	}

	public BillingCycle getBillingCycle() {
		return this.billingCycle;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public String getCouponCode() {
		return this.couponCode;
	}

	public String getReason() {
		return this.reason;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public void setTargetPlanCode(final String targetPlanCode) {
		this.targetPlanCode = targetPlanCode;
	}

	public void setBillingCycle(final BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setCouponCode(final String couponCode) {
		this.couponCode = couponCode;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof UpgradeSubscriptionRequest)) return false;
		final UpgradeSubscriptionRequest other = (UpgradeSubscriptionRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
		final Object this$targetPlanCode = this.getTargetPlanCode();
		final Object other$targetPlanCode = other.getTargetPlanCode();
		if (this$targetPlanCode == null ? other$targetPlanCode != null : !this$targetPlanCode.equals(other$targetPlanCode)) return false;
		final Object this$billingCycle = this.getBillingCycle();
		final Object other$billingCycle = other.getBillingCycle();
		if (this$billingCycle == null ? other$billingCycle != null : !this$billingCycle.equals(other$billingCycle)) return false;
		final Object this$currencyCode = this.getCurrencyCode();
		final Object other$currencyCode = other.getCurrencyCode();
		if (this$currencyCode == null ? other$currencyCode != null : !this$currencyCode.equals(other$currencyCode)) return false;
		final Object this$couponCode = this.getCouponCode();
		final Object other$couponCode = other.getCouponCode();
		if (this$couponCode == null ? other$couponCode != null : !this$couponCode.equals(other$couponCode)) return false;
		final Object this$reason = this.getReason();
		final Object other$reason = other.getReason();
		if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof UpgradeSubscriptionRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		final Object $targetPlanCode = this.getTargetPlanCode();
		result = result * PRIME + ($targetPlanCode == null ? 43 : $targetPlanCode.hashCode());
		final Object $billingCycle = this.getBillingCycle();
		result = result * PRIME + ($billingCycle == null ? 43 : $billingCycle.hashCode());
		final Object $currencyCode = this.getCurrencyCode();
		result = result * PRIME + ($currencyCode == null ? 43 : $currencyCode.hashCode());
		final Object $couponCode = this.getCouponCode();
		result = result * PRIME + ($couponCode == null ? 43 : $couponCode.hashCode());
		final Object $reason = this.getReason();
		result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "UpgradeSubscriptionRequest(tenantId=" + this.getTenantId() + ", userId=" + this.getUserId() + ", targetPlanCode=" + this.getTargetPlanCode() + ", billingCycle=" + this.getBillingCycle() + ", currencyCode=" + this.getCurrencyCode() + ", couponCode=" + this.getCouponCode() + ", reason=" + this.getReason() + ")";
	}
}
