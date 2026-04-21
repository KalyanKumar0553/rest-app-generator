package com.src.main.sm.executor.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.src.main.dto.ModelSpecDTO;

public class ModelBoilerplateContext {
	private final ModelSpecDTO model;
	private final String className;
	private final Set<String> imports;
	private final List<String> classAnnotations;
	private final List<Map<String, Object>> properties;
	private boolean generateToString;
	private boolean generateEquals;
	private boolean generateHashCode;
	private boolean generateNoArgsConstructor;
	private boolean generateAllArgsConstructor;
	private boolean generateBuilder;
	private boolean useLombok;

	public ModelBoilerplateContext(ModelSpecDTO model, String className, Set<String> imports, List<String> classAnnotations,
			List<Map<String, Object>> properties, boolean useLombok, boolean generateToString, boolean generateEquals,
			boolean generateHashCode, boolean generateNoArgsConstructor, boolean generateAllArgsConstructor,
			boolean generateBuilder) {
		this.model = model;
		this.className = className;
		this.imports = imports;
		this.classAnnotations = classAnnotations;
		this.properties = properties;
		this.useLombok = useLombok;
		this.generateToString = generateToString;
		this.generateEquals = generateEquals;
		this.generateHashCode = generateHashCode;
		this.generateNoArgsConstructor = generateNoArgsConstructor;
		this.generateAllArgsConstructor = generateAllArgsConstructor;
		this.generateBuilder = generateBuilder;
	}

	public ModelSpecDTO getModel() {
		return model;
	}

	public String getClassName() {
		return className;
	}

	public Set<String> getImports() {
		return imports;
	}

	public List<String> getClassAnnotations() {
		return classAnnotations;
	}

	public List<Map<String, Object>> getProperties() {
		return properties;
	}

	public boolean isUseLombok() {
		return useLombok;
	}

	public void setUseLombok(boolean useLombok) {
		this.useLombok = useLombok;
	}

	public boolean isGenerateToString() {
		return generateToString;
	}

	public boolean isGenerateEquals() {
		return generateEquals;
	}

	public boolean isGenerateHashCode() {
		return generateHashCode;
	}

	public boolean isGenerateNoArgsConstructor() {
		return generateNoArgsConstructor;
	}

	public boolean isGenerateAllArgsConstructor() {
		return generateAllArgsConstructor;
	}

	public boolean isGenerateBuilder() {
		return generateBuilder;
	}
}
