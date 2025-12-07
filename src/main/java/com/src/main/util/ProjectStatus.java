package com.src.main.util;

public enum ProjectStatus {

	ACTIVE("active"),
	ARCHIVED("archived");
	
	private final String description;

	private ProjectStatus(String description) {
		this.description = description;
	}

	public String getDescription(Object... params) {
		return String.format(description, params);
	}

	@Override
	public String toString() {
		return description;
	}
}
