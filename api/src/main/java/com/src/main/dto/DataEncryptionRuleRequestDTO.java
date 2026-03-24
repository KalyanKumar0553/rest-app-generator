package com.src.main.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DataEncryptionRuleRequestDTO {

	@NotBlank
	private String tableName;

	private String columnName;

	private String hashShadowColumn;

	private boolean enabled = true;
}
