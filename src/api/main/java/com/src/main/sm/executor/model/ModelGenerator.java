package com.src.main.sm.executor.model;

import static com.src.main.sm.executor.model.ModelGenerationSupport.buildMaxAnnotation;
import static com.src.main.sm.executor.model.ModelGenerationSupport.buildMessageKey;
import static com.src.main.sm.executor.model.ModelGenerationSupport.buildMinAnnotation;
import static com.src.main.sm.executor.model.ModelGenerationSupport.escapeJava;
import static com.src.main.sm.executor.model.ModelGenerationSupport.getBoolean;
import static com.src.main.sm.executor.model.ModelGenerationSupport.getInt;
import static com.src.main.sm.executor.model.ModelGenerationSupport.getLong;
import static com.src.main.sm.executor.model.ModelGenerationSupport.getString;
import static com.src.main.sm.executor.model.ModelGenerationSupport.getStringAny;
import static com.src.main.sm.executor.model.ModelGenerationSupport.msgAnno;
import static java.util.stream.Collectors.joining;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ClassMethodsSpecDTO;
import com.src.main.dto.ColumnSpecDTO;
import com.src.main.dto.ConstraintDTO;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.GenerationSpecDTO;
import com.src.main.dto.JoinColumnSpecDTO;
import com.src.main.dto.JoinTableSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.RelationSpecDTO;
import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.BoilerplateStyle;
import com.src.main.sm.executor.common.BoilerplateStyleResolver;
import com.src.main.sm.executor.enumgen.EnumGenerationSupport;
import com.src.main.sm.executor.enumgen.EnumSpecResolved;
import com.src.main.util.PathUtils;

/**
 * Converts YAML models into Java entity classes using Mustache. Focus: JPA +
 * Bean Validation + Relationships + Auditing + Composite Uniques.
 */
public class ModelGenerator {

	private static final Logger log = LoggerFactory.getLogger(ModelGenerator.class);

	private static final String TEMPLATE_MODEL = "templates/model/model.java.mustache";
	private static final String TEMPLATE_MONGO_SEQUENCE = "templates/model/mongo-primary-sequence-service.java.mustache";
	private static final String TEMPLATE_MONGO_SEQUENCE_DOCUMENT = "templates/model/mongo-database-sequence.java.mustache";
	private static final String TEMPLATE_MONGO_LISTENER = "templates/model/mongo-listener.java.mustache";

	private final TemplateEngine tpl;
	private final ObjectMapper yaml;
	private final String basePackage;

	private record ClassMethodsSelection(
			boolean generateToString,
			boolean generateEquals,
			boolean generateHashCode,
			boolean noArgsConstructor,
			boolean allArgsConstructor,
			boolean builder) {
	}

	public ModelGenerator(TemplateEngine tpl, String basePackage) {
		this.tpl = tpl;
		this.basePackage = basePackage;
		this.yaml = new ObjectMapper(new YAMLFactory());
	}

