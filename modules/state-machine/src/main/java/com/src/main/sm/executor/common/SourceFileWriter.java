package com.src.main.sm.executor.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

/**
 * Single-responsibility component that handles the three-step pattern shared
 * by every source generator: resolve candidates → render template → write file.
 *
 * <p>Before this class existed every generator duplicated:
 * <pre>
 *   Path outDir = root.resolve(PathUtils.srcPathFromPackage(pkg, lang));
 *   Files.createDirectories(outDir);
 *   String template = lang == KOTLIN ? KOTLIN_TPL : JAVA_TPL;
 *   String content = engine.renderAny(TemplatePathResolver.candidates(...), model);
 *   Files.writeString(outDir.resolve(className + "." + lang.fileExtension()), content, UTF_8);
 * </pre>
 */
@Component
public class SourceFileWriter {

    private final TemplateEngine templateEngine;

    public SourceFileWriter(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Renders {@code templateFile} with {@code model} and writes the result to the
     * correct language source directory under {@code projectRoot}.
     *
     * @param projectRoot   root of the generated project temp directory
     * @param language      target language (determines src/main/java vs kotlin path)
     * @param domain        template domain folder name (e.g. "crud", "rest", "model")
     * @param templateFile  template filename (e.g. "repository.java.mustache")
     * @param model         Mustache context
     * @param outputPackage Java/Kotlin package of the file being written
     * @param outputClass   simple class name (without extension)
     */
    public void write(Path projectRoot, GenerationLanguage language, String domain,
                      String templateFile, Map<String, Object> model,
                      String outputPackage, String outputClass) throws IOException {

        List<String> candidates = TemplatePathResolver.candidates(language, domain, templateFile);
        String content = templateEngine.renderAny(candidates, model);

        Path outDir = projectRoot.resolve(PathUtils.srcPathFromPackage(outputPackage, language));
        Files.createDirectories(outDir);
        Files.writeString(outDir.resolve(outputClass + "." + language.fileExtension()),
                content, StandardCharsets.UTF_8);
    }
}
