package com.src.main.sm.executor.enumgen;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

@Service
public class EnumGenerationService {

	private static final String TEMPLATE = "templates/enum/enum.java.mustache";

	private final TemplateEngine templateEngine;

	public EnumGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path root, String enumPackage, List<EnumSpecResolved> enums) throws Exception {
		if (enums == null || enums.isEmpty()) {
			return;
		}

		Path outDir = root.resolve(PathUtils.javaSrcPathFromPackage(enumPackage));
		Files.createDirectories(outDir);

		for (EnumSpecResolved enumSpec : enums) {
			Map<String, Object> model = new LinkedHashMap<>();
			model.put("packageName", enumPackage);
			model.put("enumName", enumSpec.name());
			model.put("constants", withCommaMetadata(enumSpec.constants()));
			String code = templateEngine.render(TEMPLATE, model);
			Files.writeString(outDir.resolve(enumSpec.name() + ".java"), code, UTF_8);
		}
	}

	private List<Map<String, Object>> withCommaMetadata(List<String> constants) {
		java.util.ArrayList<Map<String, Object>> items = new java.util.ArrayList<>();
		if (constants == null) {
			return items;
		}
		for (int i = 0; i < constants.size(); i++) {
			String value = constants.get(i);
			if (value == null || value.isBlank()) {
				continue;
			}
			java.util.LinkedHashMap<String, Object> item = new java.util.LinkedHashMap<>();
			item.put("value", value);
			item.put("last", i == constants.size() - 1);
			items.add(item);
		}
		return items;
	}
}
