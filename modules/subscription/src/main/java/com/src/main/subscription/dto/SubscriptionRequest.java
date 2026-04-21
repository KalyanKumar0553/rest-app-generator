package com.src.main.subscription.dto;

import java.time.LocalDateTime;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.SubscriptionSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubscriptionRequest {
	@NotNull
	private Long tenantId;
	private String userId;
	@NotBlank
	private String planCode;
	@NotNull
	private BillingCycle billingCycle;
	@NotBlank
	private String currencyCode;
	@NotNull
	private SubscriptionSource source;
	private Boolean autoRenew = Boolean.FALSE;
	private LocalDateTime startAt;
	private String couponCode;
	private String externalReference;
	private String metadataJson;
	private String reason;

	public SubscriptionRequest() {
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

	public BillingCycle getBillingCycle() {
		return this.billingCycle;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public SubscriptionSource getSource() {
		return this.source;
	}

	public Boolean getAutoRenew() {
		return this.autoRenew;
	}

	public LocalDateTime getStartAt() {
		return this.startAt;
	}

	public String getCouponCode() {
		return this.couponCode;
	}

	public String getExternalReference() {
		return this.externalReference;
	}

	public String getMetadataJson() {
		return this.metadataJson;
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

	public void setBillingCycle(final BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setSource(final SubscriptionSource source) {
		this.source = source;
	}

	public void setAutoRenew(final Boolean autoRenew) {
		this.autoRenew = autoRenew;
	}

	public void setStartAt(final LocalDateTime startAt) {
		this.startAt = startAt;
	}

	public void setCouponCode(final String couponCode) {
		this.couponCode = couponCode;
	}

	public void setExternalReference(final String externalReference) {
		this.externalReference = externalReference;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}

	public void setReason(final String reason) {
		this.reason = reason;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof SubscriptionRequest)) return false;
		final SubscriptionRequest other = (SubscriptionRequest) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$tenantId = this.getTenantId();
		final Object other$tenantId = other.getTenantId();
		if (this$tenantId == null ? other$tenantId != null : !this$tenantId.equals(other$tenantId)) return false;
		final Object this$autoRenew = this.getAutoRenew();
		final Object other$autoRenew = other.getAutoRenew();
		if (this$autoRenew == null ? other$autoRenew != null : !this$autoRenew.equals(other$autoRenew)) return false;
		final Object this$userId = this.getUserId();
		final Object other$userId = other.getUserId();
		if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
		final Object this$planCode = this.getPlanCode();
		final Object other$planCode = other.getPlanCode();
		if (this$planCode == null ? other$planCode != null : !this$planCode.equals(other$planCode)) return false;
		final Object this$billingCycle = this.getBillingCycle();
		final Object other$billingCycle = other.getBillingCycle();
		if (this$billingCycle == null ? other$billingCycle != null : !this$billingCycle.equals(other$billingCycle)) return false;
		final Object this$currencyCode = this.getCurrencyCode();
		final Object other$currencyCode = other.getCurrencyCode();
		if (this$currencyCode == null ? other$currencyCode != null : !this$currencyCode.equals(other$currencyCode)) return false;
		final Object this$source = this.getSource();
		final Object other$source = other.getSource();
		if (this$source == null ? other$source != null : !this$source.equals(other$source)) return false;
		final Object this$startAt = this.getStartAt();
		final Object other$startAt = other.getStartAt();
		if (this$startAt == null ? other$startAt != null : !this$startAt.equals(other$startAt)) return false;
		final Object this$couponCode = this.getCouponCode();
		final Object other$couponCode = other.getCouponCode();
		if (this$couponCode == null ? other$couponCode != null : !this$couponCode.equals(other$couponCode)) return false;
		final Object this$externalReference = this.getExternalReference();
		final Object other$externalReference = other.getExternalReference();
		if (this$externalReference == null ? other$externalReference != null : !this$externalReference.equals(other$externalReference)) return false;
		final Object this$metadataJson = this.getMetadataJson();
		final Object other$metadataJson = other.getMetadataJson();
		if (this$metadataJson == null ? other$metadataJson != null : !this$metadataJson.equals(other$metadataJson)) return false;
		final Object this$reason = this.getReason();
		final Object other$reason = other.getReason();
		if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof SubscriptionRequest;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $tenantId = this.getTenantId();
		result = result * PRIME + ($tenantId == null ? 43 : $tenantId.hashCode());
		final Object $autoRenew = this.getAutoRenew();
		result = result * PRIME + ($autoRenew == null ? 43 : $autoRenew.hashCode());
		final Object $userId = this.getUserId();
		result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
		final Object $planCode = this.getPlanCode();
		result = result * PRIME + ($planCode == null ? 43 : $planCode.hashCode());
		final Object $billingCycle = this.getBillingCycle();
		result = result * PRIME + ($billingCycle == null ? 43 : $billingCycle.hashCode());
		final Object $currencyCode = this.getCurrencyCode();
		result = result * PRIME + ($currencyCode == null ? 43 : $currencyCode.hashCode());
		final Object $source = this.getSource();
		result = result * PRIME + ($source == null ? 43 : $source.hashCode());
		final Object $startAt = this.getStartAt();
		result = result * PRIME + ($startAt == null ? 43 : $startAt.hashCode());
		final Object $couponCode = this.getCouponCode();
		result = result * PRIME + ($couponCode == null ? 43 : $couponCode.hashCode());
		final Object $externalReference = this.getExternalReference();
		result = result * PRIME + ($externalReference == null ? 43 : $externalReference.hashCode());
		final Object $metadataJson = this.getMetadataJson();
		result = result * PRIME + ($metadataJson == null ? 43 : $metadataJson.hashCode());
		final Object $reason = this.getReason();
		result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "SubscriptionRequest(tenantId=" + this.getTenantId() + ", userId=" + this.getUserId() + ", planCode=" + this.getPlanCode() + ", billingCycle=" + this.getBillingCycle() + ", currencyCode=" + this.getCurrencyCode() + ", source=" + this.getSource() + ", autoRenew=" + this.getAutoRenew() + ", startAt=" + this.getStartAt() + ", couponCode=" + this.getCouponCode() + ", externalReference=" + this.getExternalReference() + ", metadataJson=" + this.getMetadataJson() + ", reason=" + this.getReason() + ")";
	}
}
