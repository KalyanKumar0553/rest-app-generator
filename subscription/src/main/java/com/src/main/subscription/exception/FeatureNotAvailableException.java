package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

public class FeatureNotAvailableException extends SubscriptionException {
	public FeatureNotAvailableException(String featureCode) {
		super("FEATURE_NOT_AVAILABLE", "Your current subscription does not include " + featureCode, HttpStatus.FORBIDDEN);
	}
}
