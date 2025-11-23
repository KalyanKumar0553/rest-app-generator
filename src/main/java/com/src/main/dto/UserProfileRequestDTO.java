package com.src.main.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserProfileRequestDTO {
	private String email;
	private String mobile;
	private String fullName;
	private LocalDate dob;
	private String profilePic;
}
