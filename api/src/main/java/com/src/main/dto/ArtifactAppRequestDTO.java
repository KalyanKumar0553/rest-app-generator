package com.src.main.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArtifactAppRequestDTO {

	@NotBlank(message = "Code is required")
	@Size(max = 100, message = "Code can contain up to 100 characters")
	private String code;

	@NotBlank(message = "Name is required")
	@Size(max = 150, message = "Name can contain up to 150 characters")
	private String name;

	@Size(max = 5000, message = "Description can contain up to 5000 characters")
	private String description;

	@NotBlank(message = "Status is required")
	@Size(max = 32, message = "Status can contain up to 32 characters")
	private String status;

	@NotBlank(message = "Generator language is required")
	@Size(max = 32, message = "Generator language can contain up to 32 characters")
	private String generatorLanguage;

	@NotBlank(message = "Build tool is required")
	@Size(max = 32, message = "Build tool can contain up to 32 characters")
	private String buildTool;

	@NotNull(message = "Enabled packs are required")
	private List<String> enabledPacks;

	@NotNull(message = "Config is required")
	private Map<String, Object> config;
}
