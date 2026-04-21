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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;

import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.ColumnSpecDTO;
import com.src.main.dto.ConstraintDTO;
import com.src.main.dto.FieldSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.sm.executor.common.JavaNamingUtils;
import com.src.main.sm.executor.enumgen.EnumSpecResolved;

/**
 * Builds a {@link FieldBlock} (type, name, JPA + Bean Validation annotations)
 * for a single non-relational field.
 *
 * <p>Extracted from {@code ModelGenerator} (SRP): this class owns the mapping
 * from a {@code FieldSpecDTO} to its annotated Java/Kotlin field representation.
 */
public final class FieldAnnotationBuilder {

    private FieldAnnotationBuilder() {
    }

    /**
     * @param model         the owning entity spec (used for message-key generation)
     * @param field         the field spec to process
     * @param imports       mutable import set — FQCNs are added here as needed
     * @param enumByName    resolved enums keyed by simple name
     * @param enumPackage   package where enum types live
     * @param modelPackage  package of the owning entity (for same-package enum check)
     * @param noSqlDatabase {@code true} when the target is MongoDB
     * @return fully populated {@link FieldBlock}
     */
    public static FieldBlock build(ModelSpecDTO model, FieldSpecDTO field, Set<String> imports,
                                    Map<String, EnumSpecResolved> enumByName, String enumPackage,
                                    String modelPackage, boolean noSqlDatabase) {
        String name = CaseUtils.toCamel(field.getName());
        String resolvedType = ModelGenerationSupport.resolveJavaType(
                StringUtils.firstNonBlank(field.getType(), "String"), imports);

        FieldBlock fb = new FieldBlock();
        fb.setName(name);
        fb.setType(resolvedType);

        List<String> annotations = new ArrayList<>();
        ColumnSpecDTO col = field.getColumn();

        applyEnumAnnotations(resolvedType, field.getType(), enumByName, enumPackage, modelPackage,
                noSqlDatabase, imports, annotations);

        boolean hasExplicitColumn = col != null
                && (col.getName() != null || col.getLength() != null
                        || col.getNullable() != null || col.getUnique() != null
                        || col.getColumnDefinition() != null);

        col = applyConstraintAnnotations(model, field, resolvedType, col, hasExplicitColumn,
                imports, annotations, noSqlDatabase);

        applyJpaExtras(field, col, hasExplicitColumn, noSqlDatabase, imports, annotations);

        fb.setAnnotations(annotations);
        return fb;
    }

    // ── enum ─────────────────────────────────────────────────────────────────

    private static void applyEnumAnnotations(String resolvedType, String rawType,
                                              Map<String, EnumSpecResolved> enumByName,
                                              String enumPackage, String modelPackage,
                                              boolean noSqlDatabase,
                                              Set<String> imports, List<String> annotations) {
        EnumSpecResolved enumSpec = resolveEnumSpec(rawType, resolvedType, enumByName);
        if (enumSpec == null) return;

        if (enumPackage != null && !enumPackage.equals(modelPackage)) {
            imports.add(enumPackage + "." + resolvedType);
        }
        if (!noSqlDatabase) {
            imports.add("jakarta.persistence.EnumType");
            imports.add("jakarta.persistence.Enumerated");
            annotations.add("@Enumerated(EnumType." + enumSpec.storage() + ")");
        }
    }

    // ── bean validation ───────────────────────────────────────────────────────

    private static ColumnSpecDTO applyConstraintAnnotations(ModelSpecDTO model, FieldSpecDTO field,
                                                             String resolvedType, ColumnSpecDTO col,
                                                             boolean hasExplicitColumn,
                                                             Set<String> imports,
                                                             List<String> annotations,
                                                             boolean noSqlDatabase) {
        if (field.getConstraints() == null) return col;

        for (ConstraintDTO c : field.getConstraints()) {
            if (c.getName() == null) continue;
            col = applyConstraint(c, model, field, resolvedType, col, hasExplicitColumn,
                    imports, annotations, noSqlDatabase);
        }
        return col;
    }

