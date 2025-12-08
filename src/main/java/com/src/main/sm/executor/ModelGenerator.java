package com.src.main.sm.executor;

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
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.AuditingBlockDTO;
import com.src.main.dto.FieldBlockDTO;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.IdBlockDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.RelationBlockDTO;
import com.src.main.dto.RelationSpecDTO;
import com.src.main.util.CaseUtils;
import com.src.main.util.ModelGeneratorBuilderUtils;
import com.src.main.util.PathUtils;
import com.src.main.util.StringUtils;

/**
 * Converts YAML models into Java entity classes using Mustache. Focus: JPA +
 * Bean Validation + Relationships + Auditing + Composite Uniques.
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
        String schema = Strings.trimToNull(m.getSchema());

        // Build the data model for mustache
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("packageName", modelPkg);
        ctx.put("className", className);

        // Use TreeSet for sorted, de-duplicated imports (same as DTOs)
        Set<String> imports = new TreeSet<>();

        List<String> classAnnotations = new ArrayList<>();

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

        // Hibernate extras
        if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().isImmutable())) {
            imports.add("org.hibernate.annotations.Immutable");
            classAnnotations.add("@Immutable");
        }
        if (m.getOptions() != null && Boolean.TRUE.equals(m.getOptions().isNaturalIdCache())) {
            imports.add("org.hibernate.annotations.NaturalIdCache");
            classAnnotations.add("@NaturalIdCache");
        }

        // ----- Lombok detection -----
        boolean lombokEnabled = m.getOptions() != null && m.getOptions().getLombok() != null;
        ctx.put("lombokEnabled", lombokEnabled);

        // Lombok annotations/imports (only if enabled)
        if (lombokEnabled) {
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
        IdBlockDTO id = ModelGeneratorBuilderUtils.buildIdBlock(m, imports);
        ctx.put("idBlock", id);

        // ----- Fields (non-relational)
        List<FieldBlockDTO> fields = new ArrayList<>();
        if (m.getFields() != null) {
            for (FieldSpecDTO f : m.getFields()) {
                if (isRelationPlaceholder(f)) {
                    continue;
                }
                fields.add(ModelGeneratorBuilderUtils.buildFieldBlock(m, f, imports));
            }
        }
        ctx.put("fields", fields);

        // ----- Relations
        List<RelationBlockDTO> rels = new ArrayList<>();
        if (m.getRelations() != null) {
            for (RelationSpecDTO r : m.getRelations()) {
                rels.add(ModelGeneratorBuilderUtils.buildRelationBlock(m, r, imports));
            }
        }
        ctx.put("relations", rels);

        // ----- Auditing
        AuditingBlockDTO auditing = new AuditingBlockDTO();
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

    private boolean isRelationPlaceholder(FieldSpecDTO f) {
        return false;
    }

}
