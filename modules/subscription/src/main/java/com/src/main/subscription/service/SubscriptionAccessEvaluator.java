package com.src.main.subscription.service;

import org.springframework.security.core.Authentication;

public interface SubscriptionAccessEvaluator {
	boolean canAccessFeature(Authentication authentication, Long tenantId, String featureCode, String requiredAuthority);
}
