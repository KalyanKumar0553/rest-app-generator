package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnSpecDTO {
	private String name;
	private Boolean unique;
	private Boolean nullable;
	private Integer length;
	private String columnDefinition;
}
