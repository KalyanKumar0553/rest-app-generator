package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

public class PlanNotFoundException extends SubscriptionException {
	public PlanNotFoundException(String code) {
		super("PLAN_NOT_FOUND", "Subscription plan not found: " + code, HttpStatus.NOT_FOUND);
	}
}
