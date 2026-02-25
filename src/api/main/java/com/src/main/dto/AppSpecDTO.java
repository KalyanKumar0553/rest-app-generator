package com.src.main.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppSpecDTO {
	private String basePackage;
	private String packages;
	private Boolean enableOpenapi;
	private Boolean enableLombok;
	private Boolean useDockerCompose;
	private Boolean pluralizeTableNames;
	private List<String> profiles;
	private List<ModelSpecDTO> models;
	private List<EnumSpecDTO> enums;
}
