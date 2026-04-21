package com.src.main.sm.executor.common;

public enum GenerationLanguage {
	JAVA("java", "java"),
	KOTLIN("kotlin", "kt"),
	NODE("node", "js"),
	PYTHON("python", "py");

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

	/**
	 * Returns the language-appropriate template filename, eliminating the
	 * {@code language == KOTLIN ? kotlinTpl : javaTpl} ternary that was repeated
	 * in every generator.
	 */
	public String selectTemplate(String javaTemplate, String kotlinTemplate) {
		return this == KOTLIN ? kotlinTemplate : javaTemplate;
	}
}
