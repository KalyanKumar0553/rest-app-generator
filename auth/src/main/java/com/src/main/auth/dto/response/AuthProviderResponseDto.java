package com.src.main.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthProviderResponseDto {
	private boolean googleEnabled;
	private boolean keycloakEnabled;
}
