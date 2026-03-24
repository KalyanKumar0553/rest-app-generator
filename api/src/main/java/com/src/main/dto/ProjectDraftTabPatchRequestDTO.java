package com.src.main.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectDraftTabPatchRequestDTO {
	@NotBlank
	private String tabKey;

	@NotNull
	private Map<String, Object> tabData;

	@NotNull
	private Integer draftVersion;
}
