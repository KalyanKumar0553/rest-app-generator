package com.src.main.subscription.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.src.main.auth.service.RbacService;
import com.src.main.subscription.service.EntitlementService;
import com.src.main.subscription.service.SubscriptionAccessEvaluator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionAccessEvaluatorImpl implements SubscriptionAccessEvaluator {

	private final RbacService rbacService;
	private final EntitlementService entitlementService;

	@Override
	public boolean canAccessFeature(Authentication authentication, Long tenantId, String featureCode, String requiredAuthority) {
		boolean hasAuthority = requiredAuthority == null || requiredAuthority.isBlank()
				|| rbacService.hasPermission(authentication, requiredAuthority);
		return hasAuthority && entitlementService.hasFeature(tenantId, featureCode);
	}
}
