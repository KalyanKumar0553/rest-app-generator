package com.src.main.validation;

import java.util.Map;

import com.src.main.util.ProjectMetaDataConstants;

public class ProjectRequestValidator {

	public static Map<String, String> validateProjectData(Map<String, Object> app) {
		String artifact = String.valueOf(
				app.getOrDefault(ProjectMetaDataConstants.ARTIFACT_ID, ProjectMetaDataConstants.DEFAULT_ARTIFACT));
		String groupId = String
				.valueOf(app.getOrDefault(ProjectMetaDataConstants.GROUP_ID, ProjectMetaDataConstants.DEFAULT_GROUP));
		String version = String
				.valueOf(app.getOrDefault(ProjectMetaDataConstants.VERSION, ProjectMetaDataConstants.DEFAULT_VERSION));
		String buildTool = String.valueOf(
				app.getOrDefault(ProjectMetaDataConstants.BUILD_TOOL, ProjectMetaDataConstants.DEFAULT_BUILD_TOOL));
		String packaging = String.valueOf(
				app.getOrDefault(ProjectMetaDataConstants.PACKAGING, ProjectMetaDataConstants.DEFAULT_PACKAGING));
		String generator = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.GENERATOR,
				ProjectMetaDataConstants.DEFAULT_GRADLE_GENERATOR));
		String name = String
				.valueOf(app.getOrDefault(ProjectMetaDataConstants.NAME, ProjectMetaDataConstants.DEFAULT_NAME));
		String description = String.valueOf(
				app.getOrDefault(ProjectMetaDataConstants.DESCRIPTION, ProjectMetaDataConstants.DEFAULT_DESCRIPTION));
		String springBootVersion = String.valueOf(app.getOrDefault(ProjectMetaDataConstants.SPRING_BOOT_VERSION,
				ProjectMetaDataConstants.DEFAULT_BOOT_VERSION));
		String jdkVersion = String
				.valueOf(app.getOrDefault(ProjectMetaDataConstants.JDK_VERSION, ProjectMetaDataConstants.DEFAULT_JDK));
		if (artifact == null || artifact.isBlank())
			throw new IllegalArgumentException("app.artifact must be provided");
		if (groupId == null || groupId.isBlank())
			throw new IllegalArgumentException("app.groupId must be provided");
		if (version == null || version.isBlank())
			throw new IllegalArgumentException("app.version must be provided");
		return Map.of("artifact", artifact, "groupId", groupId, "version", version, "buildTool", buildTool, "packaging",
				packaging, "generator", generator, "name", name, "description", description, "springBootVersion",
				springBootVersion, "jdkVersion", jdkVersion);
	}
}
