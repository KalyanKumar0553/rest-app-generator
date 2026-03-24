package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

public class InvalidSubscriptionOperationException extends SubscriptionException {
	public InvalidSubscriptionOperationException(String message) {
		super("INVALID_SUBSCRIPTION_OPERATION", message, HttpStatus.BAD_REQUEST);
	}
}
