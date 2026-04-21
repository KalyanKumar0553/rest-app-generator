package com.src.main.payment.exception;

public class PaymentStateException extends PaymentException {
	public PaymentStateException(PaymentErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}
