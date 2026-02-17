package com.src.main.sm.executor.docker;

import java.util.Locale;

import com.src.main.common.util.StringUtils;

public final class DockerGenerationSupport {

	private DockerGenerationSupport() {
	}

	public static boolean isDockerComposeEnabled(Object raw) {
		if (raw == null) {
			return false;
		}
		if (raw instanceof Boolean enabled) {
			return enabled;
		}
		String normalized = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
	}

	public static String toServiceName(String artifactId) {
		String base = StringUtils.firstNonBlank(artifactId, "app").toLowerCase(Locale.ROOT);
		String cleaned = base.replaceAll("[^a-z0-9_-]", "-").replaceAll("-+", "-");
		if (cleaned.isBlank()) {
			return "app";
		}
		return cleaned;
	}

	public static String resolveJarGlob(String buildTool) {
		if ("gradle".equalsIgnoreCase(StringUtils.firstNonBlank(buildTool, ""))) {
			return "build/libs/*.jar";
		}
		return "target/*.jar";
	}
}
