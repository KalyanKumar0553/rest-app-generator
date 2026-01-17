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
public class JoinTableSpecDTO {
	private String name;
	private List<JoinColumnSpecDTO> joinColumns;
	private List<JoinColumnSpecDTO> inverseJoinColumns;
	private Boolean uniquePair;
	private String onDelete; // NONE|CASCADE|SET_NULL (DDL hint)
}
