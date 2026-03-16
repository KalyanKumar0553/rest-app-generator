package com.src.main.sm.executor.swagger;

public record SwaggerGroupSpec(
		String beanName,
		String groupName,
		String pathsToMatchArgs,
		boolean hasCustomizer,
		String customizerBody) {
}
