package com.src.main.sm.executor.rest;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.SourceFileWriter;

/**
 * Generates JPA / MongoDB repository interfaces for REST-layer entities.
 */
@Component
public class RestRepositoryGenerator {

    private static final String DOMAIN     = "rest";
    private static final String TPL_JAVA   = "repository.java.mustache";
    private static final String TPL_KOTLIN = "repository.kt.mustache";

    private final SourceFileWriter sourceFileWriter;

    public RestRepositoryGenerator(SourceFileWriter sourceFileWriter) {
        this.sourceFileWriter = sourceFileWriter;
    }

    public void generate(Path projectRoot, RestGenerationUnit unit,
                          GenerationLanguage language) throws IOException {
        String template = language.selectTemplate(TPL_JAVA, TPL_KOTLIN);
        sourceFileWriter.write(projectRoot, language, DOMAIN, template,
                unit.toTemplateModel(), unit.getRepositoryPackage(), unit.getRepositoryClass());
    }
}
