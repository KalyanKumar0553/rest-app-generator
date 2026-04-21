package com.src.main.sm.executor.model;

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
import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ClassMethodsSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.BoilerplateStyle;
import com.src.main.sm.executor.common.BoilerplateStyleResolver;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.sm.executor.enumgen.EnumGenerationSupport;
import com.src.main.sm.executor.enumgen.EnumSpecResolved;
import com.src.main.util.PathUtils;

/**
 * Converts YAML model specs into entity source files using Mustache templates.
 *
 * <p>Responsibility: <em>coordinating</em> entity generation — building the
 * Mustache context and writing the output file. Annotation-building details are
 * delegated to:
 * <ul>
 *   <li>{@link IdAnnotationBuilder} — primary-key field and generation strategy</li>
 *   <li>{@link FieldAnnotationBuilder} — scalar fields (JPA + Bean Validation)</li>
 *   <li>{@link RelationAnnotationBuilder} — JPA / MongoDB relationships</li>
 * </ul>
 * MongoDB support-file generation is handled by {@link MongoSupportFileGenerator}.
 */
public class ModelGenerator {

    private static final Logger log = LoggerFactory.getLogger(ModelGenerator.class);

    private static final String TPL_MODEL_JAVA = "model.java.mustache";
    private static final String TPL_MODEL_KT   = "model.kt.mustache";

    private final TemplateEngine tpl;
    private final String basePackage;
    private final GenerationLanguage language;

    private record ClassMethodsSelection(
            boolean generateToString,
            boolean generateEquals,
            boolean generateHashCode,
            boolean noArgsConstructor,
            boolean allArgsConstructor,
            boolean builder) {
    }

    public ModelGenerator(TemplateEngine tpl, String basePackage, GenerationLanguage language) {
        this.tpl = tpl;
        this.basePackage = basePackage;
        this.language = language == null ? GenerationLanguage.JAVA : language;
    }

    // ── public API ────────────────────────────────────────────────────────────

    public void generate(Map<String, Object> yaml, Path projectRoot) throws Exception {
        Validate.notNull(yaml, "YAML map must not be null");
        Validate.notNull(projectRoot, "projectRoot must not be null");

        AppSpecDTO spec = new ObjectMapper().convertValue(yaml, AppSpecDTO.class);
        if (spec.getModels() == null || spec.getModels().isEmpty()) return;

        BoilerplateStyle boilerplateStyle = BoilerplateStyleResolver.resolveFromYaml(yaml, true);
        boolean noSql = isNoSqlDatabase(yaml);

        Map<String, String> validationMessages = ModelGenerationSupport.collectValidationMessages(spec);
        ModelGenerationSupport.mergeValidationMessagesIntoYaml(yaml, validationMessages);

        boolean domainLayout = "domain".equalsIgnoreCase(
                StringUtils.firstNonBlank(spec.getPackages(), "technical"));
        String enumPackage = EnumGenerationSupport.resolveEnumPackage(basePackage, spec.getPackages());
        Map<String, EnumSpecResolved> enumByName = EnumGenerationSupport.byName(
                EnumGenerationSupport.resolveEnums(spec.getEnums()));

        Map<String, String> modelPackageByType = buildModelPackageIndex(spec, domainLayout);

        for (ModelSpecDTO model : spec.getModels()) {
            String modelPkg = resolveModelPackage(model, domainLayout);
            Path outDir = projectRoot.resolve(PathUtils.srcPathFromPackage(modelPkg, language));
            Files.createDirectories(outDir);
            renderEntity(model, modelPkg, outDir, spec, modelPackageByType,
                    enumByName, enumPackage, boilerplateStyle, noSql);
        }
    }

    // ── entity rendering ──────────────────────────────────────────────────────

