package com.src.main.sm.executor;

import com.github.mustachejava.*;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class TemplateEngine {
	
	private final MustacheFactory mf = new DefaultMustacheFactory();

	public String render(String tpl, Map<String, Object> m) {
		try (Reader r = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(tpl),
				StandardCharsets.UTF_8); StringWriter w = new StringWriter()) {
			if (r == null)
				throw new IllegalArgumentException("Template not found: " + tpl);
			Mustache mm = mf.compile(r, tpl);
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
		return getClass().getClassLoader().getResource(templatePath) != null;
	}
}
