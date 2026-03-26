package com.src.main.subscription.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_coupon_plan_mapping")
public class SubscriptionCouponPlanMappingEntity extends BaseSubscriptionEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "coupon_id", nullable = false)
	private SubscriptionCouponEntity coupon;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlanEntity plan;

	public Long getId() {
		return this.id;
	}

	public SubscriptionCouponEntity getCoupon() {
		return this.coupon;
	}

	public SubscriptionPlanEntity getPlan() {
		return this.plan;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public void setCoupon(final SubscriptionCouponEntity coupon) {
		this.coupon = coupon;
	}

	public void setPlan(final SubscriptionPlanEntity plan) {
		this.plan = plan;
	}
}
