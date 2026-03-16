package com.src.main.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldSpecDTO {
	private String name;
	private String type;
	private Boolean naturalId;
	private ColumnSpecDTO column;
	private JpaSpecDTO jpa;
	private List<ConstraintDTO> constraints;
	private Map<String, String> messages;
}
