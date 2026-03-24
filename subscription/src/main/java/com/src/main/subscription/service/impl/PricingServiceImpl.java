package com.src.main.subscription.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.subscription.dto.PlanPriceRequest;
import com.src.main.subscription.dto.PlanPriceResponse;
import com.src.main.subscription.dto.ResolvedPriceResponse;
import com.src.main.subscription.entity.PlanPriceEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.OverlappingPriceWindowException;
import com.src.main.subscription.exception.PriceNotConfiguredException;
import com.src.main.subscription.repository.PlanPriceRepository;
import com.src.main.subscription.service.PricingService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

	private final PlanPriceRepository planPriceRepository;
	private final SubscriptionLookupService lookupService;

	@Override
	@Transactional
	@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	public PlanPriceResponse createPrice(Long planId, PlanPriceRequest request) {
		SubscriptionPlanEntity plan = lookupService.getPlanById(planId);
		validatePriceRequest(plan, request, null);
		PlanPriceEntity entity = new PlanPriceEntity();
		entity.setPlan(plan);
		apply(entity, request);
		return SubscriptionMapperUtil.toPlanPriceResponse(planPriceRepository.save(entity));
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)
	public PlanPriceResponse updatePrice(Long id, PlanPriceRequest request) {
		PlanPriceEntity entity = planPriceRepository.findById(id)
				.filter(price -> Boolean.FALSE.equals(price.getDeleted()))
				.orElseThrow(() -> new InvalidSubscriptionOperationException("Plan price not found: " + id));
		validatePriceRequest(entity.getPlan(), request, id);
		apply(entity, request);
		return SubscriptionMapperUtil.toPlanPriceResponse(planPriceRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PlanPriceResponse> getPlanPrices(Long planId) {
		return planPriceRepository.findAllByPlan_IdAndDeletedFalseOrderByEffectiveFromDesc(planId).stream()
				.map(SubscriptionMapperUtil::toPlanPriceResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ResolvedPriceResponse resolvePrice(String planCode, BillingCycle cycle, String currencyCode, LocalDateTime asOf) {
		LocalDateTime effectiveAt = asOf == null ? LocalDateTime.now() : asOf;
		PlanPriceEntity entity = planPriceRepository
				.findTopByPlan_CodeAndBillingCycleAndCurrencyCodeAndIsActiveTrueAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(
						planCode.trim().toUpperCase(),
						cycle,
						currencyCode.trim().toUpperCase(),
						effectiveAt)
				.or(() -> planPriceRepository
						.findTopByPlan_CodeAndBillingCycleAndCurrencyCodeAndIsActiveTrueAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(
								planCode.trim().toUpperCase(),
								cycle,
								currencyCode.trim().toUpperCase(),
								effectiveAt,
								effectiveAt))
				.orElseThrow(() -> new PriceNotConfiguredException(planCode, cycle, currencyCode));
		return ResolvedPriceResponse.builder()
				.planCode(entity.getPlan().getCode())
				.billingCycle(entity.getBillingCycle())
				.currencyCode(entity.getCurrencyCode())
				.amount(entity.getAmount())
				.discountPercent(entity.getDiscountPercent())
				.displayLabel(entity.getDisplayLabel())
				.effectiveFrom(entity.getEffectiveFrom())
				.build();
	}

	private void validatePriceRequest(SubscriptionPlanEntity plan, PlanPriceRequest request, Long currentId) {
		if (request.getAmount() == null || request.getAmount().signum() < 0) {
			throw new InvalidSubscriptionOperationException("Price amount must be non-negative");
		}
		if (plan.getPlanType() == PlanType.PAID && request.getAmount().signum() == 0) {
			throw new InvalidSubscriptionOperationException("Paid plans must have a positive price");
		}
		List<PlanPriceEntity> existing = planPriceRepository.findAllByPlan_IdAndDeletedFalseOrderByEffectiveFromDesc(plan.getId());
		boolean overlapping = existing.stream()
				.filter(price -> currentId == null || !price.getId().equals(currentId))
				.filter(price -> price.getBillingCycle() == request.getBillingCycle())
				.filter(price -> price.getCurrencyCode().equalsIgnoreCase(request.getCurrencyCode()))
				.anyMatch(price -> overlaps(price.getEffectiveFrom(), price.getEffectiveTo(), request.getEffectiveFrom(), request.getEffectiveTo()));
		if (overlapping) {
			throw new OverlappingPriceWindowException();
		}
	}

	private boolean overlaps(LocalDateTime startA, LocalDateTime endA, LocalDateTime startB, LocalDateTime endB) {
		LocalDateTime actualEndA = endA == null ? LocalDateTime.of(9999, 12, 31, 23, 59, 59) : endA;
		LocalDateTime actualEndB = endB == null ? LocalDateTime.of(9999, 12, 31, 23, 59, 59) : endB;
		return !startA.isAfter(actualEndB) && !startB.isAfter(actualEndA);
	}

	private void apply(PlanPriceEntity entity, PlanPriceRequest request) {
		entity.setBillingCycle(request.getBillingCycle());
		entity.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
		entity.setAmount(request.getAmount());
		entity.setDiscountPercent(request.getDiscountPercent());
		entity.setEffectiveFrom(request.getEffectiveFrom());
		entity.setEffectiveTo(request.getEffectiveTo());
		entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
		entity.setDisplayLabel(request.getDisplayLabel());
		entity.setMetadataJson(request.getMetadataJson());
	}
}
