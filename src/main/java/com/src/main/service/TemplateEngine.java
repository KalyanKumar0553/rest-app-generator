package com.src.main.service;

import com.github.mustachejava.*;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
}