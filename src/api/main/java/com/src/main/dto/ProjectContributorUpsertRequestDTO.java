package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectContributorUpsertRequestDTO {
	@NotBlank(message = "userId is required")
	private String userId;
}
