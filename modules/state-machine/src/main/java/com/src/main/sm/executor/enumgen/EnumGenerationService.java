package com.src.main.sm.executor.enumgen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.SourceFileWriter;

@Service
public class EnumGenerationService {

    private static final String DOMAIN     = "enum";
    private static final String TPL_JAVA   = "enum.java.mustache";
    private static final String TPL_KOTLIN = "enum.kt.mustache";

    private final SourceFileWriter sourceFileWriter;

    public EnumGenerationService(SourceFileWriter sourceFileWriter) {
        this.sourceFileWriter = sourceFileWriter;
    }

    public void generate(Path root, String enumPackage, List<EnumSpecResolved> enums, GenerationLanguage language) throws IOException {
        if (enums == null || enums.isEmpty()) {
            return;
        }
        String template = language.selectTemplate(TPL_JAVA, TPL_KOTLIN);
        try {
            enums.stream().forEach(enumSpec -> {
                try {
                    sourceFileWriter.write(root, language, DOMAIN, template,
                            buildTemplateModel(enumPackage, enumSpec), enumPackage, enumSpec.name());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private Map<String, Object> buildTemplateModel(String enumPackage, EnumSpecResolved enumSpec) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("packageName", enumPackage);
        model.put("enumName", enumSpec.name());
        model.put("constants", withCommaMetadata(enumSpec.constants()));
        return model;
    }

    private List<Map<String, Object>> withCommaMetadata(List<String> constants) {
        if (constants == null) {
            return new ArrayList<>();
        }
        return IntStream.range(0, constants.size())
                .filter(i -> constants.get(i) != null && !constants.get(i).isBlank())
                .mapToObj(i -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("value", constants.get(i));
                    item.put("last", i == constants.size() - 1);
                    return item;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
