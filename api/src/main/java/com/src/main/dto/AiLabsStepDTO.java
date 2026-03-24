package com.src.main.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiLabsStepDTO {
	private String key;
	private String label;
	private String status;
	private String message;
	private OffsetDateTime updatedAt;
}
