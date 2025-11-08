package com.src.main.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.utils.AppConstants;

@Component
public class DtoGenerationExecutor implements StepExecutor {

	private static final Pattern LIST_PATTERN = Pattern.compile("^List\\s*<\\s*([A-Za-z0-9_$.]+)\\s*>$");

	
	private final TemplateEngine tpl;

	public DtoGenerationExecutor(TemplateEngine tpl) {
		this.tpl = tpl;
	}

	private static String str(Object o) {
		return (o == null) ? null : String.valueOf(o);
	}

	@Override
	public StepResult execute(ExtendedState data) throws Exception {
		Path root = Path.of((String) data.getVariables().get("root"));
		String groupId = (String) data.getVariables().get("groupId");
		String artifact = (String) data.getVariables().get("artifact");
		@SuppressWarnings("unchecked")
		Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get("yaml");

		String basePkg = (yaml != null) ? str(yaml.get("basePackage")) : null;
		if (basePkg == null || basePkg.isBlank()) {
			basePkg = groupId + "." + artifact.replace('-', '_');
		}
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> dtos = (List<Map<String, Object>>) yaml.getOrDefault("dtos", List.of());
		if (dtos.isEmpty()) {
			Map<String, Object> output = Map.of("status", "Success");
			return StepResult.ok(output);
		}

		if (dtos.stream().anyMatch(d -> hasNonEmpty(d.get("classConstraints")))) {
			ensureCrossFieldValidationHelpers(root, basePkg);
		}

		List<Map<String, Object>> dtosForMessages = new ArrayList<>();

		for (Map<String, Object> d : dtos) {
			String sub = "request".equals(String.valueOf(d.get("type"))) ? "request" : "response";
			String name = String.valueOf(d.get("name"));
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> fields = (List<Map<String, Object>>) d.getOrDefault("fields", List.of());

			// ---- class-level (cross-field) annotations ----
			List<Map<String, Object>> classSpecs = normalizeClassConstraints(d.get("classConstraints"));
			List<String> classAnnotations = new ArrayList<>();
			List<Map<String, Object>> classConstraintsForMessages = new ArrayList<>();
			for (Map<String, Object> c : classSpecs) {
				String ann = toClassLevelAnnotation(c);
				if (ann != null && !ann.isBlank()) {
					classAnnotations.add(ann);
					String key = str(c.get("key"));
					if (key != null && !key.isBlank()) {
						Map<String, Object> cm = new LinkedHashMap<>();
						cm.put("key", key);
						cm.put("defaultMessage", c.getOrDefault("message", defaultClassMessage(c)));
						classConstraintsForMessages.add(cm);
					}
				}
			}

			List<Map<String, Object>> fieldModels = new ArrayList<>();
			List<Map<String, Object>> fieldsForMessages = new ArrayList<>();

			Set<String> imports = new LinkedHashSet<>();

			for (Map<String, Object> f : fields) {
				String fname = String.valueOf(f.get("name"));
				String ftype = String.valueOf(f.get("type"));

				Map<String, Object> fm = new LinkedHashMap<>();
				fm.put("name", fname);
				fm.put("javaType", mapType(ftype));
				fm.put("method", toMethodName(fname));

				List<String> annotations = new ArrayList<>();

				// ---- Constraints: accept list-style OR map-style ----
				List<Map<String, Object>> constraints = normalizeConstraints(f.get("constraints"), fname);

				List<Map<String, Object>> constraintsForMessages = new ArrayList<>();
				for (Map<String, Object> c : constraints) {
					String kind = str(c.get("kind"));
					String key = str(c.get("key")); // already normalized by normalizeConstraints if map-style used
					if (kind == null || key == null) continue;

					String ann = toValidationAnnotation(kind, c, key, fname);
					if (ann != null && !ann.isBlank()) {
						annotations.add(ann);
						collectImportFromAnnotation(ann, imports);
						Map<String, Object> cm = new LinkedHashMap<>();
						cm.put("key", key);
						cm.put("defaultMessage", c.getOrDefault("message", defaultMessage(kind, fname)));
						constraintsForMessages.add(cm);
					}
				}

				// Jackson @JsonProperty
				if (f.containsKey("jsonProperty")) {
					String jp = str(f.get("jsonProperty"));
					if (jp != null && !jp.isBlank()) {
						String ann = "@com.fasterxml.jackson.annotation.JsonProperty(\"" + escapeJava(jp) + "\")";
						annotations.add(ann);
						collectImportFromAnnotation(ann, imports); // will import com.fasterxml.jackson.annotation.JsonProperty
					}
				}

				// Nested DTO or List<DTO> => @Valid
				if (isNested(ftype)) {
					String ann = "@jakarta.validation.Valid";
					annotations.add(ann);
					collectImportFromAnnotation(ann, imports);
				}

				// Convert any fully-qualified annotations on this field to simple names
				List<String> simplified = simplifyAnnotations(annotations, imports);

				fm.put("annotations", simplified);
				fieldModels.add(fm);

				Map<String, Object> msgField = new LinkedHashMap<>();
				msgField.put("constraints", constraintsForMessages);
				fieldsForMessages.add(msgField);
			}
			classAnnotations.forEach(a -> collectImportFromAnnotation(a, imports));
			classAnnotations = simplifyAnnotations(classAnnotations, imports);
			String code = tpl.render(
				AppConstants.TPL_DTO,
				Map.of(
					"basePkg", basePkg,
					"sub", sub,
					"name", name,
					"classAnnotations", String.join("\n", classAnnotations),
					"fields", fieldModels
				)
			);
			code = injectImportsAfterPackage(code, imports, basePkg);

			Path dir = root.resolve("src/main/java/" + basePkg.replace('.', '/') + "/dto/" + sub);
			Files.createDirectories(dir);
			Files.writeString(dir.resolve(name + ".java"), code, StandardCharsets.UTF_8);
			Map<String, Object> dtoMsg = new LinkedHashMap<>();
			dtoMsg.put("name", name);
			dtoMsg.put("fields", fieldsForMessages);
			if (!classConstraintsForMessages.isEmpty()) {
				dtoMsg.put("classConstraints", classConstraintsForMessages);
			}
			dtosForMessages.add(dtoMsg);
		}
		String messagesBody = tpl.render(AppConstants.TPL_VALIDATION_MESSAGES, Map.of("dtos", dtosForMessages));
		Path resDir = root.resolve("src/main/resources");
		Files.createDirectories(resDir);
		Files.writeString(resDir.resolve("messages.properties"), messagesBody, StandardCharsets.UTF_8);

		Map<String, Object> output = Map.of("status", "Success");
		return StepResult.ok(output);
	}

