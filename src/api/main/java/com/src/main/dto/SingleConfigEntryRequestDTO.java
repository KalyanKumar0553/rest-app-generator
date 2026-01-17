package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleConfigEntryRequestDTO {
	private String category;
    private String propertyKey;
    private String valueKey;
}