    private void renderEntity(ModelSpecDTO model, String modelPkg, Path outDir,
                               AppSpecDTO root, Map<String, String> modelPackageByType,
                               Map<String, EnumSpecResolved> enumByName, String enumPackage,
                               BoilerplateStyle boilerplateStyle, boolean noSql) throws Exception {

        String className = JavaNamingUtils.toJavaTypeName(model.getName(), "Entity");
        Set<String> imports = new TreeSet<>();
        List<String> classAnnotations = new ArrayList<>();

        buildClassAnnotations(model, root, noSql, imports, classAnnotations);

        IdBlock id = IdAnnotationBuilder.build(model, imports, noSql);
        List<FieldBlock> fields = buildFields(model, imports, enumByName, enumPackage, modelPkg, noSql);
        List<RelationBlock> relations = buildRelations(model, imports, modelPkg, modelPackageByType, noSql);
        AuditingBlock auditing = buildAuditing(model, imports);
        boolean softDelete = model.getOptions() != null && Boolean.TRUE.equals(model.getOptions().isSoftDelete());

        addStandardImports(auditing, softDelete, noSql, imports);

        List<Map<String, Object>> properties = buildPropertyList(id, fields, relations, auditing, softDelete, noSql);

        ClassMethodsSelection methods = resolveClassMethods(model);
        ModelBoilerplateContext boilerplateCtx = new ModelBoilerplateContext(
                model, className, imports, classAnnotations, properties, true,
                methods.generateToString(), methods.generateEquals(), methods.generateHashCode(),
                methods.noArgsConstructor(), methods.allArgsConstructor(), methods.builder());
        ModelBoilerplateStrategyFactory.forStyle(boilerplateStyle).apply(boilerplateCtx);

        if (!boilerplateCtx.isUseLombok() && (methods.generateEquals() || methods.generateHashCode())) {
            imports.add("java.util.Objects");
        }

        Map<String, Object> ctx = buildMustacheContext(modelPkg, className, id, fields, relations,
                auditing, softDelete, noSql, imports, classAnnotations, properties, boilerplateCtx, methods);

        String templateFile = language.selectTemplate(TPL_MODEL_JAVA, TPL_MODEL_KT);
        String content = tpl.renderAny(TemplatePathResolver.candidates(language, "model", templateFile), ctx);
        Path outFile = outDir.resolve(className + "." + language.fileExtension());
        Files.createDirectories(outFile.getParent());
        Files.writeString(outFile, content, StandardCharsets.UTF_8);
        log.info("Generated entity: {}", outFile);
    }

    // ── context assembly ──────────────────────────────────────────────────────

    private void buildClassAnnotations(ModelSpecDTO model, AppSpecDTO root, boolean noSql,
                                        Set<String> imports, List<String> classAnnotations) {
        String tableName = resolveTableName(model, root);
        String schema = Strings.trimToNull(model.getSchema());

        if (noSql) {
            imports.add("org.springframework.data.mongodb.core.mapping.Document");
            classAnnotations.add("@Document(\"" + tableName + "\")");
            return;
        }

        boolean entityEnabled = model.getOptions() == null || Boolean.TRUE.equals(model.getOptions().isEntity());
        if (entityEnabled) {
            imports.add("jakarta.persistence.Entity");
            classAnnotations.add("@Entity");
        }

        if (schema != null || model.getTableName() != null || hasCompositeUniques(model)) {
            imports.add("jakarta.persistence.Table");
            classAnnotations.add(buildTableAnnotation(tableName, schema, model, imports));
        }

        if (model.getOptions() != null && Boolean.TRUE.equals(model.getOptions().isImmutable())) {
            imports.add("org.hibernate.annotations.Immutable");
            classAnnotations.add("@Immutable");
        }
        if (model.getOptions() != null && Boolean.TRUE.equals(model.getOptions().isNaturalIdCache())) {
            imports.add("org.hibernate.annotations.NaturalIdCache");
            classAnnotations.add("@NaturalIdCache");
        }
    }

    private String buildTableAnnotation(String tableName, String schema,
                                         ModelSpecDTO model, Set<String> imports) {
        List<String> parts = new ArrayList<>();
        parts.add("name = \"" + tableName + "\"");
        if (schema != null) parts.add("schema = \"" + schema + "\"");
        if (hasCompositeUniques(model)) {
            imports.add("jakarta.persistence.UniqueConstraint");
            String uniques = model.getUniqueConstraints().stream()
                    .map(cols -> "\"" + cols.stream().map(CaseUtils::toSnake).collect(joining("\", \"")) + "\"")
                    .collect(joining("}, @UniqueConstraint(columnNames = {",
                            "@UniqueConstraint(columnNames = {", "})"));
            parts.add("uniqueConstraints = {" + uniques + "}");
        }
        return "@Table(" + String.join(", ", parts) + ")";
    }

    private List<FieldBlock> buildFields(ModelSpecDTO model, Set<String> imports,
                                          Map<String, EnumSpecResolved> enumByName,
                                          String enumPackage, String modelPkg, boolean noSql) {
        if (model.getFields() == null) return List.of();
        return model.getFields().stream()
                .map(f -> FieldAnnotationBuilder.build(model, f, imports, enumByName,
                        enumPackage, modelPkg, noSql))
                .toList();
    }

