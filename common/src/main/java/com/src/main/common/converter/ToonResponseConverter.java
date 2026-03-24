package com.src.main.common.converter;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

final class ToonResponseConverter implements ResponseConverter {

	@Override
	public Format format() {
		return Format.TOON;
	}

	@Override
	public String convert(JsonNode json) {
		StringBuilder builder = new StringBuilder();
		appendNode(builder, json);
		return builder.toString();
	}

	private void appendNode(StringBuilder builder, JsonNode node) {
		if (node == null || node.isNull()) {
			builder.append("null");
			return;
		}
		if (node.isObject()) {
			builder.append('{');
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			boolean first = true;
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				if (!first) {
					builder.append('|');
				}
				first = false;
				builder.append(field.getKey()).append('=');
				appendNode(builder, field.getValue());
			}
			builder.append('}');
			return;
		}
		if (node.isArray()) {
			builder.append('[');
			for (int i = 0; i < node.size(); i++) {
				if (i > 0) {
					builder.append('|');
				}
				appendNode(builder, node.get(i));
			}
			builder.append(']');
			return;
		}
		if (node.isTextual()) {
			builder.append('\'').append(escape(node.asText())).append('\'');
			return;
		}
		builder.append(node.toString());
	}

	private String escape(String value) {
		return value.replace("\\", "\\\\").replace("'", "\\'");
	}
}
