package com.src.main.dto;

import lombok.Data;

@Data
public class SendOTPRequestDTO {
	private String username;
	private String email;
	private String mobile;
	
}
