package com.src.main.auth.dto.response;

public class TokenPairResponseDto {
	private String accessToken;
	private String refreshToken;
	private AuthenticatedUserResponseDto user;

	public TokenPairResponseDto() {}

	public TokenPairResponseDto(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public TokenPairResponseDto(String accessToken, String refreshToken, AuthenticatedUserResponseDto user) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.user = user;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public AuthenticatedUserResponseDto getUser() {
		return user;
	}

	public void setUser(AuthenticatedUserResponseDto user) {
		this.user = user;
	}
}
