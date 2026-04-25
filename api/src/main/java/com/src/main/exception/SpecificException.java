package com.src.main.exception;

import org.springframework.http.HttpStatus;

public class SpecificException extends GenericException {
	public SpecificException(HttpStatus status, String errorCode, String message) {
		super(status, errorCode, message);
	}

	public SpecificException(HttpStatus status, String errorCode, String message, Throwable cause) {
		super(status, errorCode, message, cause);
	}

	@Override
	public ExceptionType getExceptionType() {
		return ExceptionType.SPECIFIC;
	}

	@Override
	public String getUserMessage() {
		return this.getMessage();
	}
}
