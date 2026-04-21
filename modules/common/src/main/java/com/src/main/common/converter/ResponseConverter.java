package com.src.main.common.converter;

import com.fasterxml.jackson.databind.JsonNode;

public interface ResponseConverter {

	Format format();

	String convert(JsonNode json);

	static String convert(String json, Format format) {
		return ResponseConverterRegistry.defaultRegistry().convert(json, format);
	}
}
