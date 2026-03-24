package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTabDefinitionDTO {
	private String key;
	private String label;
	private String icon;
	private String componentKey;
	private int order;
}
