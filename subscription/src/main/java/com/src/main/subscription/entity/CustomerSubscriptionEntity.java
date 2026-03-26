package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.SubscriptionSource;
import com.src.main.subscription.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_subscription")
public class CustomerSubscriptionEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;
	@Column(name = "subscriber_user_id")
	private String subscriberUserId;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlanEntity plan;
	@Enumerated(EnumType.STRING)
	@Column(name = "billing_cycle", nullable = false, length = 50)
	private BillingCycle billingCycle;
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private SubscriptionStatus status;
	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;
	@Column(name = "end_at")
	private LocalDateTime endAt;
	@Column(name = "trial_start_at")
	private LocalDateTime trialStartAt;
	@Column(name = "trial_end_at")
	private LocalDateTime trialEndAt;
	@Column(name = "auto_renew", nullable = false)
	private Boolean autoRenew = Boolean.FALSE;
	@Column(name = "price_snapshot", precision = 19, scale = 2)
	private BigDecimal priceSnapshot;
	@Column(name = "currency_code", length = 10)
	private String currencyCode;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "applied_coupon_id")
	private SubscriptionCouponEntity appliedCoupon;
	@Column(name = "applied_coupon_code", length = 100)
	private String appliedCouponCode;
	@Column(name = "applied_discount_type", length = 50)
	private String appliedDiscountType;
	@Column(name = "applied_discount_value", precision = 19, scale = 4)
	private BigDecimal appliedDiscountValue;
	@Column(name = "applied_discount_amount", precision = 19, scale = 2)
	private BigDecimal appliedDiscountAmount;
	@Column(name = "plan_code_snapshot", nullable = false, length = 100)
	private String planCodeSnapshot;
	@Enumerated(EnumType.STRING)
	@Column(name = "source", nullable = false, length = 50)
	private SubscriptionSource source;
	@Column(name = "external_reference", length = 255)
	private String externalReference;
	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;
	@Column(name = "cancel_reason", length = 500)
	private String cancelReason;
	@Column(name = "renewal_attempt_count")
	private Integer renewalAttemptCount = 0;
	@Column(name = "scheduled_change_at")
	private LocalDateTime scheduledChangeAt;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scheduled_target_plan_id")
	private SubscriptionPlanEntity scheduledTargetPlan;
	@Enumerated(EnumType.STRING)
	@Column(name = "scheduled_target_billing_cycle", length = 50)
	private BillingCycle scheduledTargetBillingCycle;
	@Column(name = "scheduled_target_currency_code", length = 10)
	private String scheduledTargetCurrencyCode;
	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;

	public Long getId() {
		return this.id;
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getSubscriberUserId() {
		return this.subscriberUserId;
	}

	public SubscriptionPlanEntity getPlan() {
		return this.plan;
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

	public SubscriptionCouponEntity getAppliedCoupon() {
		return this.appliedCoupon;
	}

	public String getAppliedCouponCode() {
		return this.appliedCouponCode;
	}

	public String getAppliedDiscountType() {
		return this.appliedDiscountType;
	}

	public BigDecimal getAppliedDiscountValue() {
		return this.appliedDiscountValue;
	}

	public BigDecimal getAppliedDiscountAmount() {
		return this.appliedDiscountAmount;
	}

	public String getPlanCodeSnapshot() {
		return this.planCodeSnapshot;
	}

	public SubscriptionSource getSource() {
		return this.source;
	}

	public String getExternalReference() {
		return this.externalReference;
	}

	public LocalDateTime getCancelledAt() {
		return this.cancelledAt;
	}

	public String getCancelReason() {
		return this.cancelReason;
	}

	public Integer getRenewalAttemptCount() {
		return this.renewalAttemptCount;
	}

	public LocalDateTime getScheduledChangeAt() {
		return this.scheduledChangeAt;
	}

	public SubscriptionPlanEntity getScheduledTargetPlan() {
		return this.scheduledTargetPlan;
	}

	public BillingCycle getScheduledTargetBillingCycle() {
		return this.scheduledTargetBillingCycle;
	}

	public String getScheduledTargetCurrencyCode() {
		return this.scheduledTargetCurrencyCode;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setSubscriberUserId(final String subscriberUserId) {
		this.subscriberUserId = subscriberUserId;
	}

	public void setPlan(final SubscriptionPlanEntity plan) {
		this.plan = plan;
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

	public void setAppliedCoupon(final SubscriptionCouponEntity appliedCoupon) {
		this.appliedCoupon = appliedCoupon;
	}

	public void setAppliedCouponCode(final String appliedCouponCode) {
		this.appliedCouponCode = appliedCouponCode;
	}

	public void setAppliedDiscountType(final String appliedDiscountType) {
		this.appliedDiscountType = appliedDiscountType;
	}

	public void setAppliedDiscountValue(final BigDecimal appliedDiscountValue) {
		this.appliedDiscountValue = appliedDiscountValue;
	}

	public void setAppliedDiscountAmount(final BigDecimal appliedDiscountAmount) {
		this.appliedDiscountAmount = appliedDiscountAmount;
	}

	public void setPlanCodeSnapshot(final String planCodeSnapshot) {
		this.planCodeSnapshot = planCodeSnapshot;
	}

	public void setSource(final SubscriptionSource source) {
		this.source = source;
	}

	public void setExternalReference(final String externalReference) {
		this.externalReference = externalReference;
	}

	public void setCancelledAt(final LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public void setCancelReason(final String cancelReason) {
		this.cancelReason = cancelReason;
	}

	public void setRenewalAttemptCount(final Integer renewalAttemptCount) {
		this.renewalAttemptCount = renewalAttemptCount;
	}

	public void setScheduledChangeAt(final LocalDateTime scheduledChangeAt) {
		this.scheduledChangeAt = scheduledChangeAt;
	}

	public void setScheduledTargetPlan(final SubscriptionPlanEntity scheduledTargetPlan) {
		this.scheduledTargetPlan = scheduledTargetPlan;
	}

	public void setScheduledTargetBillingCycle(final BillingCycle scheduledTargetBillingCycle) {
		this.scheduledTargetBillingCycle = scheduledTargetBillingCycle;
	}

	public void setScheduledTargetCurrencyCode(final String scheduledTargetCurrencyCode) {
		this.scheduledTargetCurrencyCode = scheduledTargetCurrencyCode;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
