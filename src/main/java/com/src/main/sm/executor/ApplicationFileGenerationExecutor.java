package com.src.main.sm.executor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		Object defaultPropsObj = yaml.get("properties");
		if (defaultPropsObj instanceof Map) {
			Map<String, Object> propsObj = (Map<String, Object>)defaultPropsObj;
			propsObj.put("spring.messages.basename","messages");
			propsObj.put("spring.messages.encoding","UTF-8");
			propsObj.put("spring.messages.fallback-to-system-locale","false");
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
				Object propsObj = profileMap.get("properties");
				if (propsObj instanceof Map) {
					writePropertiesFile(root, "application-" + profile + ".properties", (Map<String, Object>) propsObj);
				}
			}
		}

		return StepResult.ok(Map.of("status", "Success"));
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

	private static void writeYamlFile(Path projectRoot, String fileName, Map<String, Object> props) throws IOException {
		Path resources = projectRoot.resolve("src/main/resources");
		Files.createDirectories(resources);
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
		Representer representer = new Representer(options);
		representer.getPropertyUtils().setSkipMissingProperties(true);

		Yaml yaml = new Yaml(representer, options);
		Path yamlPath = resources.resolve(fileName);
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
