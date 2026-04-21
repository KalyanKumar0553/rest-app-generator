package com.src.main.auth.service;

import java.util.List;

public interface AccessProfileRoleProvider {
	List<String> getAdditionalRoles(String userId);
}
