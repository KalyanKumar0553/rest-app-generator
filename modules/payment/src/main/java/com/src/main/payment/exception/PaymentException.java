package com.src.main.payment.exception;

public abstract class PaymentException extends RuntimeException {
	private final PaymentErrorCode errorCode;
	private final boolean retryable;

	protected PaymentException(PaymentErrorCode errorCode, String message) {
		this(errorCode, message, false, null);
	}

	protected PaymentException(PaymentErrorCode errorCode, String message, boolean retryable) {
		this(errorCode, message, retryable, null);
	}

	protected PaymentException(PaymentErrorCode errorCode, String message, boolean retryable, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
		this.retryable = retryable;
	}

	public PaymentErrorCode getErrorCode() {
		return errorCode;
	}

	public boolean isRetryable() {
		return retryable;
	}
}
