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
		ensureDefaultApplicationProperties(propsObj);
		if (useYaml) {
			writeYamlFile(root, "application.yml", propsObj);
		} else {
			writePropertiesFile(root, "application.properties", propsObj);
		}
		Object profilesObj = yaml.get("profiles");
		if (profilesObj instanceof Map) {
			Map<String, Object> profiles = castMap(profilesObj);
			for (Map.Entry<String, Object> e : profiles.entrySet()) {
				String profile = e.getKey();
				Object node = e.getValue();
				if (!(node instanceof Map))
					continue;

				Map<String, Object> profileMap = castMap(node);
				Object profilePropsObj = profileMap.get("properties");
				if (profilePropsObj instanceof Map) {
					Map<String, Object> profileProps = castMap(profilePropsObj);
					if (useYaml) {
						writeYamlFile(root, "application-" + profile + ".yml", profileProps);
					} else {
						writePropertiesFile(root, "application-" + profile + ".properties", profileProps);
					}
				}
			}
		} else {
			for (String profile : extractProfileNames(profilesObj)) {
				Map<String, Object> profileProps = new LinkedHashMap<>();
				if (useYaml) {
					writeYamlFile(root, "application-" + profile + ".yml", profileProps);
				} else {
					writePropertiesFile(root, "application-" + profile + ".properties", profileProps);
				}
			}
		}

		return StepResult.ok(Map.of("status", "Success"));
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

		List<String> lines = new ArrayList<>(flat.size());
		for (Map.Entry<String, String> kv : flat.entrySet()) {
			lines.add(kv.getKey() + "=" + escapePropertiesValue(kv.getValue()));
		}
		Files.write(resources.resolve(fileName), lines, StandardCharsets.UTF_8);
	}

	private static void ensureDefaultApplicationProperties(Map<String, Object> propsObj) {
		propsObj.putIfAbsent("spring", new LinkedHashMap<String, Object>());
		Map<String, Object> spring = castMap(propsObj.get("spring"));
		propsObj.put("spring", spring);

		spring.putIfAbsent("messages", new LinkedHashMap<String, Object>());
		Map<String, Object> messages = castMap(spring.get("messages"));
		spring.put("messages", messages);

		messages.putIfAbsent("basename", "messages");
		messages.putIfAbsent("encoding", "UTF-8");
		messages.putIfAbsent("fallback-to-system-locale", "false");
	}

	@SuppressWarnings("unchecked")
	private static List<String> extractProfileNames(Object profilesObj) {
		if (!(profilesObj instanceof List<?> rawList)) {
			return List.of();
		}

		Set<String> unique = new LinkedHashSet<>();
		for (Object item : rawList) {
			String normalized = normalizeProfileName(item);
			if (normalized != null) {
				unique.add(normalized);
			}
		}
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
			for (Map.Entry<String, Object> e : m.entrySet()) {
				String key = e.getKey();
				String path = prefix.isEmpty() ? key : prefix + "." + key;
				flatten(path, e.getValue(), out);
			}
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
		StringBuilder sb = new StringBuilder();
		for (Object o : list) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append(o == null ? "" : String.valueOf(o));
		}
		return sb.toString();
	}

	private static String escapePropertiesValue(String v) {
		if (v == null)
			return "";
		StringBuilder sb = new StringBuilder(v.length() + 16);
		for (int i = 0; i < v.length(); i++) {
			char c = v.charAt(i);
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
		}
		return sb.toString();
	}
}
