package com.src.main.dto;

import java.util.List;
import java.util.UUID;

public record ProjectDraftVersionDiffDTO(
		UUID baseVersionId,
		Integer baseDraftVersion,
		UUID targetVersionId,
		Integer targetDraftVersion,
		List<String> addedPaths,
		List<String> removedPaths,
		List<String> changedPaths) {
}
