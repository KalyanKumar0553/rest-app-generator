package com.src.main.dto;

import lombok.Data;

@Data
public class ProjectTabDefinitionAdminRequestDTO {
	private String key;
	private String label;
	private String icon;
	private String componentKey;
	private Integer order;
	private String generatorLanguage;
	private Boolean enabled;
}
