package com.src.main.utils;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.src.main.dto.InitializrProjectModel;

@Component
public class InitializrGradleGenerator {

    public String settingsGradleKts(String artifactId) {
    	return "rootProject.name = \"" + artifactId + "\"\n";
    }

    public String buildGradleKts(InitializrProjectModel model, List<String> gradleDeps) {
        boolean war = "war".equalsIgnoreCase(model.packaging());

        Map<String, Object> context = new HashMap<>();
        context.put("bootVersion", model.bootVersion());
        context.put("jdkVersion", model.jdkVersion());
        context.put("groupId", model.groupId());
        context.put("version", model.version());
        context.put("description", model.description());
        context.put("isWar", war);
        context.put("angularIntegration", model.angularIntegration());
        context.put("dependencies", gradleDeps.stream()
                .map(s -> s.startsWith("    ") ? s : "    " + s)
                .collect(Collectors.toList()));

        MustacheFactory mf = new DefaultMustacheFactory();
        try (InputStreamReader reader = new InputStreamReader(
                getClass().getResourceAsStream(AppConstants.GRADLE_MUSTACHE),
                StandardCharsets.UTF_8);
             StringWriter writer = new StringWriter()) {

            Mustache mustache = mf.compile(reader, "buildGradleKts");
            mustache.execute(writer, context).flush();
            return writer.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to render build.gradle.kts from Mustache template", e);
        }
    }
}
