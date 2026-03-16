package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassMethodsSpecDTO {
	private Boolean toString;
	private Boolean hashCode;
	private Boolean equals;
	private Boolean noArgsConstructor;
	private Boolean allArgsConstructor;
	private Boolean builder;
}