    private List<RelationBlock> buildRelations(ModelSpecDTO model, Set<String> imports,
                                                String modelPkg,
                                                Map<String, String> modelPackageByType,
                                                boolean noSql) {
        if (model.getRelations() == null) return List.of();
        return model.getRelations().stream()
                .map(r -> RelationAnnotationBuilder.build(model, r, imports, modelPkg,
                        modelPackageByType, noSql))
                .toList();
    }

    private AuditingBlock buildAuditing(ModelSpecDTO model, Set<String> imports) {
        AuditingBlock auditing = new AuditingBlock();
        if (model.getOptions() != null && Boolean.TRUE.equals(model.getOptions().isAuditing())) {
            auditing.setEnabled(true);
        }
        return auditing;
    }

    private void addStandardImports(AuditingBlock auditing, boolean softDelete,
                                     boolean noSql, Set<String> imports) {
        imports.add("java.io.Serializable");
        if (Boolean.TRUE.equals(auditing.isEnabled())) {
            imports.add("org.springframework.data.annotation.CreatedDate");
            imports.add("org.springframework.data.annotation.LastModifiedDate");
            imports.add("java.time.OffsetDateTime");
        }
        if (softDelete) imports.add("java.time.OffsetDateTime");
        if (noSql)      imports.add("org.springframework.data.annotation.Version");
    }

    private List<Map<String, Object>> buildPropertyList(IdBlock id, List<FieldBlock> fields,
                                                         List<RelationBlock> relations,
                                                         AuditingBlock auditing,
                                                         boolean softDelete, boolean noSql) {
        List<Map<String, Object>> properties = new ArrayList<>();
        if (id != null) properties.add(propertyEntry(id.getType(), id.getName()));
        fields.forEach(f -> properties.add(propertyEntry(f.getType(), f.getName())));
        relations.forEach(r -> properties.add(propertyEntry(r.getDeclarationType(), r.getName())));
        if (Boolean.TRUE.equals(auditing.isEnabled())) {
            properties.add(propertyEntry("OffsetDateTime", "createdAt"));
            properties.add(propertyEntry("OffsetDateTime", "updatedAt"));
        }
        if (softDelete) {
            properties.add(propertyEntry("Boolean", "deleted"));
            properties.add(propertyEntry("OffsetDateTime", "deletedAt"));
        }
        if (noSql) properties.add(propertyEntry("Integer", "version"));

        for (int i = 0; i < properties.size(); i++) {
            properties.get(i).put("isLast", i == properties.size() - 1);
        }
        return properties;
    }

