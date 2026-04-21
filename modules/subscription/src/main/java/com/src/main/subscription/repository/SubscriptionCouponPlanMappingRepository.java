package com.src.main.subscription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.src.main.subscription.entity.SubscriptionCouponPlanMappingEntity;

public interface SubscriptionCouponPlanMappingRepository extends JpaRepository<SubscriptionCouponPlanMappingEntity, Long> {
	List<SubscriptionCouponPlanMappingEntity> findAllByCoupon_IdAndDeletedFalse(Long couponId);
	boolean existsByCoupon_IdAndPlan_IdAndDeletedFalse(Long couponId, Long planId);
	@Modifying
	void deleteByCoupon_Id(Long couponId);
}
