package com.src.main.subscription.service;

import java.util.List;

import com.src.main.subscription.dto.PlanFeatureResponse;
import com.src.main.subscription.dto.PlanFeatureUpsertRequest;

public interface PlanFeatureService {
	List<PlanFeatureResponse> getPlanFeatures(Long planId);
	List<PlanFeatureResponse> replacePlanFeatures(Long planId, List<PlanFeatureUpsertRequest> requests);
	PlanFeatureResponse upsertPlanFeature(Long planId, PlanFeatureUpsertRequest request);
	void removePlanFeature(Long planId, Long featureId);
}
