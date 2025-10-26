package com.src.main.dto;

import java.util.Map;

public record StepResult(boolean success, Map<String, Object> payload, String message) {
	
	public static StepResult ok(Map<String, Object> payload) {
		return new StepResult(true, payload, "OK");
	}

	public static StepResult fail(String msg) {
		return new StepResult(false, Map.of(), msg);
	}
}
