package com.src.main.common.converter;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ResponseConverterRegistry {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	private static final ResponseConverterRegistry DEFAULT = new ResponseConverterRegistry(
			new JsonResponseConverter(),
			new YamlResponseConverter(),
			new XmlResponseConverter(),
			new CsvResponseConverter(),
			new ToonResponseConverter());

	private final Map<Format, ResponseConverter> converters = new EnumMap<>(Format.class);

	public ResponseConverterRegistry(ResponseConverter... converters) {
		Arrays.stream(converters).forEach(converter -> this.converters.put(converter.format(), converter));
	}

	public String convert(String json, Format format) {
		if (format == null) {
			throw new IllegalArgumentException("Format is required");
		}
		if (json == null) {
			throw new IllegalArgumentException("JSON payload is required");
		}
		try {
			JsonNode jsonNode = JSON_MAPPER.readTree(json);
			ResponseConverter converter = converters.get(format);
			if (converter == null) {
				throw new IllegalStateException("Unsupported format: " + format);
			}
			if (format == Format.JSON) {
				return json;
			}
			return converter.convert(jsonNode);
		} catch (IllegalStateException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid JSON payload", ex);
		}
	}

	public static ResponseConverterRegistry defaultRegistry() {
		return DEFAULT;
	}
}
