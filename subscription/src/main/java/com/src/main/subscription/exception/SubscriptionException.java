package com.src.main.subscription.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class SubscriptionException extends RuntimeException {
	private final String code;
	private final HttpStatus status;

	public SubscriptionException(String code, String message, HttpStatus status) {
		super(message);
		this.code = code;
		this.status = status;
	}
}
