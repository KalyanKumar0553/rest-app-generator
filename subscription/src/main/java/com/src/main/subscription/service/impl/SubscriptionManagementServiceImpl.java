package com.src.main.subscription.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.subscription.config.SubscriptionModuleProperties;
import com.src.main.subscription.dto.CancelSubscriptionRequest;
import com.src.main.subscription.dto.DowngradeSubscriptionRequest;
import com.src.main.subscription.dto.RenewSubscriptionRequest;
import com.src.main.subscription.dto.ResolvedPriceResponse;
import com.src.main.subscription.dto.StartTrialRequest;
import com.src.main.subscription.dto.SubscriptionAuditCommand;
import com.src.main.subscription.dto.SubscriptionRequest;
import com.src.main.subscription.dto.SubscriptionResponse;
import com.src.main.subscription.dto.UpgradeSubscriptionRequest;
import com.src.main.subscription.entity.CustomerSubscriptionEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.AuditActorType;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.enums.SubscriptionSource;
import com.src.main.subscription.enums.SubscriptionStatus;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.SubscriptionNotFoundException;
import com.src.main.subscription.repository.CustomerSubscriptionRepository;
import com.src.main.subscription.service.PricingService;
import com.src.main.subscription.service.SubscriptionAuditService;
import com.src.main.subscription.service.SubscriptionManagementService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionManagementServiceImpl implements SubscriptionManagementService {

	private final CustomerSubscriptionRepository customerSubscriptionRepository;
	private final SubscriptionLookupService lookupService;
	private final PricingService pricingService;
	private final SubscriptionAuditService auditService;
	private final SubscriptionModuleProperties properties;

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#tenantId"),
			@CacheEvict(cacheNames = "entitlementsByTenant", key = "#tenantId")
	})
	public SubscriptionResponse assignDefaultPlan(Long tenantId) {
		CustomerSubscriptionEntity existing = lookupService.findActiveSubscription(tenantId);
		if (existing != null) {
			return SubscriptionMapperUtil.toSubscriptionResponse(existing);
		}
		SubscriptionPlanEntity defaultPlan = lookupService.getDefaultPlan();
		CustomerSubscriptionEntity entity = new CustomerSubscriptionEntity();
		entity.setTenantId(tenantId);
		entity.setPlan(defaultPlan);
		entity.setBillingCycle(BillingCycle.YEARLY);
		entity.setStatus(SubscriptionStatus.ACTIVE);
		entity.setStartAt(LocalDateTime.now());
		entity.setEndAt(defaultPlan.getPlanType() == PlanType.FREE ? null : LocalDateTime.now().plusYears(1));
		entity.setAutoRenew(Boolean.TRUE);
		entity.setPriceSnapshot(BigDecimal.ZERO);
		entity.setCurrencyCode(properties.getDefaultCurrency());
		entity.setPlanCodeSnapshot(defaultPlan.getCode());
		entity.setSource(SubscriptionSource.SYSTEM);
		CustomerSubscriptionEntity saved = customerSubscriptionRepository.save(entity);
		logEvent(saved, "PLAN_ASSIGNED", null, defaultPlan, null, saved.getStatus(), "Default plan assigned");
		return SubscriptionMapperUtil.toSubscriptionResponse(saved);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#request.tenantId"),
			@CacheEvict(cacheNames = "entitlementsByTenant", key = "#request.tenantId")
	})
	public SubscriptionResponse startTrial(StartTrialRequest request) {
		if (lookupService.findActiveSubscription(request.getTenantId()) != null) {
			throw new InvalidSubscriptionOperationException("Tenant already has an active subscription");
		}
		SubscriptionPlanEntity plan = request.getPlanCode() == null || request.getPlanCode().isBlank()
				? lookupService.getDefaultPlan()
				: lookupService.getPlanByCode(request.getPlanCode());
		int trialDays = plan.getTrialDays() == null ? 14 : plan.getTrialDays();
		LocalDateTime now = LocalDateTime.now();
		CustomerSubscriptionEntity entity = new CustomerSubscriptionEntity();
		entity.setTenantId(request.getTenantId());
		entity.setPlan(plan);
		entity.setBillingCycle(BillingCycle.MONTHLY);
		entity.setStatus(SubscriptionStatus.TRIAL);
		entity.setStartAt(now);
		entity.setEndAt(now.plusDays(trialDays));
		entity.setTrialStartAt(now);
		entity.setTrialEndAt(now.plusDays(trialDays));
		entity.setAutoRenew(Boolean.FALSE);
		entity.setPriceSnapshot(BigDecimal.ZERO);
		entity.setCurrencyCode(properties.getDefaultCurrency());
		entity.setPlanCodeSnapshot(plan.getCode());
		entity.setSource(SubscriptionSource.SYSTEM);
		CustomerSubscriptionEntity saved = customerSubscriptionRepository.save(entity);
		logEvent(saved, "TRIAL_STARTED", null, plan, null, saved.getStatus(), request.getReason());
		return SubscriptionMapperUtil.toSubscriptionResponse(saved);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#request.tenantId"),
			@CacheEvict(cacheNames = "entitlementsByTenant", key = "#request.tenantId")
	})
	public SubscriptionResponse subscribe(SubscriptionRequest request) {
		SubscriptionPlanEntity plan = lookupService.getPlanByCode(request.getPlanCode());
		LocalDateTime startAt = request.getStartAt() == null ? LocalDateTime.now() : request.getStartAt();
		closeCurrentSubscription(request.getTenantId(), startAt, "Replaced by new subscription");
		ResolvedPriceResponse price = resolvePriceForPlan(plan, request.getBillingCycle(), request.getCurrencyCode(), startAt);
		CustomerSubscriptionEntity entity = buildSubscriptionEntity(
				request.getTenantId(),
				plan,
				request.getBillingCycle(),
				startAt,
				request.getAutoRenew(),
				price == null ? BigDecimal.ZERO : price.getAmount(),
				price == null ? normalizeCurrency(request.getCurrencyCode()) : price.getCurrencyCode(),
				request.getSource(),
				request.getExternalReference(),
				request.getMetadataJson());
		CustomerSubscriptionEntity saved = customerSubscriptionRepository.save(entity);
		logEvent(saved, "PLAN_ASSIGNED", null, plan, null, saved.getStatus(), request.getReason());
		return SubscriptionMapperUtil.toSubscriptionResponse(saved);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#request.tenantId"),
			@CacheEvict(cacheNames = "entitlementsByTenant", key = "#request.tenantId")
	})
	public SubscriptionResponse upgrade(UpgradeSubscriptionRequest request) {
		CustomerSubscriptionEntity current = requireCurrent(request.getTenantId());
		SubscriptionRequest subscribeRequest = new SubscriptionRequest();
		subscribeRequest.setTenantId(request.getTenantId());
		subscribeRequest.setPlanCode(request.getTargetPlanCode());
		subscribeRequest.setBillingCycle(request.getBillingCycle());
		subscribeRequest.setCurrencyCode(request.getCurrencyCode());
		subscribeRequest.setAutoRenew(Boolean.TRUE);
		subscribeRequest.setSource(SubscriptionSource.ADMIN);
		subscribeRequest.setStartAt(LocalDateTime.now());
		subscribeRequest.setReason(request.getReason());
		SubscriptionResponse response = subscribe(subscribeRequest);
		logEvent(current, "UPGRADED", current.getPlan(), lookupService.getPlanByCode(request.getTargetPlanCode()), current.getStatus(), SubscriptionStatus.CANCELLED, request.getReason());
		return response;
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#request.tenantId"),
			@CacheEvict(cacheNames = "entitlementsByTenant", key = "#request.tenantId")
	})
	public SubscriptionResponse scheduleDowngrade(DowngradeSubscriptionRequest request) {
		CustomerSubscriptionEntity current = requireCurrent(request.getTenantId());
		SubscriptionPlanEntity targetPlan = lookupService.getPlanByCode(request.getTargetPlanCode());
		current.setScheduledTargetPlan(targetPlan);
		current.setScheduledTargetBillingCycle(request.getBillingCycle() == null ? current.getBillingCycle() : request.getBillingCycle());
		current.setScheduledTargetCurrencyCode(normalizeCurrency(request.getCurrencyCode() == null ? current.getCurrencyCode() : request.getCurrencyCode()));
		current.setScheduledChangeAt(current.getEndAt() == null ? LocalDateTime.now() : current.getEndAt());
		customerSubscriptionRepository.save(current);
		logEvent(current, "DOWNGRADED", current.getPlan(), targetPlan, current.getStatus(), current.getStatus(), request.getReason());
		return SubscriptionMapperUtil.toSubscriptionResponse(current);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#request.tenantId"),
			@CacheEvict(cacheNames = "entitlementsByTenant", key = "#request.tenantId")
	})
	public SubscriptionResponse cancel(CancelSubscriptionRequest request) {
		CustomerSubscriptionEntity current = requireCurrent(request.getTenantId());
		current.setAutoRenew(Boolean.FALSE);
		if (Boolean.TRUE.equals(request.getImmediate())) {
			current.setStatus(SubscriptionStatus.CANCELLED);
			current.setCancelledAt(LocalDateTime.now());
			current.setCancelReason(request.getReason());
			current.setEndAt(LocalDateTime.now());
		}
		customerSubscriptionRepository.save(current);
		logEvent(current, "CANCELLED", current.getPlan(), current.getPlan(), SubscriptionStatus.ACTIVE, current.getStatus(), request.getReason());
		return SubscriptionMapperUtil.toSubscriptionResponse(current);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "activeSubscriptionByTenant", key = "#request.tenantId"),
			@CacheEvict(cacheNames = "entitlementsByTenant", key = "#request.tenantId")
	})
	public SubscriptionResponse renew(RenewSubscriptionRequest request) {
		CustomerSubscriptionEntity current = requireCurrent(request.getTenantId());
		if (current.getEndAt() == null) {
			return SubscriptionMapperUtil.toSubscriptionResponse(current);
		}
		LocalDateTime base = current.getEndAt();
		current.setEndAt(addCycle(base, current.getBillingCycle()));
		current.setStatus(SubscriptionStatus.ACTIVE);
		current.setRenewalAttemptCount((current.getRenewalAttemptCount() == null ? 0 : current.getRenewalAttemptCount()) + 1);
		customerSubscriptionRepository.save(current);
		logEvent(current, "RENEWED", current.getPlan(), current.getPlan(), SubscriptionStatus.ACTIVE, SubscriptionStatus.ACTIVE, request.getReason());
		return SubscriptionMapperUtil.toSubscriptionResponse(current);
	}

	@Override
	@Transactional
	public void expireSubscriptions(LocalDateTime now) {
		for (CustomerSubscriptionEntity current : customerSubscriptionRepository.findAllDueScheduledDowngrades(now)) {
			SubscriptionPlanEntity targetPlan = current.getScheduledTargetPlan();
			if (targetPlan == null) {
				continue;
			}
			current.setStatus(SubscriptionStatus.EXPIRED);
			current.setEndAt(now);
			customerSubscriptionRepository.save(current);
			ResolvedPriceResponse price = resolvePriceForPlan(
					targetPlan,
					current.getScheduledTargetBillingCycle(),
					current.getScheduledTargetCurrencyCode(),
					now);
			CustomerSubscriptionEntity replacement = buildSubscriptionEntity(
					current.getTenantId(),
					targetPlan,
					current.getScheduledTargetBillingCycle(),
					now,
					Boolean.TRUE,
					price == null ? BigDecimal.ZERO : price.getAmount(),
					price == null ? current.getScheduledTargetCurrencyCode() : price.getCurrencyCode(),
					SubscriptionSource.SYSTEM,
					null,
					null);
			customerSubscriptionRepository.save(replacement);
			logEvent(replacement, "PLAN_ASSIGNED", current.getPlan(), targetPlan, current.getStatus(), replacement.getStatus(), "Scheduled downgrade applied");
			lookupService.evictActiveSubscription(current.getTenantId());
		}
		for (CustomerSubscriptionEntity current : customerSubscriptionRepository.findAllExpired(now, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))) {
			current.setStatus(SubscriptionStatus.EXPIRED);
			customerSubscriptionRepository.save(current);
			logEvent(current, "EXPIRED", current.getPlan(), null, SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED, "Subscription expired");
			lookupService.evictActiveSubscription(current.getTenantId());
			if (properties.isFallbackToDefaultPlanOnExpiry()) {
				assignDefaultPlan(current.getTenantId());
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public SubscriptionResponse getCurrentSubscription(Long tenantId) {
		return SubscriptionMapperUtil.toSubscriptionResponse(requireCurrent(tenantId));
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubscriptionResponse> getSubscriptionHistory(Long tenantId) {
		return customerSubscriptionRepository.findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId).stream()
				.map(SubscriptionMapperUtil::toSubscriptionResponse)
				.toList();
	}

	private CustomerSubscriptionEntity requireCurrent(Long tenantId) {
		CustomerSubscriptionEntity current = lookupService.findActiveSubscription(tenantId);
		if (current == null) {
			throw new SubscriptionNotFoundException(tenantId);
		}
		return current;
	}

	private void closeCurrentSubscription(Long tenantId, LocalDateTime closeAt, String reason) {
		CustomerSubscriptionEntity current = lookupService.findActiveSubscription(tenantId);
		if (current == null) {
			return;
		}
		current.setStatus(SubscriptionStatus.CANCELLED);
		current.setCancelledAt(closeAt);
		current.setCancelReason(reason);
		current.setEndAt(closeAt);
		customerSubscriptionRepository.save(current);
		logEvent(current, "CANCELLED", current.getPlan(), current.getPlan(), SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, reason);
	}

	private CustomerSubscriptionEntity buildSubscriptionEntity(
			Long tenantId,
			SubscriptionPlanEntity plan,
			BillingCycle billingCycle,
			LocalDateTime startAt,
			Boolean autoRenew,
			BigDecimal priceSnapshot,
			String currencyCode,
			SubscriptionSource source,
			String externalReference,
			String metadataJson) {
		CustomerSubscriptionEntity entity = new CustomerSubscriptionEntity();
		entity.setTenantId(tenantId);
		entity.setPlan(plan);
		entity.setBillingCycle(billingCycle);
		entity.setStatus(SubscriptionStatus.ACTIVE);
		entity.setStartAt(startAt);
		entity.setEndAt(plan.getPlanType() == PlanType.FREE ? null : addCycle(startAt, billingCycle));
		entity.setAutoRenew(Boolean.TRUE.equals(autoRenew));
		entity.setPriceSnapshot(priceSnapshot);
		entity.setCurrencyCode(normalizeCurrency(currencyCode));
		entity.setPlanCodeSnapshot(plan.getCode());
		entity.setSource(source == null ? SubscriptionSource.SYSTEM : source);
		entity.setExternalReference(externalReference);
		entity.setMetadataJson(metadataJson);
		return entity;
	}

	private ResolvedPriceResponse resolvePriceForPlan(SubscriptionPlanEntity plan, BillingCycle cycle, String currencyCode, LocalDateTime asOf) {
		if (plan.getPlanType() == PlanType.FREE) {
			return null;
		}
		return pricingService.resolvePrice(plan.getCode(), cycle, normalizeCurrency(currencyCode), asOf);
	}

	private LocalDateTime addCycle(LocalDateTime base, BillingCycle cycle) {
		return switch (cycle) {
			case MONTHLY -> base.plusMonths(1);
			case HALF_YEARLY -> base.plusMonths(6);
			case YEARLY -> base.plusYears(1);
		};
	}

	private void logEvent(
			CustomerSubscriptionEntity subscription,
			String eventType,
			SubscriptionPlanEntity oldPlan,
			SubscriptionPlanEntity newPlan,
			SubscriptionStatus oldStatus,
			SubscriptionStatus newStatus,
			String reason) {
		auditService.logEvent(SubscriptionAuditCommand.builder()
				.tenantId(subscription.getTenantId())
				.subscription(subscription)
				.eventType(eventType)
				.oldPlan(oldPlan)
				.newPlan(newPlan)
				.oldStatus(oldStatus)
				.newStatus(newStatus)
				.actorType(AuditActorType.SYSTEM)
				.actorId("SYSTEM")
				.reason(reason)
				.build());
	}

	private String normalizeCurrency(String currencyCode) {
		if (currencyCode == null || currencyCode.isBlank()) {
			return properties.getDefaultCurrency();
		}
		return currencyCode.trim().toUpperCase();
	}
}
