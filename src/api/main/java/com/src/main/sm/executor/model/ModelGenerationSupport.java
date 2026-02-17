package com.src.main.sm.executor.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.src.main.common.util.CaseUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ConstraintDTO;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.ModelSpecDTO;

public final class ModelGenerationSupport {

	private ModelGenerationSupport() {
	}

	@SuppressWarnings("unchecked")
	public static void mergeValidationMessagesIntoYaml(Map<String, Object> yaml, Map<String, String> messages) {
		if (messages == null || messages.isEmpty()) {
			return;
		}
		Object existingRaw = yaml.get("messages");
		Map<String, Object> merged = new LinkedHashMap<>();
		if (existingRaw instanceof Map<?, ?> existingMap) {
			merged.putAll((Map<String, Object>) existingMap);
		}
		for (Map.Entry<String, String> entry : messages.entrySet()) {
			merged.putIfAbsent(entry.getKey(), entry.getValue());
		}
		yaml.put("messages", merged);
	}

	public static Map<String, String> collectValidationMessages(AppSpecDTO spec) {
		Map<String, String> messages = new LinkedHashMap<>();
		if (spec == null || spec.getModels() == null) {
			return messages;
		}

		for (ModelSpecDTO model : spec.getModels()) {
			if (model.getFields() == null) {
				continue;
			}
			for (FieldSpecDTO field : model.getFields()) {
				if (field.getConstraints() == null) {
					continue;
				}
				for (ConstraintDTO constraint : field.getConstraints()) {
					if (constraint == null || constraint.getName() == null || constraint.getName().isBlank()) {
						continue;
					}
					String key = buildMessageKey(model, field, constraint.getName());
					String message = resolveValidationMessage(field, constraint);
					messages.putIfAbsent(key, message);
				}
			}
		}

		return messages;
	}

	public static String resolveValidationMessage(FieldSpecDTO field, ConstraintDTO constraint) {
		String custom = getString(constraint.getParams(), "message", null);
		if (custom != null && !custom.isBlank()) {
			return custom;
		}

		String fieldName = CaseUtils.toSnake(field.getName()).replace('_', ' ');
		return switch (constraint.getName()) {
		case "NotNull" -> fieldName + " must not be null";
		case "NotBlank" -> fieldName + " must not be blank";
		case "Email" -> fieldName + " must be a valid email";
		case "Pattern" -> fieldName + " has invalid format";
		case "Size" -> fieldName + " size is invalid";
		case "Min" -> fieldName + " must be greater than or equal to " + getLong(constraint.getParams(), "value", 0L);
		case "Max" -> fieldName + " must be less than or equal to " + getLong(constraint.getParams(), "value", Long.MAX_VALUE);
		case "Positive" -> fieldName + " must be positive";
		case "DecimalMin" -> fieldName + " must be greater than or equal to " + getString(constraint.getParams(), "value", "0");
		case "Digits" -> fieldName + " has invalid numeric format";
		case "Past" -> fieldName + " must be a past date";
		case "Future" -> fieldName + " must be a future date";
		case "PastOrPresent" -> fieldName + " must be in the past or present";
		case "AssertTrue" -> fieldName + " must be true";
		case "AssertFalse" -> fieldName + " must be false";
		default -> fieldName + " is invalid";
		};
	}

	public static String buildMinAnnotation(long value, String msgKey) {
		if (msgKey != null) {
			return String.format("@Min(value = %d, message = \"{%s}\")", value, msgKey);
		}
		return String.format("@Min(value = %d)", value);
	}

	public static String buildMaxAnnotation(long value, String msgKey) {
		if (msgKey != null) {
			return String.format("@Max(value = %d, message = \"{%s}\")", value, msgKey);
		}
		return String.format("@Max(value = %d)", value);
	}

	public static String buildMessageKey(ModelSpecDTO m, FieldSpecDTO f, String key) {
		return "validation." + CaseUtils.toSnake(m.getName()) + "." + CaseUtils.toSnake(f.getName()) + "." + key;
	}

