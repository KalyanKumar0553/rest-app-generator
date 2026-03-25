package com.src.main.sm.executor;

import com.github.mustachejava.*;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class TemplateEngine {

	private static final String STATE_MACHINE_RESOURCE_PREFIX = "shipped-modules/state-machine/src/main/resources/";
	
	private final MustacheFactory mf = new DefaultMustacheFactory();

	public String render(String tpl, Map<String, Object> m) {
		String resolvedTemplate = resolveTemplatePath(tpl);
		if (resolvedTemplate == null) {
			throw new IllegalArgumentException("Template not found: " + tpl);
		}
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resolvedTemplate);
				Reader r = new InputStreamReader(stream, StandardCharsets.UTF_8);
				StringWriter w = new StringWriter()) {
			Mustache mm = mf.compile(r, resolvedTemplate);
			mm.execute(w, m).flush();
			return w.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public String renderAny(List<String> templates, Map<String, Object> model) {
		if (templates == null || templates.isEmpty()) {
			throw new IllegalArgumentException("No template candidates provided");
		}
		for (String candidate : templates) {
			if (candidate != null && exists(candidate)) {
				return render(candidate, model);
			}
		}
		throw new IllegalArgumentException("Template not found in candidates: " + templates);
	}

	public boolean exists(String templatePath) {
		return resolveTemplatePath(templatePath) != null;
	}

	private String resolveTemplatePath(String templatePath) {
		if (templatePath == null || templatePath.isBlank()) {
			return null;
		}
		ClassLoader classLoader = getClass().getClassLoader();
		if (classLoader.getResource(templatePath) != null) {
			return templatePath;
		}
		String prefixedPath = STATE_MACHINE_RESOURCE_PREFIX + templatePath;
		if (classLoader.getResource(prefixedPath) != null) {
			return prefixedPath;
		}
		return null;
	}
}
