package com.src.main.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	private static final String OAUTH_CALLBACK_HASH_ROUTE = "/#/auth/oauth/callback";

	@GetMapping("/")
	public String index() {
		return "forward:/index.html";
	}

	@GetMapping("/auth/oauth/callback")
	public String oauthCallback(HttpServletRequest request) {
		String query = request.getQueryString();
		if (query == null || query.isBlank()) {
			return "redirect:" + OAUTH_CALLBACK_HASH_ROUTE;
		}
		return "redirect:" + OAUTH_CALLBACK_HASH_ROUTE + "?" + query;
	}
}
