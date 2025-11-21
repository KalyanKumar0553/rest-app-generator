package com.src.main.dto;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstraintDTO {

	private String name; // e.g. "NotBlank", "Size", "pattern"
	private Map<String, Object> params; // e.g. { min:3, max:50 } or { value: 10 }

	public ConstraintDTO() {
	}

	public ConstraintDTO(String name, Map<String, Object> params) {
		this.name = name;
		this.params = (params == null) ? Collections.emptyMap() : params;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	@JsonCreator
	public static ConstraintDTO fromJson(JsonNode node) {
		ObjectMapper mapper = new ObjectMapper();

		// Case 1: "NotBlank"
		if (node.isTextual()) {
			return new ConstraintDTO(node.asText(), Collections.emptyMap());
		}

		// Case 2: explicit object { "name": "Size", "params": {...} }
		if (node.isObject() && node.has("name")) {
			String n = node.get("name").asText();
			Map<String, Object> p = toParams(mapper, node.get("params"));
			return new ConstraintDTO(n, p);
		}

		// Case 3: single-key object { "Size": 10 } or { "Pattern": {...} }
		if (node.isObject()) {
			Iterator<String> it = node.fieldNames();
			if (it.hasNext()) {
				String firstKey = it.next();
				JsonNode val = node.get(firstKey);
				Map<String, Object> p = toParams(mapper, val);
				return new ConstraintDTO(firstKey, p);
			}
		}

		// Defensive: scalars/arrays as raw constraint → wrap
		Map<String, Object> p = toParams(mapper, node);
		return new ConstraintDTO(null, p);
	}

	private static Map<String, Object> toParams(ObjectMapper m, JsonNode v) {
		if (v == null || v.isNull())
			return Collections.emptyMap();
		if (v.isObject()) {
			return m.convertValue(v, new TypeReference<Map<String, Object>>() {
			});
		}
		if (v.isArray()) {
			List<Object> list = m.convertValue(v, new TypeReference<List<Object>>() {
			});
			Map<String, Object> wrapped = new LinkedHashMap<>();
			wrapped.put("values", list);
			return wrapped;
		}
		Map<String, Object> wrapped = new LinkedHashMap<>();
		if (v.isNumber())
			wrapped.put("value", v.numberValue());
		else if (v.isTextual())
			wrapped.put("value", v.asText());
		else if (v.isBoolean())
			wrapped.put("value", v.booleanValue());
		else
			wrapped.put("value", m.convertValue(v, Object.class));
		return wrapped;
	}

	public ConstraintDTO param(String key, Object value) {
		if (this.params == null)
			this.params = new LinkedHashMap<>();
		this.params.put(key, value);
		return this;
	}

	public ConstraintDTO params(Map<String, Object> more) {
		if (more == null || more.isEmpty())
			return this;
		if (this.params == null)
			this.params = new LinkedHashMap<>();
		this.params.putAll(more);
		return this;
	}

	public ConstraintDTO messageKey(String key) {
		return param("messageKey", key);
	}

	// handy factories
	public static ConstraintDTO of(String name) {
		return new ConstraintDTO(name, new LinkedHashMap<>());
	}

	public static ConstraintDTO of(String name, Map<String, Object> params) {
		return new ConstraintDTO(name, params);
	}

}
