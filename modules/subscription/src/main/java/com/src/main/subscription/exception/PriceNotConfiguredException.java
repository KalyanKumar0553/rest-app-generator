package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

import com.src.main.subscription.enums.BillingCycle;

public class PriceNotConfiguredException extends SubscriptionException {
	public PriceNotConfiguredException(String planCode, BillingCycle billingCycle, String currencyCode) {
		super("PRICE_NOT_CONFIGURED",
				"No active price configured for " + planCode + " / " + billingCycle + " / " + currencyCode,
				HttpStatus.BAD_REQUEST);
	}
}
