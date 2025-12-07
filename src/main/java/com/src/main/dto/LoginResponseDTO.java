package com.src.main.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
	private String accessToken;
	private String refreshToken;
    private UserResponseDTO user;
}