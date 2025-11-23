package com.src.main.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
	private String email;
	private String mobile;
	private String username;
	private String password;
	private Boolean isAdmin = true;
}
