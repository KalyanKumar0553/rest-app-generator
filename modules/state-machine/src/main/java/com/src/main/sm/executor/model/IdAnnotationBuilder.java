package com.src.main.sm.executor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.src.main.common.util.CaseUtils;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.GenerationSpecDTO;
import com.src.main.dto.ModelSpecDTO;

/**
 * Builds the {@link IdBlock} (field declaration + JPA / MongoDB annotations)
 * for an entity's primary-key field.
 *
 * <p>Extracted from {@code ModelGenerator} to satisfy the Single Responsibility
 * Principle. This class owns exactly one concern: translating an
 * {@code id} spec into a fully annotated {@link IdBlock}.
 */
public final class IdAnnotationBuilder {

    private IdAnnotationBuilder() {
    }

    /**
     * @param model        the entity spec containing the {@code id} definition
     * @param imports      mutable import set; annotations' FQCNs are added here
     * @param noSqlDatabase {@code true} when the target database is MongoDB
     * @return a fully populated {@link IdBlock}
     */
    public static IdBlock build(ModelSpecDTO model, Set<String> imports, boolean noSqlDatabase) {
        Validate.notNull(model.getId(), "entity.id missing for %s", model.getName());

        String rawName = model.getId().getField();
        String rawType = model.getId().getType();
        String name = CaseUtils.toCamel(rawName);
        String type = ModelGenerationSupport.resolveJavaType(
                StringUtils.firstNonBlank(rawType, "String"), imports);

        IdBlock id = new IdBlock();
        id.setName(name);
        id.setType(type);
        id.setAnnotations(buildAnnotations(model, imports, noSqlDatabase));
        return id;
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private static List<String> buildAnnotations(ModelSpecDTO model, Set<String> imports,
                                                  boolean noSqlDatabase) {
        List<String> annotations = new ArrayList<>();

        if (noSqlDatabase) {
            imports.add("org.springframework.data.annotation.Id");
        } else {
            imports.add("jakarta.persistence.Id");
        }
        annotations.add("@Id");

        GenerationSpecDTO generation = model.getId().getGeneration();
        if (!noSqlDatabase && generation != null && generation.getStrategy() != null) {
            appendGenerationStrategy(generation, model, imports, annotations);
        }
        return annotations;
    }

    private static void appendGenerationStrategy(GenerationSpecDTO g, ModelSpecDTO model,
                                                  Set<String> imports, List<String> annotations) {
        imports.add("jakarta.persistence.GeneratedValue");

        switch (g.getStrategy()) {
            case IDENTITY -> {
                imports.add("jakarta.persistence.GenerationType");
                annotations.add("@GeneratedValue(strategy = GenerationType.IDENTITY)");
            }
            case SEQUENCE -> {
                imports.add("jakarta.persistence.GenerationType");
                imports.add("jakarta.persistence.SequenceGenerator");
                String genName = CaseUtils.toSnake(model.getName()) + "_seq";
                String seqName = StringUtils.firstNonBlank(g.getSequenceName(), genName);
                int alloc = g.getAllocationSize() != null ? g.getAllocationSize() : 50;
                annotations.add("@SequenceGenerator(name = \"" + genName + "\", sequenceName = \""
                        + seqName + "\", allocationSize = " + alloc + ")");
                annotations.add("@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \""
                        + genName + "\")");
            }
            case UUID -> {
                imports.add("org.hibernate.annotations.GenericGenerator");
                String generatorName = StringUtils.firstNonBlank(g.getGeneratorName(), "uuid");
                String strategy = StringUtils.firstNonBlank(g.getHibernateUuidStrategy(), "uuid2");
                annotations.add("@GenericGenerator(name = \"" + generatorName + "\", strategy = \""
                        + strategy + "\")");
                annotations.add("@GeneratedValue(generator = \"" + generatorName + "\")");
            }
            case AUTO -> {
                imports.add("jakarta.persistence.GenerationType");
                annotations.add("@GeneratedValue(strategy = GenerationType.AUTO)");
            }
            case NONE -> { /* manually assigned — no annotation */ }
        }
    }
}
