package com.src.main.sm.executor.docker;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.src.main.sm.executor.TemplateEngine;

@Service
public class DockerGenerationService {

	private static final String DOCKERFILE_TEMPLATE = "templates/docker/Dockerfile.mustache";
	private static final String COMPOSE_TEMPLATE = "templates/docker/docker-compose.yml.mustache";

	private final TemplateEngine templateEngine;

	public DockerGenerationService(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void generate(Path root, String artifactId, String serviceName, String buildTool) throws Exception {
		Files.createDirectories(root);
		Map<String, Object> model = new LinkedHashMap<>();
		model.put("artifactId", artifactId);
		model.put("serviceName", serviceName);
		model.put("jarGlob", DockerGenerationSupport.resolveJarGlob(buildTool));

		String dockerfile = templateEngine.render(DOCKERFILE_TEMPLATE, model);
		String compose = templateEngine.render(COMPOSE_TEMPLATE, model);

		Files.writeString(root.resolve("Dockerfile"), dockerfile, UTF_8);
		Files.writeString(root.resolve("docker-compose.yml"), compose, UTF_8);
	}
}
