package com.src.main.sm.executor.crud;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

@Component
public class CrudRepositoryGenerator {

	private static final String TEMPLATE = "templates/crud/repository.java.mustache";
	private final TemplateEngine templateEngine;

	public CrudRepositoryGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path projectRoot, CrudGenerationUnit unit) throws Exception {
		Path outDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(unit.getRepositoryPackage()));
		Files.createDirectories(outDir);
		String content = templateEngine.render(TEMPLATE, unit.toTemplateModel());
		Files.writeString(outDir.resolve(unit.getRepositoryClass() + ".java"), content, UTF_8);
	}
}
