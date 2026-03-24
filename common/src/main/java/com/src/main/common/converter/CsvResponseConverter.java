package com.src.main.common.converter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

final class CsvResponseConverter implements ResponseConverter {

	private static final CsvMapper CSV_MAPPER = new CsvMapper();

	@Override
	public Format format() {
		return Format.CSV;
	}

	@Override
	public String convert(JsonNode json) {
		try {
			List<Map<String, String>> rows = flattenRows(json);
			if (rows.isEmpty()) {
				return "";
			}
			Set<String> headers = new LinkedHashSet<>();
			rows.forEach(row -> headers.addAll(row.keySet()));
			CsvSchema.Builder schemaBuilder = CsvSchema.builder().setUseHeader(true);
			headers.forEach(schemaBuilder::addColumn);
			return CSV_MAPPER.writer(schemaBuilder.build()).writeValueAsString(rows);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to convert JSON to CSV", ex);
		}
	}

	private List<Map<String, String>> flattenRows(JsonNode json) {
		List<Map<String, String>> rows = new ArrayList<>();
		if (json == null || json.isNull()) {
			return rows;
		}
		if (json.isArray()) {
			for (JsonNode node : json) {
				rows.add(flattenObject(node));
			}
			return rows;
		}
		rows.add(flattenObject(json));
		return rows;
	}

	private Map<String, String> flattenObject(JsonNode node) {
		Map<String, String> row = new LinkedHashMap<>();
		if (node == null || node.isNull()) {
			return row;
		}
		if (!node.isObject()) {
			row.put("value", scalarValue(node));
			return row;
		}
		Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> field = fields.next();
			JsonNode value = field.getValue();
			row.put(field.getKey(), value == null || value.isValueNode() || value.isNull() ? scalarValue(value) : value.toString());
		}
		return row;
	}

	private String scalarValue(JsonNode value) {
		if (value == null || value.isNull()) {
			return "";
		}
		return value.isTextual() ? value.asText() : value.toString();
	}
}
