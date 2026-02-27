package com.src.main.sm.executor.dto;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.dto.AppSpecDTO;
import com.src.main.sm.executor.common.BoilerplateStyle;
import com.src.main.sm.executor.common.BoilerplateStyleResolver;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.enumgen.EnumGenerationSupport;
import com.src.main.sm.executor.enumgen.EnumSpecResolved;

@Service
public class DtoGenerationService {

	private static final String TPL_DTO_JAVA = "class.java.mustache";
	private static final String TPL_DTO_KOTLIN = "class.kt.mustache";

	private record ClassMethodsSelection(
			boolean generateToString,
			boolean generateEquals,
			boolean generateHashCode,
			boolean noArgsConstructor,
			boolean allArgsConstructor,
			boolean builder) {
	}

	private final TemplateEngine templateEngine;
	private final DtoValidationHelperGenerator validationHelperGenerator;

	public DtoGenerationService(TemplateEngine templateEngine, DtoValidationHelperGenerator validationHelperGenerator) {
		this.templateEngine = templateEngine;
		this.validationHelperGenerator = validationHelperGenerator;
	}

	@SuppressWarnings("unchecked")
	public void generate(Path root, Map<String, Object> yaml, String groupId, String artifact) throws Exception {
		String basePkg = resolveBasePackage(yaml, groupId, artifact);
		BoilerplateStyle style = BoilerplateStyleResolver.resolveFromYaml(yaml, true);
		GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);

		List<Map<String, Object>> dtos = (List<Map<String, Object>>) yaml.getOrDefault("dtos", List.of());
		if (dtos.isEmpty()) {
			return;
		}
		AppSpecDTO spec = new ObjectMapper().convertValue(yaml, AppSpecDTO.class);
		List<EnumSpecResolved> enums = EnumGenerationSupport.resolveEnums(spec.getEnums());
		Map<String, EnumSpecResolved> enumByName = EnumGenerationSupport.byName(enums);
		String enumPackage = EnumGenerationSupport.resolveEnumPackage(basePkg, spec.getPackages());

		if (dtos.stream().anyMatch(d -> DtoGenerationSupport.hasNonEmpty(d.get("classConstraints")))) {
			validationHelperGenerator.ensureCrossFieldValidationHelpers(root, basePkg, language);
		}

