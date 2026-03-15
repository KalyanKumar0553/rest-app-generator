package com.src.main.common.exception;

import lombok.Data;

@Data
public class AbstractRuntimeException extends RuntimeException {

	private String message;
	private int errorCode;

	public AbstractRuntimeException(int errorCode,String message) {
		super(message);
		this.message = message;
		this.errorCode = errorCode;
	}
}
