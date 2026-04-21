package com.src.main.subscription.dto;

import jakarta.validation.constraints.NotNull;

public class StartTrialRequest {
	@NotNull
	private Long tenantId;
	private String userId;
	private String planCode;
	private String reason;

	public StartTrialRequest() {
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getPlanCode() {
		return this.planCode;
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

	public void setPlanCode(final String planCode) {
		this.planCode = planCode;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof StartTrialRequest)) return false;
		final StartTrialRequest other = (StartTrialRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
		final Object this$planCode = this.getPlanCode();
		final Object other$planCode = other.getPlanCode();
		if (this$planCode == null ? other$planCode != null : !this$planCode.equals(other$planCode)) return false;
		final Object this$reason = this.getReason();
		final Object other$reason = other.getReason();
		if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof StartTrialRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		final Object $planCode = this.getPlanCode();
		result = result * PRIME + ($planCode == null ? 43 : $planCode.hashCode());
		final Object $reason = this.getReason();
		result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "StartTrialRequest(tenantId=" + this.getTenantId() + ", userId=" + this.getUserId() + ", planCode=" + this.getPlanCode() + ", reason=" + this.getReason() + ")";
	}
}
