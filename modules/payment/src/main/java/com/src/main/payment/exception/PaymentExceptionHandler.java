package com.src.main.payment.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.src.main.payment")
public class PaymentExceptionHandler {
	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<Map<String, Object>> handlePayment(PaymentException ex) {
		HttpStatus status = ex.isRetryable() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(Map.of(
				"code", ex.getErrorCode().name(),
				"message", ex.getMessage(),
				"retryable", ex.isRetryable()));
	}
}
