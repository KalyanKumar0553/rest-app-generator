package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectImportRequestDTO {
	@NotBlank(message = "projectUrl is required")
	private String projectUrl;
}
