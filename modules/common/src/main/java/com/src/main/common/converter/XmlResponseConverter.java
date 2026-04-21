package com.src.main.common.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

final class XmlResponseConverter implements ResponseConverter {

	private static final XmlMapper XML_MAPPER = new XmlMapper();

	@Override
	public Format format() {
		return Format.XML;
	}

	@Override
	public String convert(JsonNode json) {
		try {
			return XML_MAPPER.writer().withRootName("root").writeValueAsString(json);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to convert JSON to XML", ex);
		}
	}
}
