package com.src.main.payment.exception;

public class PaymentValidationException extends PaymentException {
	public PaymentValidationException(PaymentErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
