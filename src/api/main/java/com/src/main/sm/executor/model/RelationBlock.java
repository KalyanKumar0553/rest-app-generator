package com.src.main.sm.executor.model;

import java.util.List;

public class RelationBlock {
	private String name;
	private String declarationType;
	private String targetType;
	private List<String> annotations;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeclarationType() {
		return declarationType;
	}

	public void setDeclarationType(String declarationType) {
		this.declarationType = declarationType;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<String> annotations) {
		this.annotations = annotations;
	}
}