    @SuppressWarnings("java:S3776")
    private static ColumnSpecDTO applyConstraint(ConstraintDTO c, ModelSpecDTO model,
                                                  FieldSpecDTO field, String resolvedType,
                                                  ColumnSpecDTO col, boolean hasExplicitColumn,
                                                  Set<String> imports, List<String> annotations,
                                                  boolean noSqlDatabase) {
        switch (c.getName()) {
            case "NotNull" -> {
                imports.add("jakarta.validation.constraints.NotNull");
                annotations.add(msgAnno("@NotNull", model, field, "NotNull"));
                col = ensureColumn(col);
                col.setNullable(Boolean.FALSE);
            }
            case "NotBlank" -> {
                imports.add("jakarta.validation.constraints.NotBlank");
                annotations.add(msgAnno("@NotBlank", model, field, "NotBlank"));
            }
            case "Email" -> {
                imports.add("jakarta.validation.constraints.Email");
                annotations.add(msgAnno("@Email", model, field, "Email"));
            }
            case "Pattern" -> {
                imports.add("jakarta.validation.constraints.Pattern");
                String re = getStringAny(c.getParams(), new String[]{"regex", "regexp"}, ".*");
                annotations.add(msgAnno("@Pattern(regexp = \"" + escapeJava(re) + "\")", model, field, "Pattern"));
            }
            case "Size" -> {
                imports.add("jakarta.validation.constraints.Size");
                Integer min = getInt(c.getParams(), "min", null);
                Integer max = getInt(c.getParams(), "max", null);
                String args = (min != null ? "min=" + min : "")
                        + (min != null && max != null ? ", " : "")
                        + (max != null ? "max=" + max : "");
                annotations.add(msgAnno("@Size(" + args + ")", model, field, "Size"));
                if (!hasExplicitColumn && "String".equals(resolvedType) && max != null) {
                    col = ensureColumn(col);
                    if (col.getLength() == null) col.setLength(max);
                }
            }
            case "Min" -> {
                imports.add("jakarta.validation.constraints.Min");
                annotations.add(buildMinAnnotation(getLong(c.getParams(), "value", 0L),
                        buildMessageKey(model, field, "Min")));
            }
            case "Max" -> {
                imports.add("jakarta.validation.constraints.Max");
                annotations.add(buildMaxAnnotation(getLong(c.getParams(), "value", Long.MAX_VALUE),
                        buildMessageKey(model, field, "Max")));
            }
            case "Positive" -> {
                imports.add("jakarta.validation.constraints.Positive");
                annotations.add(msgAnno("@Positive", model, field, "Positive"));
            }
            case "DecimalMin" -> {
                imports.add("jakarta.validation.constraints.DecimalMin");
                String val = getString(c.getParams(), "value", "0");
                boolean inclusive = getBoolean(c.getParams(), "inclusive", true);
                annotations.add(msgAnno("@DecimalMin(value=\"" + escapeJava(val) + "\", inclusive=" + inclusive + ")",
                        model, field, "DecimalMin"));
            }
            case "Digits" -> {
                imports.add("jakarta.validation.constraints.Digits");
                int integer = getInt(c.getParams(), "integer", 10);
                int fraction = getInt(c.getParams(), "fraction", 2);
                annotations.add(msgAnno("@Digits(integer=" + integer + ", fraction=" + fraction + ")",
                        model, field, "Digits"));
            }
            case "Past" -> {
                imports.add("jakarta.validation.constraints.Past");
                annotations.add(msgAnno("@Past", model, field, "Past"));
            }
            case "Future" -> {
                imports.add("jakarta.validation.constraints.Future");
                annotations.add(msgAnno("@Future", model, field, "Future"));
            }
            case "PastOrPresent" -> {
                imports.add("jakarta.validation.constraints.PastOrPresent");
                annotations.add(msgAnno("@PastOrPresent", model, field, "PastOrPresent"));
            }
            case "AssertTrue" -> {
                imports.add("jakarta.validation.constraints.AssertTrue");
                annotations.add(msgAnno("@AssertTrue", model, field, "AssertTrue"));
            }
            case "AssertFalse" -> {
                imports.add("jakarta.validation.constraints.AssertFalse");
                annotations.add(msgAnno("@AssertFalse", model, field, "AssertFalse"));
            }
            case "Valid" -> {
                imports.add("jakarta.validation.Valid");
                annotations.add("@Valid");
            }
            default -> { /* unknown constraint — silently ignored */ }
        }
        return col;
    }

