package com.src.main.dto;

import java.util.Map;
import java.util.Objects;

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

	public boolean isSuccess() {
		return this.success;
	}

	public String getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	public Map<String, Object> getDetails() {
		return this.details;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof StepResult)) return false;
		final StepResult other = (StepResult) o;
		if (this.isSuccess() != other.isSuccess()) return false;
		final Object this$code = this.getCode();
		final Object other$code = other.getCode();
		if (this$code == null ? other$code != null : !this$code.equals(other$code)) return false;
		final Object this$message = this.getMessage();
		final Object other$message = other.getMessage();
		if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
		final Object this$details = this.getDetails();
		final Object other$details = other.getDetails();
		if (this$details == null ? other$details != null : !this$details.equals(other$details)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isSuccess() ? 79 : 97);
		final Object $code = this.getCode();
		result = result * PRIME + ($code == null ? 43 : $code.hashCode());
		final Object $message = this.getMessage();
		result = result * PRIME + ($message == null ? 43 : $message.hashCode());
		final Object $details = this.getDetails();
		result = result * PRIME + ($details == null ? 43 : $details.hashCode());
		return result;
	}

	public StepResult(final boolean success, final String code, final String message, final Map<String, Object> details) {
		this.success = success;
		this.code = code;
		this.message = message;
		this.details = details;
	}

	@Override
	public String toString() {
		return "StepResult(success=" + this.isSuccess() + ", code=" + this.getCode() + ", message=" + this.getMessage() + ", details=" + this.getDetails() + ")";
	}
}
