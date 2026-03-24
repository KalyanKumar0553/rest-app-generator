package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

public class FeatureNotFoundException extends SubscriptionException {
	public FeatureNotFoundException(String code) {
		super("FEATURE_NOT_FOUND", "Subscription feature not found: " + code, HttpStatus.NOT_FOUND);
	}
}
