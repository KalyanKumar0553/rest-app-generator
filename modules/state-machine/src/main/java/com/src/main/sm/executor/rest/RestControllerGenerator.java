package com.src.main.sm.executor.rest;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.SourceFileWriter;

/**
 * Generates REST controller classes from Mustache templates.
 */
@Component
public class RestControllerGenerator {

    private static final String DOMAIN     = "rest";
    private static final String TPL_JAVA   = "controller.java.mustache";
    private static final String TPL_KOTLIN = "controller.kt.mustache";

    private final SourceFileWriter sourceFileWriter;

    public RestControllerGenerator(SourceFileWriter sourceFileWriter) {
        this.sourceFileWriter = sourceFileWriter;
    }

    public void generate(Path projectRoot, RestGenerationUnit unit,
                          GenerationLanguage language) throws IOException {
        String template = language.selectTemplate(TPL_JAVA, TPL_KOTLIN);
        sourceFileWriter.write(projectRoot, language, DOMAIN, template,
                unit.toTemplateModel(), unit.getControllerPackage(), unit.getControllerClass());
    }
}
