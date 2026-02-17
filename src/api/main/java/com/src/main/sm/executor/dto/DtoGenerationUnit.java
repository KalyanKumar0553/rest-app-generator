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

	public DtoGenerationUnit(String subPackage, String name, List<Map<String, Object>> fieldModels,
			List<String> classAnnotations, Set<String> imports, Map<String, Object> messageModel) {
		this.subPackage = subPackage;
		this.name = name;
		this.fieldModels = fieldModels;
		this.classAnnotations = classAnnotations;
		this.imports = imports;
		this.messageModel = messageModel;
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
}
