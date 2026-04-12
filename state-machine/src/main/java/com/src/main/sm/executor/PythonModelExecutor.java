package com.src.main.sm.executor;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.util.ProjectMetaDataConstants;

@Component("pythonModelExecutor")
public class PythonModelExecutor implements StepExecutor {

	private final TemplateEngine templateEngine;

	public PythonModelExecutor(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Object rootRaw = data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR);
			if (rootRaw == null) {
				return StepResult.error("PYTHON_MODEL_GENERATION", "Root directory not found in extended state.");
			}
			Path root = Path.of(String.valueOf(rootRaw));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			String orm = resolveOrm(yaml);
			Files.createDirectories(root.resolve("app/models"));
			for (Map<String, Object> modelSpec : extractModels(yaml)) {
				String name = toPascalCase(String.valueOf(modelSpec.getOrDefault("name", "Model")));
				if (name.isBlank()) {
					continue;
				}
				Map<String, Object> templateModel = new LinkedHashMap<>();
				templateModel.put("modelName", name);
				templateModel.put("tableName", toSnakeCase(name));
				templateModel.put("fields", extractFields(modelSpec));
				String templatePath = "django".equals(orm)
						? "templates/languages/python/model/django/model.py.mustache"
						: "templates/languages/python/model/sqlalchemy/model.py.mustache";
				write(root.resolve("app/models/" + toSnakeCase(name) + ".py"), templatePath, templateModel);
			}
			write(root.resolve("app/models/__init__.py"), "templates/languages/python/model/common/__init__.py.mustache", Map.of());
			write(root.resolve("app/db.py"),
					"django".equals(orm)
							? "templates/languages/python/model/django/db.py.mustache"
							: "templates/languages/python/model/sqlalchemy/db.py.mustache",
					Map.of());
			return StepResult.ok(Map.of("status", "Python models generated"));
		} catch (Exception ex) {
			return StepResult.error("PYTHON_MODEL_GENERATION", ex.getMessage());
		}
	}

	private void write(Path output, String templatePath, Map<String, Object> model) throws Exception {
		Files.createDirectories(output.getParent());
		Files.writeString(output, templateEngine.render(templatePath, model), UTF_8);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> extractModels(Map<String, Object> yaml) {
		if (yaml == null || !(yaml.get("models") instanceof List<?> items)) {
			return List.of();
		}
		List<Map<String, Object>> models = new ArrayList<>();
		for (Object item : items) {
			if (item instanceof Map<?, ?> rawMap) {
				models.add(new LinkedHashMap<>((Map<String, Object>) rawMap));
			}
		}
		return models;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> extractFields(Map<String, Object> modelSpec) {
		List<Map<String, Object>> fields = new ArrayList<>();
		fields.add(fieldModel("id", pythonType("uuid"), sqlalchemyType("uuid"), djangoType("uuid"), false, true));
		Object rawFields = modelSpec.get("fields");
		if (!(rawFields instanceof List<?> items)) {
			return fields;
		}
		for (Object item : items) {
			if (!(item instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Map<String, Object> fieldMap = (Map<String, Object>) rawMap;
			String name = toSnakeCase(String.valueOf(fieldMap.getOrDefault("name", "")));
			if (name.isBlank()) {
				continue;
			}
			String rawType = String.valueOf(fieldMap.getOrDefault("type", "String"));
			boolean required = isRequired(fieldMap.get("constraints"));
			fields.add(fieldModel(name, pythonType(rawType), sqlalchemyType(rawType), djangoType(rawType), !required, false));
		}
		return fields;
	}

	private Map<String, Object> fieldModel(String name, String pythonType, String sqlalchemyType, String djangoType, boolean optional, boolean primaryKey) {
		Map<String, Object> field = new LinkedHashMap<>();
		field.put("name", name);
		field.put("pythonType", pythonType);
		field.put("sqlalchemyType", sqlalchemyType);
		field.put("djangoType", djangoType);
		field.put("optional", optional);
		field.put("primaryKey", primaryKey);
		return field;
	}

	@SuppressWarnings("unchecked")
	private boolean isRequired(Object rawConstraints) {
		if (!(rawConstraints instanceof List<?> constraints)) {
			return false;
		}
		for (Object item : constraints) {
			if (item instanceof Map<?, ?> rawMap) {
				Object kind = ((Map<String, Object>) rawMap).get("kind");
				String normalized = kind == null ? "" : String.valueOf(kind).trim().toLowerCase(Locale.ROOT);
				if (normalized.equals("notnull") || normalized.equals("not_null") || normalized.equals("notblank") || normalized.equals("not_blank")) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private String resolveOrm(Map<String, Object> yaml) {
		if (yaml == null) {
			return "sqlalchemy";
		}
		Object pythonRaw = yaml.get("python");
		if (pythonRaw instanceof Map<?, ?> pythonMapRaw) {
			Object orm = ((Map<String, Object>) pythonMapRaw).get("orm");
			if (orm != null) {
				return normalizeOrm(String.valueOf(orm));
			}
		}
		Object orm = yaml.get("orm");
		return orm == null ? "sqlalchemy" : normalizeOrm(String.valueOf(orm));
	}

	private String normalizeOrm(String raw) {
		String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
		return "django".equals(normalized) ? "django" : "sqlalchemy";
	}

	private String toPascalCase(String raw) {
		String[] parts = raw.replace('-', ' ').replace('_', ' ').trim().split("\\s+");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isBlank()) {
				continue;
			}
			builder.append(Character.toUpperCase(part.charAt(0)));
			if (part.length() > 1) {
				builder.append(part.substring(1));
			}
		}
		return builder.toString();
	}

	private String toSnakeCase(String raw) {
		String normalized = raw == null ? "" : raw.trim();
		if (normalized.isBlank()) {
			return "";
		}
		return normalized
				.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
				.replace('-', '_')
				.replace(' ', '_')
				.toLowerCase(Locale.ROOT);
	}

	private String pythonType(String rawType) {
		String normalized = rawType == null ? "" : rawType.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
		case "int", "integer", "long", "short", "byte" -> "int";
		case "double", "float", "bigdecimal" -> "float";
		case "boolean" -> "bool";
		default -> "str";
		};
	}

	private String sqlalchemyType(String rawType) {
		String normalized = rawType == null ? "" : rawType.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
		case "uuid" -> "String(36)";
		case "int", "integer", "long", "short", "byte" -> "Integer";
		case "double", "float", "bigdecimal" -> "Float";
		case "boolean" -> "Boolean";
		case "localdate", "date" -> "Date";
		case "localdatetime", "offsetdatetime", "instant" -> "DateTime";
		default -> "String(255)";
		};
	}

	private String djangoType(String rawType) {
		String normalized = rawType == null ? "" : rawType.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
		case "uuid" -> "models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)";
		case "int", "integer", "long", "short", "byte" -> "models.IntegerField";
		case "double", "float", "bigdecimal" -> "models.FloatField";
		case "boolean" -> "models.BooleanField";
		case "localdate", "date" -> "models.DateField";
		case "localdatetime", "offsetdatetime", "instant" -> "models.DateTimeField";
		default -> "models.CharField(max_length=255)";
		};
	}
}
