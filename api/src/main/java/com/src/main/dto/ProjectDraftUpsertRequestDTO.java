package com.src.main.dto;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectDraftUpsertRequestDTO {
	@NotNull
	private Map<String, Object> draftData;

	private Integer draftVersion;
}
