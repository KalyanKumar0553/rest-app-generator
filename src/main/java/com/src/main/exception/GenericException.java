package com.src.main.exception;

import org.springframework.http.HttpStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GenericException extends RuntimeException {
	private HttpStatus status;
	private String errorMsg;
	public GenericException(HttpStatus status,String errorMsg) {
		super(errorMsg);
		this.status = status;
		this.errorMsg = errorMsg;
	}
}
