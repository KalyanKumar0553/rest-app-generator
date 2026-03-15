package com.src.main.common.util;

import java.util.Locale;

/**
 * Lightweight String helper utilities for null-safety and simple transformations.
 * These mirror a subset of Apache Commons StringUtils, but without extra dependencies.
 */
public final class StringUtils {

    private StringUtils() { }

    /**
     * Returns true if the given string is not null and contains at least one non-whitespace character.
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Returns true if the string is null or blank (only whitespace).
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Returns the first non-blank string among the given arguments, or null if all are blank.
     */
    public static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Returns the given string if non-blank, otherwise returns the fallback.
     */
    public static String firstNonBlank(String value, String fallback) {
        return isNotBlank(value) ? value : fallback;
    }

    /**
     * Returns the given string if not null, otherwise returns the fallback.
     */
    public static String firstNonNull(String value, String fallback) {
        return value != null ? value : fallback;
    }

    /**
     * Null-safe trim.
     */
    public static String trimToNull(String str) {
        if (str == null) return null;
        String t = str.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Converts the first character to uppercase (null-safe).
     */
    public static String capitalize(String str) {
        if (isBlank(str)) return str;
        return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1);
    }

    /**
     * Converts the first character to lowercase (null-safe).
     */
    public static String uncapitalize(String str) {
        if (isBlank(str)) return str;
        return str.substring(0, 1).toLowerCase(Locale.ROOT) + str.substring(1);
    }
}
