package com.src.main.sm.executor.mapper;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.src.main.sm.executor.common.GenerationUnit;

/**
 * Encapsulates everything needed to generate one mapper class.
 *
 * <p>The {@link #toTemplateModel()} method owns the import-deduplication and
 * type-name-selection logic that previously lived inline in
 * {@code MapperGenerationService}, keeping that service free of model-building
 * concerns.
 */
public record MapperGenerationUnit(
        String packageName,
        String className,
        String sourceSimpleName,
        String targetSimpleName,
        String sourceFqcn,
        String targetFqcn,
        List<MapperMappingLine> forwardMappings,
        List<MapperMappingLine> reverseMappings) implements GenerationUnit {

    @Override
    public Map<String, Object> toTemplateModel() {
        boolean sameSimpleNames = sourceSimpleName.equals(targetSimpleName);
        Set<String> imports = new LinkedHashSet<>();
        if (!sameSimpleNames) {
            imports.add(sourceFqcn);
            imports.add(targetFqcn);
        }
        String sourceTypeName = sameSimpleNames ? sourceFqcn  : sourceSimpleName;
        String targetTypeName = sameSimpleNames ? targetFqcn  : targetSimpleName;

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("packageName",        packageName);
        model.put("className",          className);
        model.put("sourceSimpleName",   sourceSimpleName);
        model.put("targetSimpleName",   targetSimpleName);
        model.put("sourceTypeName",     sourceTypeName);
        model.put("targetTypeName",     targetTypeName);
        model.put("sourceClassLiteral", sourceTypeName + ".class");
        model.put("targetClassLiteral", targetTypeName + ".class");
        model.put("imports",            imports);
        model.put("forwardMappings",    forwardMappings);
        model.put("reverseMappings",    reverseMappings);
        return model;
    }
}
