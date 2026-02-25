package com.src.main.sm.executor.rest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.src.main.sm.executor.TemplateEngine;
import com.src.main.util.PathUtils;

@Component
public class RestSharedSupportGenerator {

	private static final String ENTITY_UTIL_TEMPLATE = "templates/rest/rest-entity-utils.java.mustache";
	private static final String FILTER_UTIL_TEMPLATE = "templates/rest/rest-filter-utils.java.mustache";
	private static final String QUERY_UTIL_TEMPLATE = "templates/rest/rest-query-utils.java.mustache";
	private static final String ENTITY_UTIL_CLASS = "RestEntityUtils";
	private static final String FILTER_UTIL_CLASS = "RestFilterUtils";
	private static final String QUERY_UTIL_CLASS = "RestQueryUtils";

	private final TemplateEngine templateEngine;

	public RestSharedSupportGenerator(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path projectRoot, String supportPackage, boolean noSql) throws Exception {
		Path outDir = projectRoot.resolve(PathUtils.javaSrcPathFromPackage(supportPackage));
		Files.createDirectories(outDir);
		String entityUtils = templateEngine.render(ENTITY_UTIL_TEMPLATE, Map.of("supportPackage", supportPackage));
		String queryUtils = templateEngine.render(QUERY_UTIL_TEMPLATE, Map.of("supportPackage", supportPackage));
		Files.writeString(outDir.resolve(ENTITY_UTIL_CLASS + ".java"), entityUtils, UTF_8);
		Files.writeString(outDir.resolve(QUERY_UTIL_CLASS + ".java"), queryUtils, UTF_8);
		if (!noSql) {
			String filterUtils = templateEngine.render(FILTER_UTIL_TEMPLATE, Map.of("supportPackage", supportPackage));
			Files.writeString(outDir.resolve(FILTER_UTIL_CLASS + ".java"), filterUtils, UTF_8);
		}
	}
}
