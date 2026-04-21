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

@Entity
@Table(name = "subscription_coupon_redemption")
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

	public Long getId() {
		return this.id;
	}

	public SubscriptionCouponEntity getCoupon() {
		return this.coupon;
	}

	public CustomerSubscriptionEntity getSubscription() {
		return this.subscription;
	}

	public Long getTenantId() {
		return this.tenantId;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getCouponCodeSnapshot() {
		return this.couponCodeSnapshot;
	}

	public DiscountType getDiscountTypeSnapshot() {
		return this.discountTypeSnapshot;
	}

	public BigDecimal getDiscountValueSnapshot() {
		return this.discountValueSnapshot;
	}

	public BigDecimal getDiscountAmountSnapshot() {
		return this.discountAmountSnapshot;
	}

	public String getCurrencyCode() {
		return this.currencyCode;
	}

	public LocalDateTime getRedeemedAt() {
		return this.redeemedAt;
	}

	public String getMetadataJson() {
		return this.metadataJson;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setCoupon(final SubscriptionCouponEntity coupon) {
		this.coupon = coupon;
	}

	public void setSubscription(final CustomerSubscriptionEntity subscription) {
		this.subscription = subscription;
	}

	public void setTenantId(final Long tenantId) {
		this.tenantId = tenantId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public void setCouponCodeSnapshot(final String couponCodeSnapshot) {
		this.couponCodeSnapshot = couponCodeSnapshot;
	}

	public void setDiscountTypeSnapshot(final DiscountType discountTypeSnapshot) {
		this.discountTypeSnapshot = discountTypeSnapshot;
	}

	public void setDiscountValueSnapshot(final BigDecimal discountValueSnapshot) {
		this.discountValueSnapshot = discountValueSnapshot;
	}

	public void setDiscountAmountSnapshot(final BigDecimal discountAmountSnapshot) {
		this.discountAmountSnapshot = discountAmountSnapshot;
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public void setRedeemedAt(final LocalDateTime redeemedAt) {
		this.redeemedAt = redeemedAt;
	}

	public void setMetadataJson(final String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
