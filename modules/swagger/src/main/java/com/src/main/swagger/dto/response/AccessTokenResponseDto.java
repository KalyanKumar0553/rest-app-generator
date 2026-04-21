package com.src.main.swagger.dto.response;

public class AccessTokenResponseDto {
	private String accessToken;

	public AccessTokenResponseDto() {}

	public AccessTokenResponseDto(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
