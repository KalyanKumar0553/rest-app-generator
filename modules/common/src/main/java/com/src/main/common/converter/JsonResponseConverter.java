package com.src.main.common.converter;

import com.fasterxml.jackson.databind.JsonNode;

final class JsonResponseConverter implements ResponseConverter {

	@Override
	public Format format() {
		return Format.JSON;
	}

	@Override
	public String convert(JsonNode json) {
		return json == null ? "null" : json.toString();
	}
}
