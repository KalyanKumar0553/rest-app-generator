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
public class GenerationSpecDTO {
	public enum Strategy {
		IDENTITY, SEQUENCE, UUID, AUTO, NONE
	}

	private Strategy strategy;
	private String sequenceName;
	private Integer allocationSize;
	private String generatorName;
	private String hibernateUuidStrategy;
}
