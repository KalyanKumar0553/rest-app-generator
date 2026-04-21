package com.src.main.sm.executor.rest;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.SourceFileWriter;

/**
 * Generates REST service classes from Mustache templates.
 */
@Component
public class RestServiceGenerator {

    private static final String DOMAIN     = "rest";
    private static final String TPL_JAVA   = "service.java.mustache";
    private static final String TPL_KOTLIN = "service.kt.mustache";

    private final SourceFileWriter sourceFileWriter;

    public RestServiceGenerator(SourceFileWriter sourceFileWriter) {
        this.sourceFileWriter = sourceFileWriter;
    }

    public void generate(Path projectRoot, RestGenerationUnit unit,
                          GenerationLanguage language) throws IOException {
        String template = language.selectTemplate(TPL_JAVA, TPL_KOTLIN);
        sourceFileWriter.write(projectRoot, language, DOMAIN, template,
                unit.toTemplateModel(), unit.getServicePackage(), unit.getServiceClass());
    }
}
