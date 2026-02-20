package com.src.main.sm.executor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class ApplicationFileGenerationExecutor implements StepExecutor {

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) throws Exception {
		Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
		Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
		if (yaml == null) {
			return StepResult.error("YAML_NOT_FOUND", "YAML not found in ExtendedState");
		}
		String applicationFormat = resolveApplicationFormat(yaml);
		boolean useYaml = "yaml".equals(applicationFormat) || "yml".equals(applicationFormat);

		Object defaultPropsObj = yaml.get("properties");
		Map<String, Object> propsObj = defaultPropsObj instanceof Map ? castMap(defaultPropsObj) : new LinkedHashMap<>();
		boolean includeJpa = hasEntities(yaml);
		boolean includeMessageSettings = hasValidationMessages(yaml);
		String database = resolveDatabaseCode(yaml);
		ensureDefaultApplicationProperties(propsObj, includeJpa, includeMessageSettings, database);
		if (useYaml) {
			writeYamlFile(root, "application.yml", propsObj);
		} else {
			writePropertiesFile(root, "application.properties", propsObj);
		}
		Object profilesObj = yaml.get("profiles");
		if (profilesObj instanceof Map) {
			Map<String, Object> profiles = castMap(profilesObj);
			try {
				profiles.entrySet().forEach(e -> {
					String profile = e.getKey();
					Object node = e.getValue();
					if (!(node instanceof Map))
						return;

					Map<String, Object> profileMap = castMap(node);
					Object profilePropsObj = profileMap.get("properties");
					if (profilePropsObj instanceof Map) {
						Map<String, Object> profileProps = castMap(profilePropsObj);
						try {
							writeProfileConfig(root, profile, profileProps, useYaml);
						} catch (Exception ex) {
							throw new RuntimeException(ex);
						}
					}
				});
			} catch (RuntimeException ex) {
				if (ex.getCause() instanceof Exception cause) {
					throw cause;
				}
				throw ex;
			}
		} else {
			try {
				extractProfileNames(profilesObj).forEach(profile -> {
					Map<String, Object> profileProps = new LinkedHashMap<>();
					try {
						writeProfileConfig(root, profile, profileProps, useYaml);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				});
			} catch (RuntimeException ex) {
				if (ex.getCause() instanceof Exception cause) {
					throw cause;
				}
				throw ex;
			}
		}

		return StepResult.ok(Map.of("status", "Success"));
	}

	private static void writeProfileConfig(Path root, String profile, Map<String, Object> profileProps, boolean useYaml)
			throws Exception {
		if (useYaml) {
			writeYamlFile(root, "application-" + profile + ".yml", profileProps);
		} else {
			writePropertiesFile(root, "application-" + profile + ".properties", profileProps);
		}
	}

	@SuppressWarnings("unchecked")
	private static String resolveApplicationFormat(Map<String, Object> yaml) {
		if (yaml == null) {
			return "yaml";
		}
		Object raw = yaml.get("applFormat");
		if (raw == null && yaml.get("preferences") instanceof Map<?, ?> preferences) {
			raw = ((Map<String, Object>) preferences).get("applFormat");
		}
		if (raw == null && yaml.get("app") instanceof Map<?, ?> app) {
			raw = ((Map<String, Object>) app).get("applFormat");
		}

		String normalized = raw == null ? "" : String.valueOf(raw).trim().toLowerCase();
		if ("properties".equals(normalized) || "yaml".equals(normalized) || "yml".equals(normalized)) {
			return normalized;
		}
		return "yaml";
	}

	private static void writePropertiesFile(Path projectRoot, String fileName, Map<String, Object> props)
			throws Exception {
		Path resources = projectRoot.resolve("src/main/resources");
		Files.createDirectories(resources);

		LinkedHashMap<String, String> flat = new LinkedHashMap<>();
		flatten("", props, flat);

		List<String> lines = flat.entrySet().stream()
				.map(kv -> kv.getKey() + "=" + escapePropertiesValue(kv.getValue()))
				.collect(java.util.stream.Collectors.toList());
		Files.write(resources.resolve(fileName), lines, StandardCharsets.UTF_8);
	}

	private static void ensureDefaultApplicationProperties(Map<String, Object> propsObj, boolean includeJpa,
			boolean includeMessageSettings, String database) {
		propsObj.putIfAbsent("server", new LinkedHashMap<String, Object>());
		Map<String, Object> server = castMap(propsObj.get("server"));
		propsObj.put("server", server);
		server.putIfAbsent("port", 8080);

		boolean hadSpringSection = propsObj.get("spring") instanceof Map<?, ?>;
		Map<String, Object> spring = hadSpringSection ? castMap(propsObj.get("spring")) : null;
		if (spring != null) {
			propsObj.put("spring", spring);
		}
		boolean needsSpringSection = includeMessageSettings || includeJpa;
		if (spring == null && needsSpringSection) {
			spring = new LinkedHashMap<>();
			propsObj.put("spring", spring);
		}
		if (includeMessageSettings) {
			spring.putIfAbsent("messages", new LinkedHashMap<String, Object>());
			Map<String, Object> messages = castMap(spring.get("messages"));
			spring.put("messages", messages);
			messages.putIfAbsent("basename", "messages");
			messages.putIfAbsent("encoding", "UTF-8");
			messages.putIfAbsent("fallback-to-system-locale", false);
		}

		if (!includeJpa) {
			if (!hadSpringSection && spring != null && spring.isEmpty()) {
				propsObj.remove("spring");
			}
			return;
		}
		String db = database == null ? "POSTGRES" : database.trim().toUpperCase();
		if ("NONE".equals(db) || "OTHER".equals(db)) {
			return;
		}
		if ("MONGODB".equals(db)) {
			spring.putIfAbsent("data", new LinkedHashMap<String, Object>());
			Map<String, Object> data = castMap(spring.get("data"));
			spring.put("data", data);
			data.putIfAbsent("mongodb", new LinkedHashMap<String, Object>());
			Map<String, Object> mongodb = castMap(data.get("mongodb"));
			data.put("mongodb", mongodb);
			mongodb.putIfAbsent("uri", "${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/basicdb}");
			return;
		}
		spring.putIfAbsent("datasource", new LinkedHashMap<String, Object>());
		Map<String, Object> datasource = castMap(spring.get("datasource"));
		spring.put("datasource", datasource);

		switch (db) {
		case "MSSQL":
			datasource.putIfAbsent("url",
					"${SPRING_DATASOURCE_URL:jdbc:sqlserver://localhost:1433;databaseName=basicdb}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:sa}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:password}");
			datasource.putIfAbsent("driver-class-name", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
			break;
		case "MYSQL":
			datasource.putIfAbsent("url",
					"${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/basicdb?useSSL=false&serverTimezone=UTC}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:root}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:password}");
			datasource.putIfAbsent("driver-class-name", "com.mysql.cj.jdbc.Driver");
			break;
		case "MARIADB":
			datasource.putIfAbsent("url", "${SPRING_DATASOURCE_URL:jdbc:mariadb://localhost:3306/basicdb}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:root}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:password}");
			datasource.putIfAbsent("driver-class-name", "org.mariadb.jdbc.Driver");
			break;
		case "ORACLE":
			datasource.putIfAbsent("url", "${SPRING_DATASOURCE_URL:jdbc:oracle:thin:@localhost:1521:xe}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:system}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:oracle}");
			datasource.putIfAbsent("driver-class-name", "oracle.jdbc.OracleDriver");
			break;
		case "DERBY":
			datasource.putIfAbsent("url", "${SPRING_DATASOURCE_URL:jdbc:derby:memory:basicdb;create=true}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:app}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:app}");
			datasource.putIfAbsent("driver-class-name", "org.apache.derby.jdbc.EmbeddedDriver");
			break;
		case "H2":
			datasource.putIfAbsent("url",
					"${SPRING_DATASOURCE_URL:jdbc:h2:mem:basicdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:sa}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:}");
			datasource.putIfAbsent("driver-class-name", "org.h2.Driver");
			break;
		case "HSQL":
			datasource.putIfAbsent("url", "${SPRING_DATASOURCE_URL:jdbc:hsqldb:mem:basicdb}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:sa}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:}");
			datasource.putIfAbsent("driver-class-name", "org.hsqldb.jdbc.JDBCDriver");
			break;
		default:
			datasource.putIfAbsent("url", "${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/basicdb}");
			datasource.putIfAbsent("username", "${SPRING_DATASOURCE_USERNAME:postgres}");
			datasource.putIfAbsent("password", "${SPRING_DATASOURCE_PASSWORD:postgres}");
			datasource.putIfAbsent("driver-class-name", "org.postgresql.Driver");
			break;
		}

		spring.putIfAbsent("jpa", new LinkedHashMap<String, Object>());
		Map<String, Object> jpa = castMap(spring.get("jpa"));
		spring.put("jpa", jpa);
		jpa.putIfAbsent("properties", new LinkedHashMap<String, Object>());
		Map<String, Object> jpaProperties = castMap(jpa.get("properties"));
		jpa.put("properties", jpaProperties);
		jpaProperties.putIfAbsent("hibernate", new LinkedHashMap<String, Object>());
		Map<String, Object> hibernate = castMap(jpaProperties.get("hibernate"));
		jpaProperties.put("hibernate", hibernate);
		switch (db) {
		case "MSSQL":
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.SQLServerDialect");
			break;
		case "MYSQL":
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.MySQLDialect");
			break;
		case "MARIADB":
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.MariaDBDialect");
			break;
		case "ORACLE":
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.OracleDialect");
			break;
		case "DERBY":
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.DerbyDialect");
			break;
		case "H2":
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.H2Dialect");
			break;
		case "HSQL":
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.HSQLDialect");
			break;
		default:
			hibernate.putIfAbsent("dialect", "org.hibernate.dialect.PostgreSQLDialect");
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean hasEntities(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object modelsRaw = yaml.get("models");
		if (!(modelsRaw instanceof List<?> models)) {
			return false;
		}
		return models.stream().anyMatch(item -> item instanceof Map<?, ?>);
	}

	@SuppressWarnings("unchecked")
	private static String resolveDatabaseCode(Map<String, Object> yaml) {
		if (yaml == null) {
			return "POSTGRES";
		}
		Object db = yaml.get("database");
		if (db != null) {
			return String.valueOf(db);
		}
		if (yaml.get("app") instanceof Map<?, ?> appRaw) {
			Object appDb = ((Map<String, Object>) appRaw).get("database");
			if (appDb != null) {
				return String.valueOf(appDb);
			}
		}
		return "POSTGRES";
	}

	@SuppressWarnings("unchecked")
	private static boolean hasValidationMessages(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object messagesRaw = yaml.get("messages");
		if (!(messagesRaw instanceof Map<?, ?> messagesMap) || messagesMap.isEmpty()) {
			return false;
		}
		return messagesMap.keySet().stream()
				.filter(java.util.Objects::nonNull)
				.map(String::valueOf)
				.anyMatch(key -> key.startsWith("validation."));
	}

	@SuppressWarnings("unchecked")
	private static List<String> extractProfileNames(Object profilesObj) {
		if (!(profilesObj instanceof List<?> rawList)) {
			return List.of();
		}

		Set<String> unique = rawList.stream()
				.map(ApplicationFileGenerationExecutor::normalizeProfileName)
				.filter(java.util.Objects::nonNull)
				.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
		return new ArrayList<>(unique);
	}

	private static String normalizeProfileName(Object value) {
		if (value == null) {
			return null;
		}
		String profile = String.valueOf(value).trim().toLowerCase();
		if (profile.isBlank()) {
			return null;
		}
		if (!profile.matches("[a-z0-9._-]+")) {
			return null;
		}
		return profile;
	}

	private static void writeYamlFile(Path projectRoot, String fileName, Map<String, Object> props) throws IOException {
		Path resources = projectRoot.resolve("src/main/resources");
		Files.createDirectories(resources);
		Path yamlPath = resources.resolve(fileName);

		if (props == null || props.isEmpty()) {
			Files.writeString(yamlPath, "", StandardCharsets.UTF_8);
			return;
		}

		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
		Representer representer = new Representer(options);
		representer.getPropertyUtils().setSkipMissingProperties(true);

		Yaml yaml = new Yaml(representer, options);
		try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(yamlPath),
				StandardCharsets.UTF_8)) {
			yaml.dump(props, writer);
		}
	}

	@SuppressWarnings("unchecked")
	private static void flatten(String prefix, Object node, LinkedHashMap<String, String> out) {
		if (node == null)
			return;

		if (node instanceof Map) {
			Map<String, Object> m = castMap(node);
			m.forEach((key, value) -> {
				String path = prefix.isEmpty() ? key : prefix + "." + key;
				flatten(path, value, out);
			});
			return;
		}

		if (node instanceof List) {
			List<?> list = (List<?>) node;
			out.put(prefix, joinList(list));
			return;
		}
		out.put(prefix, String.valueOf(node));
	}
	  
	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object obj) {
		if (obj instanceof Map) {
			Map<String, Object> src = (Map<String, Object>) obj;
			if (src instanceof LinkedHashMap)
				return (Map<String, Object>) obj;
			return new LinkedHashMap<>(src);
		}
		throw new IllegalArgumentException("Expected Map but was: " + obj.getClass());
	}

	private static String joinList(List<?> list) {
		if (list == null) {
			list = Collections.emptyList();
		}
		if (list.isEmpty())
			return "";
		return list.stream().map(o -> o == null ? "" : String.valueOf(o)).collect(java.util.stream.Collectors.joining(","));
	}

	private static String escapePropertiesValue(String v) {
		if (v == null)
			return "";
		StringBuilder sb = new StringBuilder(v.length() + 16);
		v.chars().forEach(ch -> {
			char c = (char) ch;
			switch (c) {
			case '\\':
				sb.append("\\\\");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '=':
				sb.append("\\=");
				break;
			case ':':
				sb.append("\\:");
				break;
			default:
				sb.append(c);
			}
		});
		return sb.toString();
	}
}
