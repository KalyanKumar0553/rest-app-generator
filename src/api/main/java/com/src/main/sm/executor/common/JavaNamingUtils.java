package com.src.main.sm.executor.common;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.src.main.common.util.StringUtils;

public final class JavaNamingUtils {

	private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9]+");
	private static final Set<String> PASSTHROUGH_TYPES = Set.of(
			"String",
			"Integer",
			"Long",
			"Boolean",
			"Double",
			"Float",
			"Short",
			"Byte",
			"Character",
			"BigDecimal",
			"BigInteger",
			"UUID",
			"LocalDate",
			"LocalTime",
			"LocalDateTime",
			"OffsetDateTime",
			"Instant",
			"Object",
			"Void",
			"int",
			"long",
			"boolean",
			"double",
			"float",
			"short",
			"byte",
			"char");

	private JavaNamingUtils() {
	}

	public static String toJavaTypeName(String rawValue, String fallbackPrefix) {
		String fallback = normalizeFallback(fallbackPrefix);
		String raw = StringUtils.firstNonBlank(rawValue, "").trim();
		Matcher matcher = TOKEN_PATTERN.matcher(raw);
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String token = matcher.group();
			if (token == null || token.isBlank()) {
				continue;
			}
			if (Character.isLetter(token.charAt(0))) {
				result.append(Character.toUpperCase(token.charAt(0)));
				if (token.length() > 1) {
					result.append(token.substring(1));
				}
			} else {
				result.append(token);
			}
		}
		if (result.isEmpty()) {
			result.append(fallback);
		}
		if (!Character.isLetter(result.charAt(0))) {
			result.insert(0, fallback);
		}
		return result.toString();
	}

	public static String toJavaTypeExpression(String rawValue, String fallbackPrefix) {
		String decoded = decodeHtml(rawValue);
		String value = StringUtils.firstNonBlank(decoded, "").trim();
		if (value.isBlank()) {
			return toJavaTypeName(null, fallbackPrefix);
		}

		if (value.startsWith("List<") && value.endsWith(">")) {
			String inner = value.substring(5, value.length() - 1).trim();
			return "List<" + toJavaTypeExpression(inner, fallbackPrefix) + ">";
		}
		if (value.startsWith("Page<") && value.endsWith(">")) {
			String inner = value.substring(5, value.length() - 1).trim();
			return "Page<" + toJavaTypeExpression(inner, fallbackPrefix) + ">";
		}
		if (value.startsWith("ResponseEntity<") && value.endsWith(">")) {
			String inner = value.substring("ResponseEntity<".length(), value.length() - 1).trim();
			return "ResponseEntity<" + toJavaTypeExpression(inner, fallbackPrefix) + ">";
		}
		if (PASSTHROUGH_TYPES.contains(value)) {
			return value;
		}
		if (value.contains(".")) {
			int idx = value.lastIndexOf('.');
			String pkg = value.substring(0, idx);
			String simple = value.substring(idx + 1);
			return pkg + "." + toJavaTypeName(simple, fallbackPrefix);
		}
		return toJavaTypeName(value, fallbackPrefix);
	}

	public static String decodeHtml(String raw) {
		if (raw == null) {
			return null;
		}
		return raw
				.replace("&lt;", "<")
				.replace("&gt;", ">")
				.replace("&amp;", "&")
				.trim();
	}

	private static String normalizeFallback(String rawFallback) {
		String fallback = StringUtils.firstNonBlank(rawFallback, "X").trim();
		String normalized = toJavaTypeNameInternal(fallback);
		if (normalized.isBlank()) {
			return "X";
		}
		if (!Character.isLetter(normalized.charAt(0))) {
			return "X";
		}
		return normalized;
	}

	private static String toJavaTypeNameInternal(String rawValue) {
		String raw = StringUtils.firstNonBlank(rawValue, "").trim();
		Matcher matcher = TOKEN_PATTERN.matcher(raw);
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String token = matcher.group();
			if (token == null || token.isBlank()) {
				continue;
			}
			if (Character.isLetter(token.charAt(0))) {
				result.append(Character.toUpperCase(token.charAt(0)));
				if (token.length() > 1) {
					result.append(token.substring(1));
				}
			} else {
				result.append(token);
			}
		}
		return result.toString();
	}
}
