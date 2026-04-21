package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

public class OverlappingPriceWindowException extends SubscriptionException {
	public OverlappingPriceWindowException() {
		super("OVERLAPPING_PRICE_WINDOW", "Overlapping price windows are not allowed.", HttpStatus.CONFLICT);
	}
}
