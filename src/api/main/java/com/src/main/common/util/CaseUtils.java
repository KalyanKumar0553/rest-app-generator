package com.src.main.common.util;


public final class CaseUtils {

    private CaseUtils() { }

    /** Convert to lower_snake_case */
    public static String toSnake(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                if (i > 0 && (Character.isLowerCase(chars[i - 1]) || Character.isDigit(chars[i - 1]))) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Convert to lowerCamelCase */
    public static String toCamel(String input) {
        if (input == null || input.isEmpty()) return input;
        String pascal = toPascal(input);
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    /** Convert to UpperCamelCase (PascalCase) */
    public static String toPascal(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
