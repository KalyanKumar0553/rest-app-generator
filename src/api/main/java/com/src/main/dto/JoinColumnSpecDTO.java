package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinColumnSpecDTO {
	private String name;
	private Boolean nullable;
	private String referencedColumnName;
	private Boolean index;
	private String onDelete; // NONE|CASCADE|SET_NULL (DDL hint)
}
