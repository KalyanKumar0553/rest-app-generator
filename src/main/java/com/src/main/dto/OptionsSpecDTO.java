package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OptionsSpecDTO {

	private boolean auditing;
	private boolean softDelete;
	private boolean entity;
	private boolean immutable;
	private boolean naturalIdCache;
	private LombokSpecDTO lombok;
}
