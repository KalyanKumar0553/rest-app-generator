package com.src.main.subscription.service;

import java.util.List;

import com.src.main.subscription.dto.FeatureRequest;
import com.src.main.subscription.dto.FeatureResponse;

public interface SubscriptionFeatureService {
	FeatureResponse createFeature(FeatureRequest request);
	FeatureResponse updateFeature(Long id, FeatureRequest request);
	List<FeatureResponse> getAllFeatures(Boolean activeOnly);
	FeatureResponse getFeatureByCode(String code);
}
