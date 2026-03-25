package com.src.main.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.src.main.subscription.enums.DiscountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscription_coupon")
@Getter
@Setter
public class SubscriptionCouponEntity extends BaseSubscriptionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "code", nullable = false, length = 100, unique = true)
	private String code;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description", length = 1000)
	private String description;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = Boolean.TRUE;

	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type", nullable = false, length = 50)
	private DiscountType discountType;

	@Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
	private BigDecimal discountValue;

	@Column(name = "currency_code", length = 10)
	private String currencyCode;

	@Column(name = "valid_from", nullable = false)
	private LocalDateTime validFrom;

	@Column(name = "valid_to")
	private LocalDateTime validTo;

	@Column(name = "max_redemptions")
	private Integer maxRedemptions;

	@Column(name = "max_redemptions_per_tenant")
	private Integer maxRedemptionsPerTenant;

	@Column(name = "first_subscription_only", nullable = false)
	private Boolean firstSubscriptionOnly = Boolean.FALSE;

	@Column(name = "metadata_json", columnDefinition = "text")
	private String metadataJson;
}
