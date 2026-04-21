package com.src.main.sm.executor.mapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.SourceFileWriter;

@Service
public class MapperGenerationService {

    private static final String DOMAIN     = "mapper";
    private static final String TPL_JAVA   = "mapper.java.mustache";
    private static final String TPL_KOTLIN = "mapper.kt.mustache";

    private final SourceFileWriter sourceFileWriter;

    public MapperGenerationService(SourceFileWriter sourceFileWriter) {
        this.sourceFileWriter = sourceFileWriter;
    }

    public int generate(Path root, List<MapperGenerationUnit> units, GenerationLanguage language) throws IOException {
        if (units == null || units.isEmpty()) {
            return 0;
        }
        String template = language.selectTemplate(TPL_JAVA, TPL_KOTLIN);
        try {
            units.stream().forEach(unit -> {
                try {
                    sourceFileWriter.write(root, language, DOMAIN, template,
                            unit.toTemplateModel(), unit.packageName(), unit.className());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
        return units.size();
    }
}
