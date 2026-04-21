package com.src.main.sm.executor.crud;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.SourceFileWriter;

/**
 * Generates JPA / MongoDB repository interfaces from Mustache templates.
 *
 * <p>Delegates the template-resolve → render → write pipeline to
 * {@link SourceFileWriter}, keeping this class focused on what it knows:
 * which domain folder and template filenames apply to CRUD repositories.
 */
@Component
public class CrudRepositoryGenerator {

    private static final String DOMAIN        = "crud";
    private static final String TPL_JAVA      = "repository.java.mustache";
    private static final String TPL_KOTLIN    = "repository.kt.mustache";

    private final SourceFileWriter sourceFileWriter;

    public CrudRepositoryGenerator(SourceFileWriter sourceFileWriter) {
        this.sourceFileWriter = sourceFileWriter;
    }

    public void generate(Path projectRoot, CrudGenerationUnit unit,
                          GenerationLanguage language) throws IOException {
        String template = language.selectTemplate(TPL_JAVA, TPL_KOTLIN);
        sourceFileWriter.write(projectRoot, language, DOMAIN, template,
                unit.toTemplateModel(), unit.getRepositoryPackage(), unit.getRepositoryClass());
    }
}
