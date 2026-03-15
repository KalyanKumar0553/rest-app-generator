package com.src.main.sm.executor.node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class NodeValidationSupport {

	private NodeValidationSupport() {
	}

	static String renderEnumSchema(NodeEnumDefinition enumDefinition) {
		String objectFields = enumDefinition.constants().stream()
				.map(constant -> "  " + constant + ": '" + escapeTs(constant) + "'")
				.reduce((left, right) -> left + ",\n" + right)
				.orElse("");
		return """
				import { z } from 'zod';
				
				export const %1$s = {
				%2$s
				} as const;
				
				export const %1$sSchema = z.nativeEnum(%1$s);
				export type %1$s = z.infer<typeof %1$sSchema>;
				""".formatted(enumDefinition.name(), indent(objectFields, 2));
	}

	static String renderDtoSchema(NodeProjectContext context, NodeDtoDefinition dtoDefinition) {
		String currentPath = "validation/dto/" + ("response".equals(dtoDefinition.dtoType()) ? "response" : "request");
		SchemaFile schemaFile = buildObjectSchema(context, dtoDefinition.name(), dtoDefinition.fields(), dtoDefinition.classConstraints(), currentPath);
		return schemaFile.render();
	}

	static String renderModelSchema(NodeProjectContext context, NodeModelDefinition modelDefinition) {
		SchemaFile schemaFile = buildObjectSchema(context, modelDefinition.name(), modelDefinition.fields(), List.of(), "validation/models");
		StringBuilder body = new StringBuilder(schemaFile.render());
		body.append("\n");
		body.append("export const Create").append(modelDefinition.name()).append("InputSchema = ")
				.append(modelDefinition.name()).append("Schema.omit({ id: true });\n");
		body.append("export const Update").append(modelDefinition.name()).append("InputSchema = Create")
				.append(modelDefinition.name()).append("InputSchema.partial();\n");
		body.append("export type ").append(modelDefinition.name()).append(" = z.infer<typeof ")
				.append(modelDefinition.name()).append("Schema>;\n");
		body.append("export type Create").append(modelDefinition.name()).append("Input = z.infer<typeof Create")
				.append(modelDefinition.name()).append("InputSchema>;\n");
		body.append("export type Update").append(modelDefinition.name()).append("Input = z.infer<typeof Update")
				.append(modelDefinition.name()).append("InputSchema>;\n");
		return body.toString();
	}

	static String dtoSchemaImportPath(NodeProjectContext context, String currentPath, String dtoName) {
		NodeDtoDefinition dtoDefinition = findDto(context, dtoName);
		if (dtoDefinition == null) {
			return "";
		}
		String targetPath = "validation/dto/" + ("response".equals(dtoDefinition.dtoType()) ? "response" : "request") + "/" + dtoDefinition.name() + ".schema";
		return relativeImport(currentPath, targetPath);
	}

	static boolean hasDto(NodeProjectContext context, String dtoName) {
		return findDto(context, dtoName) != null;
	}

	private static NodeDtoDefinition findDto(NodeProjectContext context, String dtoName) {
		String normalized = normalizeTypeName(dtoName);
		if (normalized.isEmpty()) {
			return null;
		}
		return context.dtos().stream()
				.filter(dto -> dto.name().equalsIgnoreCase(normalized))
				.findFirst()
				.orElse(null);
	}

	private static SchemaFile buildObjectSchema(
			NodeProjectContext context,
			String schemaName,
			List<NodeFieldDefinition> fields,
			List<NodeConstraintDefinition> classConstraints,
			String currentPath) {
		Set<String> imports = new LinkedHashSet<>();
		imports.add("import { z } from 'zod';");
		List<String> fieldLines = new ArrayList<>();
		for (NodeFieldDefinition field : fields) {
			String schema = buildFieldSchema(context, field, currentPath, imports, schemaName);
			fieldLines.add("  " + field.name() + ": " + schema + ",");
		}
		StringBuilder expression = new StringBuilder();
		expression.append("z.object({\n");
		expression.append(String.join("\n", fieldLines));
		if (!fieldLines.isEmpty()) {
			expression.append("\n");
		}
		expression.append("}).strict()");
		for (NodeConstraintDefinition constraint : classConstraints) {
			String refine = buildClassRefinement(constraint);
			if (!refine.isBlank()) {
				expression.append(refine);
			}
		}
		return new SchemaFile(imports, "export const " + schemaName + "Schema = " + expression + ";\n\nexport type " + schemaName + " = z.infer<typeof " + schemaName + "Schema>;\n");
	}

	private static String buildFieldSchema(
			NodeProjectContext context,
			NodeFieldDefinition field,
			String currentPath,
			Set<String> imports,
			String currentSchemaName) {
		String schema = baseSchemaExpression(context, field.rawType(), currentPath, imports, currentSchemaName);
		String normalizedType = normalizeScalarType(field.rawType());
		boolean arrayType = isListType(field.rawType());
		for (NodeConstraintDefinition constraint : field.constraints()) {
			schema = applyConstraint(schema, constraint, normalizedType, arrayType);
		}
		if (field.optional()) {
			schema += ".optional()";
		}
		return schema;
	}

	private static String applyConstraint(String schema, NodeConstraintDefinition constraint, String normalizedType, boolean arrayType) {
		String kind = normalizeKind(constraint.kind());
		String message = escapeTs(constraint.message());
		Map<String, Object> params = constraint.params();
		switch (kind) {
		case "notblank":
			if (isStringType(normalizedType)) {
				return schema + ".trim().min(1, { message: '" + message + "' })";
			}
			return schema;
		case "notempty":
			if (isStringType(normalizedType) || arrayType) {
				return schema + ".min(1, { message: '" + message + "' })";
			}
			return schema;
		case "size": {
			String updated = schema;
			Object min = params.get("min");
			Object max = params.get("max");
			if ((isStringType(normalizedType) || arrayType) && min instanceof Number minValue) {
				updated += ".min(" + minValue + ", { message: '" + message + "' })";
			}
			if ((isStringType(normalizedType) || arrayType) && max instanceof Number maxValue) {
				updated += ".max(" + maxValue + ", { message: '" + message + "' })";
			}
			return updated;
		}
		case "pattern": {
			Object regex = firstNonNull(params.get("regex"), firstNonNull(params.get("regexp"), params.get("pattern")));
			if (isStringType(normalizedType) && regex != null) {
				return schema + ".regex(new RegExp('" + escapeTs(String.valueOf(regex)) + "'), { message: '" + message + "' })";
			}
			return schema;
		}
		case "email":
			return isStringType(normalizedType) ? schema + ".email({ message: '" + message + "' })" : schema;
		case "min":
			if (isNumberType(normalizedType) && params.get("value") instanceof Number minValue) {
				return schema + ".min(" + minValue + ", { message: '" + message + "' })";
			}
			return schema;
		case "max":
			if (isNumberType(normalizedType) && params.get("value") instanceof Number maxValue) {
				return schema + ".max(" + maxValue + ", { message: '" + message + "' })";
			}
			return schema;
		case "decimalmin":
			return decimalBoundary(schema, params.get("value"), params.get("inclusive"), true, message, normalizedType);
		case "decimalmax":
			return decimalBoundary(schema, params.get("value"), params.get("inclusive"), false, message, normalizedType);
		case "digits":
			if (isNumberType(normalizedType)) {
				Number integer = params.get("integer") instanceof Number value ? value : Integer.valueOf(0);
				Number fraction = params.get("fraction") instanceof Number value ? value : Integer.valueOf(0);
				String regex = "^-?\\\\d{1," + integer.intValue() + "}(?:\\\\.\\\\d{1," + fraction.intValue() + "})?$";
				return schema + ".refine((value) => new RegExp('" + regex + "').test(String(value)), { message: '" + message + "' })";
			}
			return schema;
		case "positive":
			return isNumberType(normalizedType) ? schema + ".gt(0, { message: '" + message + "' })" : schema;
		case "positiveorzero":
			return isNumberType(normalizedType) ? schema + ".gte(0, { message: '" + message + "' })" : schema;
		case "negative":
			return isNumberType(normalizedType) ? schema + ".lt(0, { message: '" + message + "' })" : schema;
		case "negativeorzero":
			return isNumberType(normalizedType) ? schema + ".lte(0, { message: '" + message + "' })" : schema;
		case "past":
			return isDateLikeType(normalizedType) ? schema + dateRefinement("<", message) : schema;
		case "pastorpresent":
			return isDateLikeType(normalizedType) ? schema + dateRefinement("<=", message) : schema;
		case "future":
			return isDateLikeType(normalizedType) ? schema + dateRefinement(">", message) : schema;
		case "futureorpresent":
			return isDateLikeType(normalizedType) ? schema + dateRefinement(">=", message) : schema;
		case "asserttrue":
			return "boolean".equals(normalizedType) ? schema + ".refine((value) => value === true, { message: '" + message + "' })" : schema;
		case "assertfalse":
			return "boolean".equals(normalizedType) ? schema + ".refine((value) => value === false, { message: '" + message + "' })" : schema;
		default:
			return schema;
		}
	}

	private static String buildClassRefinement(NodeConstraintDefinition constraint) {
		String kind = normalizeKind(constraint.kind());
		String message = escapeTs(constraint.message());
		Map<String, Object> params = constraint.params();
		switch (kind) {
		case "fieldmatch": {
			String first = stringValue(params.get("first"));
			String second = stringValue(params.get("second"));
			if (first.isBlank() || second.isBlank()) {
				return "";
			}
			return "\n  .superRefine((data, ctx) => {\n"
					+ "    if (data['" + escapeTs(first) + "'] !== data['" + escapeTs(second) + "']) {\n"
					+ "      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['" + escapeTs(second) + "'], message: '" + message + "' });\n"
					+ "    }\n"
					+ "  })";
		}
		case "conditionalrequired": {
			String field = stringValue(params.get("field"));
			String dependsOn = stringValue(params.get("dependsOn"));
			String equalsValue = stringValue(params.get("equals"));
			if (field.isBlank() || dependsOn.isBlank()) {
				return "";
			}
			String condition = equalsValue.isBlank()
					? "Boolean(data['" + escapeTs(dependsOn) + "'])"
					: "data['" + escapeTs(dependsOn) + "'] === '" + escapeTs(equalsValue) + "'";
			return "\n  .superRefine((data, ctx) => {\n"
					+ "    const value = data['" + escapeTs(field) + "'];\n"
					+ "    if (" + condition + " && (value === undefined || value === null || (typeof value === 'string' && value.trim().length === 0))) {\n"
					+ "      ctx.addIssue({ code: z.ZodIssueCode.custom, path: ['" + escapeTs(field) + "'], message: '" + message + "' });\n"
					+ "    }\n"
					+ "  })";
		}
		case "scriptassert": {
			String script = stringValue(params.get("script"));
			if (script.isBlank()) {
				return "";
			}
			return "\n  .superRefine((data, ctx) => {\n"
					+ "    try {\n"
					+ "      const validator = new Function('data', `return Boolean(" + escapeTemplateLiteral(script) + ")`);\n"
					+ "      if (!validator(data)) {\n"
					+ "        ctx.addIssue({ code: z.ZodIssueCode.custom, message: '" + message + "' });\n"
					+ "      }\n"
					+ "    } catch {\n"
					+ "      ctx.addIssue({ code: z.ZodIssueCode.custom, message: '" + message + "' });\n"
					+ "    }\n"
					+ "  })";
		}
		default:
			return "";
		}
	}

	private static String baseSchemaExpression(
			NodeProjectContext context,
			String rawType,
			String currentPath,
			Set<String> imports,
			String currentSchemaName) {
		if (isListType(rawType)) {
			return "z.array(" + baseSchemaExpression(context, extractInnerType(rawType), currentPath, imports, currentSchemaName) + ")";
		}
		String leafType = normalizeTypeName(rawType);
		String normalized = normalizeScalarType(rawType);
		switch (normalized) {
		case "string":
		case "uuid":
			return "uuid".equals(normalized) ? "z.string().uuid()" : "z.string()";
		case "date":
		case "localdate":
		case "localdatetime":
		case "offsetdatetime":
		case "instant":
			return "z.string()";
		case "number":
			return "z.number()";
		case "boolean":
			return "z.boolean()";
		default:
			if (hasEnum(context, leafType)) {
				imports.add("import { " + leafType + "Schema } from '" + relativeImport(currentPath, "validation/enums/" + leafType + ".schema") + "';");
				return leafType + "Schema";
			}
			NodeDtoDefinition dtoDefinition = findDto(context, leafType);
			if (dtoDefinition != null) {
				if (dtoDefinition.name().equalsIgnoreCase(currentSchemaName)) {
					return "z.lazy(() => " + dtoDefinition.name() + "Schema)";
				}
				imports.add("import { " + dtoDefinition.name() + "Schema } from '" + dtoSchemaImportPath(context, currentPath, dtoDefinition.name()) + "';");
				return "z.lazy(() => " + dtoDefinition.name() + "Schema)";
			}
			if (hasModel(context, leafType)) {
				imports.add("import { " + leafType + "Schema } from '" + relativeImport(currentPath, "validation/models/" + leafType + ".schema") + "';");
				return "z.lazy(() => " + leafType + "Schema)";
			}
			return "z.any()";
		}
	}

	private static boolean hasEnum(NodeProjectContext context, String typeName) {
		return context.enums().stream().anyMatch(item -> item.name().equalsIgnoreCase(typeName));
	}

	private static boolean hasModel(NodeProjectContext context, String typeName) {
		return context.models().stream().anyMatch(item -> item.name().equalsIgnoreCase(typeName));
	}

	private static String decimalBoundary(String schema, Object rawValue, Object inclusiveRaw, boolean minimum, String message, String normalizedType) {
		if (!isNumberType(normalizedType) || rawValue == null) {
			return schema;
		}
		BigDecimal boundary;
		try {
			boundary = new BigDecimal(String.valueOf(rawValue));
		} catch (NumberFormatException ex) {
			return schema;
		}
		boolean inclusive = inclusiveRaw == null || Boolean.parseBoolean(String.valueOf(inclusiveRaw));
		String operator = minimum ? (inclusive ? ">=" : ">") : (inclusive ? "<=" : "<");
		return schema + ".refine((value) => Number(value) " + operator + " " + boundary.toPlainString() + ", { message: '" + message + "' })";
	}

	private static String dateRefinement(String operator, String message) {
		return ".refine((value) => { const timestamp = Date.parse(value); return !Number.isNaN(timestamp) && timestamp "
				+ operator + " Date.now(); }, { message: '" + message + "' })";
	}

	private static boolean isListType(String rawType) {
		String value = stringValue(rawType);
		return value.startsWith("List<") && value.endsWith(">") || value.endsWith("[]");
	}

	private static String extractInnerType(String rawType) {
		String value = stringValue(rawType).trim();
		if (value.startsWith("List<") && value.endsWith(">")) {
			return value.substring(5, value.length() - 1).trim();
		}
		if (value.endsWith("[]")) {
			return value.substring(0, value.length() - 2).trim();
		}
		return value;
	}

	private static String normalizeScalarType(String rawType) {
		String value = normalizeTypeName(rawType).toLowerCase(Locale.ROOT);
		switch (value) {
		case "string":
			return "string";
		case "uuid":
			return "uuid";
		case "date":
		case "localdate":
		case "localdatetime":
		case "offsetdatetime":
		case "instant":
			return value;
		case "int":
		case "integer":
		case "long":
		case "double":
		case "float":
		case "bigdecimal":
		case "short":
		case "byte":
		case "decimal":
			return "number";
		case "boolean":
			return "boolean";
		default:
			return value;
		}
	}

	private static boolean isStringType(String normalizedType) {
		return Objects.equals(normalizedType, "string") || isDateLikeType(normalizedType);
	}

	private static boolean isNumberType(String normalizedType) {
		return Objects.equals(normalizedType, "number");
	}

	private static boolean isDateLikeType(String normalizedType) {
		return Set.of("date", "localdate", "localdatetime", "offsetdatetime", "instant").contains(normalizedType);
	}

	private static String normalizeTypeName(Object rawType) {
		String value = extractInnerType(stringValue(rawType));
		int lastDot = value.lastIndexOf('.');
		return lastDot >= 0 ? value.substring(lastDot + 1) : value;
	}

	private static String relativeImport(String fromDirectory, String targetPath) {
		List<String> fromParts = splitPath(fromDirectory);
		List<String> targetParts = splitPath(targetPath);
		int common = 0;
		while (common < fromParts.size() && common < targetParts.size() && fromParts.get(common).equals(targetParts.get(common))) {
			common++;
		}
		List<String> result = new ArrayList<>();
		for (int i = common; i < fromParts.size(); i++) {
			result.add("..");
		}
		result.addAll(targetParts.subList(common, targetParts.size()));
		if (!result.isEmpty()) {
			String last = result.get(result.size() - 1);
			if (last.endsWith(".ts")) {
				result.set(result.size() - 1, last.substring(0, last.length() - 3));
			}
		}
		String joined = String.join("/", result);
		return joined.startsWith(".") ? joined : "./" + joined;
	}

	private static List<String> splitPath(String value) {
		String normalized = stringValue(value).replace('\\', '/');
		if (normalized.isBlank()) {
			return List.of();
		}
		String[] parts = normalized.split("/");
		List<String> filtered = new ArrayList<>();
		for (String part : parts) {
			if (!part.isBlank()) {
				filtered.add(part);
			}
		}
		return filtered;
	}

	private static String normalizeKind(String raw) {
		return stringValue(raw).trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
	}

	private static Object firstNonNull(Object first, Object second) {
		return first != null ? first : second;
	}

	private static String stringValue(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	private static String escapeTs(String value) {
		return stringValue(value).replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
	}

	private static String escapeTemplateLiteral(String value) {
		return stringValue(value).replace("\\", "\\\\").replace("`", "\\`").replace("${", "\\${");
	}

	private static String indent(String value, int spaces) {
		String prefix = " ".repeat(Math.max(0, spaces));
		return value.lines().map(line -> prefix + line).reduce((left, right) -> left + "\n" + right).orElse("");
	}

	private record SchemaFile(Set<String> imports, String body) {
		String render() {
			List<String> sortedImports = imports.stream().sorted(Comparator.naturalOrder()).toList();
			return String.join("\n", sortedImports) + "\n\n" + body;
		}
	}
}
