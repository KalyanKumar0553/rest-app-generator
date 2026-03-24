package com.src.main.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiLabsGenerateResponseDTO {
	private UUID jobId;
	private String status;
}
