package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiLabsGenerateRequestDTO {

	@NotBlank
	private String prompt;
}
