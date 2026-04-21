package com.src.main.auth.dto.response;

public class UserProfileResponseDto {
	private String userId;
	private String email;
	private String name;
	private String firstName;
	private String lastName;
	private String avatarUrl;
	private String timeZoneId;

	public UserProfileResponseDto() {
	}

	public UserProfileResponseDto(
			String userId,
			String email,
			String name,
			String firstName,
			String lastName,
			String avatarUrl,
			String timeZoneId) {
		this.userId = userId;
		this.email = email;
		this.name = name;
		this.firstName = firstName;
		this.lastName = lastName;
		this.avatarUrl = avatarUrl;
		this.timeZoneId = timeZoneId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}
}
