package com.src.main.exception;

import org.springframework.http.HttpStatus;

public class GenericException extends AppException {

	public GenericException() {
		this(HttpStatus.INTERNAL_SERVER_ERROR, "GENERIC_ERROR", "An unexpected error occurred.");
	}

	public GenericException(HttpStatus status, String errorMsg) {
		this(status, "GENERIC_ERROR", errorMsg);
	}

	public GenericException(HttpStatus status, String errorCode, String errorMsg) {
		super(status, ExceptionType.GENERIC, errorCode, errorMsg, buildUserMessage(status));
	}

	public GenericException(HttpStatus status, String errorMsg, Throwable cause) {
		this(status, "GENERIC_ERROR", errorMsg, cause);
	}

	public String getErrorMsg() {
		return this.getMessage();
	}

	public GenericException(HttpStatus status, String errorCode, String errorMsg, Throwable cause) {
		super(status, ExceptionType.GENERIC, errorCode, errorMsg, buildUserMessage(status), cause);
	}

	private static String buildUserMessage(HttpStatus status) {
		if (status != null && status.is4xxClientError()) {
			return "Unable to process the request.";
		}
		return "An unexpected error occurred while processing the request.";
	}

	@Override
	public String toString() {
		return "GenericException(status=" + this.getStatus() + ", errorCode=" + this.getErrorCode() + ", errorMsg=" + this.getErrorMsg() + ")";
	}
}
