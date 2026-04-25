package com.src.main.exception;

import org.springframework.http.HttpStatus;

public class SpecificException extends AppException {
	public SpecificException(HttpStatus status, String errorCode, String message) {
		super(status, ExceptionType.SPECIFIC, errorCode, message, message);
	}

	public SpecificException(HttpStatus status, String errorCode, String message, Throwable cause) {
		super(status, ExceptionType.SPECIFIC, errorCode, message, message, cause);
	}
}
