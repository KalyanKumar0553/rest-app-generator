package com.src.main.dto;

public record InitializrProjectModel(String groupId, String artifactId, String version, String name, String description,
		String packaging, String jdkVersion, String bootVersion, boolean includeOpenapi, boolean angularIntegration) {
}
