package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleConfigEntryRequestDTO {
	private String category;
    private String propertyKey;
    private String valueKey;
}
