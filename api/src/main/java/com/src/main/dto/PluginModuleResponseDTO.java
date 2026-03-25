package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PluginModuleResponseDTO(
		UUID id,
		String code,
		String name,
		String description,
		String category,
		boolean enabled,
		boolean enableConfig,
		List<String> generatorTargets,
		UUID currentPublishedVersionId,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		List<PluginModuleVersionDTO> versions) {
}
