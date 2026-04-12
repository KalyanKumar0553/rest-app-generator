package com.src.main.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.dto.ConfigPropertyResponseDTO;
import com.src.main.dto.ConfigPropertySaveRequestDTO;
import com.src.main.dto.SingleConfigEntryRequestDTO;
import com.src.main.exception.GenericException;
import com.src.main.mapper.ConfigMetadataMapper;
import com.src.main.model.ConfigProperty;
import com.src.main.repository.ConfigPropertyRepository;

@Service
public class ConfigMetadataServiceImpl implements ConfigMetadataService {
	private final ConfigPropertyRepository configPropertyRepository;
	private final DataSource dataSource;
	private final ConfigMetadataMapper configMetadataMapper;

	@Override
	@Transactional
	@Caching(evict = {@CacheEvict(cacheNames = "configMetadataAll", allEntries = true), @CacheEvict(cacheNames = "configMetadataByCategory", allEntries = true)})
	public ConfigPropertyResponseDTO saveOrUpdateProperty(ConfigPropertySaveRequestDTO request) {
		ConfigProperty property = configPropertyRepository.findByPropertyKey(request.getPropertyKey()).map(existing -> configMetadataMapper.toEntity(request, existing, normalizeCurrentValue(request.getCurrentValueKey()))).orElseGet(() -> configMetadataMapper.toEntity(request, null, normalizeCurrentValue(request.getCurrentValueKey())));
		var newValues = configMetadataMapper.toValueEntitySet(request.getValues());
		validateCurrentValue(request.getCurrentValueKey(), newValues);
		property.clearAndAddValues(newValues);
		ConfigProperty saved = configPropertyRepository.save(property);
		return configMetadataMapper.toResponseDTO(saved);
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "configMetadataAll", sync = true)
	public List<ConfigPropertyResponseDTO> getAllProperties() {
		return configPropertyRepository.findAll().stream().map(configMetadataMapper::toResponseDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "configMetadataByCategory", key = "#category == null ? '__all__' : #category.trim().toLowerCase()", sync = true)
	public List<ConfigPropertyResponseDTO> getPropertiesByCategory(String category) {
		if (category == null || category.isBlank()) {
			return getAllProperties();
		}
		return configPropertyRepository.findByCategoryOrderByPropertyKeyAsc(category.trim()).stream().map(configMetadataMapper::toResponseDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional
	@Caching(evict = {@CacheEvict(cacheNames = "configMetadataAll", allEntries = true), @CacheEvict(cacheNames = "configMetadataByCategory", allEntries = true)})
	public ConfigPropertyResponseDTO updateCurrentValue(SingleConfigEntryRequestDTO request) {
		ConfigProperty property = configPropertyRepository.findByPropertyKey(request.getPropertyKey()).orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "Configuration property not found: " + request.getPropertyKey()));
		String normalizedCategory = request.getCategory() == null ? null : request.getCategory().trim();
		if (normalizedCategory != null && !normalizedCategory.isBlank() && !property.getCategory().equalsIgnoreCase(normalizedCategory)) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Configuration category does not match the requested property.");
		}
		validateCurrentValue(request.getValueKey(), property.getAllowedValues());
		property.setCurrentValueKey(normalizeCurrentValue(request.getValueKey()));
		return configMetadataMapper.toResponseDTO(configPropertyRepository.save(property));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isPropertyEnabled(String propertyKey, boolean defaultValue) {
		return configPropertyRepository.findByPropertyKey(propertyKey).map(property -> {
			String currentValue = property.getCurrentValueKey();
			if (currentValue == null || currentValue.isBlank()) {
				return defaultValue;
			}
			return Boolean.parseBoolean(currentValue.trim());
		}).orElse(defaultValue);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<String> getPropertyCurrentValue(String propertyKey) {
		return configPropertyRepository.findByPropertyKey(propertyKey).map(ConfigProperty::getCurrentValueKey).map(String::trim).filter(value -> !value.isBlank());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Integer> getPropertyCurrentIntValue(String propertyKey) {
		return getPropertyCurrentValue(propertyKey).flatMap(value -> {
			try {
				return Optional.of(Integer.parseInt(value));
			} catch (NumberFormatException ex) {
				return Optional.empty();
			}
		});
	}

	@Override
	@Transactional
	@Caching(evict = {@CacheEvict(cacheNames = "configMetadataAll", allEntries = true), @CacheEvict(cacheNames = "configMetadataByCategory", allEntries = true)})
	public void reloadDefaults() {
		configPropertyRepository.deleteAllInBatch();
		Resource resource = new ClassPathResource("db/data/default-config-data.sql");
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
		populator.execute(dataSource);
	}

	private void validateCurrentValue(String currentValueKey, java.util.Collection<com.src.main.model.ConfigPropertyValue> values) {
		String normalized = normalizeCurrentValue(currentValueKey);
		if (normalized == null) {
			return;
		}
		boolean isAllowed = values.stream().anyMatch(value -> normalized.equals(value.getValueKey()));
		if (!isAllowed) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Current value must match one of the allowed values.");
		}
	}

	private String normalizeCurrentValue(String currentValueKey) {
		if (currentValueKey == null) {
			return null;
		}
		String normalized = currentValueKey.trim();
		return normalized.isEmpty() ? null : normalized;
	}

	public ConfigMetadataServiceImpl(final ConfigPropertyRepository configPropertyRepository, final DataSource dataSource, final ConfigMetadataMapper configMetadataMapper) {
		this.configPropertyRepository = configPropertyRepository;
		this.dataSource = dataSource;
		this.configMetadataMapper = configMetadataMapper;
	}
}
