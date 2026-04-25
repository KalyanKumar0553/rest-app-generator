package com.src.main.exception;

import org.springframework.http.HttpStatus;

public abstract class AppException extends RuntimeException {
	private final HttpStatus status;
	private final ExceptionType exceptionType;
	private final String errorCode;
	private final String userMessage;

	protected AppException(HttpStatus status, ExceptionType exceptionType, String errorCode, String message, String userMessage) {
		super(message);
		this.status = status;
		this.exceptionType = exceptionType;
		this.errorCode = errorCode;
		this.userMessage = userMessage;
	}

	protected AppException(HttpStatus status, ExceptionType exceptionType, String errorCode, String message, String userMessage, Throwable cause) {
		super(message, cause);
		this.status = status;
		this.exceptionType = exceptionType;
		this.errorCode = errorCode;
		this.userMessage = userMessage;
	}

	public HttpStatus getStatus() {
		return this.status;
	}

	public ExceptionType getExceptionType() {
		return this.exceptionType;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public String getUserMessage() {
		return this.userMessage;
	}
}