    // ── JPA extras (@Convert, @NaturalId, @Column) ──────────────────────────

    private static void applyJpaExtras(FieldSpecDTO field, ColumnSpecDTO col,
                                        boolean hasExplicitColumn, boolean noSqlDatabase,
                                        Set<String> imports, List<String> annotations) {
        if (!noSqlDatabase && field.getJpa() != null && field.getJpa().getConvert() != null
                && Strings.isNotBlank(field.getJpa().getConvert().getConverter())) {
            imports.add("jakarta.persistence.Convert");
            annotations.add("@Convert(converter = " + field.getJpa().getConvert().getConverter() + ".class)");
        }

        if (!noSqlDatabase && Boolean.TRUE.equals(field.getNaturalId())) {
            imports.add("org.hibernate.annotations.NaturalId");
            annotations.add("@NaturalId");
        }

        if (!noSqlDatabase && col != null && (hasExplicitColumn || col.getNullable() != null
                || col.getUnique() != null || col.getLength() != null
                || col.getColumnDefinition() != null || col.getName() != null)) {
            imports.add("jakarta.persistence.Column");
            annotations.add(buildColumnAnnotation(col));
        }
    }

    private static String buildColumnAnnotation(ColumnSpecDTO col) {
        List<String> args = new ArrayList<>();
        if (col.getName() != null)              args.add("name = \"" + col.getName() + "\"");
        if (col.getUnique() != null)            args.add("unique = " + col.getUnique());
        if (col.getNullable() != null)          args.add("nullable = " + col.getNullable());
        if (col.getLength() != null)            args.add("length = " + col.getLength());
        if (col.getColumnDefinition() != null)  args.add("columnDefinition = \"" + escapeJava(col.getColumnDefinition()) + "\"");
        return "@Column(" + String.join(", ", args) + ")";
    }

    // ── enum resolution ───────────────────────────────────────────────────────

    private static EnumSpecResolved resolveEnumSpec(String rawType, String resolvedType,
                                                     Map<String, EnumSpecResolved> enumByName) {
        if (enumByName == null || enumByName.isEmpty()) return null;

        EnumSpecResolved direct = enumByName.get(resolvedType);
        if (direct != null) return direct;

        String normalized = JavaNamingUtils.toJavaTypeName(
                StringUtils.firstNonBlank(resolvedType, "").trim(), "Enum");
        if (!normalized.isBlank()) {
            EnumSpecResolved byNormalized = enumByName.get(normalized);
            if (byNormalized != null) return byNormalized;
        }

        String rawLeaf = extractLeafType(rawType);
        if (!rawLeaf.isBlank()) {
            EnumSpecResolved byRaw = enumByName.get(rawLeaf);
            if (byRaw != null) return byRaw;
            String normalizedRaw = JavaNamingUtils.toJavaTypeName(rawLeaf, "Enum");
            return normalizedRaw.isBlank() ? null : enumByName.get(normalizedRaw);
        }
        return null;
    }

    private static String extractLeafType(String rawType) {
        String type = StringUtils.firstNonBlank(rawType, "").trim();
        if (type.isBlank()) return "";
        int genericStart = type.indexOf('<');
        if (genericStart >= 0 && type.endsWith(">")) {
            type = type.substring(genericStart + 1, type.length() - 1).trim();
            if (type.contains(",")) type = type.substring(type.lastIndexOf(',') + 1).trim();
        }
        if (type.contains(".")) type = type.substring(type.lastIndexOf('.') + 1);
        return type.trim();
    }

    private static ColumnSpecDTO ensureColumn(ColumnSpecDTO col) {
        return col != null ? col : new ColumnSpecDTO();
    }
}
