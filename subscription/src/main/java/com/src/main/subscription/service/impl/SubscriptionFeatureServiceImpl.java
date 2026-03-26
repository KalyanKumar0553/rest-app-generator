package com.src.main.subscription.service.impl;

import java.util.List;
import java.util.Locale;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.subscription.dto.FeatureRequest;
import com.src.main.subscription.dto.FeatureResponse;
import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.enums.FeatureType;
import com.src.main.subscription.exception.FeatureNotFoundException;
import com.src.main.subscription.exception.InvalidSubscriptionOperationException;
import com.src.main.subscription.repository.SubscriptionFeatureRepository;
import com.src.main.subscription.service.SubscriptionFeatureService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

@Service
public class SubscriptionFeatureServiceImpl implements SubscriptionFeatureService {
	private final SubscriptionFeatureRepository featureRepository;

	@Override
	@Transactional
	@Caching(evict = {@CacheEvict(cacheNames = "subscriptionFeatureByCode", allEntries = true), @CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)})
	public FeatureResponse createFeature(FeatureRequest request) {
		String code = normalizeCode(request.getCode());
		if (featureRepository.findByCodeAndDeletedFalse(code).isPresent()) {
			throw new InvalidSubscriptionOperationException("Feature code already exists: " + code);
		}
		SubscriptionFeatureEntity entity = new SubscriptionFeatureEntity();
		apply(entity, request, code);
		return SubscriptionMapperUtil.toFeatureResponse(featureRepository.save(entity));
	}

	@Override
	@Transactional
	@Caching(evict = {@CacheEvict(cacheNames = "subscriptionFeatureByCode", allEntries = true), @CacheEvict(cacheNames = "entitlementsByTenant", allEntries = true)})
	public FeatureResponse updateFeature(Long id, FeatureRequest request) {
		SubscriptionFeatureEntity entity = featureRepository.findById(id).filter(feature -> Boolean.FALSE.equals(feature.getDeleted())).orElseThrow(() -> new FeatureNotFoundException(String.valueOf(id)));
		String code = normalizeCode(request.getCode());
		featureRepository.findByCodeAndDeletedFalse(code).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> {
			throw new InvalidSubscriptionOperationException("Feature code already exists: " + code);
		});
		apply(entity, request, code);
		return SubscriptionMapperUtil.toFeatureResponse(featureRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<FeatureResponse> getAllFeatures(Boolean activeOnly) {
		List<SubscriptionFeatureEntity> entities = Boolean.TRUE.equals(activeOnly) ? featureRepository.findAllByIsActiveTrueAndDeletedFalseOrderByNameAsc() : featureRepository.findAllByDeletedFalseOrderByNameAsc();
		return entities.stream().map(SubscriptionMapperUtil::toFeatureResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public FeatureResponse getFeatureByCode(String code) {
		return SubscriptionMapperUtil.toFeatureResponse(featureRepository.findByCodeAndDeletedFalse(normalizeCode(code)).orElseThrow(() -> new FeatureNotFoundException(code)));
	}

	private void apply(SubscriptionFeatureEntity entity, FeatureRequest request, String code) {
		entity.setCode(code);
		entity.setName(requireText(request.getName(), "Feature name is required"));
		entity.setDescription(trimToNull(request.getDescription()));
		entity.setFeatureType(request.getFeatureType());
		entity.setValueDataType(request.getValueDataType());
		entity.setUnit(trimToNull(request.getUnit()));
		entity.setResetPolicy(request.getResetPolicy());
		entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
		entity.setIsSystem(Boolean.TRUE.equals(request.getIsSystem()));
		entity.setMetadataJson(trimToNull(request.getMetadataJson()));
		if (FeatureType.QUOTA.equals(request.getFeatureType()) && request.getResetPolicy() == null) {
			throw new InvalidSubscriptionOperationException("Quota features require a reset policy");
		}
	}

	private String normalizeCode(String value) {
		return requireText(value, "Feature code is required").toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
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

	public SubscriptionFeatureServiceImpl(final SubscriptionFeatureRepository featureRepository) {
		this.featureRepository = featureRepository;
	}
}
