package com.src.main.util;

public final class SpringBootVersionResolver {

	private static final String JAVA_8_11_BOOT = "2.7.18";
	private static final String JAVA_17_PLUS_BOOT = ProjectMetaDataConstants.DEFAULT_BOOT_VERSION;

	private SpringBootVersionResolver() {
	}

	public static String resolveCompatibleVersion(String requestedBootVersion, String jdkVersion) {
		int majorVersion = parseJavaMajorVersion(jdkVersion);
		String fallbackBootVersion = majorVersion >= 17 ? JAVA_17_PLUS_BOOT : JAVA_8_11_BOOT;
		if (isCompatible(requestedBootVersion, majorVersion)) {
			return requestedBootVersion.trim();
		}
		return fallbackBootVersion;
	}

	private static boolean isCompatible(String requestedBootVersion, int javaMajorVersion) {
		if (requestedBootVersion == null || requestedBootVersion.isBlank()) {
			return false;
		}
		String version = requestedBootVersion.trim();
		if (version.startsWith("1.") || version.startsWith("2.")) {
			return javaMajorVersion >= 8 && javaMajorVersion < 17;
		}
		if (version.startsWith("3.") || version.startsWith("4.")) {
			return javaMajorVersion >= 17;
		}
		return false;
	}

	private static int parseJavaMajorVersion(String rawVersion) {
		if (rawVersion == null || rawVersion.isBlank()) {
			return 21;
		}
		String version = rawVersion.trim();
		if (version.startsWith("1.")) {
			version = version.substring(2);
		}
		int end = 0;
		while (end < version.length() && Character.isDigit(version.charAt(end))) {
			end++;
		}
		if (end == 0) {
			return 21;
		}
		try {
			return Integer.parseInt(version.substring(0, end));
		} catch (NumberFormatException ex) {
			return 21;
		}
	}
}
