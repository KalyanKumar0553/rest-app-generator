package com.src.main.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ConfigPropertyResponseDTO {
	private String category;
    private String label;
    private String propertyKey;
    private String currentValueKey;
    private List<ConfigPropertyValueDTO> values;
}
