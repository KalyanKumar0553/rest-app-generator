package com.src.main.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataEncryptionRuleResponseDTO {
	private UUID id;
	private String tableName;
	private String columnName;
	private String hashShadowColumn;
	private boolean enabled;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
