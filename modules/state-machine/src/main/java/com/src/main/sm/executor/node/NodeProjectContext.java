package com.src.main.sm.executor.node;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

record NodeProjectContext(
		Path root,
		String appName,
		String artifactId,
		String description,
		String version,
		int port,
		String packageManager,
		String orm,
		boolean dockerEnabled,
		List<NodeEnumDefinition> enums,
		List<NodeDtoDefinition> dtos,
		List<NodeModelDefinition> models) {
}

record NodeEnumDefinition(String name, List<String> constants) {
}

record NodeDtoDefinition(String name, String dtoType, List<NodeFieldDefinition> fields, List<NodeConstraintDefinition> classConstraints) {
}

record NodeModelDefinition(String name, List<NodeFieldDefinition> fields, NodeRestConfig restConfig) {
}

record NodeFieldDefinition(String name, String rawType, String tsType, boolean optional, List<NodeConstraintDefinition> constraints) {
}

record NodeConstraintDefinition(String kind, Map<String, Object> params, String key, String message) {
}

record NodeRestConfig(
		String basePath,
		boolean listEnabled,
		boolean getEnabled,
		boolean createEnabled,
		boolean updateEnabled,
		boolean deleteEnabled,
		String createDtoName,
		String updateDtoName,
		String patchDtoName) {
}