	public static String msgAnno(String base, ModelSpecDTO m, FieldSpecDTO f, String key) {
		String messageKey = buildMessageKey(m, f, key);
		if (base.contains("(")) {
			return base.substring(0, base.length() - 1) + ", message=\"{" + messageKey + "}\")";
		}
		return base + "(message=\"{" + messageKey + "}\")";
	}

	public static String resolveJavaType(String rawType, Set<String> imports) {
		if (rawType == null)
			return null;

		String type = rawType.trim();
		if (type.contains("<") && type.contains(">")) {
			String outer = type.substring(0, type.indexOf("<")).trim();
			String inner = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();
			addImportIfNeeded(outer, imports);
			String resolvedInner;
			if (inner.contains(",")) {
				String[] parts = inner.split(",");
				resolvedInner = Arrays.stream(parts).map(String::trim).map(t -> resolveJavaType(t, imports))
						.collect(Collectors.joining(", "));
			} else {
				resolvedInner = resolveJavaType(inner, imports);
			}
			return outer + "<" + resolvedInner + ">";
		}

		return switch (type) {
		case "String" -> {
			imports.add("java.lang.String");
			yield "String";
		}
		case "Integer" -> {
			imports.add("java.lang.Integer");
			yield "Integer";
		}
		case "Long" -> {
			imports.add("java.lang.Long");
			yield "Long";
		}
		case "Boolean" -> {
			imports.add("java.lang.Boolean");
			yield "Boolean";
		}
		case "BigDecimal" -> {
			imports.add("java.math.BigDecimal");
			yield "BigDecimal";
		}
		case "UUID" -> {
			imports.add("java.util.UUID");
			yield "UUID";
		}
		case "LocalDate" -> {
			imports.add("java.time.LocalDate");
			yield "LocalDate";
		}
		case "LocalDateTime" -> {
			imports.add("java.time.LocalDateTime");
			yield "LocalDateTime";
		}
		case "OffsetDateTime" -> {
			imports.add("java.time.OffsetDateTime");
			yield "OffsetDateTime";
		}
		default -> {
			if (type.contains(".")) {
				imports.add(type);
				yield type.substring(type.lastIndexOf('.') + 1);
			}
			yield type;
		}
		};
	}

	public static void addImportIfNeeded(String outer, Set<String> imports) {
		switch (outer) {
		case "List" -> imports.add("java.util.List");
		case "Set" -> imports.add("java.util.Set");
		case "Map" -> imports.add("java.util.Map");
		default -> {
			if (outer.contains(".")) {
				imports.add(outer);
			}
		}
		}
	}

	public static String getString(Map<String, Object> m, String key, String def) {
		if (m == null)
			return def;
		Object v = m.get(key);
		return (v == null) ? def : String.valueOf(v);
	}

	public static String getStringAny(Map<String, Object> m, String[] keys, String def) {
		if (m == null)
			return def;
		for (String k : keys) {
			Object v = m.get(k);
			if (v != null)
				return String.valueOf(v);
		}
		return def;
	}

	public static Integer getInt(Map<String, Object> m, String key, Integer def) {
		if (m == null)
			return def;
		Object v = m.get(key);
		if (v == null)
			return def;
		if (v instanceof Number n)
			return n.intValue();
		try {
			return Integer.parseInt(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
	}

	public static Long getLong(Map<String, Object> m, String key, Long def) {
		if (m == null)
			return def;
		Object v = m.get(key);
		if (v == null)
			return def;
		if (v instanceof Number n)
			return n.longValue();
		try {
			return Long.parseLong(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
	}

	public static boolean getBoolean(Map<String, Object> m, String key, boolean def) {
		if (m == null)
			return def;
		Object v = m.get(key);
		if (v == null)
			return def;
		if (v instanceof Boolean b)
			return b;
		String s = String.valueOf(v).trim().toLowerCase();
		return switch (s) {
		case "true", "1", "yes", "y" -> true;
		case "false", "0", "no", "n" -> false;
		default -> def;
		};
	}

	public static String escapeJava(String s) {
		return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
