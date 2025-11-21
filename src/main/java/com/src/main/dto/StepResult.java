package com.src.main.dto;

import java.util.Map;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public final class StepResult {

	private final boolean success;
	private final String code;
	private final String message;
	private final Map<String, Object> details;

	public static StepResult ok() {
		return new StepResult(true, "OK", "Success", null);
	}

	public static StepResult ok(Map<String, Object> details) {
		return new StepResult(true, "OK", "Success", details);
	}

	public static StepResult error(String code, String message) {
		return new StepResult(false, Objects.requireNonNullElse(code, "ERROR"), message, null);
	}

	public static StepResult error(String code, String message, Map<String, Object> details) {
		return new StepResult(false, Objects.requireNonNullElse(code, "ERROR"), message, details);
	}

	public static StepResult fromException(String code, Throwable t) {
		String msg = (t == null ? "Unknown error" : (t.getMessage() == null ? t.toString() : t.getMessage()));
		return new StepResult(false, Objects.requireNonNullElse(code, "ERROR"), msg, null);
	}
}
