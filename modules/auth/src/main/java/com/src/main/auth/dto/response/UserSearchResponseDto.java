package com.src.main.auth.dto.response;

public class UserSearchResponseDto {
	private String userId;
	private String name;
	private String email;
	private String avatarUrl;

	public UserSearchResponseDto() {
	}

	public UserSearchResponseDto(String userId, String name, String email, String avatarUrl) {
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.avatarUrl = avatarUrl;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
