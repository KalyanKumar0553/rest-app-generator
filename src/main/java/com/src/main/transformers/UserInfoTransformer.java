package com.src.main.transformers;

import org.springframework.stereotype.Component;

import com.src.main.dto.SignupRequestDTO;
import com.src.main.model.UserInfo;
import com.src.main.service.UserDetailsServiceImpl;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class UserInfoTransformer {

	final UserDetailsServiceImpl userService;

	public UserInfo fromSignupRequestDTO(SignupRequestDTO signupRequest) {
		UserInfo userInfo = UserInfo.builder().username(signupRequest.getEmail()).email(signupRequest.getEmail())
				.password(signupRequest.getPassword()).build();
		return userInfo;
	}
}
