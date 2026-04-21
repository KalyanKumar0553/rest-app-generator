package com.src.main.subscription.dto;

import jakarta.validation.constraints.NotNull;

public class CancelSubscriptionRequest {
	@NotNull
	private Long tenantId;
	private Boolean immediate = Boolean.FALSE;
	private String reason;

	public CancelSubscriptionRequest() {
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public Boolean getImmediate() {
		return this.immediate;
	}

	public String getReason() {
		return this.reason;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setImmediate(final Boolean immediate) {
		this.immediate = immediate;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof CancelSubscriptionRequest)) return false;
		final CancelSubscriptionRequest other = (CancelSubscriptionRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$immediate = this.getImmediate();
		final Object other$immediate = other.getImmediate();
		if (this$immediate == null ? other$immediate != null : !this$immediate.equals(other$immediate)) return false;
		final Object this$reason = this.getReason();
		final Object other$reason = other.getReason();
		if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof CancelSubscriptionRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $immediate = this.getImmediate();
		result = result * PRIME + ($immediate == null ? 43 : $immediate.hashCode());
		final Object $reason = this.getReason();
		result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "CancelSubscriptionRequest(tenantId=" + this.getTenantId() + ", immediate=" + this.getImmediate() + ", reason=" + this.getReason() + ")";
	}
}
