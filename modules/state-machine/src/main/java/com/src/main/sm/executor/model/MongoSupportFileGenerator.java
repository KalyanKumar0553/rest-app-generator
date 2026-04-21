package com.src.main.sm.executor.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.JavaNamingUtils;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

/**
 * Generates the MongoDB sequence/listener support files that every Mongo-backed
 * project needs ({@code DatabaseSequence}, {@code PrimarySequenceService}, and
 * per-entity {@code *Listener} classes).
 *
 * <p>Extracted from {@code ModelGenerator} (SRP): model generation is purely
 * about entity mapping; Mongo infrastructure files are a separate concern.
 */
@Component
public class MongoSupportFileGenerator {

    private static final String TPL_SEQUENCE_DOC_JAVA  = "mongo-database-sequence.java.mustache";
    private static final String TPL_SEQUENCE_DOC_KT    = "mongo-database-sequence.kt.mustache";
    private static final String TPL_SEQUENCE_SVC_JAVA  = "mongo-primary-sequence-service.java.mustache";
    private static final String TPL_SEQUENCE_SVC_KT    = "mongo-primary-sequence-service.kt.mustache";
    private static final String TPL_LISTENER_JAVA      = "mongo-listener.java.mustache";
    private static final String TPL_LISTENER_KT        = "mongo-listener.kt.mustache";

    private final TemplateEngine templateEngine;

    public MongoSupportFileGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generates {@code DatabaseSequence}, {@code PrimarySequenceService}, and a
     * listener for each model that has a numeric ID.
     *
     * @param spec            parsed application spec
     * @param projectRoot     root of the generated project temp directory
     * @param basePackage     the project's base package
     * @param domainStructure {@code true} when domain-layered package layout is used
     * @param language        target language
     */
    public void generate(AppSpecDTO spec, Path projectRoot, String basePackage,
                          boolean domainStructure, GenerationLanguage language) throws IOException {
        String utilPkg = domainStructure ? basePackage + ".domain.util" : basePackage + ".util";
        Path utilDir = projectRoot.resolve(PathUtils.srcPathFromPackage(utilPkg, language));
        Files.createDirectories(utilDir);

        Map<String, Object> utilCtx = Map.of("packageName", utilPkg);
        writeFile(utilDir, "DatabaseSequence", language, utilCtx,
                language.selectTemplate(TPL_SEQUENCE_DOC_JAVA, TPL_SEQUENCE_DOC_KT), "model");
        writeFile(utilDir, "PrimarySequenceService", language, utilCtx,
                language.selectTemplate(TPL_SEQUENCE_SVC_JAVA, TPL_SEQUENCE_SVC_KT), "model");

        if (spec == null || spec.getModels() == null) return;

        spec.getModels().stream()
                .filter(MongoSupportFileGenerator::supportsSequenceListener)
                .forEach(model -> generateListener(model, projectRoot, basePackage, utilPkg,
                        domainStructure, language));
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void generateListener(ModelSpecDTO model, Path projectRoot, String basePackage,
                                   String utilPkg, boolean domainStructure,
                                   GenerationLanguage language) {
        try {
            String modelPkg = resolveModelPackage(model, basePackage, domainStructure);
            Path modelDir = projectRoot.resolve(PathUtils.srcPathFromPackage(modelPkg, language));
            Files.createDirectories(modelDir);

            String entityName = JavaNamingUtils.toJavaTypeName(model.getName(), "Entity");
            Map<String, Object> ctx = Map.of(
                    "packageName", modelPkg,
                    "entityName", entityName,
                    "sequenceServicePackage", utilPkg,
                    "intId", isIntegerId(model));

            writeFile(modelDir, entityName + "Listener", language, ctx,
                    language.selectTemplate(TPL_LISTENER_JAVA, TPL_LISTENER_KT), "model");
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException(ex);
        }
    }

    private void writeFile(Path dir, String className, GenerationLanguage language,
                            Map<String, Object> ctx, String templateFile, String domain) throws IOException {
        List<String> candidates = TemplatePathResolver.candidates(language, domain, templateFile);
        String content = templateEngine.renderAny(candidates, ctx);
        Files.writeString(dir.resolve(className + "." + language.fileExtension()),
                content, StandardCharsets.UTF_8);
    }

    private static String resolveModelPackage(ModelSpecDTO model, String basePackage,
                                               boolean domainStructure) {
        if (!domainStructure) return basePackage + ".model";
        String segment = model.getName().replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return basePackage + ".domain." + segment + ".model";
    }

    private static boolean supportsSequenceListener(ModelSpecDTO model) {
        if (model == null || model.getId() == null) return false;
        String raw = StringUtils.firstNonBlank(model.getId().getType(), "").trim();
        return "Long".equalsIgnoreCase(raw) || "long".equalsIgnoreCase(raw)
                || "Integer".equalsIgnoreCase(raw) || "int".equalsIgnoreCase(raw);
    }

    private static boolean isIntegerId(ModelSpecDTO model) {
        if (model == null || model.getId() == null) return false;
        String raw = StringUtils.firstNonBlank(model.getId().getType(), "").trim();
        return "Integer".equalsIgnoreCase(raw) || "int".equalsIgnoreCase(raw);
    }
}
