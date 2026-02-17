package com.src.main.sm.executor.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public final class DtoGenerationSupport {

	private static final Pattern LIST_PATTERN = Pattern.compile("^List\\s*<\\s*([A-Za-z0-9_$.]+)\\s*>$");

	private DtoGenerationSupport() {
	}

	public static String str(Object o) {
		return (o == null) ? null : String.valueOf(o);
	}

	public static boolean hasNonEmpty(Object raw) {
		if (raw == null)
			return false;
		if (raw instanceof List)
			return !((List<?>) raw).isEmpty();
		if (raw instanceof Map)
			return !((Map<?, ?>) raw).isEmpty();
		return false;
	}

	public static List<Map<String, Object>> normalizeClassConstraints(Object raw) {
		List<Map<String, Object>> out = new ArrayList<>();
		if (raw == null)
			return out;

		if (raw instanceof List) {
			for (Object n : (List<?>) raw) {
				if (n instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> m = new HashMap<>((Map<String, Object>) n);
					copyMessageKey(n, m);
					if (!m.containsKey("kind") && m.containsKey("type")) {
						m.put("kind", m.get("type"));
					}
					if (m.get("kind") != null)
						out.add(m);
				}
			}
			return out;
		}

		if (raw instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) raw;
			map.forEach((k, v) -> {
				if (v instanceof Map) {
					Map<String, Object> spec = new HashMap<>((Map<String, Object>) v);
					copyMessageKey(v, spec);
					spec.put("kind", k);
					out.add(spec);
				}
			});
		}
		return out;
	}

	public static List<Map<String, Object>> normalizeConstraints(Object raw) {
		List<Map<String, Object>> out = new ArrayList<>();
		if (raw == null)
			return out;

		java.util.function.BiConsumer<String, Map<String, Object>> add = (kindRaw, specRaw) -> {
			if (kindRaw == null || kindRaw.isBlank())
				return;
			Map<String, Object> spec = new HashMap<>();
			if (specRaw != null)
				spec.putAll(specRaw);
			copyMessageKey(specRaw, spec);
			spec.put("kind", kindRaw);
			out.add(spec);
		};

		if (raw instanceof List<?>) {
			for (Object n : (List<?>) raw) {
				if (n == null)
					continue;
				if (n instanceof String s) {
					add.accept(s, Map.of());
					continue;
				}
				if (n instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> m = new HashMap<>((Map<String, Object>) n);
					Object kind = m.get("kind");
					if (kind instanceof String ks && !ks.isBlank()) {
						copyMessageKey(m, m);
						add.accept(ks, m);
						continue;
					}
					if (m.size() == 1) {
						Map.Entry<String, Object> e = m.entrySet().iterator().next();
						String k = e.getKey();
						Map<String, Object> vmap = (e.getValue() instanceof Map)
								? new HashMap<>((Map<String, Object>) e.getValue())
								: new HashMap<>();
						copyMessageKey(e.getValue(), vmap);
						add.accept(k, vmap);
						continue;
					}
					Object type = m.get("type");
					if (type instanceof String ts && !ts.isBlank()) {
						copyMessageKey(m, m);
						add.accept(ts, m);
					}
				}
			}
			return out;
		}

		if (raw instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> e : map.entrySet()) {
				String k = String.valueOf(e.getKey());
				Map<String, Object> spec = (e.getValue() instanceof Map)
						? new HashMap<>((Map<String, Object>) e.getValue())
						: new HashMap<>();
				copyMessageKey(e.getValue(), spec);
				add.accept(k, spec);
			}
		}
		return out;
	}

	public static String toClassLevelAnnotation(Map<String, Object> c) {
		String kind = normalizeKind(str(c.get("kind")));
		String key = str(c.get("key"));

		switch (kind) {
		case "fieldmatch": {
			String first = str(c.get("first"));
			String second = str(c.get("second"));
			if (first == null || first.isBlank() || second == null || second.isBlank())
				return null;
			String msg = (key == null || key.isBlank()) ? "" : ", message=\"{" + key + "}\"";
			return "@com.src.main.validation.FieldMatch(first=\"" + escapeJava(first) + "\", second=\""
					+ escapeJava(second) + "\"" + msg + ")";
		}
		case "conditionalrequired": {
			String field = str(c.get("field"));
			String dependsOn = str(c.get("dependsOn"));
			String eq = str(c.get("equals"));
			if (field == null || field.isBlank() || dependsOn == null || dependsOn.isBlank())
				return null;
			String eqPart = (eq == null || eq.isBlank()) ? "" : ", equals=\"" + escapeJava(eq) + "\"";
			String msg = (key == null || key.isBlank()) ? "" : ", message=\"{" + key + "}\"";
			return "@com.src.main.validation.ConditionalRequired(field=\"" + escapeJava(field) + "\", dependsOn=\""
					+ escapeJava(dependsOn) + "\"" + eqPart + msg + ")";
		}
		case "scriptassert": {
			String lang = str(c.getOrDefault("lang", "javascript"));
			String script = str(c.get("script"));
			if (script == null || script.isBlank())
				return null;
			String msg = (key == null || key.isBlank()) ? "" : ", message=\"{" + key + "}\"";
			return "@org.hibernate.validator.constraints.ScriptAssert(lang=\"" + escapeJava(lang) + "\", script=\""
					+ escapeJava(script) + "\"" + msg + ")";
		}
		default:
			return null;
		}
	}

	public static String defaultClassMessage(Map<String, Object> c) {
		String kind = normalizeKind(String.valueOf(c.get("kind")));
		if ("fieldmatch".equals(kind))
			return "Fields must match";
		if ("conditionalrequired".equals(kind))
			return "Field is required based on other field";
		if ("scriptassert".equals(kind))
			return "Class-level rule violated";
		return "Invalid object";
	}

	public static String defaultMessage(String kind, String field) {
		switch (kind) {
		case "NotNull":
			return field + " must not be null";
		case "NotBlank":
			return field + " must not be blank";
		case "Email":
			return field + " must be a valid email";
		case "Size":
			return field + " has invalid size";
		case "Pattern":
			return field + " has invalid format";
		case "Min":
			return field + " is below minimum";
		case "Max":
			return field + " is above maximum";
		default:
			return "Invalid value for " + field;
		}
	}

	public static String toValidationAnnotation(String kind, Map<String, Object> c, String key) {
		String lower = (kind == null) ? "" : kind.trim().toLowerCase();
		String msgPart = (key == null || key.isBlank()) ? "" : String.format(", message=\"{%s}\"", key);

		switch (lower) {
		case "notnull", "not_null", "not-null":
			return "@jakarta.validation.constraints.NotNull" + wrap(msgPart);
		case "notblank", "not_blank", "not-blank":
			return "@jakarta.validation.constraints.NotBlank" + wrap(msgPart);
		case "notempty", "not_empty", "not-empty":
			return "@jakarta.validation.constraints.NotEmpty" + wrap(msgPart);
		case "null", "isnull", "is_null":
			return "@jakarta.validation.constraints.Null" + wrap(msgPart);
		case "size": {
			String min = optNum(c, "min");
			String max = optNum(c, "max");
			StringBuilder args = new StringBuilder();
			if (min != null)
				args.append("min=").append(min);
			if (max != null) {
				if (args.length() > 0)
					args.append(", ");
				args.append("max=").append(max);
			}
			if (!msgPart.isEmpty()) {
				if (args.length() > 0)
					args.append(", ");
				args.append(msgPart.substring(2));
			}
			return "@jakarta.validation.constraints.Size(" + args + ")";
		}
		case "pattern": {
			Object r = c.getOrDefault("regex", c.getOrDefault("regexp", c.get("pattern")));
			if (r == null)
				return null;
			String regex = escapeJavaRegex(r.toString());
			String ann = "@jakarta.validation.constraints.Pattern(regexp=\"" + regex + "\"";
			if (!msgPart.isEmpty())
				ann += msgPart;
			return ann + ")";
		}
		case "email":
			return "@jakarta.validation.constraints.Email" + wrap(msgPart);
		case "min": {
			String v = optNum(c, "value");
			if (v == null)
				return null;
			String ann = "@jakarta.validation.constraints.Min(value=" + v;
			if (!msgPart.isEmpty())
				ann += msgPart;
			return ann + ")";
		}
		case "max": {
			String v = optNum(c, "value");
			if (v == null)
				return null;
			String ann = "@jakarta.validation.constraints.Max(value=" + v;
			if (!msgPart.isEmpty())
				ann += msgPart;
			return ann + ")";
		}
		case "decimalmin", "decimal_min": {
			String v = Objects.toString(c.get("value"), null);
			if (v == null)
				return null;
			boolean inclusive = Boolean.parseBoolean(Objects.toString(c.getOrDefault("inclusive", "true")));
			String ann = "@jakarta.validation.constraints.DecimalMin(value=\"" + v + "\", inclusive=" + inclusive;
			if (!msgPart.isEmpty())
				ann += msgPart;
			return ann + ")";
		}
		case "decimalmax", "decimal_max": {
			String v = Objects.toString(c.get("value"), null);
			if (v == null)
				return null;
			boolean inclusive = Boolean.parseBoolean(Objects.toString(c.getOrDefault("inclusive", "true")));
			String ann = "@jakarta.validation.constraints.DecimalMax(value=\"" + v + "\", inclusive=" + inclusive;
			if (!msgPart.isEmpty())
				ann += msgPart;
			return ann + ")";
		}
		case "digits": {
			String integer = optNum(c, "integer");
			if (integer == null)
				integer = "0";
			String fraction = optNum(c, "fraction");
			if (fraction == null)
				fraction = "0";
			String ann = "@jakarta.validation.constraints.Digits(integer=" + integer + ", fraction=" + fraction;
			if (!msgPart.isEmpty())
				ann += msgPart;
			return ann + ")";
		}
		case "positive":
			return "@jakarta.validation.constraints.Positive" + wrap(msgPart);
		case "positiveorzero", "positive_or_zero":
			return "@jakarta.validation.constraints.PositiveOrZero" + wrap(msgPart);
		case "negative":
			return "@jakarta.validation.constraints.Negative" + wrap(msgPart);
		case "negativeorzero", "negative_or_zero":
			return "@jakarta.validation.constraints.NegativeOrZero" + wrap(msgPart);
		case "past":
			return "@jakarta.validation.constraints.Past" + wrap(msgPart);
		case "pastorpresent", "past_or_present":
			return "@jakarta.validation.constraints.PastOrPresent" + wrap(msgPart);
		case "future":
			return "@jakarta.validation.constraints.Future" + wrap(msgPart);
		case "futureorpresent", "future_or_present":
			return "@jakarta.validation.constraints.FutureOrPresent" + wrap(msgPart);
		case "asserttrue", "assert_true":
			return "@jakarta.validation.constraints.AssertTrue" + wrap(msgPart);
		case "assertfalse", "assert_false":
			return "@jakarta.validation.constraints.AssertFalse" + wrap(msgPart);
		default:
			return null;
		}
	}

	public static String mapType(String t) {
		if (t == null)
			return "String";
		t = t.trim();
		var m = LIST_PATTERN.matcher(t);
		if (m.matches()) {
			String inner = mapType(m.group(1));
			return "java.util.List<" + inner + ">";
		}
		if (t.endsWith("[]")) {
			String el = mapType(t.substring(0, t.length() - 2));
			return "java.util.List<" + el + ">";
		}
		switch (t) {
		case "String", "Text":
			return "String";
		case "Boolean":
			return "Boolean";
		case "Int":
			return "Integer";
		case "Long":
			return "Long";
		case "Double":
			return "Double";
		case "Decimal":
			return "java.math.BigDecimal";
		case "UUID":
			return "java.util.UUID";
		case "Date":
			return "java.time.LocalDate";
		case "Time":
			return "java.time.LocalTime";
		case "DateTime":
			return "java.time.LocalDateTime";
		case "Instant":
			return "java.time.Instant";
		case "Json":
			return "java.util.Map<String,Object>";
		case "Binary":
			return "org.springframework.web.multipart.MultipartFile";
		default:
			return t;
		}
	}

	public static String toMethodName(String field) {
		if (field == null || field.isEmpty())
			return field;
		return Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}

	public static boolean isNested(String type) {
		if (type == null)
			return false;
		type = type.trim();
		if (type.startsWith("List<") && type.endsWith(">"))
			return true;
		char c = type.charAt(0);
		if (Character.isUpperCase(c)) {
			return !"String Integer Long Boolean Double BigDecimal UUID Instant Date Time DateTime Json Binary"
					.contains(type);
		}
		return false;
	}

	public static String escapeJava(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	public static void collectImportFromAnnotation(String annotation, Set<String> imports) {
		if (annotation == null)
			return;
		int at = annotation.indexOf('@');
		if (at < 0)
			return;
		int start = at + 1;
		int end = start;
		while (end < annotation.length()) {
			char ch = annotation.charAt(end);
			if (ch == '(' || Character.isWhitespace(ch))
				break;
			end++;
		}
		if (end <= start)
			return;
		String name = annotation.substring(start, end).trim();
		if (name.contains(".")) {
			imports.add(name);
		}
	}

	public static List<String> simplifyAnnotations(List<String> annotations, Set<String> imports) {
		List<String> out = new ArrayList<>(annotations.size());
		for (String a : annotations) {
			if (a == null || a.isBlank()) {
				out.add(a);
				continue;
			}
			int at = a.indexOf('@');
			if (at < 0) {
				out.add(a);
				continue;
			}
			int start = at + 1;
			int end = start;
			while (end < a.length()) {
				char ch = a.charAt(end);
				if (ch == '(' || Character.isWhitespace(ch))
					break;
				end++;
			}
			if (end <= start) {
				out.add(a);
				continue;
			}
			String fq = a.substring(start, end).trim();
			String simple = fq.contains(".") ? fq.substring(fq.lastIndexOf('.') + 1) : fq;
			if (!fq.equals(simple) && imports.contains(fq)) {
				out.add(a.substring(0, start) + simple + a.substring(end));
			} else {
				out.add(a);
			}
		}
		return out;
	}

	public static String injectImportsAfterPackage(String code, Set<String> imports) {
		if (imports == null || imports.isEmpty())
			return code;

		List<String> javaJakarta = new ArrayList<>();
		List<String> lombok = new ArrayList<>();
		List<String> thirdParty = new ArrayList<>();
		List<String> project = new ArrayList<>();

		for (String fq : imports) {
			if (fq.startsWith("java.") || fq.startsWith("javax.") || fq.startsWith("jakarta.")) {
				javaJakarta.add(fq);
			} else if (fq.startsWith("lombok.")) {
				lombok.add(fq);
			} else if (fq.startsWith("com.src.main.")) {
				project.add(fq);
			} else {
				thirdParty.add(fq);
			}
		}

		Comparator<String> cmp = Comparator.naturalOrder();
		javaJakarta.sort(cmp);
		lombok.sort(cmp);
		thirdParty.sort(cmp);
		project.sort(cmp);

		StringBuilder importBlock = new StringBuilder();
		appendImports(importBlock, javaJakarta);
		if (importBlock.length() > 0 && (!lombok.isEmpty() || !thirdParty.isEmpty() || !project.isEmpty()))
			importBlock.append('\n');
		appendImports(importBlock, lombok);
		if (!lombok.isEmpty() && (!thirdParty.isEmpty() || !project.isEmpty()))
			importBlock.append('\n');
		appendImports(importBlock, thirdParty);
		if (!thirdParty.isEmpty() && !project.isEmpty())
			importBlock.append('\n');
		appendImports(importBlock, project);

		if (importBlock.length() == 0)
			return code;
		int pkgIdx = code.indexOf("package ");
		if (pkgIdx < 0) {
			return importBlock.append('\n').append(code).toString();
		}
		int semi = code.indexOf(';', pkgIdx);
		if (semi < 0) {
			return importBlock.append('\n').append(code).toString();
		}
		int lineEnd = code.indexOf('\n', semi);
		if (lineEnd < 0)
			lineEnd = semi + 1;
		String prefix = code.substring(0, lineEnd + 1);
		String suffix = code.substring(lineEnd + 1);
		return prefix + "\n" + importBlock + "\n" + suffix;
	}

	public static void mergeDtoMessagesIntoYaml(Map<String, Object> yaml, List<Map<String, Object>> dtosForMessages) {
		if (yaml == null || dtosForMessages == null || dtosForMessages.isEmpty()) {
			return;
		}

		Map<String, Object> messages = new LinkedHashMap<>();
		Object existing = yaml.get("messages");
		if (existing instanceof Map<?, ?> existingMap) {
			messages.putAll((Map<String, Object>) existingMap);
		}

		for (Map<String, Object> dto : dtosForMessages) {
			Object fieldsRaw = dto.get("fields");
			if (fieldsRaw instanceof List<?> fields) {
				for (Object fieldRaw : fields) {
					if (!(fieldRaw instanceof Map<?, ?> fieldMap)) {
						continue;
					}
					Object constraintsRaw = ((Map<String, Object>) fieldMap).get("constraints");
					if (!(constraintsRaw instanceof List<?> constraints)) {
						continue;
					}
					for (Object cRaw : constraints) {
						if (!(cRaw instanceof Map<?, ?> cMap)) {
							continue;
						}
						String key = str(((Map<String, Object>) cMap).get("key"));
						String def = str(((Map<String, Object>) cMap).get("defaultMessage"));
						if (key != null && !key.isBlank()) {
							messages.putIfAbsent(key, (def == null || def.isBlank()) ? key : def);
						}
					}
				}
			}

			Object classRaw = dto.get("classConstraints");
			if (classRaw instanceof List<?> constraints) {
				for (Object cRaw : constraints) {
					if (!(cRaw instanceof Map<?, ?> cMap)) {
						continue;
					}
					String key = str(((Map<String, Object>) cMap).get("key"));
					String def = str(((Map<String, Object>) cMap).get("defaultMessage"));
					if (key != null && !key.isBlank()) {
						messages.putIfAbsent(key, (def == null || def.isBlank()) ? key : def);
					}
				}
			}
		}

		if (!messages.isEmpty()) {
			yaml.put("messages", messages);
		}
	}

	private static void appendImports(StringBuilder sb, List<String> fqcns) {
		for (String fq : fqcns) {
			sb.append("import ").append(fq).append(";\n");
		}
	}

	private static String wrap(String msgPart) {
		return (msgPart == null || msgPart.isBlank()) ? "" : "(" + msgPart.substring(2) + ")";
	}

	private static String optNum(Map<String, Object> c, String key) {
		Object v = c.get(key);
		if (v == null)
			return null;
		String s = v.toString();
		if (s.matches("^-?\\d+(?:\\.\\d+)?$"))
			return s;
		return null;
	}

	private static String normalizeKind(String raw) {
		if (raw == null)
			return "";
		return raw.trim().toLowerCase().replaceAll("[-_]", "");
	}

	private static String escapeJavaRegex(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static void copyMessageKey(Object src, Map<String, Object> dst) {
		if (src instanceof Map) {
			Object mk = ((Map<?, ?>) src).get("messageKey");
			if (mk != null)
				dst.put("key", mk.toString());
		}
	}
}
