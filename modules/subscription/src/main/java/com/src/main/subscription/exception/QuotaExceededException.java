package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

public class QuotaExceededException extends SubscriptionException {
	public QuotaExceededException(String featureCode) {
		super("QUOTA_EXCEEDED", "Quota exhausted for feature " + featureCode, HttpStatus.CONFLICT);
	}
}
