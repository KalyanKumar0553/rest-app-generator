package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.DiscountType;

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
@Table(name = "subscription_coupon_redemption")
@Getter
@Setter
public class SubscriptionCouponRedemptionEntity extends BaseSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "coupon_id", nullable = false)
	private SubscriptionCouponEntity coupon;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subscription_id", nullable = false)
	private CustomerSubscriptionEntity subscription;

	@Column(name = "tenant_id", nullable = false)
	private Long tenantId;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "coupon_code_snapshot", nullable = false, length = 100)
	private String couponCodeSnapshot;

	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type_snapshot", nullable = false, length = 50)
	private DiscountType discountTypeSnapshot;

	@Column(name = "discount_value_snapshot", nullable = false, precision = 19, scale = 4)
	private BigDecimal discountValueSnapshot;

	@Column(name = "discount_amount_snapshot", nullable = false, precision = 19, scale = 2)
	private BigDecimal discountAmountSnapshot;

	@Column(name = "currency_code", length = 10)
	private String currencyCode;

	@Column(name = "redeemed_at", nullable = false)
	private LocalDateTime redeemedAt;

	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;
}
