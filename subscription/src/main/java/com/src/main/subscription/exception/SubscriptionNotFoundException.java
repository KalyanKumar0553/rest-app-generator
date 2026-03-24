package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

public class SubscriptionNotFoundException extends SubscriptionException {
	public SubscriptionNotFoundException(Long tenantId) {
		super("SUBSCRIPTION_NOT_FOUND", "No subscription found for tenant " + tenantId, HttpStatus.NOT_FOUND);
	}
}
