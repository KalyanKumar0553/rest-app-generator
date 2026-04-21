package com.src.main.subscription.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.SubscriptionStatus;

public class SubscriptionContextResponse {
	private Long tenantId;
	private String planCode;
	private SubscriptionStatus subscriptionStatus;
	private BillingCycle billingCycle;
	private LocalDateTime expiresAt;
	private Boolean isTrial;
	private List<EntitlementValueResponse> entitlements;

	SubscriptionContextResponse(final Long tenantId, final String planCode, final SubscriptionStatus subscriptionStatus, final BillingCycle billingCycle, final LocalDateTime expiresAt, final Boolean isTrial, final List<EntitlementValueResponse> entitlements) {
		this.tenantId = tenantId;
		this.planCode = planCode;
		this.subscriptionStatus = subscriptionStatus;
		this.billingCycle = billingCycle;
		this.expiresAt = expiresAt;
		this.isTrial = isTrial;
		this.entitlements = entitlements;
	}


	public static class SubscriptionContextResponseBuilder {
		private Long tenantId;
		private String planCode;
		private SubscriptionStatus subscriptionStatus;
		private BillingCycle billingCycle;
		private LocalDateTime expiresAt;
		private Boolean isTrial;
		private List<EntitlementValueResponse> entitlements;

		SubscriptionContextResponseBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionContextResponse.SubscriptionContextResponseBuilder tenantId(final Long tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionContextResponse.SubscriptionContextResponseBuilder planCode(final String planCode) {
			this.planCode = planCode;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionContextResponse.SubscriptionContextResponseBuilder subscriptionStatus(final SubscriptionStatus subscriptionStatus) {
			this.subscriptionStatus = subscriptionStatus;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionContextResponse.SubscriptionContextResponseBuilder billingCycle(final BillingCycle billingCycle) {
			this.billingCycle = billingCycle;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionContextResponse.SubscriptionContextResponseBuilder expiresAt(final LocalDateTime expiresAt) {
			this.expiresAt = expiresAt;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionContextResponse.SubscriptionContextResponseBuilder isTrial(final Boolean isTrial) {
			this.isTrial = isTrial;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public SubscriptionContextResponse.SubscriptionContextResponseBuilder entitlements(final List<EntitlementValueResponse> entitlements) {
			this.entitlements = entitlements;
			return this;
		}

		public SubscriptionContextResponse build() {
			return new SubscriptionContextResponse(this.tenantId, this.planCode, this.subscriptionStatus, this.billingCycle, this.expiresAt, this.isTrial, this.entitlements);
		}

		@Override
		public String toString() {
			return "SubscriptionContextResponse.SubscriptionContextResponseBuilder(tenantId=" + this.tenantId + ", planCode=" + this.planCode + ", subscriptionStatus=" + this.subscriptionStatus + ", billingCycle=" + this.billingCycle + ", expiresAt=" + this.expiresAt + ", isTrial=" + this.isTrial + ", entitlements=" + this.entitlements + ")";
		}
	}

	public static SubscriptionContextResponse.SubscriptionContextResponseBuilder builder() {
		return new SubscriptionContextResponse.SubscriptionContextResponseBuilder();
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getPlanCode() {
		return this.planCode;
	}

	public SubscriptionStatus getSubscriptionStatus() {
		return this.subscriptionStatus;
	}

	public BillingCycle getBillingCycle() {
		return this.billingCycle;
	}

	public LocalDateTime getExpiresAt() {
		return this.expiresAt;
	}

	public Boolean getIsTrial() {
		return this.isTrial;
	}

	public List<EntitlementValueResponse> getEntitlements() {
		return this.entitlements;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setPlanCode(final String planCode) {
		this.planCode = planCode;
	}

	public void setSubscriptionStatus(final SubscriptionStatus subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public void setBillingCycle(final BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public void setExpiresAt(final LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public void setIsTrial(final Boolean isTrial) {
		this.isTrial = isTrial;
	}

	public void setEntitlements(final List<EntitlementValueResponse> entitlements) {
		this.entitlements = entitlements;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionContextResponse)) return false;
		final SubscriptionContextResponse other = (SubscriptionContextResponse) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$isTrial = this.getIsTrial();
		final Object other$isTrial = other.getIsTrial();
		if (this$isTrial == null ? other$isTrial != null : !this$isTrial.equals(other$isTrial)) return false;
		final Object this$planCode = this.getPlanCode();
		final Object other$planCode = other.getPlanCode();
		if (this$planCode == null ? other$planCode != null : !this$planCode.equals(other$planCode)) return false;
		final Object this$subscriptionStatus = this.getSubscriptionStatus();
		final Object other$subscriptionStatus = other.getSubscriptionStatus();
		if (this$subscriptionStatus == null ? other$subscriptionStatus != null : !this$subscriptionStatus.equals(other$subscriptionStatus)) return false;
		final Object this$billingCycle = this.getBillingCycle();
		final Object other$billingCycle = other.getBillingCycle();
		if (this$billingCycle == null ? other$billingCycle != null : !this$billingCycle.equals(other$billingCycle)) return false;
		final Object this$expiresAt = this.getExpiresAt();
		final Object other$expiresAt = other.getExpiresAt();
		if (this$expiresAt == null ? other$expiresAt != null : !this$expiresAt.equals(other$expiresAt)) return false;
		final Object this$entitlements = this.getEntitlements();
		final Object other$entitlements = other.getEntitlements();
		if (this$entitlements == null ? other$entitlements != null : !this$entitlements.equals(other$entitlements)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionContextResponse;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $isTrial = this.getIsTrial();
		result = result * PRIME + ($isTrial == null ? 43 : $isTrial.hashCode());
		final Object $planCode = this.getPlanCode();
		result = result * PRIME + ($planCode == null ? 43 : $planCode.hashCode());
		final Object $subscriptionStatus = this.getSubscriptionStatus();
		result = result * PRIME + ($subscriptionStatus == null ? 43 : $subscriptionStatus.hashCode());
		final Object $billingCycle = this.getBillingCycle();
		result = result * PRIME + ($billingCycle == null ? 43 : $billingCycle.hashCode());
		final Object $expiresAt = this.getExpiresAt();
		result = result * PRIME + ($expiresAt == null ? 43 : $expiresAt.hashCode());
		final Object $entitlements = this.getEntitlements();
		result = result * PRIME + ($entitlements == null ? 43 : $entitlements.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionContextResponse(tenantId=" + this.getTenantId() + ", planCode=" + this.getPlanCode() + ", subscriptionStatus=" + this.getSubscriptionStatus() + ", billingCycle=" + this.getBillingCycle() + ", expiresAt=" + this.getExpiresAt() + ", isTrial=" + this.getIsTrial() + ", entitlements=" + this.getEntitlements() + ")";
	}
}
