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
public class RelationSpecDTO {
	public enum Type {
		OneToOne, OneToMany, ManyToOne, ManyToMany
	}

	private String name;
	private Type type;
	private String target;

	// OneToMany-specific
	private String mappedBy;
	private List<String> cascade;
	private Boolean orphanRemoval;
	private String orderBy;
	private OrderColumnDTO orderColumn;

	// ManyToOne-specific
	private Boolean optional;
	private JoinColumnSpecDTO joinColumn;

	// ManyToMany-specific
	private JoinTableSpecDTO joinTable;
}
