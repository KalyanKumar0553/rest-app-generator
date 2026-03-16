package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)@Data
@AllArgsConstructor
@NoArgsConstructor
public class LombokSpecDTO {
	private Boolean builder;
	private Boolean toString;
	private Boolean equalsAndHashCode;
}