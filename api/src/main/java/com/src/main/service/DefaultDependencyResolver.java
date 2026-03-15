package com.src.main.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.src.main.dto.MavenDependencyDTO;
import com.src.main.http.RemoteDependencyLookup;

@Service
public class DefaultDependencyResolver implements DependencyResolver {

	private final RemoteDependencyLookup remoteLookup;

	public DefaultDependencyResolver(RemoteDependencyLookup remoteLookup) {
		this.remoteLookup = remoteLookup;
	}

	private static final Map<String, MavenDependencyDTO> LOGICAL = new HashMap<>();
	static {
		LOGICAL.put("web", new MavenDependencyDTO("org.springframework.boot", "spring-boot-starter-web", null, false));
		LOGICAL.put("validation",
				new MavenDependencyDTO("org.springframework.boot", "spring-boot-starter-validation", null, false));
		LOGICAL.put("data-jpa",
				new MavenDependencyDTO("org.springframework.boot", "spring-boot-starter-data-jpa", null, false));
		LOGICAL.put("jpa",
				new MavenDependencyDTO("org.springframework.boot", "spring-boot-starter-data-jpa", null, false)); // alias
		LOGICAL.put("actuator",
				new MavenDependencyDTO("org.springframework.boot", "spring-boot-starter-actuator", null, false));
		LOGICAL.put("test",
				new MavenDependencyDTO("org.springframework.boot", "spring-boot-starter-test", "test_compile", false));

		LOGICAL.put("h2", new MavenDependencyDTO("com.h2database", "h2", "runtime", false));
		LOGICAL.put("postgres", new MavenDependencyDTO("org.postgresql", "postgresql", "runtime", false));
		LOGICAL.put("mysql", new MavenDependencyDTO("com.mysql", "mysql-connector-j", "runtime", false));

		LOGICAL.put("lombok", new MavenDependencyDTO("org.projectlombok", "lombok", "compile_only", true));

		LOGICAL.put("mapstruct", new MavenDependencyDTO("org.mapstruct", "mapstruct", null, false));
		LOGICAL.put("mapstruct-processor",
				new MavenDependencyDTO("org.mapstruct", "mapstruct-processor", "annotation_processor", false));
	}

	@Override
	public List<MavenDependencyDTO> resolveForMaven(List<String> idsOrGavs, String bootVersion,
			boolean includeOpenApiFlag) {
		List<MavenDependencyDTO> out = new ArrayList<>();
		boolean includeOpenApi = includeOpenApiFlag;

		if (idsOrGavs != null) {
			for (String raw : idsOrGavs) {
				if (raw == null || raw.isBlank())
					continue;
				String token = raw.trim().toLowerCase();
				if ("openapi".equals(token) || "springdoc".equals(token)) {
					includeOpenApi = true;
					continue;
				}
				if ("mapstruct".equals(token)) {
					out.add(LOGICAL.get("mapstruct"));
					out.add(LOGICAL.get("mapstruct-processor"));
					continue;
				}

				MavenDependencyDTO md = LOGICAL.get(token);
				if (md != null) {
					out.add(md);
					continue;
				}

				// Raw GAV? "group:artifact[:scope]"
				if (token.contains(":")) {
					String[] p = token.split(":");
					String scope = p.length >= 3 ? p[2] : null;
					if (p[0].isBlank() || p[1].isBlank())
						continue; // guard against invalid
					out.add(new MavenDependencyDTO(p[0], p[1], scope, false));
					continue;
				}
				remoteLookup.findByKeyword(token).ifPresent(out::add);
			}
		}

		if (includeOpenApi && LOGICAL.get("springdoc") == null) {
			// do nothing here; InitializrPomGenerator handles springdoc via
			// model.includeOpenapi()
		}

		return out.stream().filter(Objects::nonNull).filter(d -> d.groupId() != null && !d.groupId().isBlank())
				.filter(d -> d.artifactId() != null && !d.artifactId().isBlank()).toList();
	}

	@Override
	public List<String> resolveForGradle(List<String> idsOrGavs, String bootVersion, boolean includeOpenApi) {
		// reuse Maven logic, then map to Gradle lines
		return resolveForMaven(idsOrGavs, bootVersion, includeOpenApi).stream().flatMap(d -> gradleLines(d).stream())
				.toList();
	}

	private List<String> gradleLines(MavenDependencyDTO d) {
		String gav = d.groupId() + ":" + d.artifactId();
		String s = d.scopeOrCompile();
		return switch (s) {
		case "test", "test_compile" -> List.of("testImplementation(\"" + gav + "\")");
		case "runtime", "runtimeonly" -> List.of("runtimeOnly(\"" + gav + "\")");
		case "provided", "providedruntime", "provided_runtime" -> List.of("providedRuntime(\"" + gav + "\")");
		case "compile_only", "compile-only" -> {
			if ("org.projectlombok".equals(d.groupId()) && "lombok".equals(d.artifactId())) {
				yield List.of("compileOnly(\"org.projectlombok:lombok\")",
						"annotationProcessor(\"org.projectlombok:lombok\")");
			}
			yield List.of("compileOnly(\"" + gav + "\")");
		}
		case "annotation_processor", "annotation-processor" -> List.of("annotationProcessor(\"" + gav + "\")");
		default -> {
			// MapStruct special: add processor pair
			if ("org.mapstruct".equals(d.groupId()) && "mapstruct".equals(d.artifactId())) {
				yield List.of("implementation(\"org.mapstruct:mapstruct\")",
						"annotationProcessor(\"org.mapstruct:mapstruct-processor\")");
			}
			yield List.of("implementation(\"" + gav + "\")");
		}
		};
	}
}
