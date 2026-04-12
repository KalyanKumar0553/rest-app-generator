package com.src.main.exception;

import org.springframework.http.HttpStatus;

public class GenericException extends RuntimeException {
	private final HttpStatus status;
	private final String errorMsg;

	public GenericException() {
		this(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
	}

	public GenericException(HttpStatus status, String errorMsg) {
		super(errorMsg);
		this.status = status;
		this.errorMsg = errorMsg;
	}

	public GenericException(HttpStatus status, String errorMsg, Throwable cause) {
		super(errorMsg, cause);
		this.status = status;
		this.errorMsg = errorMsg;
	}

	public HttpStatus getStatus() {
		return this.status;
	}

	public String getErrorMsg() {
		return this.errorMsg;
	}

	@Override
	public String toString() {
		return "GenericException(status=" + this.getStatus() + ", errorMsg=" + this.getErrorMsg() + ")";
	}
}
