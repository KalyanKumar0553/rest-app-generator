package com.src.main.subscription.service;

import java.util.List;

import com.src.main.subscription.entity.CustomerSubscriptionEntity;

public interface SubscriptionRoleAssignmentService {
	void syncAssignments(CustomerSubscriptionEntity subscription);
	List<String> getActiveRolesForUser(String userId);
}
