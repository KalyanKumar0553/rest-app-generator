package com.src.main.subscription.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.subscription.dto.SubscriptionCouponRequest;
import com.src.main.subscription.dto.SubscriptionCouponResponse;
import com.src.main.subscription.entity.SubscriptionCouponEntity;
import com.src.main.subscription.entity.SubscriptionCouponPlanMappingEntity;
import com.src.main.subscription.entity.SubscriptionPlanEntity;
import com.src.main.subscription.enums.DiscountType;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.exception.PlanNotFoundException;
import com.src.main.subscription.repository.SubscriptionCouponPlanMappingRepository;
import com.src.main.subscription.repository.SubscriptionCouponRepository;
import com.src.main.subscription.repository.SubscriptionPlanRepository;
import com.src.main.subscription.service.SubscriptionCouponService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

@Service
public class SubscriptionCouponServiceImpl implements SubscriptionCouponService {
	private final SubscriptionCouponRepository couponRepository;
	private final SubscriptionCouponPlanMappingRepository couponPlanMappingRepository;
	private final SubscriptionPlanRepository planRepository;

	@Override
	@Transactional
	public SubscriptionCouponResponse createCoupon(SubscriptionCouponRequest request) {
		String code = normalizeCode(request.getCode());
		if (couponRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
			throw new InvalidSubscriptionOperationException("Coupon code already exists: " + code);
		}
		SubscriptionCouponEntity entity = new SubscriptionCouponEntity();
		apply(entity, request, code);
		SubscriptionCouponEntity saved = couponRepository.save(entity);
		replaceApplicablePlans(saved, request.getApplicablePlanIds());
		return toResponse(saved);
	}

	@Override
	@Transactional
	public SubscriptionCouponResponse updateCoupon(Long id, SubscriptionCouponRequest request) {
		SubscriptionCouponEntity entity = getEntity(id);
		String code = normalizeCode(request.getCode());
		if (!entity.getCode().equalsIgnoreCase(code) && couponRepository.existsByCodeIgnoreCaseAndDeletedFalse(code)) {
			throw new InvalidSubscriptionOperationException("Coupon code already exists: " + code);
		}
		apply(entity, request, code);
		SubscriptionCouponEntity saved = couponRepository.save(entity);
		replaceApplicablePlans(saved, request.getApplicablePlanIds());
		return toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public SubscriptionCouponResponse getCoupon(Long id) {
		return toResponse(getEntity(id));
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubscriptionCouponResponse> getAllCoupons(Boolean activeOnly) {
		return couponRepository.findAllByDeletedFalseOrderByCreatedAtDesc().stream().filter(coupon -> !Boolean.TRUE.equals(activeOnly) || Boolean.TRUE.equals(coupon.getIsActive())).map(this::toResponse).toList();
	}

	@Override
	@Transactional
	public void activateCoupon(Long id) {
		SubscriptionCouponEntity entity = getEntity(id);
		entity.setIsActive(Boolean.TRUE);
		couponRepository.save(entity);
	}

	@Override
	@Transactional
	public void deactivateCoupon(Long id) {
		SubscriptionCouponEntity entity = getEntity(id);
		entity.setIsActive(Boolean.FALSE);
		couponRepository.save(entity);
	}

	private SubscriptionCouponEntity getEntity(Long id) {
		return couponRepository.findById(id).filter(coupon -> Boolean.FALSE.equals(coupon.getDeleted())).orElseThrow(() -> new InvalidSubscriptionOperationException("Coupon not found: " + id));
	}

	private SubscriptionCouponResponse toResponse(SubscriptionCouponEntity entity) {
		return SubscriptionMapperUtil.toCouponResponse(entity, couponPlanMappingRepository.findAllByCoupon_IdAndDeletedFalse(entity.getId()));
	}

	private void replaceApplicablePlans(SubscriptionCouponEntity coupon, List<Long> applicablePlanIds) {
		couponPlanMappingRepository.deleteByCoupon_Id(coupon.getId());
		if (applicablePlanIds == null || applicablePlanIds.isEmpty()) {
			return;
		}
		List<SubscriptionPlanEntity> plans = planRepository.findAllById(applicablePlanIds).stream().filter(plan -> Boolean.FALSE.equals(plan.getDeleted())).toList();
		if (plans.size() != applicablePlanIds.size()) {
			throw new PlanNotFoundException("One or more applicable plans do not exist");
		}
		List<SubscriptionCouponPlanMappingEntity> mappings = plans.stream().map(plan -> {
			SubscriptionCouponPlanMappingEntity mapping = new SubscriptionCouponPlanMappingEntity();
			mapping.setCoupon(coupon);
			mapping.setPlan(plan);
			return mapping;
		}).toList();
		couponPlanMappingRepository.saveAll(mappings);
	}

	private void apply(SubscriptionCouponEntity entity, SubscriptionCouponRequest request, String code) {
		validateRequest(request);
		entity.setCode(code);
		entity.setName(requireText(request.getName(), "Coupon name is required"));
		entity.setDescription(trimToNull(request.getDescription()));
		entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
		entity.setDiscountType(request.getDiscountType());
		entity.setDiscountValue(request.getDiscountValue());
		entity.setCurrencyCode(trimToNull(request.getCurrencyCode()) == null ? null : request.getCurrencyCode().trim().toUpperCase(Locale.ROOT));
		entity.setValidFrom(request.getValidFrom());
		entity.setValidTo(request.getValidTo());
		entity.setMaxRedemptions(request.getMaxRedemptions());
		entity.setMaxRedemptionsPerTenant(request.getMaxRedemptionsPerTenant());
		entity.setFirstSubscriptionOnly(Boolean.TRUE.equals(request.getFirstSubscriptionOnly()));
		entity.setMetadataJson(trimToNull(request.getMetadataJson()));
	}

	private void validateRequest(SubscriptionCouponRequest request) {
		if (request.getDiscountValue() == null || request.getDiscountValue().signum() <= 0) {
			throw new InvalidSubscriptionOperationException("Coupon discount value must be positive");
		}
		if (request.getDiscountType() == DiscountType.PERCENTAGE && request.getDiscountValue().compareTo(new BigDecimal("100")) > 0) {
			throw new InvalidSubscriptionOperationException("Percentage coupon cannot exceed 100");
		}
		if (request.getDiscountType() == DiscountType.FIXED_AMOUNT && (request.getCurrencyCode() == null || request.getCurrencyCode().isBlank())) {
			throw new InvalidSubscriptionOperationException("Fixed amount coupons require a currency code");
		}
		LocalDateTime validFrom = request.getValidFrom();
		LocalDateTime validTo = request.getValidTo();
		if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
			throw new InvalidSubscriptionOperationException("Coupon validTo must be after validFrom");
		}
	}

	private String normalizeCode(String value) {
		return requireText(value, "Coupon code is required").toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
	}

	private String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new InvalidSubscriptionOperationException(message);
		}
		return value.trim();
	}

	private String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	public SubscriptionCouponServiceImpl(final SubscriptionCouponRepository couponRepository, final SubscriptionCouponPlanMappingRepository couponPlanMappingRepository, final SubscriptionPlanRepository planRepository) {
		this.couponRepository = couponRepository;
		this.couponPlanMappingRepository = couponPlanMappingRepository;
		this.planRepository = planRepository;
	}
}