	private static boolean hasNonEmpty(Object raw) {
		if (raw == null) return false;
		if (raw instanceof List) return !((List<?>) raw).isEmpty();
		if (raw instanceof Map)  return !((Map<?, ?>) raw).isEmpty();
		return false;
	}

	private void ensureCrossFieldValidationHelpers(Path root, String basePkg) {
		try {
			Path baseDir = root.resolve("src/main/java/" + basePkg.replace('.', '/') + "/validation");
			Files.createDirectories(baseDir);

			Map<String, String> files = Map.of(
				"FieldMatch.java",                   AppConstants.TPL_VALIDATION_FIELD_MATCH,
				"FieldMatchValidator.java",          AppConstants.TPL_VALIDATION_FIELD_MATCH_VALIDATOR,
				"ConditionalRequired.java",          AppConstants.TPL_VALIDATION_CONDITIONAL_REQUIRED,
				"ConditionalRequiredValidator.java", AppConstants.TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR
			);

			for (Map.Entry<String, String> e : files.entrySet()) {
				Path target = baseDir.resolve(e.getKey());
				if (Files.exists(target)) continue;

				String templatePath = e.getValue();
				if (templatePath == null || templatePath.isBlank()) {
					continue;
				}

				String body;
				try {
					body = tpl.render(templatePath, Map.of("basePkg", basePkg));
				} catch (Exception renderErr) {
					continue;
				}
				Files.writeString(target, body, StandardCharsets.UTF_8);
			}
		} catch (Exception ignored) {
			// Intentionally non-fatal. Optionally log.
		}
	}

