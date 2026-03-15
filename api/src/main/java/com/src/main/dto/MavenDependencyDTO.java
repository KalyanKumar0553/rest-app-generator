package com.src.main.dto;

import java.util.Objects;

public record MavenDependencyDTO(String groupId, String artifactId, String scope, boolean optional) {

	public String scopeOrCompile() {
		if (scope == null || scope.isBlank())
			return "compile";
		return scope.trim().toLowerCase();
	}

	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("        <dependency>\n");
		sb.append("            <groupId>").append(groupId).append("</groupId>\n");
		sb.append("            <artifactId>").append(artifactId).append("</artifactId>\n");
		if (scope != null && !scope.isBlank()) {
			sb.append("            <scope>").append(scope).append("</scope>\n");
		}
		if (optional) {
			sb.append("            <optional>true</optional>\n");
		}
		sb.append("        </dependency>\n");
		return sb.toString();
	}

	public String toGradleLine() {
		final String gav = groupId + ":" + artifactId;
		String s = scopeOrCompile();
		return switch (s) {
		case "test" -> "testImplementation(\"" + gav + "\")";
		case "runtime", "runtimeonly" -> "runtimeOnly(\"" + gav + "\")";
		case "provided", "providedruntime" -> "providedRuntime(\"" + gav + "\")";
		default -> {
			if ("org.projectlombok".equals(groupId) && "lombok".equals(artifactId)) {
				yield "compileOnly(\"org.projectlombok:lombok\")\n    annotationProcessor(\"org.projectlombok:lombok\")";
			}
			yield "implementation(\"" + gav + "\")";
		}
		};
	}

	@Override
	public String toString() {
		return groupId + ":" + artifactId + (scope != null ? ":" + scope : "");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MavenDependencyDTO that))
			return false;
		return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId)
				&& Objects.equals(scopeOrCompile(), that.scopeOrCompile());
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupId, artifactId, scopeOrCompile());
	}
}