    private Map<String, Object> buildMustacheContext(String modelPkg, String className, IdBlock id,
                                                      List<FieldBlock> fields,
                                                      List<RelationBlock> relations,
                                                      AuditingBlock auditing, boolean softDelete,
                                                      boolean noSql, Set<String> imports,
                                                      List<String> classAnnotations,
                                                      List<Map<String, Object>> properties,
                                                      ModelBoilerplateContext boilerplateCtx,
                                                      ClassMethodsSelection methods) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("packageName", modelPkg);
        ctx.put("className", className);
        ctx.put("classAnnotations", classAnnotations);
        ctx.put("idBlock", id);
        ctx.put("fields", fields);
        ctx.put("relations", relations);
        ctx.put("auditing", auditing);
        ctx.put("softDelete", Map.of("enabled", softDelete));
        ctx.put("properties", properties);
        ctx.put("hasProperties", !properties.isEmpty());
        ctx.put("implements", "Serializable");
        ctx.put("noSql", noSql);
        ctx.put("useLombok", boilerplateCtx.isUseLombok());
        ctx.put("generateNoArgsConstructor",  !boilerplateCtx.isUseLombok() && methods.noArgsConstructor());
        ctx.put("generateAllArgsConstructor", !boilerplateCtx.isUseLombok() && methods.allArgsConstructor());
        ctx.put("generateBuilder",    !boilerplateCtx.isUseLombok() && methods.builder());
        ctx.put("generateToString",   !boilerplateCtx.isUseLombok() && methods.generateToString());
        ctx.put("generateEquals",     !boilerplateCtx.isUseLombok() && methods.generateEquals());
        ctx.put("generateHashCode",   !boilerplateCtx.isUseLombok() && methods.generateHashCode());
        ctx.put("imports", new ArrayList<>(imports));
        return ctx;
    }

    // ── package / naming helpers ──────────────────────────────────────────────

    private Map<String, String> buildModelPackageIndex(AppSpecDTO spec, boolean domainLayout) {
        Map<String, String> index = new LinkedHashMap<>();
        spec.getModels().forEach(m -> {
            String className = JavaNamingUtils.toJavaTypeName(m.getName(), "Entity");
            index.put(className, resolveModelPackage(m, domainLayout));
        });
        return index;
    }

    private String resolveModelPackage(ModelSpecDTO model, boolean domainLayout) {
        if (!domainLayout) return basePackage + ".model";
        String segment = CaseUtils.toSnake(StringUtils.firstNonBlank(model.getName(), "entity"))
                .replaceAll("[^a-zA-Z0-9_]", "_").replaceAll("_+", "_").toLowerCase();
        if (segment.isBlank()) segment = "entity";
        if (!Character.isJavaIdentifierStart(segment.charAt(0))) segment = "x_" + segment;
        return basePackage + ".domain." + segment + ".model";
    }

    private String resolveTableName(ModelSpecDTO model, AppSpecDTO root) {
        String tableName = StringUtils.firstNonBlank(model.getTableName(), CaseUtils.toSnake(model.getName()));
        return Boolean.TRUE.equals(root.getPluralizeTableNames()) ? pluralize(tableName) : tableName;
    }

    /** English pluralization for snake_case table names. */
    private static String pluralize(String name) {
        if (name == null || name.isBlank()) return name;
        if (name.endsWith("_")) return pluralize(name.substring(0, name.length() - 1)) + "_";
        if (name.endsWith("ies")) return name;
        if (name.endsWith("s") || name.endsWith("x") || name.endsWith("z")
                || name.endsWith("ch") || name.endsWith("sh")) return name + "es";
        if (name.endsWith("y") && name.length() > 1) {
            char before = name.charAt(name.length() - 2);
            if (before != 'a' && before != 'e' && before != 'i' && before != 'o' && before != 'u') {
                return name.substring(0, name.length() - 1) + "ies";
            }
        }
        return name + "s";
    }

    // ── miscellaneous helpers ─────────────────────────────────────────────────

    private ClassMethodsSelection resolveClassMethods(ModelSpecDTO model) {
        ClassMethodsSpecDTO m = model.getClassMethods();
        if (m != null) {
            return new ClassMethodsSelection(
                    Boolean.TRUE.equals(m.getToString()),
                    Boolean.TRUE.equals(m.getEquals()),
                    Boolean.TRUE.equals(m.getHashCode()),
                    !Boolean.FALSE.equals(m.getNoArgsConstructor()),
                    !Boolean.FALSE.equals(m.getAllArgsConstructor()),
                    Boolean.TRUE.equals(m.getBuilder()));
        }
        boolean builder = false, toString = false, equalsHash = false;
        if (model.getOptions() != null && model.getOptions().getLombok() != null) {
            builder    = Boolean.TRUE.equals(model.getOptions().getLombok().getBuilder());
            toString   = Boolean.TRUE.equals(model.getOptions().getLombok().getToString());
            equalsHash = Boolean.TRUE.equals(model.getOptions().getLombok().getEqualsAndHashCode());
        }
        return new ClassMethodsSelection(toString, equalsHash, equalsHash, true, true, builder);
    }

    private static Map<String, Object> propertyEntry(String type, String name) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("type", type);
        p.put("name", name);
        p.put("method", Character.toUpperCase(name.charAt(0)) + name.substring(1));
        return p;
    }

    private static boolean hasCompositeUniques(ModelSpecDTO m) {
        return m.getUniqueConstraints() != null && !m.getUniqueConstraints().isEmpty();
    }

    private static boolean isNoSqlDatabase(Map<String, Object> yaml) {
        if (yaml == null) return false;
        Object dbType = com.src.main.sm.executor.common.LayeredSpecSupport.resolveDatabaseType(yaml);
        if (dbType != null && "NOSQL".equalsIgnoreCase(String.valueOf(dbType).trim())) return true;
        Object dbCode = com.src.main.sm.executor.common.LayeredSpecSupport.resolveDatabaseCode(yaml);
        return dbCode != null && "MONGODB".equalsIgnoreCase(String.valueOf(dbCode).trim());
    }

    // ── JavaNamingUtils forwarding (avoids static import collision) ───────────

    private static final class JavaNamingUtils {
        private JavaNamingUtils() {}
        static String toJavaTypeName(String name, String suffix) {
            return com.src.main.sm.executor.common.JavaNamingUtils.toJavaTypeName(name, suffix);
        }
    }
}
