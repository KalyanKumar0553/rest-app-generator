package com.src.main.service;

import static java.util.stream.Collectors.joining;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ColumnSpecDTO;
import com.src.main.dto.ConstraintDTO;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.GenerationSpecDTO;
import com.src.main.dto.JoinColumnSpecDTO;
import com.src.main.dto.JoinTableSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.RelationSpecDTO;
import com.src.main.utils.CaseUtils;
import com.src.main.utils.PathUtils;
import com.src.main.utils.StringUtils;

/**
 * Converts YAML models into Java entity classes using Mustache.
 * Focus: JPA + Bean Validation + Relationships + Auditing + Composite Uniques.
 */
public class ModelGenerator {

    private static final Logger log = LoggerFactory.getLogger(ModelGenerator.class);

    private static final String TEMPLATE_MODEL = "templates/model/model.java.mustache";

    private final TemplateEngine tpl;
    private final ObjectMapper yaml;
    private final String basePackage;

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

        if (spec.getModels() == null || spec.getModels().isEmpty()) {
            return;
        }
        Validate.notEmpty(spec.getModels(), "No models defined in YAML");

        String modelPkg = basePackage + ".model";
        Path outDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(modelPkg));
        Files.createDirectories(outDir);

        for (ModelSpecDTO m : spec.getModels()) {
            renderModel(m, modelPkg, outDir, spec);
        }
    }

    private void renderModel(ModelSpecDTO m, String modelPkg, Path outDir, AppSpecDTO root) throws Exception {
        String className = CaseUtils.toPascal(m.getName());
        String tableName = StringUtils.firstNonBlank(m.getTableName(), CaseUtils.toSnake(m.getName()));
        String schema    = Strings.trimToNull(m.getSchema());

        // Build the data model for mustache
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("packageName", modelPkg);
        ctx.put("className", className);

        // Use TreeSet for sorted, de-duplicated imports (same as DTOs)
        Set<String> imports = new TreeSet<>();

        List<String> classAnnotations = new ArrayList<>();

        // ----- @Entity + @Table
        boolean entityEnabled = (m.getOptions() == null) || Boolean.TRUE.equals(m.getOptions().getEntity());
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
                        .map(cols -> "\"" + cols.stream()
                                .map(CaseUtils::toSnake)
                                .collect(joining("\", \"")) + "\"")
                        .collect(joining("}, @UniqueConstraint(columnNames = {",
                                         "@UniqueConstraint(columnNames = {",
                                         "})"));
                parts.add("uniqueConstraints = {" + uniques + "}");
            }
            sb.append(String.join(", ", parts)).append(")");
            classAnnotations.add(sb.toString());
        }

        // Hibernate extras
        if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().getImmutable())) {
            imports.add("org.hibernate.annotations.Immutable");
            classAnnotations.add("@Immutable");
        }
        if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().getNaturalIdCache())) {
            imports.add("org.hibernate.annotations.NaturalIdCache");
            classAnnotations.add("@NaturalIdCache");
        }

        // Lombok
        if (m.getOptions() != null && m.getOptions().getLombok() != null) {
            imports.add("lombok.Getter");
            imports.add("lombok.Setter");
            classAnnotations.add("@Getter");
            classAnnotations.add("@Setter");

            if (Boolean.TRUE.equals(m.getOptions().getLombok().getBuilder())) {
                imports.add("lombok.Builder");
                classAnnotations.add("@Builder");
            }
            if (Boolean.TRUE.equals(m.getOptions().getLombok().getToString())) {
                imports.add("lombok.ToString");
                classAnnotations.add("@ToString");
            }
            if (Boolean.TRUE.equals(m.getOptions().getLombok().getEqualsAndHashCode())) {
                imports.add("lombok.EqualsAndHashCode");
                classAnnotations.add("@EqualsAndHashCode");
            }

            imports.add("lombok.NoArgsConstructor");
            imports.add("lombok.AllArgsConstructor");
            classAnnotations.add("@NoArgsConstructor");
            classAnnotations.add("@AllArgsConstructor");
        }

        ctx.put("classAnnotations", classAnnotations);

        // ----- ID block
        IdBlock id = buildIdBlock(m, imports);
        ctx.put("idBlock", id);

        // ----- Fields (non-relational)
        List<FieldBlock> fields = new ArrayList<>();
        if (m.getFields() != null) {
            for (FieldSpecDTO f : m.getFields()) {
                if (isRelationPlaceholder(f)) {
                    continue;
                }
                fields.add(buildFieldBlock(m, f, imports));
            }
        }
        ctx.put("fields", fields);

        // ----- Relations
        List<RelationBlock> rels = new ArrayList<>();
        if (m.getRelations() != null) {
            for (RelationSpecDTO r : m.getRelations()) {
                rels.add(buildRelationBlock(m, r, imports));
            }
        }
        ctx.put("relations", rels);

        // ----- Auditing
        AuditingBlock auditing = new AuditingBlock();
        if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().getAuditing())) {
            auditing.setEnabled(true);
            // Optional: add imports/annotations for auditing here if you wire them in templates
        }
        ctx.put("auditing", auditing);

        // ----- Soft delete
        boolean softDeleteEnabled = m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().getSoftDelete());
        Map<String, Object> softDelete = Map.of("enabled", softDeleteEnabled);
        ctx.put("softDelete", softDelete);

        // Implement Serializable (if your template uses this)
        imports.add("java.io.Serializable");

        if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().getAuditing())) {
            imports.add("org.springframework.data.annotation.CreatedDate");
            imports.add("org.springframework.data.annotation.LastModifiedDate");
            imports.add("java.time.OffsetDateTime");
            
        }

        ctx.put("implements", "java.io.Serializable");

        // ----- Imports (already de-dup + sorted via TreeSet)
        ctx.put("imports", new ArrayList<>(imports));

        // ----- Write file
        Path outFile = outDir.resolve(className + ".java");
        String content = tpl.render(TEMPLATE_MODEL, ctx);
        Files.createDirectories(outFile.getParent());
        Files.writeString(outFile, content, StandardCharsets.UTF_8);

        log.info("Generated model: {}", outFile);
    }

    private boolean hasCompositeUniques(ModelSpecDTO m) {
        return m.getUniqueConstraints() != null && !m.getUniqueConstraints().isEmpty();
    }

    private IdBlock buildIdBlock(ModelSpecDTO m, Set<String> imports) {
        Validate.notNull(m.getId(), "entity.id missing for %s", m.getName());

        FieldNameAndType nat = normalizeNameType(m.getId().getField(), m.getId().getType());
        IdBlock id = new IdBlock();
        id.setName(nat.name());
        id.setType(resolveJavaType(nat.type(), imports));

        List<String> ann = new ArrayList<>();

        // Always @Id
        imports.add("jakarta.persistence.Id");
        ann.add("@Id");

        GenerationSpecDTO g = m.getId().getGeneration();
        if (g != null && g.getStrategy() != null) {
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

    private FieldBlock buildFieldBlock(ModelSpecDTO m, FieldSpecDTO f, Set<String> imports) {
        FieldNameAndType nat = normalizeNameType(f.getName(), f.getType());
        FieldBlock fb = new FieldBlock();
        fb.setName(nat.name());
        String resolvedType = resolveJavaType(nat.type(), imports);
        fb.setType(resolvedType);

        List<String> ann = new ArrayList<>();

        // @Column (from explicit column or inferred from constraints)
        ColumnSpecDTO col = f.getColumn();
        boolean hasExplicitColumn =
                col != null && (col.getName() != null
                             || col.getLength() != null
                             || col.getNullable() != null
                             || col.getUnique() != null
                             || col.getColumnDefinition() != null);

        // Bean Validation
        if (f.getConstraints() != null) {
            for (ConstraintDTO c : f.getConstraints()) {
                final String name = c.getName();
                if (name == null) continue;

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
                        String re = getStringAny(c.getParams(), new String[]{"regex", "regexp"}, ".*");
                        ann.add(msgAnno("@Pattern(regexp = \"" + escapeJava(re) + "\")", m, f, "Pattern"));
                    }
                    case "Size" -> {
                        imports.add("jakarta.validation.constraints.Size");
                        Integer min = getInt(c.getParams(), "min", null);
                        Integer max = getInt(c.getParams(), "max", null);

                        String args = (min != null ? "min=" + min : "")
                                      + ((min != null && max != null) ? ", " : "")
                                      + (max != null ? "max=" + max : "");
                        ann.add(msgAnno("@Size(" + args + ")", m, f, "Size"));

                        // infer @Column(length=...) for Strings when max provided and no explicit length
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
                        ann.add(msgAnno("@DecimalMin(value=\"" + escapeJava(val) + "\", inclusive=" + inclusive + ")",
                                        m, f, "DecimalMin"));
                    }
                    case "Digits" -> {
                        imports.add("jakarta.validation.constraints.Digits");
                        int integer = getInt(c.getParams(), "integer", 10);
                        int fraction = getInt(c.getParams(), "fraction", 2);
                        ann.add(msgAnno("@Digits(integer=" + integer + ", fraction=" + fraction + ")",
                                        m, f, "Digits"));
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
        if (f.getJpa() != null
                && f.getJpa().getConvert() != null
                && Strings.isNotBlank(f.getJpa().getConvert().getConverter())) {
            imports.add("jakarta.persistence.Convert");
            ann.add("@Convert(converter = " + f.getJpa().getConvert().getConverter() + ".class)");
        }

        // @NaturalId
        if (Boolean.TRUE.equals(f.getNaturalId())) {
            imports.add("org.hibernate.annotations.NaturalId");
            ann.add("@NaturalId");
        }

        // @Column
        if (col != null && (hasExplicitColumn
                || col.getNullable() != null
                || col.getUnique() != null
                || col.getLength() != null
                || col.getColumnDefinition() != null
                || col.getName() != null)) {

            imports.add("jakarta.persistence.Column");
            StringBuilder sb = new StringBuilder("@Column(");
            List<String> args = new ArrayList<>();
            if (col.getName() != null) args.add("name = \"" + col.getName() + "\"");
            if (col.getUnique() != null) args.add("unique = " + col.getUnique());
            if (col.getNullable() != null) args.add("nullable = " + col.getNullable());
            if (col.getLength() != null) args.add("length = " + col.getLength());
            if (col.getColumnDefinition() != null)
                args.add("columnDefinition = \"" + escapeJava(col.getColumnDefinition()) + "\"");
            sb.append(String.join(", ", args)).append(")");
            ann.add(sb.toString());
        }

        fb.setAnnotations(ann);
        return fb;
    }

    /* ==== helpers (null-safe, type-safe, with defaults) ==== */

    private static String getString(Map<String, Object> m, String key, String def) {
        if (m == null) return def;
        Object v = m.get(key);
        return (v == null) ? def : String.valueOf(v);
    }

    private static String getStringAny(Map<String, Object> m, String[] keys, String def) {
        if (m == null) return def;
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null) return String.valueOf(v);
        }
        return def;
    }

    private static Integer getInt(Map<String, Object> m, String key, Integer def) {
        if (m == null) return def;
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private static Long getLong(Map<String, Object> m, String key, Long def) {
        if (m == null) return def;
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean getBoolean(Map<String, Object> m, String key, boolean def) {
        if (m == null) return def;
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Boolean b) return b;
        String s = String.valueOf(v).trim().toLowerCase();
        return switch (s) {
            case "true", "1", "yes", "y" -> true;
            case "false", "0", "no", "n" -> false;
            default -> def;
        };
    }

    private ColumnSpecDTO ensureColumn(ColumnSpecDTO col) {
        return col != null ? col : new ColumnSpecDTO();
    }

    private RelationBlock buildRelationBlock(ModelSpecDTO m, RelationSpecDTO r, Set<String> imports) {
        RelationBlock rb = new RelationBlock();
        rb.setName(CaseUtils.toCamel(r.getName()));

        String targetType = CaseUtils.toPascal(r.getTarget());
        String fieldType;
        List<String> ann = new ArrayList<>();

        switch (r.getType()) {
            case OneToMany -> {
                imports.add("jakarta.persistence.OneToMany");
                imports.add("jakarta.persistence.FetchType");
                String mappedBy = Validate.notBlank(r.getMappedBy(),
                        "OneToMany mappedBy required on %s.%s", m.getName(), r.getName());
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
        if (cascade == null || cascade.isEmpty()) return "";
        return ", cascade = {" +
                cascade.stream()
                        .map(s -> "jakarta.persistence.CascadeType." + s)
                        .collect(joining(", ")) +
                "}";
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

    private record FieldNameAndType(String name, String type) {}

    private FieldNameAndType normalizeNameType(String rawName, String rawType) {
        String name = CaseUtils.toCamel(rawName);
        String type = StringUtils.firstNonBlank(rawType, "String");
        return new FieldNameAndType(name, type);
    }

    private String buildMinAnnotation(long value, String msgKey) {
        if (msgKey != null) {
            return String.format("@Min(value = %d, message = \"{%s}\")", value, msgKey);
        }
        return String.format("@Min(value = %d)", value);
    }

    private String buildMaxAnnotation(long value, String msgKey) {
        if (msgKey != null) {
            return String.format("@Max(value = %d, message = \"{%s}\")", value, msgKey);
        }
        return String.format("@Max(value = %d)", value);
    }

    private String buildMessageKey(ModelSpecDTO m, FieldSpecDTO f, String key) {
        return "validation."
                + CaseUtils.toSnake(m.getName()) + "."
                + CaseUtils.toSnake(f.getName()) + "."
                + key;
    }

    private String resolveJavaType(String rawType, Set<String> imports) {
        if (rawType == null) return null;

        String type = rawType.trim();

        // 1) Handle generics: List<Address>, Map<String, Integer> etc.
        if (type.contains("<") && type.contains(">")) {
            String outer = type.substring(0, type.indexOf("<")).trim();
            String inner = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">")).trim();

            // Import outer generic type
            addImportIfNeeded(outer, imports);

            // Recursively resolve inner type(s)
            String resolvedInner;
            if (inner.contains(",")) {
                // Map<K,V>
                String[] parts = inner.split(",");
                resolvedInner = Arrays.stream(parts)
                        .map(String::trim)
                        .map(t -> resolveJavaType(t, imports))
                        .collect(Collectors.joining(", "));
            } else {
                resolvedInner = resolveJavaType(inner, imports);
            }

            return outer + "<" + resolvedInner + ">";
        }

        // 2) Non-generic simple types
        return switch (type) {
            case "String" -> {
                imports.add("java.lang.String");
                yield "String";
            }
            case "Integer" -> {
                imports.add("java.lang.Integer");
                yield "Integer";
            }
            case "Long" -> {
                imports.add("java.lang.Long");
                yield "Long";
            }
            case "Boolean" -> {
                imports.add("java.lang.Boolean");
                yield "Boolean";
            }
            case "BigDecimal" -> {
                imports.add("java.math.BigDecimal");
                yield "BigDecimal";
            }
            case "UUID" -> {
                imports.add("java.util.UUID");
                yield "UUID";
            }
            case "LocalDate" -> {
                imports.add("java.time.LocalDate");
                yield "LocalDate";
            }
            case "LocalDateTime" -> {
                imports.add("java.time.LocalDateTime");
                yield "LocalDateTime";
            }
            case "OffsetDateTime" -> {
                imports.add("java.time.OffsetDateTime");
                yield "OffsetDateTime";
            }
            default -> {
                // 3) Custom classes or fully-qualified types
                if (type.contains(".")) {
                    imports.add(type);
                    yield type.substring(type.lastIndexOf('.') + 1);
                }
                yield type;
            }
        };
    }

    private void addImportIfNeeded(String outer, Set<String> imports) {
        switch (outer) {
            case "List" -> imports.add("java.util.List");
            case "Set"  -> imports.add("java.util.Set");
            case "Map"  -> imports.add("java.util.Map");
            default -> {
                if (outer.contains(".")) {
                    imports.add(outer);
                }
            }
        }
    }

    private boolean isRelationPlaceholder(FieldSpecDTO f) {
        // fields[] is strictly scalar/value objects per our spec; relations come from relations[]
        return false;
    }

    private String msgAnno(String base, ModelSpecDTO m, FieldSpecDTO f, String key) {
        String messageKey = "validation."
                + CaseUtils.toSnake(m.getName()) + "."
                + CaseUtils.toSnake(f.getName()) + "."
                + key;
        if (base.contains("(")) {
            return base.substring(0, base.length() - 1) + ", message=\"{" + messageKey + "}\")";
        }
        return base + "(message=\"{" + messageKey + "}\")";
    }

    private String escapeJava(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // --- Mustache blocks (simple DTOs) ---

    public static class IdBlock {
        private String name;
        private String type;
        private List<String> annotations;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<String> annotations) {
            this.annotations = annotations;
        }
    }

    public static class FieldBlock {
        private String name;
        private String type;
        private List<String> annotations;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<String> annotations) {
            this.annotations = annotations;
        }
    }

    public static class RelationBlock {
        private String name;
        private String declarationType;
        private String targetType;
        private List<String> annotations;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDeclarationType() {
            return declarationType;
        }

        public void setDeclarationType(String declarationType) {
            this.declarationType = declarationType;
        }

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public List<String> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<String> annotations) {
            this.annotations = annotations;
        }
    }

    public static class AuditingBlock {
        private boolean enabled;
        private List<String> annotations = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(List<String> annotations) {
            this.annotations = annotations;
        }
    }
}
