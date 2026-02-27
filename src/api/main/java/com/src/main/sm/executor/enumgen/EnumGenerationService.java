package com.src.main.sm.executor.enumgen;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Service
public class EnumGenerationService {

	private static final String TEMPLATE_JAVA = "enum.java.mustache";
	private static final String TEMPLATE_KOTLIN = "enum.kt.mustache";

	private final TemplateEngine templateEngine;

	public EnumGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path root, String enumPackage, List<EnumSpecResolved> enums, GenerationLanguage language) throws Exception {
		if (enums == null || enums.isEmpty()) {
			return;
		}

		Path outDir = root.resolve(PathUtils.srcPathFromPackage(enumPackage, language));
		Files.createDirectories(outDir);

		try {
			enums.forEach(enumSpec -> {
				try {
					writeEnumFile(outDir, enumPackage, enumSpec, language);
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

	private void writeEnumFile(Path outDir, String enumPackage, EnumSpecResolved enumSpec, GenerationLanguage language) throws Exception {
		String template = language == GenerationLanguage.KOTLIN ? TEMPLATE_KOTLIN : TEMPLATE_JAVA;
		String code = templateEngine.renderAny(TemplatePathResolver.candidates(language, "enum", template),
				buildTemplateModel(enumPackage, enumSpec));
		Files.writeString(outDir.resolve(enumSpec.name() + "." + language.fileExtension()), code, UTF_8);
	}

	private Map<String, Object> buildTemplateModel(String enumPackage, EnumSpecResolved enumSpec) {
		Map<String, Object> model = new LinkedHashMap<>();
		model.put("packageName", enumPackage);
		model.put("enumName", enumSpec.name());
		model.put("constants", withCommaMetadata(enumSpec.constants()));
		return model;
	}

	private List<Map<String, Object>> withCommaMetadata(List<String> constants) {
		ArrayList<Map<String, Object>> items = new ArrayList<>();
		if (constants == null) {
			return items;
		}
		return IntStream.range(0, constants.size())
				.mapToObj(i -> {
					String value = constants.get(i);
					if (value == null || value.isBlank()) {
						return null;
					}
					LinkedHashMap<String, Object> item = new LinkedHashMap<>();
					item.put("value", value);
					item.put("last", i == constants.size() - 1);
					return item;
				})
				.filter(java.util.Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
