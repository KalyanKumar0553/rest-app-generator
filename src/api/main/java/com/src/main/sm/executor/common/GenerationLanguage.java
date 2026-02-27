package com.src.main.sm.executor.common;

public enum GenerationLanguage {
	JAVA("java", "java"),
	KOTLIN("kotlin", "kt");

	private final String templateFolder;
	private final String fileExtension;

	GenerationLanguage(String templateFolder, String fileExtension) {
		this.templateFolder = templateFolder;
		this.fileExtension = fileExtension;
	}

	public String templateFolder() {
		return templateFolder;
	}

	public String fileExtension() {
		return fileExtension;
	}
}

