package com.src.main.exception;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.catalina.connector.ClientAbortException;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.src.main.dto.ApiErrorResponseDTO;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponseDTO> onConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
		String firstError = ex.getConstraintViolations()
				.stream()
				.findFirst()
				.map(v -> v.getPropertyPath() + " " + v.getMessage())
				.orElse("Validation failed");
		return specific(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", firstError, request, null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponseDTO> onMethodArgInvalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		String firstError = fieldErrors.values().stream().findFirst().orElse("Validation failed");
		return specific(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", firstError, request, fieldErrors);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponseDTO> onMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		return specific(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "Invalid value for parameter: " + ex.getName(), request, null);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponseDTO> onMissingRequestParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
		return specific(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "Missing required parameter: " + ex.getParameterName(), request, null);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiErrorResponseDTO> onMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
		return specific(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), request, null);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ApiErrorResponseDTO> onMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
		return specific(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", ex.getMessage(), request, null);
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ResponseEntity<ApiErrorResponseDTO> onMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
		return specific(HttpStatus.NOT_ACCEPTABLE, "NOT_ACCEPTABLE", ex.getMessage(), request, null);
	}

	@ExceptionHandler(SpecificException.class)
	public ResponseEntity<ApiErrorResponseDTO> onSpecific(SpecificException ex, HttpServletRequest request) {
		return build(ex, request, null);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponseDTO> onIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		return specific(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request, null);
	}

	@ExceptionHandler(GenericException.class)
	public ResponseEntity<ApiErrorResponseDTO> onGeneric(GenericException ex, HttpServletRequest request) {
		if (ex.getCause() != null) {
			log.warn("Application generic error handled: status={}, errorCode={}, message={}, cause={}",
					ex.getStatus().value(), ex.getErrorCode(), ex.getMessage(), ex.getCause().getMessage());
		} else {
			log.warn("Application generic error handled: status={}, errorCode={}, message={}",
					ex.getStatus().value(), ex.getErrorCode(), ex.getMessage());
		}
		return build(ex, request, null);
	}

	@ExceptionHandler({ SecurityException.class, AccessDeniedException.class })
	public ResponseEntity<ApiErrorResponseDTO> onSecurity(RuntimeException ex, HttpServletRequest request) {
		return specific(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request, null);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiErrorResponseDTO> onNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
		String resourcePath = ex.getResourcePath();
		if (resourcePath != null && resourcePath.contains(".well-known/appspecific/com.chrome.devtools.json")) {
			log.debug("Ignoring browser probe for missing static resource: {}", resourcePath);
		} else {
			log.debug("Static resource not found: {}", resourcePath);
		}
		return specific(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request, null);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ApiErrorResponseDTO> onEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
		return specific(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", ex.getMessage(), request, null);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponseDTO> onResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
		String message = ex.getReason() == null || ex.getReason().isBlank()
				? ex.getStatusCode().toString()
				: ex.getReason();
		return specific(HttpStatus.valueOf(ex.getStatusCode().value()), "RESPONSE_STATUS_EXCEPTION", message, request, null);
	}

	@ExceptionHandler(ClientAbortException.class)
	public ResponseEntity<ApiErrorResponseDTO> onClientAbort(ClientAbortException ex) {
		log.warn("Client connection closed before response completed: {}", summarizeIoMessage(ex));
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ApiErrorResponseDTO> onIoException(IOException ex) throws IOException {
		if (isClientDisconnect(ex)) {
			log.warn("Client I/O disconnected before response completed: {}", summarizeIoMessage(ex));
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		throw ex;
	}

	@ExceptionHandler(HttpMessageNotWritableException.class)
	public ResponseEntity<Void> onHttpMessageNotWritable(HttpMessageNotWritableException ex) {
		String message = ex.getMessage() == null || ex.getMessage().isBlank()
				? ex.getClass().getSimpleName()
				: ex.getMessage();
		if (message.contains("text/event-stream")) {
			log.warn("SSE response could not be written because the stream was no longer writable: {}", message);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		log.error("Response write error handled: type={}, message={}", ex.getClass().getSimpleName(), message);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

	@ExceptionHandler({
			LazyInitializationException.class,
			JpaSystemException.class,
			DataIntegrityViolationException.class,
			DataAccessException.class
	})
	public ResponseEntity<ApiErrorResponseDTO> onPersistenceException(Exception ex, HttpServletRequest request) {
		log.error("Persistence error handled: type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage());
		if (ex instanceof DataIntegrityViolationException) {
			return generic(HttpStatus.CONFLICT, "DATA_INTEGRITY_CONFLICT", "The request could not be completed because of a data integrity conflict.", request, ex);
		}
		return generic(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "A database error occurred while processing the request.", request, ex);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponseDTO> onUnhandled(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception handled: type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
		return generic(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "An unexpected error occurred.", request, ex);
	}

	private ResponseEntity<ApiErrorResponseDTO> specific(
			HttpStatus status,
			String errorCode,
			String message,
			HttpServletRequest request,
			Map<String, String> fieldErrors) {
		return build(new SpecificException(status, errorCode, message), request, fieldErrors);
	}

	private ResponseEntity<ApiErrorResponseDTO> generic(
			HttpStatus status,
			String errorCode,
			String message,
			HttpServletRequest request,
			Throwable cause) {
		return build(new GenericException(status, errorCode, message, cause), request, null);
	}

	private ResponseEntity<ApiErrorResponseDTO> build(AppException ex, HttpServletRequest request, Map<String, String> fieldErrors) {
		HttpStatus status = ex.getStatus() == null ? HttpStatus.INTERNAL_SERVER_ERROR : ex.getStatus();
		String traceId = UUID.randomUUID().toString();
		ApiErrorResponseDTO body = new ApiErrorResponseDTO(
				status.value(),
				ex.getExceptionType().name(),
				ex.getErrorCode(),
				safeMessage(ex.getMessage(), status),
				safeMessage(ex.getUserMessage(), status),
				request == null ? null : request.getRequestURI(),
				OffsetDateTime.now(),
				traceId,
				fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors,
				safeMessage(ex.getUserMessage(), status));
		return ResponseEntity.status(status).body(body);
	}

	private String safeMessage(String message, HttpStatus status) {
		if (message != null && !message.isBlank()) {
			return message;
		}
		return status.getReasonPhrase();
	}

	private boolean isClientDisconnect(IOException ex) {
		String message = summarizeIoMessage(ex).toLowerCase();
		return message.contains("broken pipe") || message.contains("connection reset by peer");
	}

	private String summarizeIoMessage(IOException ex) {
		String message = ex == null ? "" : ex.getMessage();
		return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
	}
}
