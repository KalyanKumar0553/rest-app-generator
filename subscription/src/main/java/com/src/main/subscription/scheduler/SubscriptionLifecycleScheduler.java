package com.src.main.subscription.scheduler;

import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.src.main.subscription.service.SubscriptionManagementService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.subscription", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionLifecycleScheduler {

	private final SubscriptionManagementService subscriptionManagementService;

	@Scheduled(fixedDelayString = "${app.subscription.expiry.scheduler.fixed-delay-ms:300000}")
	public void processLifecycleTransitions() {
		subscriptionManagementService.expireSubscriptions(LocalDateTime.now());
	}
}
