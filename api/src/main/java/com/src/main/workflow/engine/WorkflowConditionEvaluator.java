package com.src.main.workflow.engine;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowConditionEvaluator {

	private final ObjectMapper objectMapper;

	public WorkflowConditionEvaluator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public boolean matches(String conditionJson, ExtendedState state) {
		if (conditionJson == null || conditionJson.isBlank()) {
			return true;
		}
		try {
			JsonNode root = objectMapper.readTree(conditionJson);
			return evaluateNode(root, state);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid workflow condition JSON: " + ex.getMessage(), ex);
		}
	}

	private boolean evaluateNode(JsonNode node, ExtendedState state) {
		if (node == null || node.isNull() || node.isEmpty()) {
			return true;
		}
		if (node.has("all")) {
			for (JsonNode child : node.get("all")) {
				if (!evaluateNode(child, state)) {
					return false;
				}
			}
			return true;
		}
		if (node.has("any")) {
			for (JsonNode child : node.get("any")) {
				if (evaluateNode(child, state)) {
					return true;
				}
			}
			return false;
		}
		return evaluateRule(node, state);
	}

	private boolean evaluateRule(JsonNode node, ExtendedState state) {
		String key = requiredText(node, "key");
		String op = requiredText(node, "op");
		Object actual = state.getVariables().get(key);
		JsonNode valueNode = node.get("value");
		return switch (op) {
		case "EQ" -> compare(actual, valueNode) == 0;
		case "NE" -> compare(actual, valueNode) != 0;
		case "IN" -> contains(valueNode, actual);
		case "NOT_IN" -> !contains(valueNode, actual);
		case "EXISTS" -> actual != null;
		case "NOT_EXISTS" -> actual == null;
		case "GT" -> compare(actual, valueNode) > 0;
		case "GTE" -> compare(actual, valueNode) >= 0;
		case "LT" -> compare(actual, valueNode) < 0;
		case "LTE" -> compare(actual, valueNode) <= 0;
		default -> throw new IllegalArgumentException("Unsupported workflow condition operator: " + op);
		};
	}

	private String requiredText(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull() || value.asText().isBlank()) {
			throw new IllegalArgumentException("Missing workflow condition field: " + field);
		}
		return value.asText();
	}

	private int compare(Object actual, JsonNode valueNode) {
		if (actual == null) {
			return valueNode == null || valueNode.isNull() ? 0 : -1;
		}
		if (actual instanceof Number) {
			BigDecimal left = new BigDecimal(actual.toString());
			BigDecimal right = new BigDecimal(valueNode.asText());
			return left.compareTo(right);
		}
		if (actual instanceof Boolean) {
			return Boolean.compare((Boolean) actual, valueNode.asBoolean());
		}
		String right = valueNode == null || valueNode.isNull() ? "" : valueNode.asText();
		return String.valueOf(actual).compareTo(right);
	}

	private boolean contains(JsonNode valueNode, Object actual) {
		if (valueNode == null || !valueNode.isArray()) {
			throw new IllegalArgumentException("IN/NOT_IN operator requires array value");
		}
		Iterator<JsonNode> iterator = valueNode.elements();
		while (iterator.hasNext()) {
			JsonNode candidate = iterator.next();
			if (compare(actual, candidate) == 0) {
				return true;
			}
		}
		return false;
	}
}
