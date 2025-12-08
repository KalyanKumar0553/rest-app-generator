package com.src.main.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.Strings;

import com.src.main.dto.ColumnSpecDTO;
import com.src.main.dto.ConstraintDTO;
import com.src.main.dto.FieldBlockDTO;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.JoinColumnSpecDTO;
import com.src.main.dto.JoinTableSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.RelationBlockDTO;
import com.src.main.dto.RelationSpecDTO;
import com.src.main.dto.FieldNameAndType;

public final class ModelGeneratorUtils {

	private ModelGeneratorUtils() {
		// utility
	}

	public static String getString(Map<String, Object> m, String key, String def) {
		if (m == null) {
			return def;
		}
		Object v = m.get(key);
		return (v == null) ? def : String.valueOf(v);
	}

	public static String getStringAny(Map<String, Object> m, String[] keys, String def) {
		if (m == null) {
			return def;
		}
		for (String k : keys) {
			Object v = m.get(k);
			if (v != null) {
				return String.valueOf(v);
			}
		}
		return def;
	}

	public static Integer getInt(Map<String, Object> m, String key, Integer def) {
		if (m == null) {
			return def;
		}
		Object v = m.get(key);
		if (v == null) {
			return def;
		}
		if (v instanceof Number n) {
			return n.intValue();
		}
		try {
			return Integer.parseInt(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
	}

	public static Long getLong(Map<String, Object> m, String key, Long def) {
		if (m == null) {
			return def;
		}
		Object v = m.get(key);
		if (v == null) {
			return def;
		}
		if (v instanceof Number n) {
			return n.longValue();
		}
		try {
			return Long.parseLong(String.valueOf(v));
		} catch (Exception e) {
			return def;
		}
	}

	public static boolean getBoolean(Map<String, Object> m, String key, boolean def) {
		if (m == null) {
			return def;
		}
		Object v = m.get(key);
		if (v == null) {
			return def;
		}
		if (v instanceof Boolean b) {
			return b;
		}
		String s = String.valueOf(v).trim().toLowerCase();
		return switch (s) {
		case "true", "1", "yes", "y" -> true;
		case "false", "0", "no", "n" -> false;
		default -> def;
		};
	}

	/* ==== JPA / annotation helpers ==== */

	public static String toCascadeArray(List<String> cascade) {
		if (cascade == null || cascade.isEmpty()) {
			return "";
		}
		return ", cascade = {"
				+ cascade.stream().map(s -> "jakarta.persistence.CascadeType." + s).collect(Collectors.joining(", "))
				+ "}";
	}

	public static String joinTableAnnotation(JoinTableSpecDTO jt) {
		StringBuilder sb = new StringBuilder("@JoinTable(name = \"").append(jt.getName()).append("\"");

		if (jt.getJoinColumns() != null && !jt.getJoinColumns().isEmpty()) {
			sb.append(", joinColumns = {");
			sb.append(jt.getJoinColumns().stream()
					.map(jc -> "@JoinColumn(name = \"" + jc.getName() + "\""
							+ (Strings.isNotBlank(jc.getReferencedColumnName())
									? ", referencedColumnName = \"" + jc.getReferencedColumnName() + "\""
									: "")
							+ ")")
					.collect(Collectors.joining(", ")));
			sb.append("}");
		}

		if (jt.getInverseJoinColumns() != null && !jt.getInverseJoinColumns().isEmpty()) {
			sb.append(", inverseJoinColumns = {");
			sb.append(jt.getInverseJoinColumns().stream()
					.map(jc -> "@JoinColumn(name = \"" + jc.getName() + "\""
							+ (Strings.isNotBlank(jc.getReferencedColumnName())
									? ", referencedColumnName = \"" + jc.getReferencedColumnName() + "\""
									: "")
							+ ")")
					.collect(Collectors.joining(", ")));
			sb.append("}");
		}

		sb.append(")");
		return sb.toString();
	}

	public static String escapeJava(String s) {
		return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	public static ColumnSpecDTO ensureColumn(ColumnSpecDTO col) {
		return (col != null) ? col : new ColumnSpecDTO();
	}

	public static String buildMessageKey(ModelSpecDTO m, FieldSpecDTO f, String key) {
		return "validation." + CaseUtils.toSnake(m.getName()) + "." + CaseUtils.toSnake(f.getName()) + "." + key;
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

	public static String msgAnno(String base, ModelSpecDTO m, FieldSpecDTO f, String key) {
		String messageKey = buildMessageKey(m, f, key);
		if (base.contains("(")) {
			return base.substring(0, base.length() - 1) + ", message=\"{" + messageKey + "}\")";
		}
		return base + "(message=\"{" + messageKey + "}\")";
	}
}
