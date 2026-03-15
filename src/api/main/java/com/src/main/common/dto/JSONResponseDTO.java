package com.src.main.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JSONResponseDTO<T> {
	T data;
	private boolean success;
	private String message;
	boolean isError;
}
