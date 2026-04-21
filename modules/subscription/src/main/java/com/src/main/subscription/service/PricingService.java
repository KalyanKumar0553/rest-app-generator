package com.src.main.subscription.service;

import java.time.LocalDateTime;
import java.util.List;

import com.src.main.subscription.dto.PlanPriceRequest;
import com.src.main.subscription.dto.PlanPriceResponse;
import com.src.main.subscription.dto.ResolvedPriceResponse;
import com.src.main.subscription.enums.BillingCycle;

public interface PricingService {
	PlanPriceResponse createPrice(Long planId, PlanPriceRequest request);
	PlanPriceResponse updatePrice(Long id, PlanPriceRequest request);
	List<PlanPriceResponse> getPlanPrices(Long planId);
	ResolvedPriceResponse resolvePrice(String planCode, BillingCycle cycle, String currencyCode, LocalDateTime asOf);
	ResolvedPriceResponse resolvePrice(String planCode, BillingCycle cycle, String currencyCode, String couponCode, Long tenantId, LocalDateTime asOf);
}
