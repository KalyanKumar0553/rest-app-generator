package com.src.main.subscription.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.subscription.dto.PlanPriceRequest;
import com.src.main.subscription.dto.PlanPriceResponse;
import com.src.main.subscription.dto.ResolvedPriceResponse;
import com.src.main.subscription.entity.PlanPriceEntity;
import com.src.main.subscription.entity.SubscriptionCouponEntity;
import com.src.main.subscription.entity.SubscriptionCouponPlanMappingEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.BillingCycle;
import com.src.main.subscription.enums.DiscountType;
import com.src.main.subscription.enums.PlanType;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.OverlappingPriceWindowException;
import com.src.main.subscription.exception.PriceNotConfiguredException;
import com.src.main.subscription.repository.CustomerSubscriptionRepository;
import com.src.main.subscription.repository.PlanPriceRepository;
import com.src.main.subscription.repository.SubscriptionCouponPlanMappingRepository;
import com.src.main.subscription.repository.SubscriptionCouponRedemptionRepository;
import com.src.main.subscription.repository.SubscriptionCouponRepository;
import com.src.main.subscription.service.PricingService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

@Service
public class PricingServiceImpl implements PricingService {
	private final PlanPriceRepository planPriceRepository;
	private final SubscriptionLookupService lookupService;
	private final SubscriptionCouponRepository couponRepository;
	private final SubscriptionCouponPlanMappingRepository couponPlanMappingRepository;
	private final SubscriptionCouponRedemptionRepository couponRedemptionRepository;
	private final CustomerSubscriptionRepository customerSubscriptionRepository;

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
		PlanPriceEntity entity = planPriceRepository.findById(id).filter(price -> Boolean.FALSE.equals(price.getDeleted())).orElseThrow(() -> new InvalidSubscriptionOperationException("Plan price not found: " + id));
		validatePriceRequest(entity.getPlan(), request, id);
		apply(entity, request);
		return SubscriptionMapperUtil.toPlanPriceResponse(planPriceRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PlanPriceResponse> getPlanPrices(Long planId) {
		return planPriceRepository.findAllByPlan_IdAndDeletedFalseOrderByEffectiveFromDesc(planId).stream().map(SubscriptionMapperUtil::toPlanPriceResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public ResolvedPriceResponse resolvePrice(String planCode, BillingCycle cycle, String currencyCode, LocalDateTime asOf) {
		return resolvePrice(planCode, cycle, currencyCode, null, null, asOf);
	}

	@Override
	@Transactional(readOnly = true)
	public ResolvedPriceResponse resolvePrice(String planCode, BillingCycle cycle, String currencyCode, String couponCode, Long tenantId, LocalDateTime asOf) {
		LocalDateTime effectiveAt = asOf == null ? LocalDateTime.now() : asOf;
		PlanPriceEntity entity = resolvePlanPrice(planCode, cycle, currencyCode, effectiveAt);
		BigDecimal baseAmount = entity.getAmount();
		BigDecimal planAdjustedAmount = applyPlanDiscount(baseAmount, entity.getDiscountPercent());
		CouponResolution couponResolution = resolveCoupon(entity.getPlan(), normalizeCouponCode(couponCode), tenantId, effectiveAt, planAdjustedAmount, entity.getCurrencyCode());
		BigDecimal finalAmount = planAdjustedAmount.subtract(couponResolution.discountAmount()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
		return ResolvedPriceResponse.builder().planCode(entity.getPlan().getCode()).billingCycle(entity.getBillingCycle()).currencyCode(entity.getCurrencyCode()).baseAmount(baseAmount).amount(finalAmount).discountPercent(entity.getDiscountPercent()).couponCode(couponResolution.couponCode()).couponDiscountAmount(couponResolution.discountAmount()).displayLabel(entity.getDisplayLabel()).effectiveFrom(entity.getEffectiveFrom()).build();
	}

	private PlanPriceEntity resolvePlanPrice(String planCode, BillingCycle cycle, String currencyCode, LocalDateTime effectiveAt) {
		String normalizedPlanCode = planCode == null ? null : planCode.trim().toUpperCase();
		String normalizedCurrencyCode = normalizeCurrency(currencyCode);
		return planPriceRepository.findTopByPlan_CodeAndBillingCycleAndCurrencyCodeAndIsActiveTrueAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToIsNullOrderByEffectiveFromDesc(normalizedPlanCode, cycle, normalizedCurrencyCode, effectiveAt).or(() -> planPriceRepository.findTopByPlan_CodeAndBillingCycleAndCurrencyCodeAndIsActiveTrueAndDeletedFalseAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqualOrderByEffectiveFromDesc(normalizedPlanCode, cycle, normalizedCurrencyCode, effectiveAt, effectiveAt)).orElseThrow(() -> new PriceNotConfiguredException(planCode, cycle, currencyCode));
	}

	private CouponResolution resolveCoupon(SubscriptionPlanEntity plan, String couponCode, Long tenantId, LocalDateTime effectiveAt, BigDecimal planAdjustedAmount, String currencyCode) {
		if (couponCode == null) {
			return new CouponResolution(null, BigDecimal.ZERO);
		}
		SubscriptionCouponEntity coupon = couponRepository.findByCodeAndDeletedFalse(couponCode).orElseThrow(() -> new InvalidSubscriptionOperationException("Coupon not found: " + couponCode));
		validateCoupon(coupon, plan, tenantId, effectiveAt, currencyCode);
		BigDecimal discountAmount = calculateCouponDiscount(coupon, planAdjustedAmount);
		return new CouponResolution(coupon.getCode(), discountAmount);
	}

	private void validateCoupon(SubscriptionCouponEntity coupon, SubscriptionPlanEntity plan, Long tenantId, LocalDateTime effectiveAt, String currencyCode) {
		if (!Boolean.TRUE.equals(coupon.getIsActive())) {
			throw new InvalidSubscriptionOperationException("Coupon is not active: " + coupon.getCode());
		}
		boolean validWindow = coupon.getValidTo() == null ? couponRepository.countByIdAndDeletedFalseAndIsActiveTrueAndValidFromLessThanEqualAndValidToIsNull(coupon.getId(), effectiveAt) > 0 : couponRepository.countByIdAndDeletedFalseAndIsActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(coupon.getId(), effectiveAt, effectiveAt) > 0;
		if (!validWindow) {
			throw new InvalidSubscriptionOperationException("Coupon is not valid at the requested time: " + coupon.getCode());
		}
		List<SubscriptionCouponPlanMappingEntity> applicablePlans = couponPlanMappingRepository.findAllByCoupon_IdAndDeletedFalse(coupon.getId());
		if (!applicablePlans.isEmpty() && applicablePlans.stream().noneMatch(mapping -> mapping.getPlan().getId().equals(plan.getId()))) {
			throw new InvalidSubscriptionOperationException("Coupon is not applicable to plan: " + plan.getCode());
		}
		if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT && coupon.getCurrencyCode() != null && !coupon.getCurrencyCode().equalsIgnoreCase(currencyCode)) {
			throw new InvalidSubscriptionOperationException("Coupon currency does not match price currency");
		}
		if (coupon.getMaxRedemptions() != null && couponRedemptionRepository.countByCoupon_IdAndDeletedFalse(coupon.getId()) >= coupon.getMaxRedemptions()) {
			throw new InvalidSubscriptionOperationException("Coupon redemption limit reached");
		}
		if (tenantId != null && coupon.getMaxRedemptionsPerTenant() != null && couponRedemptionRepository.countByCoupon_IdAndTenantIdAndDeletedFalse(coupon.getId(), tenantId) >= coupon.getMaxRedemptionsPerTenant()) {
			throw new InvalidSubscriptionOperationException("Coupon redemption limit reached for tenant");
		}
		if (tenantId != null && Boolean.TRUE.equals(coupon.getFirstSubscriptionOnly()) && customerSubscriptionRepository.countByTenantIdAndDeletedFalse(tenantId) > 0) {
			throw new InvalidSubscriptionOperationException("Coupon is only valid for a first subscription");
		}
	}

	private BigDecimal calculateCouponDiscount(SubscriptionCouponEntity coupon, BigDecimal planAdjustedAmount) {
		if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
			return percentageDiscount(planAdjustedAmount, coupon.getDiscountValue());
		}
		return coupon.getDiscountValue().setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal applyPlanDiscount(BigDecimal amount, BigDecimal discountPercent) {
		if (discountPercent == null || discountPercent.signum() <= 0) {
			return amount.setScale(2, RoundingMode.HALF_UP);
		}
		return amount.subtract(percentageDiscount(amount, discountPercent)).setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal percentageDiscount(BigDecimal amount, BigDecimal percent) {
		return amount.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
	}

	private void validatePriceRequest(SubscriptionPlanEntity plan, PlanPriceRequest request, Long currentId) {
		if (request.getAmount() == null || request.getAmount().signum() < 0) {
			throw new InvalidSubscriptionOperationException("Price amount must be non-negative");
		}
		if (plan.getPlanType() == PlanType.PAID && request.getAmount().signum() == 0) {
			throw new InvalidSubscriptionOperationException("Paid plans must have a positive price");
		}
		List<PlanPriceEntity> existing = planPriceRepository.findAllByPlan_IdAndDeletedFalseOrderByEffectiveFromDesc(plan.getId());
		boolean overlapping = existing.stream().filter(price -> currentId == null || !price.getId().equals(currentId)).filter(price -> price.getBillingCycle() == request.getBillingCycle()).filter(price -> price.getCurrencyCode().equalsIgnoreCase(request.getCurrencyCode())).anyMatch(price -> overlaps(price.getEffectiveFrom(), price.getEffectiveTo(), request.getEffectiveFrom(), request.getEffectiveTo()));
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
		entity.setCurrencyCode(normalizeCurrency(request.getCurrencyCode()));
		entity.setAmount(request.getAmount());
		entity.setDiscountPercent(request.getDiscountPercent());
		entity.setEffectiveFrom(request.getEffectiveFrom());
		entity.setEffectiveTo(request.getEffectiveTo());
		entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
		entity.setDisplayLabel(request.getDisplayLabel());
		entity.setMetadataJson(request.getMetadataJson());
	}

	private String normalizeCurrency(String currencyCode) {
		if (currencyCode == null || currencyCode.isBlank()) {
			throw new InvalidSubscriptionOperationException("Currency code is required");
		}
		return currencyCode.trim().toUpperCase();
	}

	private String normalizeCouponCode(String couponCode) {
		if (couponCode == null || couponCode.isBlank()) {
			return null;
		}
		return couponCode.trim().toUpperCase();
	}


	private record CouponResolution(String couponCode, BigDecimal discountAmount) {
	}

	public PricingServiceImpl(final PlanPriceRepository planPriceRepository, final SubscriptionLookupService lookupService, final SubscriptionCouponRepository couponRepository, final SubscriptionCouponPlanMappingRepository couponPlanMappingRepository, final SubscriptionCouponRedemptionRepository couponRedemptionRepository, final CustomerSubscriptionRepository customerSubscriptionRepository) {
		this.planPriceRepository = planPriceRepository;
		this.lookupService = lookupService;
		this.couponRepository = couponRepository;
		this.couponPlanMappingRepository = couponPlanMappingRepository;
		this.couponRedemptionRepository = couponRedemptionRepository;
		this.customerSubscriptionRepository = customerSubscriptionRepository;
	}
}
