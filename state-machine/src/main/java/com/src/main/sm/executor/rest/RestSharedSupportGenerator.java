package com.src.main.sm.executor.rest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.TemplatePathResolver;
import com.src.main.util.PathUtils;

@Component
public class RestSharedSupportGenerator {

	private static final String ENTITY_UTIL_TEMPLATE_JAVA = "rest-entity-utils.java.mustache";
	private static final String FILTER_UTIL_TEMPLATE_JAVA = "rest-filter-utils.java.mustache";
	private static final String QUERY_UTIL_TEMPLATE_JAVA = "rest-query-utils.java.mustache";
	private static final String ENTITY_UTIL_TEMPLATE_KOTLIN = "rest-entity-utils.kt.mustache";
	private static final String FILTER_UTIL_TEMPLATE_KOTLIN = "rest-filter-utils.kt.mustache";
	private static final String QUERY_UTIL_TEMPLATE_KOTLIN = "rest-query-utils.kt.mustache";
	private static final String ENTITY_UTIL_CLASS = "RestEntityUtils";
	private static final String FILTER_UTIL_CLASS = "RestFilterUtils";
	private static final String QUERY_UTIL_CLASS = "RestQueryUtils";

	private final TemplateEngine templateEngine;

	public RestSharedSupportGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path projectRoot, String supportPackage, boolean noSql, GenerationLanguage language) throws Exception {
		Path outDir = projectRoot.resolve(PathUtils.srcPathFromPackage(supportPackage, language));
		Files.createDirectories(outDir);
		boolean kotlin = language == GenerationLanguage.KOTLIN;
		String entityTemplate = kotlin ? ENTITY_UTIL_TEMPLATE_KOTLIN : ENTITY_UTIL_TEMPLATE_JAVA;
		String queryTemplate = kotlin ? QUERY_UTIL_TEMPLATE_KOTLIN : QUERY_UTIL_TEMPLATE_JAVA;
		String filterTemplate = kotlin ? FILTER_UTIL_TEMPLATE_KOTLIN : FILTER_UTIL_TEMPLATE_JAVA;
		String entityUtils = templateEngine.renderAny(TemplatePathResolver.candidates(language, "rest", entityTemplate),
				Map.of("supportPackage", supportPackage));
		String queryUtils = templateEngine.renderAny(TemplatePathResolver.candidates(language, "rest", queryTemplate),
				Map.of("supportPackage", supportPackage));
		Files.writeString(outDir.resolve(ENTITY_UTIL_CLASS + "." + language.fileExtension()), entityUtils, UTF_8);
		Files.writeString(outDir.resolve(QUERY_UTIL_CLASS + "." + language.fileExtension()), queryUtils, UTF_8);
		if (!noSql) {
			String filterUtils = templateEngine.renderAny(TemplatePathResolver.candidates(language, "rest", filterTemplate),
					Map.of("supportPackage", supportPackage));
			Files.writeString(outDir.resolve(FILTER_UTIL_CLASS + "." + language.fileExtension()), filterUtils, UTF_8);
		}
	}
}
