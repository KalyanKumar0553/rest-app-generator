package com.src.main.exception;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> onConstraintViolation(ConstraintViolationException ex) {
		String firstError = ex.getConstraintViolations()
				.stream()
				.findFirst()
				.map(v -> v.getPropertyPath() + " " + v.getMessage())
				.orElse("Validation failed");
		return error(HttpStatus.BAD_REQUEST, firstError);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> onMethodArgInvalid(MethodArgumentNotValidException ex) {
		String firstError = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(fe -> fe.getDefaultMessage())
				.orElse("Validation failed");
		return error(HttpStatus.BAD_REQUEST, firstError);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> onMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return error(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getName());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> onMissingRequestParam(MissingServletRequestParameterException ex) {
		return error(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> onMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		return error(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> onMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
		return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ResponseEntity<Map<String, Object>> onMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
		return error(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> onIllegalArgument(IllegalArgumentException ex) {
		return error(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(GenericException.class)
	public ResponseEntity<Map<String, Object>> onGeneric(GenericException ex) {
		if (ex.getCause() != null) {
			log.warn("Application error handled: status={}, message={}, cause={}", ex.getStatus().value(), ex.getErrorMsg(), ex.getCause().getMessage());
		} else {
			log.warn("Application error handled: status={}, message={}", ex.getStatus().value(), ex.getErrorMsg());
		}
		return error(ex.getStatus(), ex.getErrorMsg());
	}

	@ExceptionHandler({ SecurityException.class, AccessDeniedException.class })
	public ResponseEntity<Map<String, Object>> onSecurity(RuntimeException ex) {
		return error(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, Object>> onNoResourceFound(NoResourceFoundException ex) {
		String resourcePath = ex.getResourcePath();
		if (resourcePath != null && resourcePath.contains(".well-known/appspecific/com.chrome.devtools.json")) {
			log.debug("Ignoring browser probe for missing static resource: {}", resourcePath);
		} else {
			log.debug("Static resource not found: {}", resourcePath);
		}
		return error(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, Object>> onEntityNotFound(EntityNotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, Object>> onResponseStatus(ResponseStatusException ex) {
		String message = ex.getReason() == null || ex.getReason().isBlank()
				? ex.getStatusCode().toString()
				: ex.getReason();
		return error(HttpStatus.valueOf(ex.getStatusCode().value()), message);
	}

	@ExceptionHandler(ClientAbortException.class)
	public ResponseEntity<Map<String, Object>> onClientAbort(ClientAbortException ex) {
		log.warn("Client connection closed before response completed: {}", summarizeIoMessage(ex));
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<Map<String, Object>> onIoException(IOException ex) throws IOException {
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
	public ResponseEntity<Map<String, Object>> onPersistenceException(Exception ex) {
		log.error("Persistence error handled: type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage());
		if (ex instanceof LazyInitializationException) {
			return error(HttpStatus.INTERNAL_SERVER_ERROR, "The requested data could not be loaded completely.");
		}
		if (ex instanceof DataIntegrityViolationException) {
			return error(HttpStatus.CONFLICT, "The request could not be completed because of a data integrity conflict.");
		}
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred while processing the request.");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> onUnhandled(Exception ex) {
		log.error("Unhandled exception handled: type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage());
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
	}

	private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("errorCode", status.value());
		body.put("errorMsg", message == null || message.isBlank() ? status.getReasonPhrase() : message);
		return ResponseEntity.status(status).body(body);
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
