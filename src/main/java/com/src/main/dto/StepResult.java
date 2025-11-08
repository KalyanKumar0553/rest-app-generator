package com.src.main.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class StepResult {

	private final boolean success;
	private final String code;
	private final String message;
	private final Map<String, Object> details;

	private StepResult(boolean success, String code, String message, Map<String, Object> details) {
		this.success = success;
		this.code = code;
		this.message = message;
		this.details = details == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(details));
	}

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

	public boolean isSuccess() {
		return success;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	@Override
	public String toString() {
		return "StepResult{success=" + success + ", code='" + code + '\'' + ", message='" + message + '\''
				+ ", details=" + details + '}';
	}
}
