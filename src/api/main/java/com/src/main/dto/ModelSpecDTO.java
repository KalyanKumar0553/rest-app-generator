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
public class ModelSpecDTO {
	private String name;
	private String schema;
	private String tableName;
	private OptionsSpecDTO options;
	private IdSpecDTO id;
	private List<List<String>> uniqueConstraints;
	private List<FieldSpecDTO> fields;
	private List<RelationSpecDTO> relations;
}
