package com.src.main.subscription.service;

import java.util.List;

import com.src.main.subscription.dto.SubscriptionCouponRequest;
import com.src.main.subscription.dto.SubscriptionCouponResponse;

public interface SubscriptionCouponService {
	SubscriptionCouponResponse createCoupon(SubscriptionCouponRequest request);
	SubscriptionCouponResponse updateCoupon(Long id, SubscriptionCouponRequest request);
	SubscriptionCouponResponse getCoupon(Long id);
	List<SubscriptionCouponResponse> getAllCoupons(Boolean activeOnly);
	void activateCoupon(Long id);
	void deactivateCoupon(Long id);
}
