package com.src.main.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.dto.ConfigPropertyResponseDTO;
import com.src.main.dto.ConfigPropertySaveRequestDTO;
import com.src.main.dto.ConfigPropertyValueDTO;
import com.src.main.dto.SingleConfigEntryRequestDTO;
import com.src.main.exception.GenericException;
import com.src.main.model.ConfigProperty;
import com.src.main.model.ConfigPropertyValue;
import com.src.main.repository.ConfigPropertyRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ConfigMetadataService {

	private final ConfigPropertyRepository configPropertyRepository;
	private final DataSource dataSource;

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "configMetadataAll", allEntries = true),
			@CacheEvict(cacheNames = "configMetadataByCategory", allEntries = true)
	})
	public ConfigPropertyResponseDTO saveOrUpdateProperty(ConfigPropertySaveRequestDTO request) {

		ConfigProperty property = configPropertyRepository
		        .findByPropertyKey(request.getPropertyKey())
		        .map(p -> toEntity(request, p))
		        .orElseGet(() -> toEntity(request, null));

		LinkedHashSet<ConfigPropertyValue> newValues = toValueEntitySet(request.getValues());
		validateCurrentValue(request.getCurrentValueKey(), newValues);

		property.clearAndAddValues(newValues);
		property.setCurrentValueKey(normalizeCurrentValue(request.getCurrentValueKey()));
		
		ConfigProperty saved = configPropertyRepository.save(property);

		return toResponseDTO(saved);
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "configMetadataAll", sync = true)
	public List<ConfigPropertyResponseDTO> getAllProperties() {
		return configPropertyRepository.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	@Cacheable(cacheNames = "configMetadataByCategory", key = "#category == null ? '__all__' : #category.trim().toLowerCase()", sync = true)
	public List<ConfigPropertyResponseDTO> getPropertiesByCategory(String category) {
		if (category == null || category.isBlank()) {
			return getAllProperties();
		}
		return configPropertyRepository.findByCategoryOrderByPropertyKeyAsc(category.trim()).stream()
				.map(this::toResponseDTO).collect(Collectors.toList());
	}

	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "configMetadataAll", allEntries = true),
			@CacheEvict(cacheNames = "configMetadataByCategory", allEntries = true)
	})
	public ConfigPropertyResponseDTO updateCurrentValue(SingleConfigEntryRequestDTO request) {
		ConfigProperty property = configPropertyRepository.findByPropertyKey(request.getPropertyKey())
				.orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND,
						"Configuration property not found: " + request.getPropertyKey()));
		String normalizedCategory = request.getCategory() == null ? null : request.getCategory().trim();
		if (normalizedCategory != null && !normalizedCategory.isBlank()
				&& !property.getCategory().equalsIgnoreCase(normalizedCategory)) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Configuration category does not match the requested property.");
		}
		validateCurrentValue(request.getValueKey(), property.getAllowedValues());
		property.setCurrentValueKey(normalizeCurrentValue(request.getValueKey()));
		return toResponseDTO(configPropertyRepository.save(property));
	}

	@Transactional(readOnly = true)
	public boolean isPropertyEnabled(String propertyKey, boolean defaultValue) {
		return configPropertyRepository.findByPropertyKey(propertyKey)
				.map(property -> {
					String currentValue = property.getCurrentValueKey();
					if (currentValue == null || currentValue.isBlank()) {
						return defaultValue;
					}
					return Boolean.parseBoolean(currentValue.trim());
				})
				.orElse(defaultValue);
	}

	@Transactional(readOnly = true)
	public Optional<String> getPropertyCurrentValue(String propertyKey) {
		return configPropertyRepository.findByPropertyKey(propertyKey)
				.map(ConfigProperty::getCurrentValueKey)
				.map(String::trim)
				.filter(value -> !value.isBlank());
	}

	@Transactional(readOnly = true)
	public Optional<Integer> getPropertyCurrentIntValue(String propertyKey) {
		return getPropertyCurrentValue(propertyKey)
				.flatMap(value -> {
					try {
						return Optional.of(Integer.parseInt(value));
					} catch (NumberFormatException ex) {
						return Optional.empty();
					}
				});
	}

	private ConfigPropertyValue toEntityValue(ConfigPropertyValueDTO dto) {
		return ConfigPropertyValue.builder().valueKey(dto.getValueKey().trim()).valueLabel(dto.getValueLabel().trim())
				.build();
	}

	private ConfigPropertyResponseDTO toResponseDTO(ConfigProperty property) {
		List<ConfigPropertyValueDTO> values = property.getAllowedValues().stream().map(this::toValueDTO)
				.collect(Collectors.toList());

		return new ConfigPropertyResponseDTO(property.getCategory(), property.getLabel(), property.getPropertyKey(),
				property.getCurrentValueKey(), values);
	}

	private ConfigPropertyValueDTO toValueDTO(ConfigPropertyValue value) {
		ConfigPropertyValueDTO dto = new ConfigPropertyValueDTO();
		dto.setValueKey(value.getValueKey());
		dto.setValueLabel(value.getValueLabel());
		return dto;
	}

	private ConfigProperty toEntity(ConfigPropertySaveRequestDTO request, ConfigProperty existing) {
		ConfigProperty property = existing != null ? existing : new ConfigProperty();
		property.setCategory(request.getCategory().trim());
		property.setLabel(request.getLabel().trim());
		property.setPropertyKey(request.getPropertyKey().trim());
		property.setCurrentValueKey(normalizeCurrentValue(request.getCurrentValueKey()));
		return property;
	}

	private LinkedHashSet<ConfigPropertyValue> toValueEntitySet(List<ConfigPropertyValueDTO> values) {
		return values == null ? new LinkedHashSet<>()
				: values.stream().filter(Objects::nonNull).map(this::toEntityValue) // method reference you already have
						.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private void validateCurrentValue(String currentValueKey, java.util.Collection<ConfigPropertyValue> values) {
		String normalized = normalizeCurrentValue(currentValueKey);
		if (normalized == null) {
			return;
		}
		boolean isAllowed = values.stream()
				.anyMatch(value -> normalized.equals(value.getValueKey()));
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
	
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "configMetadataAll", allEntries = true),
			@CacheEvict(cacheNames = "configMetadataByCategory", allEntries = true)
	})
    public void reloadDefaults() {
        configPropertyRepository.deleteAllInBatch();
        Resource resource = new ClassPathResource("db/data/default-config-data.sql");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.execute(dataSource);
    }

}
