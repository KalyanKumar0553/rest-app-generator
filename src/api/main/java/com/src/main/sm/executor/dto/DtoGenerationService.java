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
import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.enumgen.EnumGenerationSupport;
import com.src.main.sm.executor.enumgen.EnumSpecResolved;

@Service
public class DtoGenerationService {

	private static final String TPL_DTO = "templates/dto/class.java.mustache";

	private final TemplateEngine templateEngine;
	private final DtoValidationHelperGenerator validationHelperGenerator;

	public DtoGenerationService(TemplateEngine templateEngine, DtoValidationHelperGenerator validationHelperGenerator) {
		this.templateEngine = templateEngine;
		this.validationHelperGenerator = validationHelperGenerator;
	}

	@SuppressWarnings("unchecked")
	public void generate(Path root, Map<String, Object> yaml, String groupId, String artifact) throws Exception {
		String basePkg = (yaml != null) ? DtoGenerationSupport.str(yaml.get("basePackage")) : null;
		if (basePkg == null || basePkg.isBlank()) {
			basePkg = groupId + "." + artifact.replace('-', '_');
		}

		List<Map<String, Object>> dtos = (List<Map<String, Object>>) yaml.getOrDefault("dtos", List.of());
		if (dtos.isEmpty()) {
			return;
		}
		AppSpecDTO spec = new ObjectMapper().convertValue(yaml, AppSpecDTO.class);
		List<EnumSpecResolved> enums = EnumGenerationSupport.resolveEnums(spec.getEnums());
		Map<String, EnumSpecResolved> enumByName = EnumGenerationSupport.byName(enums);
		String enumPackage = EnumGenerationSupport.resolveEnumPackage(basePkg, spec.getPackages());

		if (dtos.stream().anyMatch(d -> DtoGenerationSupport.hasNonEmpty(d.get("classConstraints")))) {
			validationHelperGenerator.ensureCrossFieldValidationHelpers(root, basePkg);
		}

		List<Map<String, Object>> dtosForMessages = new ArrayList<>();
		for (Map<String, Object> dto : dtos) {
			DtoGenerationUnit unit = buildUnit(dto, enumByName, enumPackage);
			String code = templateEngine.render(TPL_DTO, Map.of("basePkg", basePkg, "sub", unit.getSubPackage(), "name",
					unit.getName(), "classAnnotations", String.join("\n", unit.getClassAnnotations()), "fields",
					unit.getFieldModels()));
			code = DtoGenerationSupport.injectImportsAfterPackage(code, unit.getImports());

			Path dir = root.resolve("src/main/java/" + basePkg.replace('.', '/') + "/dto/" + unit.getSubPackage());
			Files.createDirectories(dir);
			Files.writeString(dir.resolve(unit.getName() + ".java"), code, StandardCharsets.UTF_8);
			dtosForMessages.add(unit.getMessageModel());
		}

		DtoGenerationSupport.mergeDtoMessagesIntoYaml(yaml, dtosForMessages);
	}

	@SuppressWarnings("unchecked")
	private DtoGenerationUnit buildUnit(Map<String, Object> dto, Map<String, EnumSpecResolved> enumByName,
			String enumPackage) {
		String sub = "request".equals(String.valueOf(dto.get("type"))) ? "request" : "response";
		String name = String.valueOf(dto.get("name"));
		List<Map<String, Object>> fields = (List<Map<String, Object>>) dto.getOrDefault("fields", List.of());

		List<Map<String, Object>> classSpecs = DtoGenerationSupport.normalizeClassConstraints(dto.get("classConstraints"));
		List<String> classAnnotations = new ArrayList<>();
		List<Map<String, Object>> classConstraintsForMessages = new ArrayList<>();
		Set<String> imports = new LinkedHashSet<>();

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

		for (Map<String, Object> f : fields) {
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

			for (Map<String, Object> c : constraints) {
				String kind = DtoGenerationSupport.str(c.get("kind"));
				String key = DtoGenerationSupport.str(c.get("key"));
				if (kind == null)
					continue;
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
			}

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
		}

		classAnnotations.forEach(a -> DtoGenerationSupport.collectImportFromAnnotation(a, imports));
		classAnnotations = DtoGenerationSupport.simplifyAnnotations(classAnnotations, imports);

		Map<String, Object> dtoMsg = new LinkedHashMap<>();
		dtoMsg.put("name", name);
		dtoMsg.put("fields", fieldsForMessages);
		if (!classConstraintsForMessages.isEmpty()) {
			dtoMsg.put("classConstraints", classConstraintsForMessages);
		}

		return new DtoGenerationUnit(sub, name, fieldModels, classAnnotations, imports, dtoMsg);
	}
}
