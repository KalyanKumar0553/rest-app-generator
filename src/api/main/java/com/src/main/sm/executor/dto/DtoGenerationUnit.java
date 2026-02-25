package com.src.main.sm.executor.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DtoGenerationUnit {

	private final String subPackage;
	private final String name;
	private final List<Map<String, Object>> fieldModels;
	private final List<String> classAnnotations;
	private final Set<String> imports;
	private final Map<String, Object> messageModel;
	private final boolean useLombok;
	private final boolean generateToString;
	private final boolean generateEquals;
	private final boolean generateHashCode;
	private final boolean generateNoArgsConstructor;
	private final boolean generateAllArgsConstructor;
	private final boolean generateBuilder;

	public DtoGenerationUnit(String subPackage, String name, List<Map<String, Object>> fieldModels,
			List<String> classAnnotations, Set<String> imports, Map<String, Object> messageModel, boolean useLombok,
			boolean generateToString, boolean generateEquals, boolean generateHashCode, boolean generateNoArgsConstructor,
			boolean generateAllArgsConstructor, boolean generateBuilder) {
		this.subPackage = subPackage;
		this.name = name;
		this.fieldModels = fieldModels;
		this.classAnnotations = classAnnotations;
		this.imports = imports;
		this.messageModel = messageModel;
		this.useLombok = useLombok;
		this.generateToString = generateToString;
		this.generateEquals = generateEquals;
		this.generateHashCode = generateHashCode;
		this.generateNoArgsConstructor = generateNoArgsConstructor;
		this.generateAllArgsConstructor = generateAllArgsConstructor;
		this.generateBuilder = generateBuilder;
	}

	public String getSubPackage() {
		return subPackage;
	}

	public String getName() {
		return name;
	}

	public List<Map<String, Object>> getFieldModels() {
		return fieldModels;
	}

	public List<String> getClassAnnotations() {
		return classAnnotations;
	}

	public Set<String> getImports() {
		return imports;
	}

	public Map<String, Object> getMessageModel() {
		return messageModel;
	}

	public boolean isUseLombok() {
		return useLombok;
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