	private List<Map<String, Object>> normalizeClassConstraints(Object raw) {
		List<Map<String, Object>> out = new ArrayList<>();
		if (raw == null) return out;

		if (raw instanceof List) {
			for (Object n : (List<?>) raw) {
				if (n instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> m = new HashMap<>((Map<String, Object>) n);
					copyMessageKey(n, m); // puts 'key' if messageKey exists
					if (!m.containsKey("kind") && m.containsKey("type")) {
						m.put("kind", m.get("type"));
					}
					if (m.get("kind") != null) out.add(m);
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

	private String toClassLevelAnnotation(Map<String, Object> c) {
		String kind = normalizeKind(str(c.get("kind")));
		String key  = str(c.get("key")); // optional

		switch (kind) {
			case "fieldmatch": {
				String first  = str(c.get("first"));
				String second = str(c.get("second"));
				if (first == null || first.isBlank() || second == null || second.isBlank()) return null;
				String msg = (key == null || key.isBlank()) ? "" : ", message=\"{" + key + "}\"";
				return "@com.src.main.validation.FieldMatch(first=\"" + escapeJava(first) + "\", second=\"" + escapeJava(second) + "\"" + msg + ")";
			}
			case "conditionalrequired": {
				String field     = str(c.get("field"));
				String dependsOn = str(c.get("dependsOn"));
				String eq        = str(c.get("equals"));
				if (field == null || field.isBlank() || dependsOn == null || dependsOn.isBlank()) return null;
				String eqPart = (eq == null || eq.isBlank()) ? "" : ", equals=\"" + escapeJava(eq) + "\"";
				String msg = (key == null || key.isBlank()) ? "" : ", message=\"{" + key + "}\"";
				return "@com.src.main.validation.ConditionalRequired(field=\"" + escapeJava(field) + "\", dependsOn=\"" + escapeJava(dependsOn) + "\"" + eqPart + msg + ")";
			}
			case "scriptassert": {
				String lang   = str(c.getOrDefault("lang", "javascript"));
				String script = str(c.get("script"));
				if (script == null || script.isBlank()) return null;
				String msg = (key == null || key.isBlank()) ? "" : ", message=\"{" + key + "}\"";
				return "@org.hibernate.validator.constraints.ScriptAssert(lang=\"" + escapeJava(lang) + "\", script=\"" + escapeJava(script) + "\"" + msg + ")";
			}
			default:
				return null;
		}
	}

	private static String defaultClassMessage(Map<String, Object> c) {
		String kind = normalizeKind(String.valueOf(c.get("kind")));
		if ("fieldmatch".equals(kind)) return "Fields must match";
		if ("conditionalrequired".equals(kind)) return "Field is required based on other field";
		if ("scriptassert".equals(kind)) return "Class-level rule violated";
		return "Invalid object";
	}

	private static String normalizeKind(String raw) {
		if (raw == null) return "";
		return raw.trim().toLowerCase().replaceAll("[-_]", "");
	}

	private List<Map<String, Object>> normalizeConstraints(Object raw, String fieldName) {
		List<Map<String, Object>> out = new ArrayList<>();
		if (raw == null) return out;

		if (raw instanceof List) {
			for (Object n : (List<?>) raw) {
				if (n instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> m = new HashMap<>((Map<String, Object>) n);
					copyMessageKey(n, m);
					if (m.get("kind") != null && m.get("key") != null) out.add(m);
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
					if (isTruthy(spec.get("value")) || hasParameters(spec)) {
						if (spec.get("key") != null) out.add(spec);
					}
				}
			});
		}
		return out;
	}

	private static boolean isTruthy(Object v) {
		if (v == null) return false;
		if (v instanceof Boolean) return (Boolean) v;
		if (v instanceof Number) return ((Number) v).intValue() != 0;
		return Boolean.parseBoolean(v.toString());
	}

	private static boolean hasParameters(Map<String, Object> spec) {
		return spec.containsKey("min") || spec.containsKey("max") || spec.containsKey("regex")
				|| spec.containsKey("value") || spec.containsKey("integer") || spec.containsKey("fraction")
				|| spec.containsKey("inclusive");
	}

	private static void copyMessageKey(Object src, Map<String, Object> dst) {
		if (src instanceof Map) {
			Object mk = ((Map<?, ?>) src).get("messageKey");
			if (mk != null) dst.put("key", mk.toString());
		}
	}

	private static String defaultMessage(String kind, String field) {
		switch (kind) {
			case "NotNull":  return field + " must not be null";
			case "NotBlank": return field + " must not be blank";
			case "Email":    return field + " must be a valid email";
			case "Size":     return field + " has invalid size";
			case "Pattern":  return field + " has invalid format";
			case "Min":      return field + " is below minimum";
			case "Max":      return field + " is above maximum";
			default:         return "Invalid value for " + field;
		}
	}

	private static String toValidationAnnotation(String kind, Map<String, Object> c, String key, String fieldName) {
		if (key == null || key.isBlank()) return null;

		String lower = kind.trim().toLowerCase();

		switch (lower) {
			case "notnull":
			case "not_null":
			case "not-null":
				return String.format("@jakarta.validation.constraints.NotNull(message=\"{%s}\")", key);
			case "notblank":
			case "not_blank":
			case "not-blank":
				return String.format("@jakarta.validation.constraints.NotBlank(message=\"{%s}\")", key);
			case "notempty":
			case "not_empty":
			case "not-empty":
				return String.format("@jakarta.validation.constraints.NotEmpty(message=\"{%s}\")", key);
			case "null":
			case "isnull":
			case "is_null":
				return String.format("@jakarta.validation.constraints.Null(message=\"{%s}\")", key);
			case "size": {
				String min = optNum(c, "min");
				String max = optNum(c, "max");
				StringBuilder b = new StringBuilder("@jakarta.validation.constraints.Size(");
				boolean first = true;
				if (min != null) { b.append("min=").append(min); first = false; }
				if (max != null) { if (!first) b.append(", "); b.append("max=").append(max); }
				b.append(", message=\"{").append(key).append("}\")");
				return b.toString();
			}
			case "length":
				return toValidationAnnotation(kind, c, key, fieldName);
			case "pattern": {
				Object r = c.getOrDefault("regex", c.get("pattern"));
				if (r == null) return null;
				String regex = escapeJavaRegex(r.toString());
				return String.format("@jakarta.validation.constraints.Pattern(regexp=\"%s\", message=\"{%s}\")", regex, key);
			}
			case "email":
				return String.format("@jakarta.validation.constraints.Email(message=\"{%s}\")", key);
			case "min": {
				String v = optNum(c, "value");
				if (v == null) return null;
				return String.format("@jakarta.validation.constraints.Min(value=%s, message=\"{%s}\")", v, key);
			}
			case "max": {
				String v = optNum(c, "value");
				if (v == null) return null;
				return String.format("@jakarta.validation.constraints.Max(value=%s, message=\"{%s}\")", v, key);
			}
			case "decimalmin":
			case "decimal_min": {
				String v = Objects.toString(c.get("value"), null);
				if (v == null) return null;
				boolean inclusive = Boolean.parseBoolean(Objects.toString(c.getOrDefault("inclusive", "true")));
				return String.format("@jakarta.validation.constraints.DecimalMin(value=\"%s\", inclusive=%s, message=\"{%s}\")", v, inclusive, key);
			}
			case "decimalmax":
			case "decimal_max": {
				String v = Objects.toString(c.get("value"), null);
				if (v == null) return null;
				boolean inclusive = Boolean.parseBoolean(Objects.toString(c.getOrDefault("inclusive", "true")));
				return String.format("@jakarta.validation.constraints.DecimalMax(value=\"%s\", inclusive=%s, message=\"{%s}\")", v, inclusive, key);
			}
			case "digits": {
				String integer = optNum(c, "integer");
				String fraction = optNum(c, "fraction");
				if (integer == null) integer = "0";
				if (fraction == null) fraction = "0";
				return String.format("@jakarta.validation.constraints.Digits(integer=%s, fraction=%s, message=\"{%s}\")", integer, fraction, key);
			}
			case "positive":
				return String.format("@jakarta.validation.constraints.Positive(message=\"{%s}\")", key);
			case "positiveorzero":
			case "positive_or_zero":
				return String.format("@jakarta.validation.constraints.PositiveOrZero(message=\"{%s}\")", key);
			case "negative":
				return String.format("@jakarta.validation.constraints.Negative(message=\"{%s}\")", key);
			case "negativeorzero":
			case "negative_or_zero":
				return String.format("@jakarta.validation.constraints.NegativeOrZero(message=\"{%s}\")", key);
			case "past":
				return String.format("@jakarta.validation.constraints.Past(message=\"{%s}\")", key);
			case "pastorpresent":
			case "past_or_present":
				return String.format("@jakarta.validation.constraints.PastOrPresent(message=\"{%s}\")", key);
			case "future":
				return String.format("@jakarta.validation.constraints.Future(message=\"{%s}\")", key);
			case "futureorpresent":
			case "future_or_present":
				return String.format("@jakarta.validation.constraints.FutureOrPresent(message=\"{%s}\")", key);
			case "asserttrue":
			case "assert_true":
				return String.format("@jakarta.validation.constraints.AssertTrue(message=\"{%s}\")", key);
			case "assertfalse":
			case "assert_false":
				return String.format("@jakarta.validation.constraints.AssertFalse(message=\"{%s}\")", key);
			default:
				return null;
		}
	}

	private static String mapType(String t) {
		if (t == null) return "String";
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
			case "String":
			case "Text":    return "String";
			case "Boolean": return "Boolean";
			case "Int":     return "Integer";
			case "Long":    return "Long";
			case "Double":  return "Double";
			case "Decimal": return "java.math.BigDecimal";
			case "UUID":    return "java.util.UUID";
			case "Date":    return "java.time.LocalDate";
			case "Time":    return "java.time.LocalTime";
			case "DateTime":return "java.time.LocalDateTime";
			case "Instant": return "java.time.Instant";
			case "Json":    return "java.util.Map<String,Object>";
			case "Binary":  return "org.springframework.web.multipart.MultipartFile";
			default:        return t;
		}
	}

	private static String optNum(Map<String, Object> c, String key) {
		Object v = c.get(key);
		if (v == null) return null;
		String s = v.toString();
		if (s.matches("^-?\\d+(?:\\.\\d+)?$")) return s;
		return null;
	}

	private static String toMethodName(String field) {
		if (field == null || field.isEmpty()) return field;
		return Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}

	private static String escapeJavaRegex(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static boolean isNested(String type) {
		if (type == null) return false;
		type = type.trim();
		if (type.startsWith("List<") && type.endsWith(">")) return true;
		char c = type.charAt(0);
		if (Character.isUpperCase(c)) {
			if ("String Integer Long Boolean Double BigDecimal UUID Instant Date Time DateTime Json Binary".contains(type)) {
				return false;
			}
			return true;
		}
		return false;
	}

	private static String escapeJava(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static void collectImportFromAnnotation(String annotation, Set<String> imports) {
		if (annotation == null) return;
		int at = annotation.indexOf('@');
		if (at < 0) return;
		int start = at + 1;
		int end = start;
		while (end < annotation.length()) {
			char ch = annotation.charAt(end);
			if (ch == '(' || Character.isWhitespace(ch)) break;
			end++;
		}
		if (end <= start) return;
		String name = annotation.substring(start, end).trim();
		if (name.contains(".")) {
			// do not import same-package class annotations (none expected here), but keep rule generic
			imports.add(name);
		}
	}

	private static List<String> simplifyAnnotations(List<String> annotations, Set<String> imports) {
		List<String> out = new ArrayList<>(annotations.size());
		for (String a : annotations) {
			if (a == null || a.isBlank()) {
				out.add(a);
				continue;
			}
			int at = a.indexOf('@');
			if (at < 0) { out.add(a); continue; }

			int start = at + 1;
			int end = start;
			while (end < a.length()) {
				char ch = a.charAt(end);
				if (ch == '(' || Character.isWhitespace(ch)) break;
				end++;
			}
			if (end <= start) { out.add(a); continue; }

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

	private static String injectImportsAfterPackage(String code, Set<String> imports, String basePkg) {
		if (imports == null || imports.isEmpty()) return code;

		// De-duplicate & sort each group
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
		if (importBlock.length() > 0 && (!lombok.isEmpty() || !thirdParty.isEmpty() || !project.isEmpty())) importBlock.append('\n');
		appendImports(importBlock, lombok);
		if (!lombok.isEmpty() && (!thirdParty.isEmpty() || !project.isEmpty())) importBlock.append('\n');
		appendImports(importBlock, thirdParty);
		if (!thirdParty.isEmpty() && !project.isEmpty()) importBlock.append('\n');
		appendImports(importBlock, project);

		if (importBlock.length() == 0) return code;
		int pkgIdx = code.indexOf("package ");
		if (pkgIdx < 0) {
			return importBlock.append('\n').append(code).toString();
		}
		int semi = code.indexOf(';', pkgIdx);
		if (semi < 0) {
			return importBlock.append('\n').append(code).toString();
		}
		int lineEnd = code.indexOf('\n', semi);
		if (lineEnd < 0) lineEnd = semi + 1;

		String prefix = code.substring(0, lineEnd + 1);
		String suffix = code.substring(lineEnd + 1);
		return prefix + "\n" + importBlock + "\n" + suffix;
	}

	private static void appendImports(StringBuilder sb, List<String> fqcns) {
		for (String fq : fqcns) {
			sb.append("import ").append(fq).append(";\n");
		}
	}
}
