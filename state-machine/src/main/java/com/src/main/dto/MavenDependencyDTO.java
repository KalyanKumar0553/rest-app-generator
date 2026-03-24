package com.src.main.dto;

import java.util.Objects;

public record MavenDependencyDTO(String groupId, String artifactId, String version, String scope, boolean optional) {

	public MavenDependencyDTO(String groupId, String artifactId, String scope, boolean optional) {
		this(groupId, artifactId, null, scope, optional);
	}

	public String versionOrNull() {
		return version == null || version.isBlank() ? null : version.trim();
	}

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
		if (versionOrNull() != null) {
			sb.append("            <version>").append(versionOrNull()).append("</version>\n");
		}
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
		final String gav = versionOrNull() == null ? groupId + ":" + artifactId
				: groupId + ":" + artifactId + ":" + versionOrNull();
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
		StringBuilder sb = new StringBuilder(groupId).append(":").append(artifactId);
		if (versionOrNull() != null) {
			sb.append(":").append(versionOrNull());
		}
		if (scope != null) {
			sb.append(":").append(scope);
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MavenDependencyDTO that))
			return false;
		return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId)
				&& Objects.equals(versionOrNull(), that.versionOrNull())
				&& Objects.equals(scopeOrCompile(), that.scopeOrCompile());
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupId, artifactId, versionOrNull(), scopeOrCompile());
	}
}
