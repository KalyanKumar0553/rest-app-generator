package com.src.main.payment.exception;

public class PaymentWebhookException extends PaymentException {
	public PaymentWebhookException(PaymentErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
