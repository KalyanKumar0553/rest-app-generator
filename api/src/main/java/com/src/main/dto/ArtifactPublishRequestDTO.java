package com.src.main.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArtifactPublishRequestDTO {

	@Size(max = 64, message = "Version code can contain up to 64 characters")
	private String versionCode;
}
