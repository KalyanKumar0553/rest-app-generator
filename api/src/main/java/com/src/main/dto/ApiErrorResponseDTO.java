package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponseDTO(
		int status,
		String exceptionType,
		String errorCode,
		String message,
		String userMessage,
		String path,
		OffsetDateTime timestamp,
		String traceId,
		Map<String, String> fieldErrors,
		String errorMsg) {
}
