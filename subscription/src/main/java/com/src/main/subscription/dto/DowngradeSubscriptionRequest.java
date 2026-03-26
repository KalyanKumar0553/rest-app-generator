package com.src.main.subscription.dto;

import com.src.main.subscription.enums.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DowngradeSubscriptionRequest {
	@NotNull
	private Long tenantId;
	@NotBlank
	private String targetPlanCode;
	private BillingCycle billingCycle;
	private String currencyCode;
	private String reason;

	public DowngradeSubscriptionRequest() {
	}

	public Long getTenantId() {
		return this.tenantId;
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

	public String getReason() {
		return this.reason;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
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

	public void setReason(final String reason) {
		this.reason = reason;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof DowngradeSubscriptionRequest)) return false;
		final DowngradeSubscriptionRequest other = (DowngradeSubscriptionRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$targetPlanCode = this.getTargetPlanCode();
		final Object other$targetPlanCode = other.getTargetPlanCode();
		if (this$targetPlanCode == null ? other$targetPlanCode != null : !this$targetPlanCode.equals(other$targetPlanCode)) return false;
		final Object this$billingCycle = this.getBillingCycle();
		final Object other$billingCycle = other.getBillingCycle();
		if (this$billingCycle == null ? other$billingCycle != null : !this$billingCycle.equals(other$billingCycle)) return false;
		final Object this$currencyCode = this.getCurrencyCode();
		final Object other$currencyCode = other.getCurrencyCode();
		if (this$currencyCode == null ? other$currencyCode != null : !this$currencyCode.equals(other$currencyCode)) return false;
		final Object this$reason = this.getReason();
		final Object other$reason = other.getReason();
		if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof DowngradeSubscriptionRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $targetPlanCode = this.getTargetPlanCode();
		result = result * PRIME + ($targetPlanCode == null ? 43 : $targetPlanCode.hashCode());
		final Object $billingCycle = this.getBillingCycle();
		result = result * PRIME + ($billingCycle == null ? 43 : $billingCycle.hashCode());
		final Object $currencyCode = this.getCurrencyCode();
		result = result * PRIME + ($currencyCode == null ? 43 : $currencyCode.hashCode());
		final Object $reason = this.getReason();
		result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "DowngradeSubscriptionRequest(tenantId=" + this.getTenantId() + ", targetPlanCode=" + this.getTargetPlanCode() + ", billingCycle=" + this.getBillingCycle() + ", currencyCode=" + this.getCurrencyCode() + ", reason=" + this.getReason() + ")";
	}
}
