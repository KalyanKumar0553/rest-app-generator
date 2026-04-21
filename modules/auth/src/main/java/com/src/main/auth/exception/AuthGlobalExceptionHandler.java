package com.src.main.auth.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.src.main.auth.dto.common.ApiResponseDto;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(basePackages = "com.src.main.auth.controller")
public class AuthGlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(AuthGlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponseDto<Void>> onMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse("Validation failed");
		log.warn("Auth validation error: {}", message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.fail(message));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponseDto<Void>> onConstraintViolation(ConstraintViolationException ex) {
		String message = ex.getConstraintViolations()
				.stream()
				.map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
				.collect(Collectors.joining(", "));
		if (message.isBlank()) {
			message = "Validation failed";
		}
		log.warn("Auth constraint violation: {}", message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.fail(message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponseDto<Void>> onIllegalArgument(IllegalArgumentException ex) {
		String message = ex.getMessage() == null || ex.getMessage().isBlank() ? "Invalid request" : ex.getMessage();
		log.warn("Auth request error: {}", message);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseDto.fail(message));
	}

	@ExceptionHandler({ SecurityException.class, AccessDeniedException.class })
	public ResponseEntity<ApiResponseDto<Void>> onSecurity(RuntimeException ex) {
		String message = ex.getMessage() == null || ex.getMessage().isBlank() ? "Access denied" : ex.getMessage();
		log.warn("Auth access error: {}", message);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseDto.fail(message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponseDto<Void>> onUnhandled(Exception ex) {
		log.error("Auth unhandled exception: type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseDto.fail("An unexpected error occurred."));
	}
}
