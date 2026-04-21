package com.src.main.sm.executor.model;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.util.Strings;

import com.src.main.common.util.CaseUtils;
import com.src.main.dto.JoinColumnSpecDTO;
import com.src.main.dto.JoinTableSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.RelationSpecDTO;
import com.src.main.sm.executor.common.JavaNamingUtils;

/**
 * Builds a {@link RelationBlock} (field type + JPA / MongoDB relation
 * annotations) for a single relationship on an entity.
 *
 * <p>Extracted from {@code ModelGenerator} (SRP): this class owns the mapping
 * from a {@code RelationSpecDTO} to its annotated Java/Kotlin field.
 */
public final class RelationAnnotationBuilder {

    private RelationAnnotationBuilder() {
    }

    /**
     * @param model                the owning entity spec
     * @param relation             the relation spec to process
     * @param imports              mutable import set — FQCNs are added here
     * @param currentModelPackage  package of the owning entity
     * @param modelPackageByType   map of entity simple-name → package (for cross-package imports)
     * @param noSqlDatabase        {@code true} when the target is MongoDB
     * @return fully populated {@link RelationBlock}
     */
    public static RelationBlock build(ModelSpecDTO model, RelationSpecDTO relation,
                                       Set<String> imports, String currentModelPackage,
                                       Map<String, String> modelPackageByType,
                                       boolean noSqlDatabase) {
        RelationBlock rb = new RelationBlock();
        rb.setName(CaseUtils.toCamel(relation.getName()));

        String targetType = JavaNamingUtils.toJavaTypeName(relation.getTarget(), "Entity");
        String targetModelPackage = modelPackageByType.get(targetType);
        if (targetModelPackage != null && !targetModelPackage.equals(currentModelPackage)) {
            imports.add(targetModelPackage + "." + targetType);
        }

        if (noSqlDatabase) {
            buildMongoRelation(relation, targetType, imports, rb);
        } else {
            buildJpaRelation(model, relation, targetType, imports, rb);
        }
        return rb;
    }

    // ── MongoDB ──────────────────────────────────────────────────────────────

    private static void buildMongoRelation(RelationSpecDTO relation, String targetType,
                                            Set<String> imports, RelationBlock rb) {
        imports.add("org.springframework.data.mongodb.core.mapping.DocumentReference");
        List<String> annotations = List.of("@DocumentReference(lazy = true)");

        String fieldType = switch (relation.getType()) {
            case OneToMany, ManyToMany -> {
                imports.add("java.util.List");
                yield "List<" + targetType + ">";
            }
            default -> targetType;
        };
        rb.setAnnotations(annotations);
        rb.setDeclarationType(fieldType);
        rb.setTargetType(targetType);
    }

    // ── JPA ──────────────────────────────────────────────────────────────────

