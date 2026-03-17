package com.src.main.auth.dto.response;

public class PermissionResponseDto {
	private String name;
	private String displayName;
	private String description;
	private String category;

	public PermissionResponseDto() {
	}

	public PermissionResponseDto(String name, String displayName, String description, String category) {
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
