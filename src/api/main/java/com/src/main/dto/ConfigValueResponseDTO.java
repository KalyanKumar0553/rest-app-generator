package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfigValueResponseDTO {
	private String key;
    private String label;
}
