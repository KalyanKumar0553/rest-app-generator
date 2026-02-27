package com.src.main.sm.executor.mapper;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Service
public class MapperGenerationService {

	private static final String TEMPLATE_JAVA = "mapper.java.mustache";
	private static final String TEMPLATE_KOTLIN = "mapper.kt.mustache";

	private final TemplateEngine templateEngine;

	public MapperGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public int generate(Path root, List<MapperGenerationUnit> units, GenerationLanguage language) throws Exception {
		if (units == null || units.isEmpty()) {
			return 0;
		}
		int generated = 0;
		for (MapperGenerationUnit unit : units) {
			Set<String> imports = new LinkedHashSet<>();
			boolean sameSimpleTypes = unit.sourceSimpleName().equals(unit.targetSimpleName());
			if (!sameSimpleTypes) {
				imports.add(unit.sourceFqcn());
				imports.add(unit.targetFqcn());
			}
			String sourceTypeName = sameSimpleTypes ? unit.sourceFqcn() : unit.sourceSimpleName();
			String targetTypeName = sameSimpleTypes ? unit.targetFqcn() : unit.targetSimpleName();

			Map<String, Object> model = new LinkedHashMap<>();
			model.put("packageName", unit.packageName());
			model.put("className", unit.className());
			model.put("sourceSimpleName", unit.sourceSimpleName());
			model.put("targetSimpleName", unit.targetSimpleName());
			model.put("sourceTypeName", sourceTypeName);
			model.put("targetTypeName", targetTypeName);
			model.put("sourceClassLiteral", sourceTypeName + ".class");
			model.put("targetClassLiteral", targetTypeName + ".class");
			model.put("imports", imports);
			model.put("forwardMappings", unit.forwardMappings());
			model.put("reverseMappings", unit.reverseMappings());

			String template = language == GenerationLanguage.KOTLIN ? TEMPLATE_KOTLIN : TEMPLATE_JAVA;
			String code = templateEngine.renderAny(TemplatePathResolver.candidates(language, "mapper", template), model);
			Path outDir = root.resolve(PathUtils.srcPathFromPackage(unit.packageName(), language));
			Files.createDirectories(outDir);
			Files.writeString(outDir.resolve(unit.className() + "." + language.fileExtension()), code, UTF_8);
			generated += 1;
		}
		return generated;
	}
}
