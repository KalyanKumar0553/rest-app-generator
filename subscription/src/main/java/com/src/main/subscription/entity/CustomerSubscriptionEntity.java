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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer_subscription")
@Getter
@Setter
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
}
