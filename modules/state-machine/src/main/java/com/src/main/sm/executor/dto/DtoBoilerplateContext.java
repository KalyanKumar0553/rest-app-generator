package com.src.main.sm.executor.dto;

import java.util.List;
import java.util.Set;

public class DtoBoilerplateContext {
	private final List<String> classAnnotations;
	private final Set<String> imports;
	private final String className;
	private boolean generateToString;
	private boolean generateEquals;
	private boolean generateHashCode;
	private boolean generateNoArgsConstructor;
	private boolean generateAllArgsConstructor;
	private boolean generateBuilder;
	private boolean useLombok;

	public DtoBoilerplateContext(String className, List<String> classAnnotations, Set<String> imports, boolean useLombok,
			boolean generateToString, boolean generateEquals, boolean generateHashCode, boolean generateNoArgsConstructor,
			boolean generateAllArgsConstructor, boolean generateBuilder) {
		this.className = className;
		this.classAnnotations = classAnnotations;
		this.imports = imports;
		this.useLombok = useLombok;
		this.generateToString = generateToString;
		this.generateEquals = generateEquals;
		this.generateHashCode = generateHashCode;
		this.generateNoArgsConstructor = generateNoArgsConstructor;
		this.generateAllArgsConstructor = generateAllArgsConstructor;
		this.generateBuilder = generateBuilder;
	}

	public List<String> getClassAnnotations() {
		return classAnnotations;
	}

	public Set<String> getImports() {
		return imports;
	}

	public String getClassName() {
		return className;
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