	public void generate(Map<String, Object> yaml, Path projectRoot) throws Exception {
		Validate.notNull(yaml, "YAML map must not be null");
		Validate.notNull(projectRoot, "projectRoot must not be null");

		// Convert the generic map into AppSpec via Jackson
		ObjectMapper mapper = new ObjectMapper();
		AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
		BoilerplateStyle boilerplateStyle = BoilerplateStyleResolver.resolveFromYaml(yaml, true);
		boolean noSqlDatabase = isNoSqlDatabase(yaml);

		if (spec.getModels() == null || spec.getModels().isEmpty()) {
			return;
		}
		Validate.notEmpty(spec.getModels(), "No models defined in YAML");

		Map<String, String> validationMessages = collectValidationMessages(spec);
		mergeValidationMessagesIntoYaml(yaml, validationMessages);

		boolean domainStructure = "domain".equalsIgnoreCase(StringUtils.firstNonBlank(spec.getPackages(), "technical"));
		String enumPackage = EnumGenerationSupport.resolveEnumPackage(basePackage, spec.getPackages());
		Map<String, EnumSpecResolved> enumByName = EnumGenerationSupport.byName(
				EnumGenerationSupport.resolveEnums(spec.getEnums()));
		Map<String, String> modelPackageByType = new LinkedHashMap<>();
		spec.getModels().forEach(m -> {
			String className = CaseUtils.toPascal(m.getName());
			modelPackageByType.put(className, resolveModelPackage(m, domainStructure));
		});

		try {
			spec.getModels().forEach(m -> {
				try {
					String modelPkg = resolveModelPackage(m, domainStructure);
					Path outDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(modelPkg));
					Files.createDirectories(outDir);
					renderModel(m, modelPkg, outDir, spec, modelPackageByType, enumByName, enumPackage, boilerplateStyle, noSqlDatabase);
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
		if (noSqlDatabase) {
			generateMongoSupportClasses(spec, projectRoot, domainStructure);
		}
	}

	private Map<String, String> collectValidationMessages(AppSpecDTO spec) {
		return ModelGenerationSupport.collectValidationMessages(spec);
	}

	private void mergeValidationMessagesIntoYaml(Map<String, Object> yaml, Map<String, String> messages) {
		ModelGenerationSupport.mergeValidationMessagesIntoYaml(yaml, messages);
	}

	private String resolveModelPackage(ModelSpecDTO model, boolean domainStructure) {
		if (!domainStructure) {
			return basePackage + ".model";
		}
		String entitySegment = normalizePackageSegment(model.getName());
		return basePackage + ".domain." + entitySegment + ".model";
	}

	private String normalizePackageSegment(String value) {
		String normalized = CaseUtils.toSnake(StringUtils.firstNonBlank(value, "entity"));
		normalized = normalized.replaceAll("[^a-zA-Z0-9_]", "_").replaceAll("_+", "_").toLowerCase();
		if (normalized.isBlank()) {
			return "entity";
		}
		if (!Character.isJavaIdentifierStart(normalized.charAt(0))) {
			normalized = "x_" + normalized;
		}
		return normalized;
	}

	private Map<String, Object> propertyModel(String type, String name) {
		Map<String, Object> property = new LinkedHashMap<>();
		property.put("type", type);
		property.put("name", name);
		property.put("method", toMethodName(name));
		return property;
	}

	private String toMethodName(String field) {
		if (field == null || field.isEmpty()) {
			return field;
		}
		return Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}

	private void renderModel(ModelSpecDTO m, String modelPkg, Path outDir, AppSpecDTO root,
			Map<String, String> modelPackageByType, Map<String, EnumSpecResolved> enumByName, String enumPackage,
			BoilerplateStyle boilerplateStyle, boolean noSqlDatabase)
			throws Exception {
		String className = CaseUtils.toPascal(m.getName());
		String tableName = StringUtils.firstNonBlank(m.getTableName(), CaseUtils.toSnake(m.getName()));
		if (Boolean.TRUE.equals(root.getPluralizeTableNames())) {
			tableName = pluralizeSnakeTableName(tableName);
		}
		String schema = Strings.trimToNull(m.getSchema());

		// Build the data model for mustache
		Map<String, Object> ctx = new LinkedHashMap<>();
		ctx.put("packageName", modelPkg);
		ctx.put("className", className);

		// Use TreeSet for sorted, de-duplicated imports (same as DTOs)
		Set<String> imports = new TreeSet<>();

		List<String> classAnnotations = new ArrayList<>();

		if (noSqlDatabase) {
			imports.add("org.springframework.data.mongodb.core.mapping.Document");
			classAnnotations.add("@Document(\"" + tableName + "\")");
		} else {
			// ----- @Entity + @Table
			boolean entityEnabled = (m.getOptions() == null) || Boolean.TRUE.equals(m.getOptions().isEntity());
			if (entityEnabled) {
				imports.add("jakarta.persistence.Entity");
				classAnnotations.add("@Entity");
			}
			// @Table with schema/name/uniqueConstraints
			boolean hasTableAnno = schema != null || (m.getTableName() != null) || hasCompositeUniques(m);
			if (hasTableAnno) {
				imports.add("jakarta.persistence.Table");
				StringBuilder sb = new StringBuilder("@Table(");
				List<String> parts = new ArrayList<>();
				parts.add("name = \"" + tableName + "\"");
				if (schema != null) {
					parts.add("schema = \"" + schema + "\"");
				}
				if (hasCompositeUniques(m)) {
					imports.add("jakarta.persistence.UniqueConstraint");
					String uniques = m.getUniqueConstraints().stream()
							.map(cols -> "\"" + cols.stream().map(CaseUtils::toSnake).collect(joining("\", \"")) + "\"")
							.collect(joining("}, @UniqueConstraint(columnNames = {", "@UniqueConstraint(columnNames = {",
									"})"));
					parts.add("uniqueConstraints = {" + uniques + "}");
				}
				sb.append(String.join(", ", parts)).append(")");
				classAnnotations.add(sb.toString());
			}
		}

		// Hibernate extras
		if (!noSqlDatabase && m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().isImmutable())) {
			imports.add("org.hibernate.annotations.Immutable");
			classAnnotations.add("@Immutable");
		}
		if (!noSqlDatabase && m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().isNaturalIdCache())) {
			imports.add("org.hibernate.annotations.NaturalIdCache");
			classAnnotations.add("@NaturalIdCache");
		}

		ctx.put("classAnnotations", classAnnotations);

		// ----- ID block
		IdBlock id = buildIdBlock(m, imports, noSqlDatabase);
		ctx.put("idBlock", id);

		// ----- Fields (non-relational)
		List<FieldBlock> fields = new ArrayList<>();
		if (m.getFields() != null) {
			m.getFields().stream()
					.filter(f -> !isRelationPlaceholder(f))
					.forEach(f -> fields.add(buildFieldBlock(m, f, imports, enumByName, enumPackage, modelPkg, noSqlDatabase)));
		}
		ctx.put("fields", fields);

		// ----- Relations
		List<RelationBlock> rels = new ArrayList<>();
		if (m.getRelations() != null) {
			m.getRelations().forEach(r -> rels.add(buildRelationBlock(m, r, imports, modelPkg, modelPackageByType, noSqlDatabase)));
		}
		ctx.put("relations", rels);

		// ----- Auditing
		AuditingBlock auditing = new AuditingBlock();
		if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().isAuditing())) {
			auditing.setEnabled(true);
			// Optional: add imports/annotations for auditing here if you wire them in
			// templates
		}
		ctx.put("auditing", auditing);