    private static void buildJpaRelation(ModelSpecDTO model, RelationSpecDTO relation,
                                          String targetType, Set<String> imports,
                                          RelationBlock rb) {
        List<String> annotations = new ArrayList<>();
        String fieldType;

        switch (relation.getType()) {
            case OneToOne -> {
                imports.add("jakarta.persistence.OneToOne");
                imports.add("jakarta.persistence.FetchType");
                String cascade = toCascadeArray(relation.getCascade());
                String optional = relation.getOptional() == null ? "true" : relation.getOptional().toString();
                String orphan = Boolean.TRUE.equals(relation.getOrphanRemoval()) ? ", orphanRemoval = true" : "";

                if (Strings.isNotBlank(relation.getMappedBy())) {
                    annotations.add("@OneToOne(mappedBy = \"" + relation.getMappedBy().trim()
                            + "\", fetch = FetchType.LAZY, optional = " + optional + cascade + orphan + ")");
                } else {
                    annotations.add("@OneToOne(fetch = FetchType.LAZY, optional = " + optional + cascade + orphan + ")");
                    appendJoinColumn(relation.getJoinColumn(), imports, annotations);
                }
                fieldType = targetType;
            }
            case OneToMany -> {
                imports.add("jakarta.persistence.OneToMany");
                imports.add("jakarta.persistence.FetchType");
                String mappedBy = Validate.notBlank(relation.getMappedBy(),
                        "OneToMany mappedBy required on %s.%s", model.getName(), relation.getName());
                String cascade = toCascadeArray(relation.getCascade());
                String orphan = Boolean.TRUE.equals(relation.getOrphanRemoval()) ? ", orphanRemoval = true" : "";
                annotations.add("@OneToMany(mappedBy = \"" + mappedBy + "\", fetch = FetchType.LAZY" + cascade + orphan + ")");

                if (Strings.isNotBlank(relation.getOrderBy())) {
                    imports.add("jakarta.persistence.OrderBy");
                    annotations.add("@OrderBy(\"" + relation.getOrderBy().trim() + "\")");
                }
                if (relation.getOrderColumn() != null && Strings.isNotBlank(relation.getOrderColumn().getName())) {
                    imports.add("jakarta.persistence.OrderColumn");
                    annotations.add("@OrderColumn(name = \"" + relation.getOrderColumn().getName() + "\")");
                }
                imports.add("java.util.List");
                fieldType = "List<" + targetType + ">";
            }
            case ManyToOne -> {
                imports.add("jakarta.persistence.ManyToOne");
                imports.add("jakarta.persistence.FetchType");
                String optional = relation.getOptional() == null ? "true" : relation.getOptional().toString();
                annotations.add("@ManyToOne(fetch = FetchType.LAZY, optional = " + optional + ")");
                appendJoinColumn(relation.getJoinColumn(), imports, annotations);
                fieldType = targetType;
            }
            case ManyToMany -> {
                imports.add("jakarta.persistence.ManyToMany");
                imports.add("jakarta.persistence.FetchType");
                String cascade = toCascadeArray(relation.getCascade());
                annotations.add("@ManyToMany(fetch = FetchType.LAZY" + cascade + ")");
                if (relation.getJoinTable() != null) {
                    imports.add("jakarta.persistence.JoinTable");
                    imports.add("jakarta.persistence.JoinColumn");
                    annotations.add(buildJoinTableAnnotation(relation.getJoinTable()));
                }
                imports.add("java.util.Set");
                fieldType = "Set<" + targetType + ">";
            }
            default -> throw new IllegalStateException("Unsupported relation type: " + relation.getType());
        }

        rb.setAnnotations(annotations);
        rb.setDeclarationType(fieldType);
        rb.setTargetType(targetType);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static void appendJoinColumn(JoinColumnSpecDTO jc, Set<String> imports,
                                          List<String> annotations) {
        if (jc == null) return;
        imports.add("jakarta.persistence.JoinColumn");
        List<String> args = new ArrayList<>();
        if (Strings.isNotBlank(jc.getName()))                   args.add("name = \"" + jc.getName() + "\"");
        if (jc.getNullable() != null)                           args.add("nullable = " + jc.getNullable());
        if (Strings.isNotBlank(jc.getReferencedColumnName()))   args.add("referencedColumnName = \"" + jc.getReferencedColumnName() + "\"");
        annotations.add("@JoinColumn(" + String.join(", ", args) + ")");
    }

    private static String toCascadeArray(List<String> cascade) {
        if (cascade == null || cascade.isEmpty()) return "";
        List<String> normalized = cascade.stream()
                .filter(Strings::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();
        if (normalized.isEmpty()) return "";
        if (normalized.stream().anyMatch(c -> "ALL".equalsIgnoreCase(c))) {
            return ", cascade = {jakarta.persistence.CascadeType.ALL}";
        }
        return ", cascade = {" + normalized.stream()
                .map(s -> "jakarta.persistence.CascadeType." + s)
                .collect(joining(", ")) + "}";
    }

    private static String buildJoinTableAnnotation(JoinTableSpecDTO jt) {
        StringBuilder sb = new StringBuilder("@JoinTable(name = \"" + jt.getName() + "\"");
        appendJoinColumnList(sb, "joinColumns", jt.getJoinColumns());
        appendJoinColumnList(sb, "inverseJoinColumns", jt.getInverseJoinColumns());
        sb.append(")");
        return sb.toString();
    }

    private static void appendJoinColumnList(StringBuilder sb, String attr,
                                              List<JoinColumnSpecDTO> cols) {
        if (cols == null || cols.isEmpty()) return;
        sb.append(", ").append(attr).append(" = {");
        sb.append(cols.stream()
                .map(jc -> "@JoinColumn(name = \"" + jc.getName() + "\""
                        + (Strings.isNotBlank(jc.getReferencedColumnName())
                        ? ", referencedColumnName = \"" + jc.getReferencedColumnName() + "\""
                        : "") + ")")
                .collect(joining(", ")));
        sb.append("}");
    }
}
