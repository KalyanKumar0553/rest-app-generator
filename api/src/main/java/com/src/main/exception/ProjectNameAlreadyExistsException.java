package com.src.main.exception;

import org.springframework.http.HttpStatus;

public class ProjectNameAlreadyExistsException extends SpecificException {
	public static final String ERROR_CODE = "PROJECT_NAME_ALREADY_EXISTS";

	public ProjectNameAlreadyExistsException() {
		super(HttpStatus.BAD_REQUEST, ERROR_CODE, "Project name already exists for this user. Choose a different project name.");
	}
}
