package com.src.main.payment.exception;

public class PaymentProviderException extends PaymentException {
	public PaymentProviderException(PaymentErrorCode errorCode, String message, boolean retryable, Throwable cause) {
		super(errorCode, message, retryable, cause);
	}
	public PaymentProviderException(PaymentErrorCode errorCode, String message) {
		super(errorCode, message, true);
	}
}
