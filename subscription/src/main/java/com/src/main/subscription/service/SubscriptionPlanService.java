package com.src.main.subscription.service;

import java.util.List;

import com.src.main.subscription.dto.PlanRequest;
import com.src.main.subscription.dto.PlanResponse;

public interface SubscriptionPlanService {
	PlanResponse createPlan(PlanRequest request);
	PlanResponse updatePlan(Long id, PlanRequest request);
	void activatePlan(Long id);
	void deactivatePlan(Long id);
	void setDefaultPlan(Long id);
	PlanResponse getPlan(Long id);
	List<PlanResponse> getAllPlans(Boolean activeOnly);
}
