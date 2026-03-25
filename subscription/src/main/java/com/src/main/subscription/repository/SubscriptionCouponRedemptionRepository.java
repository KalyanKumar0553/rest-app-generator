package com.src.main.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.src.main.subscription.entity.SubscriptionCouponRedemptionEntity;

public interface SubscriptionCouponRedemptionRepository extends JpaRepository<SubscriptionCouponRedemptionEntity, Long> {
	long countByCoupon_IdAndDeletedFalse(Long couponId);
	long countByCoupon_IdAndTenantIdAndDeletedFalse(Long couponId, Long tenantId);
}
