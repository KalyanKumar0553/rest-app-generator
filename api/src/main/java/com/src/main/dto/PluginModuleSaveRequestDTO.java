package com.src.main.dto;

import java.util.List;

import lombok.Data;

@Data
public class PluginModuleSaveRequestDTO {
	private String code;
	private String name;
	private String description;
	private String category;
	private Boolean enabled;
	private Boolean enableConfig;
	private List<String> generatorTargets;
}
