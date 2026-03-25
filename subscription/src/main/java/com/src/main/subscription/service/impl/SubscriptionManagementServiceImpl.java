package com.src.main.subscription.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.src.main.subscription.entity.SubscriptionCouponEntity;
import com.src.main.subscription.entity.SubscriptionCouponRedemptionEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.DiscountType;
import com.src.main.subscription.enums.AuditActorType;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.enums.SubscriptionSource;
import com.src.main.subscription.enums.SubscriptionStatus;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.SubscriptionNotFoundException;
import com.src.main.subscription.repository.CustomerSubscriptionRepository;
import com.src.main.subscription.repository.SubscriptionCouponRedemptionRepository;
import com.src.main.subscription.repository.SubscriptionCouponRepository;
import com.src.main.subscription.service.PricingService;
import com.src.main.subscription.service.SubscriptionAuditService;
import com.src.main.subscription.service.SubscriptionManagementService;
import com.src.main.subscription.service.SubscriptionRoleAssignmentService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionManagementServiceImpl implements SubscriptionManagementService {

	private final CustomerSubscriptionRepository customerSubscriptionRepository;
	private final SubscriptionLookupService lookupService;
	private final PricingService pricingService;
	private final SubscriptionAuditService auditService;
	private final SubscriptionRoleAssignmentService subscriptionRoleAssignmentService;
	private final SubscriptionCouponRepository subscriptionCouponRepository;
	private final SubscriptionCouponRedemptionRepository subscriptionCouponRedemptionRepository;
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
		entity.setSubscriberUserId(resolveCurrentUserId());
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
		subscriptionRoleAssignmentService.syncAssignments(saved);
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
		entity.setSubscriberUserId(trimToNull(request.getUserId()) == null ? resolveCurrentUserId() : trimToNull(request.getUserId()));
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
		subscriptionRoleAssignmentService.syncAssignments(saved);
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
		ResolvedPriceResponse price = resolvePriceForPlan(plan, request.getBillingCycle(), request.getCurrencyCode(), request.getCouponCode(), request.getTenantId(), startAt);
		CustomerSubscriptionEntity entity = buildSubscriptionEntity(
				request.getTenantId(),
				resolveSubscriberUserId(request.getUserId()),
				plan,
				request.getBillingCycle(),
				startAt,
				request.getAutoRenew(),
				price == null ? BigDecimal.ZERO : price.getAmount(),
				price == null ? normalizeCurrency(request.getCurrencyCode()) : price.getCurrencyCode(),
				price == null ? null : price.getCouponCode(),
				price == null ? null : price.getCouponDiscountAmount(),
				request.getSource(),
				request.getExternalReference(),
				request.getMetadataJson());
		CustomerSubscriptionEntity saved = customerSubscriptionRepository.save(entity);
		saveCouponRedemption(saved);
		subscriptionRoleAssignmentService.syncAssignments(saved);
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
		subscribeRequest.setUserId(resolveSubscriberUserId(request.getUserId()));
		subscribeRequest.setPlanCode(request.getTargetPlanCode());
		subscribeRequest.setBillingCycle(request.getBillingCycle());
		subscribeRequest.setCurrencyCode(request.getCurrencyCode());
		subscribeRequest.setCouponCode(request.getCouponCode());
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
		subscriptionRoleAssignmentService.syncAssignments(current);
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
			subscriptionRoleAssignmentService.syncAssignments(current);
			ResolvedPriceResponse price = resolvePriceForPlan(
					targetPlan,
					current.getScheduledTargetBillingCycle(),
					current.getScheduledTargetCurrencyCode(),
					null,
					current.getTenantId(),
					now);
			CustomerSubscriptionEntity replacement = buildSubscriptionEntity(
					current.getTenantId(),
					current.getSubscriberUserId(),
					targetPlan,
					current.getScheduledTargetBillingCycle(),
					now,
					Boolean.TRUE,
					price == null ? BigDecimal.ZERO : price.getAmount(),
					price == null ? current.getScheduledTargetCurrencyCode() : price.getCurrencyCode(),
					price == null ? null : price.getCouponCode(),
					price == null ? null : price.getCouponDiscountAmount(),
					SubscriptionSource.SYSTEM,
					null,
					null);
			CustomerSubscriptionEntity savedReplacement = customerSubscriptionRepository.save(replacement);
			saveCouponRedemption(savedReplacement);
			subscriptionRoleAssignmentService.syncAssignments(savedReplacement);
			logEvent(replacement, "PLAN_ASSIGNED", current.getPlan(), targetPlan, current.getStatus(), replacement.getStatus(), "Scheduled downgrade applied");
			lookupService.evictActiveSubscription(current.getTenantId());
		}
		for (CustomerSubscriptionEntity current : customerSubscriptionRepository.findAllExpired(now, List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL))) {
			current.setStatus(SubscriptionStatus.EXPIRED);
			customerSubscriptionRepository.save(current);
			subscriptionRoleAssignmentService.syncAssignments(current);
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
		subscriptionRoleAssignmentService.syncAssignments(current);
		logEvent(current, "CANCELLED", current.getPlan(), current.getPlan(), SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLED, reason);
	}

	private CustomerSubscriptionEntity buildSubscriptionEntity(
			Long tenantId,
			String subscriberUserId,
			SubscriptionPlanEntity plan,
			BillingCycle billingCycle,
			LocalDateTime startAt,
			Boolean autoRenew,
			BigDecimal priceSnapshot,
			String currencyCode,
			String couponCode,
			BigDecimal couponDiscountAmount,
			SubscriptionSource source,
			String externalReference,
			String metadataJson) {
		CustomerSubscriptionEntity entity = new CustomerSubscriptionEntity();
		entity.setTenantId(tenantId);
		entity.setSubscriberUserId(subscriberUserId);
		entity.setPlan(plan);
		entity.setBillingCycle(billingCycle);
		entity.setStatus(SubscriptionStatus.ACTIVE);
		entity.setStartAt(startAt);
		entity.setEndAt(plan.getPlanType() == PlanType.FREE ? null : addCycle(startAt, billingCycle));
		entity.setAutoRenew(Boolean.TRUE.equals(autoRenew));
		entity.setPriceSnapshot(priceSnapshot);
		entity.setCurrencyCode(normalizeCurrency(currencyCode));
		if (couponCode != null) {
			SubscriptionCouponEntity coupon = subscriptionCouponRepository.findByCodeAndDeletedFalse(couponCode)
					.orElseThrow(() -> new InvalidSubscriptionOperationException("Coupon not found: " + couponCode));
			entity.setAppliedCoupon(coupon);
			entity.setAppliedCouponCode(coupon.getCode());
			entity.setAppliedDiscountType(coupon.getDiscountType().name());
			entity.setAppliedDiscountValue(coupon.getDiscountValue());
			entity.setAppliedDiscountAmount(couponDiscountAmount);
		}
		entity.setPlanCodeSnapshot(plan.getCode());
		entity.setSource(source == null ? SubscriptionSource.SYSTEM : source);
		entity.setExternalReference(externalReference);
		entity.setMetadataJson(metadataJson);
		return entity;
	}

	private ResolvedPriceResponse resolvePriceForPlan(SubscriptionPlanEntity plan, BillingCycle cycle, String currencyCode, LocalDateTime asOf) {
		return resolvePriceForPlan(plan, cycle, currencyCode, null, null, asOf);
	}

	private ResolvedPriceResponse resolvePriceForPlan(
			SubscriptionPlanEntity plan,
			BillingCycle cycle,
			String currencyCode,
			String couponCode,
			Long tenantId,
			LocalDateTime asOf) {
		if (plan.getPlanType() == PlanType.FREE) {
			return null;
		}
		return pricingService.resolvePrice(plan.getCode(), cycle, normalizeCurrency(currencyCode), couponCode, tenantId, asOf);
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

	private String resolveSubscriberUserId(String requestedUserId) {
		String userId = trimToNull(requestedUserId);
		return userId == null ? resolveCurrentUserId() : userId;
	}

	private String resolveCurrentUserId() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
			return null;
		}
		return authentication.getName();
	}

	private void saveCouponRedemption(CustomerSubscriptionEntity subscription) {
		if (subscription.getAppliedCoupon() == null) {
			return;
		}
		SubscriptionCouponRedemptionEntity redemption = new SubscriptionCouponRedemptionEntity();
		redemption.setCoupon(subscription.getAppliedCoupon());
		redemption.setSubscription(subscription);
		redemption.setTenantId(subscription.getTenantId());
		redemption.setUserId(subscription.getSubscriberUserId());
		redemption.setCouponCodeSnapshot(subscription.getAppliedCouponCode());
		redemption.setDiscountTypeSnapshot(DiscountType.valueOf(subscription.getAppliedDiscountType()));
		redemption.setDiscountValueSnapshot(subscription.getAppliedDiscountValue());
		redemption.setDiscountAmountSnapshot(subscription.getAppliedDiscountAmount() == null ? BigDecimal.ZERO : subscription.getAppliedDiscountAmount());
		redemption.setCurrencyCode(subscription.getCurrencyCode());
		redemption.setRedeemedAt(LocalDateTime.now());
		subscriptionCouponRedemptionRepository.save(redemption);
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String normalizeCurrency(String currencyCode) {
		if (currencyCode == null || currencyCode.isBlank()) {
			return properties.getDefaultCurrency();
		}
		return currencyCode.trim().toUpperCase();
	}
}