		// ----- Soft delete
		boolean softDeleteEnabled = m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().isSoftDelete());
		Map<String, Object> softDelete = Map.of("enabled", softDeleteEnabled);
		ctx.put("softDelete", softDelete);

		// Implement Serializable (if your template uses this)
		imports.add("java.io.Serializable");

		if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().isAuditing())) {
			imports.add("org.springframework.data.annotation.CreatedDate");
			imports.add("org.springframework.data.annotation.LastModifiedDate");
			imports.add("java.time.OffsetDateTime");

		}
		if (softDeleteEnabled) {
			imports.add("java.time.OffsetDateTime");
		}
		if (noSqlDatabase) {
			imports.add("org.springframework.data.annotation.Version");
		}

		List<Map<String, Object>> properties = new ArrayList<>();
		if (id != null) {
			properties.add(propertyModel(id.getType(), id.getName()));
		}
		fields.forEach(field -> properties.add(propertyModel(field.getType(), field.getName())));
		rels.forEach(relation -> properties.add(propertyModel(relation.getDeclarationType(), relation.getName())));
		if (Boolean.TRUE.equals(auditing.isEnabled())) {
			properties.add(propertyModel("OffsetDateTime", "createdAt"));
			properties.add(propertyModel("OffsetDateTime", "updatedAt"));
		}
		if (softDeleteEnabled) {
			properties.add(propertyModel("Boolean", "deleted"));
			properties.add(propertyModel("OffsetDateTime", "deletedAt"));
		}
		if (noSqlDatabase) {
			properties.add(propertyModel("Integer", "version"));
		}
		for (int i = 0; i < properties.size(); i++) {
			properties.get(i).put("isLast", i == properties.size() - 1);
		}
		ClassMethodsSelection classMethods = resolveClassMethods(m);

		ModelBoilerplateContext boilerplateContext = new ModelBoilerplateContext(m, className, imports, classAnnotations,
				properties,
				true,
				classMethods.generateToString(),
				classMethods.generateEquals(),
				classMethods.generateHashCode(),
				classMethods.noArgsConstructor(),
				classMethods.allArgsConstructor(),
				classMethods.builder());
		ModelBoilerplateStrategyFactory.forStyle(boilerplateStyle).apply(boilerplateContext);
		if (!boilerplateContext.isUseLombok() && (classMethods.generateEquals() || classMethods.generateHashCode())) {
			imports.add("java.util.Objects");
		}

		ctx.put("properties", properties);
		ctx.put("hasProperties", !properties.isEmpty());
		ctx.put("useLombok", boilerplateContext.isUseLombok());
		ctx.put("generateNoArgsConstructor", !boilerplateContext.isUseLombok() && classMethods.noArgsConstructor());
		ctx.put("generateAllArgsConstructor", !boilerplateContext.isUseLombok() && classMethods.allArgsConstructor());
		ctx.put("generateBuilder", !boilerplateContext.isUseLombok() && classMethods.builder());
		ctx.put("generateToString", !boilerplateContext.isUseLombok() && classMethods.generateToString());
		ctx.put("generateEquals", !boilerplateContext.isUseLombok() && classMethods.generateEquals());
		ctx.put("generateHashCode", !boilerplateContext.isUseLombok() && classMethods.generateHashCode());
		ctx.put("classAnnotations", classAnnotations);
		ctx.put("implements", "Serializable");
		ctx.put("noSql", noSqlDatabase);

		// ----- Imports (already de-dup + sorted via TreeSet)
		ctx.put("imports", new ArrayList<>(imports));

		// ----- Write file
		Path outFile = outDir.resolve(className + ".java");
		String content = tpl.render(TEMPLATE_MODEL, ctx);
		Files.createDirectories(outFile.getParent());
		Files.writeString(outFile, content, StandardCharsets.UTF_8);

		log.info("Generated model: {}", outFile);
	}

	private ClassMethodsSelection resolveClassMethods(ModelSpecDTO model) {
		ClassMethodsSpecDTO methods = model.getClassMethods();
		if (methods != null) {
			return new ClassMethodsSelection(
					Boolean.TRUE.equals(methods.getToString()),
					Boolean.TRUE.equals(methods.getEquals()),
					Boolean.TRUE.equals(methods.getHashCode()),
					!Boolean.FALSE.equals(methods.getNoArgsConstructor()),
					!Boolean.FALSE.equals(methods.getAllArgsConstructor()),
					Boolean.TRUE.equals(methods.getBuilder()));
		}

		boolean legacyBuilder = false;
		boolean legacyToString = false;
		boolean legacyEqualsHash = false;
		if (model.getOptions() != null && model.getOptions().getLombok() != null) {
			legacyBuilder = Boolean.TRUE.equals(model.getOptions().getLombok().getBuilder());
			legacyToString = Boolean.TRUE.equals(model.getOptions().getLombok().getToString());
			legacyEqualsHash = Boolean.TRUE.equals(model.getOptions().getLombok().getEqualsAndHashCode());
		}
		return new ClassMethodsSelection(
				legacyToString,
				legacyEqualsHash,
				legacyEqualsHash,
				true,
				true,
				legacyBuilder);
	}

	private String pluralizeSnakeTableName(String value) {
		String name = StringUtils.firstNonBlank(value, "");
		if (name.isBlank()) {
			return name;
		}

		if (name.endsWith("_")) {
			String prefix = name.substring(0, name.length() - 1);
			return pluralizeSnakeTableName(prefix) + "_";
		}

		if (name.endsWith("ies")) {
			return name;
		}
		if (name.endsWith("s") || name.endsWith("x") || name.endsWith("z") || name.endsWith("ch")
				|| name.endsWith("sh")) {
			return name + "es";
		}
		if (name.endsWith("y") && name.length() > 1) {
			char beforeY = name.charAt(name.length() - 2);
			boolean vowel = beforeY == 'a' || beforeY == 'e' || beforeY == 'i' || beforeY == 'o' || beforeY == 'u';
			if (!vowel) {
				return name.substring(0, name.length() - 1) + "ies";
			}
		}
		return name + "s";
	}

	private boolean hasCompositeUniques(ModelSpecDTO m) {
		return m.getUniqueConstraints() != null && !m.getUniqueConstraints().isEmpty();
	}

	private IdBlock buildIdBlock(ModelSpecDTO m, Set<String> imports, boolean noSqlDatabase) {
		Validate.notNull(m.getId(), "entity.id missing for %s", m.getName());

		FieldNameAndType nat = normalizeNameType(m.getId().getField(), m.getId().getType());
		IdBlock id = new IdBlock();
		id.setName(nat.name());
		id.setType(resolveJavaType(nat.type(), imports));

		List<String> ann = new ArrayList<>();

		// Always @Id
		if (noSqlDatabase) {
			imports.add("org.springframework.data.annotation.Id");
		} else {
			imports.add("jakarta.persistence.Id");
		}
		ann.add("@Id");

		GenerationSpecDTO g = m.getId().getGeneration();
		if (!noSqlDatabase && g != null && g.getStrategy() != null) {
			imports.add("jakarta.persistence.GeneratedValue");

			switch (g.getStrategy()) {
			case IDENTITY -> {
				imports.add("jakarta.persistence.GenerationType");
				ann.add("@GeneratedValue(strategy = GenerationType.IDENTITY)");
			}
			case SEQUENCE -> {
				imports.add("jakarta.persistence.GenerationType");
				imports.add("jakarta.persistence.SequenceGenerator");

				String genName = defaultSeqName(m);
				String seqName = StringUtils.firstNonBlank(g.getSequenceName(), genName);
				Integer allocationSize = g.getAllocationSize();
				int alloc = allocationSize != null ? allocationSize : 50;

				ann.add("@SequenceGenerator(name = \"" + genName + "\", sequenceName = \"" + seqName
						+ "\", allocationSize = " + alloc + ")");
				ann.add("@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"" + genName + "\")");
			}
			case UUID -> {
				// Default to Hibernate @GenericGenerator("uuid","uuid2") for compatibility
				imports.add("org.hibernate.annotations.GenericGenerator");

				String generatorName = StringUtils.firstNonBlank(g.getGeneratorName(), "uuid");
				String strategy = StringUtils.firstNonBlank(g.getHibernateUuidStrategy(), "uuid2");

				ann.add("@GenericGenerator(name = \"" + generatorName + "\", strategy = \"" + strategy + "\")");
				ann.add("@GeneratedValue(generator = \"" + generatorName + "\")");
			}
			case AUTO -> {
				imports.add("jakarta.persistence.GenerationType");
				ann.add("@GeneratedValue(strategy = GenerationType.AUTO)");
			}
			case NONE -> {
				// no-op: manually assigned IDs
			}
			}
		}

		id.setAnnotations(ann);
		return id;
	}

	private String defaultSeqName(ModelSpecDTO m) {
		return CaseUtils.toSnake(m.getName()) + "_seq";
	}

	private FieldBlock buildFieldBlock(ModelSpecDTO m, FieldSpecDTO f, Set<String> imports,
			Map<String, EnumSpecResolved> enumByName, String enumPackage, String modelPackage, boolean noSqlDatabase) {
		FieldNameAndType nat = normalizeNameType(f.getName(), f.getType());
		FieldBlock fb = new FieldBlock();
		fb.setName(nat.name());
		String resolvedType = resolveJavaType(nat.type(), imports);
		fb.setType(resolvedType);
		EnumSpecResolved enumSpec = resolveEnumSpec(nat.type(), resolvedType, enumByName);

		List<String> ann = new ArrayList<>();

		if (enumSpec != null) {
			if (enumPackage != null && !enumPackage.equals(modelPackage)) {
				imports.add(enumPackage + "." + resolvedType);
			}
			if (!noSqlDatabase) {
				imports.add("jakarta.persistence.EnumType");
				imports.add("jakarta.persistence.Enumerated");
				ann.add("@Enumerated(EnumType." + enumSpec.storage() + ")");
			}
		}

		// @Column (from explicit column or inferred from constraints)
		ColumnSpecDTO col = f.getColumn();
		boolean hasExplicitColumn = col != null && (col.getName() != null || col.getLength() != null
				|| col.getNullable() != null || col.getUnique() != null || col.getColumnDefinition() != null);

		// Bean Validation
		if (f.getConstraints() != null) {
			for (ConstraintDTO c : f.getConstraints()) {
				final String name = c.getName();
				if (name == null) {
					continue;
				}

				switch (name) {
				case "NotNull" -> {
					imports.add("jakarta.validation.constraints.NotNull");
					ann.add(msgAnno("@NotNull", m, f, "NotNull"));
					col = ensureColumn(col);
					col.setNullable(Boolean.FALSE);
				}
				case "NotBlank" -> {
					imports.add("jakarta.validation.constraints.NotBlank");
					ann.add(msgAnno("@NotBlank", m, f, "NotBlank"));
				}
				case "Email" -> {
					imports.add("jakarta.validation.constraints.Email");
					ann.add(msgAnno("@Email", m, f, "Email"));
				}
				case "Pattern" -> {
					imports.add("jakarta.validation.constraints.Pattern");
					// support either 'regex' or 'regexp' keys; default ".*"
					String re = getStringAny(c.getParams(), new String[] { "regex", "regexp" }, ".*");
					ann.add(msgAnno("@Pattern(regexp = \"" + escapeJava(re) + "\")", m, f, "Pattern"));
				}
				case "Size" -> {
					imports.add("jakarta.validation.constraints.Size");
					Integer min = getInt(c.getParams(), "min", null);
					Integer max = getInt(c.getParams(), "max", null);

					String args = (min != null ? "min=" + min : "") + ((min != null && max != null) ? ", " : "")
							+ (max != null ? "max=" + max : "");
					ann.add(msgAnno("@Size(" + args + ")", m, f, "Size"));

					// infer @Column(length=...) for Strings when max provided and no explicit
					// length
					if (!hasExplicitColumn && "String".equals(resolvedType) && max != null) {
						col = ensureColumn(col);
						if (col.getLength() == null) {
							col.setLength(max);
						}
					}
				}
				case "Min" -> {
					imports.add("jakarta.validation.constraints.Min");
					long val = getLong(c.getParams(), "value", 0L);
					String msgKey = buildMessageKey(m, f, "Min");
					ann.add(buildMinAnnotation(val, msgKey));
				}
				case "Max" -> {
					imports.add("jakarta.validation.constraints.Max");
					long val = getLong(c.getParams(), "value", Long.MAX_VALUE);
					String msgKey = buildMessageKey(m, f, "Max");
					ann.add(buildMaxAnnotation(val, msgKey));
				}
				case "Positive" -> {
					imports.add("jakarta.validation.constraints.Positive");
					ann.add(msgAnno("@Positive", m, f, "Positive"));
				}
				case "DecimalMin" -> {
					imports.add("jakarta.validation.constraints.DecimalMin");
					String val = getString(c.getParams(), "value", "0");
					boolean inclusive = getBoolean(c.getParams(), "inclusive", true);
					ann.add(msgAnno("@DecimalMin(value=\"" + escapeJava(val) + "\", inclusive=" + inclusive + ")", m, f,
							"DecimalMin"));
				}
				case "Digits" -> {
					imports.add("jakarta.validation.constraints.Digits");
					int integer = getInt(c.getParams(), "integer", 10);
					int fraction = getInt(c.getParams(), "fraction", 2);
					ann.add(msgAnno("@Digits(integer=" + integer + ", fraction=" + fraction + ")", m, f, "Digits"));
				}
				case "Past" -> {
					imports.add("jakarta.validation.constraints.Past");
					ann.add(msgAnno("@Past", m, f, "Past"));
				}
				case "Future" -> {
					imports.add("jakarta.validation.constraints.Future");
					ann.add(msgAnno("@Future", m, f, "Future"));
				}
				case "PastOrPresent" -> {
					imports.add("jakarta.validation.constraints.PastOrPresent");
					ann.add(msgAnno("@PastOrPresent", m, f, "PastOrPresent"));
				}
				case "AssertTrue" -> {
					imports.add("jakarta.validation.constraints.AssertTrue");
					ann.add(msgAnno("@AssertTrue", m, f, "AssertTrue"));
				}
				case "AssertFalse" -> {
					imports.add("jakarta.validation.constraints.AssertFalse");
					ann.add(msgAnno("@AssertFalse", m, f, "AssertFalse"));
				}
				case "Valid" -> {
					imports.add("jakarta.validation.Valid");
					ann.add("@Valid");
				}
				default -> {
					// ignore unknowns here
				}
				}
			}
		}

		// @Convert (explicit only)
		if (!noSqlDatabase && f.getJpa() != null && f.getJpa().getConvert() != null
				&& Strings.isNotBlank(f.getJpa().getConvert().getConverter())) {
			imports.add("jakarta.persistence.Convert");
			ann.add("@Convert(converter = " + f.getJpa().getConvert().getConverter() + ".class)");
		}

		// @NaturalId
		if (!noSqlDatabase && Boolean.TRUE.equals(f.getNaturalId())) {
			imports.add("org.hibernate.annotations.NaturalId");
			ann.add("@NaturalId");
		}

		// @Column
		if (!noSqlDatabase && col != null && (hasExplicitColumn || col.getNullable() != null || col.getUnique() != null
				|| col.getLength() != null || col.getColumnDefinition() != null || col.getName() != null)) {

			imports.add("jakarta.persistence.Column");
			StringBuilder sb = new StringBuilder("@Column(");
			List<String> args = new ArrayList<>();
			if (col.getName() != null)
				args.add("name = \"" + col.getName() + "\"");
			if (col.getUnique() != null)
				args.add("unique = " + col.getUnique());
			if (col.getNullable() != null)
				args.add("nullable = " + col.getNullable());
			if (col.getLength() != null)
				args.add("length = " + col.getLength());
			if (col.getColumnDefinition() != null)
				args.add("columnDefinition = \"" + escapeJava(col.getColumnDefinition()) + "\"");
			sb.append(String.join(", ", args)).append(")");
			ann.add(sb.toString());
		}

		fb.setAnnotations(ann);
		return fb;
	}

	private EnumSpecResolved resolveEnumSpec(String rawType, String resolvedType, Map<String, EnumSpecResolved> enumByName) {
		if (enumByName == null || enumByName.isEmpty()) {
			return null;
		}

		EnumSpecResolved directResolved = enumByName.get(resolvedType);
		if (directResolved != null) {
			return directResolved;
		}

		String normalizedResolved = CaseUtils.toPascal(StringUtils.firstNonBlank(resolvedType, "").trim());
		if (!normalizedResolved.isBlank()) {
			EnumSpecResolved byResolvedPascal = enumByName.get(normalizedResolved);
			if (byResolvedPascal != null) {
				return byResolvedPascal;
			}
		}

		String rawLeafType = extractLeafType(rawType);
		if (!rawLeafType.isBlank()) {
			EnumSpecResolved byRawLeaf = enumByName.get(rawLeafType);
			if (byRawLeaf != null) {
				return byRawLeaf;
			}
			String normalizedRawLeaf = CaseUtils.toPascal(rawLeafType);
			if (!normalizedRawLeaf.isBlank()) {
				return enumByName.get(normalizedRawLeaf);
			}
		}

		return null;
	}

	private String extractLeafType(String rawType) {
		String type = StringUtils.firstNonBlank(rawType, "").trim();
		if (type.isBlank()) {
			return "";
		}
		int genericStart = type.indexOf('<');
		if (genericStart >= 0 && type.endsWith(">")) {
			type = type.substring(genericStart + 1, type.length() - 1).trim();
			if (type.contains(",")) {
				type = type.substring(type.lastIndexOf(',') + 1).trim();
			}
		}
		if (type.contains(".")) {
			type = type.substring(type.lastIndexOf('.') + 1);
		}
		return type.trim();
	}

	/* ==== helpers (null-safe, type-safe, with defaults) ==== */

	private ColumnSpecDTO ensureColumn(ColumnSpecDTO col) {
		return col != null ? col : new ColumnSpecDTO();
	}

	private RelationBlock buildRelationBlock(ModelSpecDTO m, RelationSpecDTO r, Set<String> imports,
			String currentModelPackage, Map<String, String> modelPackageByType, boolean noSqlDatabase) {
		RelationBlock rb = new RelationBlock();
		rb.setName(CaseUtils.toCamel(r.getName()));

		String targetType = CaseUtils.toPascal(r.getTarget());
		String targetModelPackage = modelPackageByType.get(targetType);
		if (targetModelPackage != null && !targetModelPackage.equals(currentModelPackage)) {
			imports.add(targetModelPackage + "." + targetType);
		}
		String fieldType;
		List<String> ann = new ArrayList<>();

		if (noSqlDatabase) {
			imports.add("org.springframework.data.mongodb.core.mapping.DocumentReference");
			ann.add("@DocumentReference(lazy = true)");
			switch (r.getType()) {
			case OneToMany, ManyToMany -> {
				fieldType = "List<" + targetType + ">";
				imports.add("java.util.List");
			}
			default -> fieldType = targetType;
			}
			rb.setAnnotations(ann);
			rb.setDeclarationType(fieldType);
			return rb;
		}

		switch (r.getType()) {
		case OneToOne -> {
			imports.add("jakarta.persistence.OneToOne");
			imports.add("jakarta.persistence.FetchType");
			String cascade = toCascadeArray(r.getCascade());
			String optional = r.getOptional() == null ? "true" : r.getOptional().toString();

			if (Strings.isNotBlank(r.getMappedBy())) {
				ann.add("@OneToOne(mappedBy = \"" + r.getMappedBy().trim() + "\", fetch = FetchType.LAZY, optional = "
						+ optional + cascade + (Boolean.TRUE.equals(r.getOrphanRemoval()) ? ", orphanRemoval = true" : "")
						+ ")");
			} else {
				ann.add("@OneToOne(fetch = FetchType.LAZY, optional = " + optional + cascade
						+ (Boolean.TRUE.equals(r.getOrphanRemoval()) ? ", orphanRemoval = true" : "") + ")");
				JoinColumnSpecDTO jc = r.getJoinColumn();
				if (jc != null) {
					imports.add("jakarta.persistence.JoinColumn");
					StringBuilder s = new StringBuilder("@JoinColumn(");
					List<String> args = new ArrayList<>();
					if (Strings.isNotBlank(jc.getName()))
						args.add("name = \"" + jc.getName() + "\"");
					if (jc.getNullable() != null)
						args.add("nullable = " + jc.getNullable());
					if (Strings.isNotBlank(jc.getReferencedColumnName()))
						args.add("referencedColumnName = \"" + jc.getReferencedColumnName() + "\"");
					s.append(String.join(", ", args)).append(")");
					ann.add(s.toString());
				}
			}
			fieldType = targetType;
		}
		case OneToMany -> {
			imports.add("jakarta.persistence.OneToMany");
			imports.add("jakarta.persistence.FetchType");
			String mappedBy = Validate.notBlank(r.getMappedBy(), "OneToMany mappedBy required on %s.%s", m.getName(),
					r.getName());
			String cascade = toCascadeArray(r.getCascade());
			ann.add("@OneToMany(mappedBy = \"" + mappedBy + "\", fetch = FetchType.LAZY" + cascade
					+ (Boolean.TRUE.equals(r.getOrphanRemoval()) ? ", orphanRemoval = true" : "") + ")");
			if (Strings.isNotBlank(r.getOrderBy())) {
				imports.add("jakarta.persistence.OrderBy");
				ann.add("@OrderBy(\"" + r.getOrderBy().trim() + "\")");
			}
			if (r.getOrderColumn() != null && Strings.isNotBlank(r.getOrderColumn().getName())) {
				imports.add("jakarta.persistence.OrderColumn");
				ann.add("@OrderColumn(name = \"" + r.getOrderColumn().getName() + "\")");
			}
			fieldType = "List<" + targetType + ">";
			imports.add("java.util.List");
		}
		case ManyToOne -> {
			imports.add("jakarta.persistence.ManyToOne");
			imports.add("jakarta.persistence.FetchType");
			String optional = r.getOptional() == null ? "true" : r.getOptional().toString();
			ann.add("@ManyToOne(fetch = FetchType.LAZY, optional = " + optional + ")");
			JoinColumnSpecDTO jc = r.getJoinColumn();
			if (jc != null) {
				imports.add("jakarta.persistence.JoinColumn");
				StringBuilder s = new StringBuilder("@JoinColumn(");
				List<String> args = new ArrayList<>();
				if (Strings.isNotBlank(jc.getName()))
					args.add("name = \"" + jc.getName() + "\"");
				if (jc.getNullable() != null)
					args.add("nullable = " + jc.getNullable());
				if (Strings.isNotBlank(jc.getReferencedColumnName()))
					args.add("referencedColumnName = \"" + jc.getReferencedColumnName() + "\"");
				s.append(String.join(", ", args)).append(")");
				ann.add(s.toString());
			}
			fieldType = targetType;
		}
		case ManyToMany -> {
			imports.add("jakarta.persistence.ManyToMany");
			imports.add("jakarta.persistence.FetchType");
			String cascade = toCascadeArray(r.getCascade());
			ann.add("@ManyToMany(fetch = FetchType.LAZY" + cascade + ")");
			if (r.getJoinTable() != null) {
				imports.add("jakarta.persistence.JoinTable");
				imports.add("jakarta.persistence.JoinColumn");
				String jt = joinTableAnnotation(r.getJoinTable());
				ann.add(jt);
			}
			fieldType = "Set<" + targetType + ">";
			imports.add("java.util.Set");
		}
		default -> throw new IllegalStateException("Unsupported relation: " + r.getType());
		}

		rb.setAnnotations(ann);
		rb.setDeclarationType(fieldType);
		rb.setTargetType(targetType);
		return rb;
	}

	private String toCascadeArray(List<String> cascade) {
		if (cascade == null || cascade.isEmpty())
			return "";

		List<String> normalized = cascade.stream().filter(Strings::isNotBlank).map(String::trim).distinct().toList();
		if (normalized.isEmpty())
			return "";
		if (normalized.stream().anyMatch(c -> "ALL".equalsIgnoreCase(c))) {
			return ", cascade = {jakarta.persistence.CascadeType.ALL}";
		}
		return ", cascade = {"
				+ normalized.stream().map(s -> "jakarta.persistence.CascadeType." + s).collect(joining(", ")) + "}";
	}

	private String joinTableAnnotation(JoinTableSpecDTO jt) {
		StringBuilder sb = new StringBuilder("@JoinTable(name = \"" + jt.getName() + "\"");
		if (jt.getJoinColumns() != null && !jt.getJoinColumns().isEmpty()) {
			sb.append(", joinColumns = {");
			sb.append(jt.getJoinColumns().stream()
					.map(jc -> "@JoinColumn(name = \"" + jc.getName() + "\""
							+ (Strings.isNotBlank(jc.getReferencedColumnName())
									? ", referencedColumnName = \"" + jc.getReferencedColumnName() + "\""
									: "")
							+ ")")
					.collect(joining(", ")));
			sb.append("}");
		}
		if (jt.getInverseJoinColumns() != null && !jt.getInverseJoinColumns().isEmpty()) {
			sb.append(", inverseJoinColumns = {");
			sb.append(jt.getInverseJoinColumns().stream()
					.map(jc -> "@JoinColumn(name = \"" + jc.getName() + "\""
							+ (Strings.isNotBlank(jc.getReferencedColumnName())
									? ", referencedColumnName = \"" + jc.getReferencedColumnName() + "\""
									: "")
							+ ")")
					.collect(joining(", ")));
			sb.append("}");
		}
		sb.append(")");
		return sb.toString();
	}

	private record FieldNameAndType(String name, String type) {
	}

	private FieldNameAndType normalizeNameType(String rawName, String rawType) {
		String name = CaseUtils.toCamel(rawName);
		String type = StringUtils.firstNonBlank(rawType, "String");
		return new FieldNameAndType(name, type);
	}

	private boolean isRelationPlaceholder(FieldSpecDTO f) {
		// fields[] is strictly scalar/value objects per our spec; relations come from
		// relations[]
		return false;
	}

	private String resolveJavaType(String rawType, Set<String> imports) {
		return ModelGenerationSupport.resolveJavaType(rawType, imports);
	}

	private void generateMongoSupportClasses(AppSpecDTO spec, Path projectRoot, boolean domainStructure) throws Exception {
		String utilPackage = domainStructure ? basePackage + ".domain.util" : basePackage + ".util";
		Path utilDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(utilPackage));
		Files.createDirectories(utilDir);

		Map<String, Object> utilCtx = Map.of("packageName", utilPackage);
		String sequenceDocContent = tpl.render(TEMPLATE_MONGO_SEQUENCE_DOCUMENT, utilCtx);
		Files.writeString(utilDir.resolve("DatabaseSequence.java"), sequenceDocContent, StandardCharsets.UTF_8);
		String sequenceServiceContent = tpl.render(TEMPLATE_MONGO_SEQUENCE, utilCtx);
		Files.writeString(utilDir.resolve("PrimarySequenceService.java"), sequenceServiceContent, StandardCharsets.UTF_8);

		if (spec == null || spec.getModels() == null) {
			return;
		}

		for (ModelSpecDTO model : spec.getModels()) {
			if (!supportsMongoSequenceListener(model)) {
				continue;
			}
			String modelPkg = resolveModelPackage(model, domainStructure);
			Path modelDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(modelPkg));
			Files.createDirectories(modelDir);
			String entityName = CaseUtils.toPascal(model.getName());
			Map<String, Object> listenerCtx = new LinkedHashMap<>();
			listenerCtx.put("packageName", modelPkg);
			listenerCtx.put("entityName", entityName);
			listenerCtx.put("sequenceServicePackage", utilPackage);
			listenerCtx.put("intId", isIntegerId(model));

			String listenerContent = tpl.render(TEMPLATE_MONGO_LISTENER, listenerCtx);
			Files.writeString(modelDir.resolve(entityName + "Listener.java"), listenerContent, StandardCharsets.UTF_8);
		}
	}

	private boolean supportsMongoSequenceListener(ModelSpecDTO model) {
		if (model == null || model.getId() == null) {
			return false;
		}
		String raw = StringUtils.firstNonBlank(model.getId().getType(), "").trim();
		return "Long".equalsIgnoreCase(raw) || "long".equalsIgnoreCase(raw)
				|| "Integer".equalsIgnoreCase(raw) || "int".equalsIgnoreCase(raw);
	}

	private boolean isIntegerId(ModelSpecDTO model) {
		if (model == null || model.getId() == null) {
			return false;
		}
		String raw = StringUtils.firstNonBlank(model.getId().getType(), "").trim();
		return "Integer".equalsIgnoreCase(raw) || "int".equalsIgnoreCase(raw);
	}

	@SuppressWarnings("unchecked")
	private boolean isNoSqlDatabase(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object dbTypeRaw = yaml.get("dbType");
		if (dbTypeRaw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			dbTypeRaw = ((Map<String, Object>) appRaw).get("dbType");
		}
		if (dbTypeRaw != null && "NOSQL".equalsIgnoreCase(String.valueOf(dbTypeRaw).trim())) {
			return true;
		}
		Object dbRaw = yaml.get("database");
		if (dbRaw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			dbRaw = ((Map<String, Object>) appRaw).get("database");
		}
		return dbRaw != null && "MONGODB".equalsIgnoreCase(String.valueOf(dbRaw).trim());
	}
}
