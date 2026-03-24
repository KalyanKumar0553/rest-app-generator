package com.src.main.subscription.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.src.main.subscription")
public class SubscriptionExceptionHandler {

	@ExceptionHandler(SubscriptionException.class)
	public ResponseEntity<Map<String, Object>> handleSubscriptionException(SubscriptionException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("code", ex.getCode());
		body.put("message", ex.getMessage());
		return ResponseEntity.status(ex.getStatus()).body(body);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("code", "INVALID_REQUEST");
		body.put("message", ex.getMessage());
		return ResponseEntity.badRequest().body(body);
	}
}
