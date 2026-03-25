package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PluginModuleVersionDTO(
		UUID id,
		String versionCode,
		String changelog,
		String fileName,
		long sizeBytes,
		boolean published,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