		List<Map<String, Object>> dtosForMessages = new ArrayList<>();
		try {
			dtos.stream().map(dto -> buildUnit(dto, enumByName, enumPackage, style)).forEach(unit -> {
				try {
					writeDtoUnit(root, basePkg, unit, language);
					dtosForMessages.add(unit.getMessageModel());
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

		DtoGenerationSupport.mergeDtoMessagesIntoYaml(yaml, dtosForMessages);
	}

	private String resolveBasePackage(Map<String, Object> yaml, String groupId, String artifact) {
		String basePkg = (yaml != null) ? DtoGenerationSupport.str(yaml.get("basePackage")) : null;
		if (basePkg == null || basePkg.isBlank()) {
			return groupId + "." + artifact.replace('-', '_');
		}
		return basePkg;
	}

	private void writeDtoUnit(Path root, String basePkg, DtoGenerationUnit unit, GenerationLanguage language) throws Exception {
		Map<String, Object> templateModel = new LinkedHashMap<>();
		templateModel.put("basePkg", basePkg);
		templateModel.put("sub", unit.getSubPackage());
		templateModel.put("name", unit.getName());
		templateModel.put("dtoName", unit.getName());
		templateModel.put("classAnnotations", unit.getClassAnnotations());
		templateModel.put("fields", unit.getFieldModels());
		templateModel.put("useLombok", unit.isUseLombok());
		templateModel.put("hasFields", !unit.getFieldModels().isEmpty());
		templateModel.put("generateNoArgsConstructor", !unit.isUseLombok() && unit.isGenerateNoArgsConstructor());
		templateModel.put("generateAllArgsConstructor", !unit.isUseLombok() && unit.isGenerateAllArgsConstructor());
		templateModel.put("generateBuilder", !unit.isUseLombok() && unit.isGenerateBuilder());
		templateModel.put("generateToString", !unit.isUseLombok() && unit.isGenerateToString());
		templateModel.put("generateEquals", !unit.isUseLombok() && unit.isGenerateEquals());
		templateModel.put("generateHashCode", !unit.isUseLombok() && unit.isGenerateHashCode());
		String dtoTemplate = language == GenerationLanguage.KOTLIN ? TPL_DTO_KOTLIN : TPL_DTO_JAVA;
		String code = templateEngine.renderAny(TemplatePathResolver.candidates(language, "dto", dtoTemplate), templateModel);
		code = DtoGenerationSupport.injectImportsAfterPackage(code, unit.getImports());
		Path dir = root.resolve("src/main/" + language.templateFolder() + "/" + basePkg.replace('.', '/') + "/dto/" + unit.getSubPackage());
		Files.createDirectories(dir);
		Files.writeString(dir.resolve(unit.getName() + "." + language.fileExtension()), code, StandardCharsets.UTF_8);
	}

	@SuppressWarnings("unchecked")
	private DtoGenerationUnit buildUnit(Map<String, Object> dto, Map<String, EnumSpecResolved> enumByName,
			String enumPackage, BoilerplateStyle style) {
		String sub = "request".equals(String.valueOf(dto.get("type"))) ? "request" : "response";
		String name = String.valueOf(dto.get("name"));
		List<Map<String, Object>> fields = (List<Map<String, Object>>) dto.getOrDefault("fields", List.of());

		List<Map<String, Object>> classSpecs = DtoGenerationSupport.normalizeClassConstraints(dto.get("classConstraints"));
		List<String> classAnnotations = new ArrayList<>();
		List<Map<String, Object>> classConstraintsForMessages = new ArrayList<>();
		Set<String> imports = new LinkedHashSet<>();
		ClassMethodsSelection classMethods = resolveClassMethods(dto);
		DtoBoilerplateContext boilerplateContext = new DtoBoilerplateContext(
				name,
				classAnnotations,
				imports,
				true,
				classMethods.generateToString(),
				classMethods.generateEquals(),
				classMethods.generateHashCode(),
				classMethods.noArgsConstructor(),
				classMethods.allArgsConstructor(),
				classMethods.builder());
		DtoBoilerplateStrategyFactory.forStyle(style).apply(boilerplateContext);

		classAnnotations.add("@JsonInclude(JsonInclude.Include.NON_NULL)");
		DtoGenerationSupport.collectImportFromAnnotation("@com.fasterxml.jackson.annotation.JsonInclude", imports);

		for (Map<String, Object> c : classSpecs) {
			String ann = DtoGenerationSupport.toClassLevelAnnotation(c);
			if (ann != null && !ann.isBlank()) {
				classAnnotations.add(ann);
				String key = DtoGenerationSupport.str(c.get("key"));
				if (key != null && !key.isBlank()) {
					Map<String, Object> cm = new LinkedHashMap<>();
					cm.put("key", key);
					cm.put("defaultMessage", c.getOrDefault("message", DtoGenerationSupport.defaultClassMessage(c)));
					classConstraintsForMessages.add(cm);
				}
			}
		}

		List<Map<String, Object>> fieldModels = new ArrayList<>();
		List<Map<String, Object>> fieldsForMessages = new ArrayList<>();

		fields.forEach(f -> {
			String fname = String.valueOf(f.get("name"));
			String ftype = String.valueOf(f.get("type"));

			Map<String, Object> fm = new LinkedHashMap<>();
			fm.put("name", fname);
			String javaType = DtoGenerationSupport.mapType(ftype);
			fm.put("javaType", javaType);
			fm.put("method", DtoGenerationSupport.toMethodName(fname));
			String leafType = DtoGenerationSupport.extractLeafType(ftype);
			if (enumByName.containsKey(leafType)) {
				imports.add(enumPackage + "." + leafType);
			}

			List<String> annotations = new ArrayList<>();
			List<Map<String, Object>> constraints = DtoGenerationSupport.normalizeConstraints(f.get("constraints"));
			List<Map<String, Object>> constraintsForMessages = new ArrayList<>();

			constraints.forEach(c -> {
				String kind = DtoGenerationSupport.str(c.get("kind"));
				String key = DtoGenerationSupport.str(c.get("key"));
				if (kind == null)
					return;
				String ann = DtoGenerationSupport.toValidationAnnotation(kind, c, key);
				if (ann != null && !ann.isBlank()) {
					annotations.add(ann);
					DtoGenerationSupport.collectImportFromAnnotation(ann, imports);
					if (key != null && !key.isBlank()) {
						Map<String, Object> cm = new LinkedHashMap<>();
						cm.put("key", key);
						cm.put("defaultMessage",
								c.getOrDefault("message", DtoGenerationSupport.defaultMessage(kind, fname)));
						constraintsForMessages.add(cm);
					}
				}
			});

			if (f.containsKey("jsonProperty")) {
				String jp = DtoGenerationSupport.str(f.get("jsonProperty"));
				if (jp != null && !jp.isBlank()) {
					String ann = "@com.fasterxml.jackson.annotation.JsonProperty(\"" + DtoGenerationSupport.escapeJava(jp)
							+ "\")";
					annotations.add(ann);
					DtoGenerationSupport.collectImportFromAnnotation(ann, imports);
				}
			}

			if (DtoGenerationSupport.isNested(ftype)) {
				String ann = "@jakarta.validation.Valid";
				annotations.add(ann);
				DtoGenerationSupport.collectImportFromAnnotation(ann, imports);
			}

			fm.put("annotations", DtoGenerationSupport.simplifyAnnotations(annotations, imports));
			fieldModels.add(fm);

			Map<String, Object> msgField = new LinkedHashMap<>();
			msgField.put("constraints", constraintsForMessages);
			fieldsForMessages.add(msgField);
		});

		for (int i = 0; i < fieldModels.size(); i++) {
			fieldModels.get(i).put("isLast", i == fieldModels.size() - 1);
		}
		if (!boilerplateContext.isUseLombok() && (classMethods.generateEquals() || classMethods.generateHashCode())) {
			imports.add("java.util.Objects");
		}

		classAnnotations.forEach(a -> DtoGenerationSupport.collectImportFromAnnotation(a, imports));
		List<String> simplifiedClassAnnotations = DtoGenerationSupport.simplifyAnnotations(classAnnotations, imports);

		Map<String, Object> dtoMsg = new LinkedHashMap<>();
		dtoMsg.put("name", name);
		dtoMsg.put("fields", fieldsForMessages);
		if (!classConstraintsForMessages.isEmpty()) {
			dtoMsg.put("classConstraints", classConstraintsForMessages);
		}

		return new DtoGenerationUnit(
				sub,
				name,
				fieldModels,
				simplifiedClassAnnotations,
				imports,
				dtoMsg,
				boilerplateContext.isUseLombok(),
				classMethods.generateToString(),
				classMethods.generateEquals(),
				classMethods.generateHashCode(),
				classMethods.noArgsConstructor(),
				classMethods.allArgsConstructor(),
				classMethods.builder());
	}

	@SuppressWarnings("unchecked")
	private ClassMethodsSelection resolveClassMethods(Map<String, Object> dto) {
		boolean defaultToString = true;
		boolean defaultEquals = true;
		boolean defaultHashCode = true;
		boolean defaultNoArgsConstructor = true;
		boolean defaultAllArgsConstructor = true;
		boolean defaultBuilder = false;

		Object raw = dto.get("classMethods");
		if (!(raw instanceof Map<?, ?> mapRaw)) {
			return new ClassMethodsSelection(
					defaultToString,
					defaultEquals,
					defaultHashCode,
					defaultNoArgsConstructor,
					defaultAllArgsConstructor,
					defaultBuilder);
		}
		Map<String, Object> map = (Map<String, Object>) mapRaw;
		return new ClassMethodsSelection(
				parseBoolean(map.get("toString"), defaultToString),
				parseBoolean(map.get("equals"), defaultEquals),
				parseBoolean(map.get("hashCode"), defaultHashCode),
				parseBoolean(map.get("noArgsConstructor"), defaultNoArgsConstructor),
				parseBoolean(map.get("allArgsConstructor"), defaultAllArgsConstructor),
				parseBoolean(map.get("builder"), defaultBuilder));
	}

	private boolean parseBoolean(Object value, boolean defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		if (value instanceof Boolean bool) {
			return bool;
		}
		String normalized = String.valueOf(value).trim().toLowerCase();
		if (normalized.isEmpty()) {
			return defaultValue;
		}
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized)
				|| "y".equals(normalized);
	}
}
