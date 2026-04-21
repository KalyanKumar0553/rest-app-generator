package com.src.main.payment.exception;

public class PaymentConfigurationException extends PaymentException {
	public PaymentConfigurationException(PaymentErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
