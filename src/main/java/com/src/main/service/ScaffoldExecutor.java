package com.src.main.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.src.main.dto.StepResult;
import com.src.main.utils.AppConstants;

@Component
public class ScaffoldExecutor implements StepExecutor {

	private final TemplateEngine tpl;

	public ScaffoldExecutor(TemplateEngine tpl) {
		this.tpl = tpl;
	}

	@Override
	public StepResult execute(ExtendedState data) throws Exception {
		String groupId = (String) data.getVariables().get("groupId");
		String artifact = (String) data.getVariables().get("artifact");
		String version = (String) data.getVariables().get("version");
		Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get("yaml");
		Path root = Path.of((String) data.getVariables().get("root"));
		generate(root,groupId,artifact,version);
		Map<String, Object> output = Map.of("status", "Success");
		return StepResult.ok(output); 
	}

	public void generate(Path root, String groupId, String artifact, String version) throws Exception {
		String pom = tpl.render(AppConstants.TPL_POM,
				Map.of("groupId", groupId, "artifact", artifact, "version", version));
		Files.writeString(root.resolve("pom.xml"), pom, StandardCharsets.UTF_8);
		String basePkg = groupId;
		Path mainDir = root.resolve("src/main/java/" + basePkg.replace('.', '/'));
		Files.createDirectories(mainDir);
		String mainClass = toPascal(artifact) + "Application";
		String main = tpl.render(AppConstants.TPL_MAIN, Map.of("basePkg", basePkg, "mainClass", mainClass));
		Files.writeString(mainDir.resolve(mainClass + ".java"), main, StandardCharsets.UTF_8);
		Path res = root.resolve("src/main/resources");
		Files.createDirectories(res);
		Files.writeString(res.resolve("application.yml"), tpl.render(AppConstants.TPL_APP_YML, Map.of()),
				StandardCharsets.UTF_8);
		Files.writeString(res.resolve("messages.properties"), "", StandardCharsets.UTF_8);
	}

	private static String toPascal(String s) {
		String[] p = s.replace('-', ' ').replace('_', ' ').split(" ");
		StringBuilder b = new StringBuilder();
		for (String x : p) {
			if (!x.isEmpty()) {
				b.append(Character.toUpperCase(x.charAt(0))).append(x.substring(1));
			}
		}
		return b.toString();
	}
}