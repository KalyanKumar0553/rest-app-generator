package com.src.main.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.dto.ConfigPropertyResponseDTO;
import com.src.main.dto.ConfigPropertySaveRequestDTO;
import com.src.main.dto.ConfigPropertyValueDTO;
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
	public ConfigPropertyResponseDTO saveOrUpdateProperty(ConfigPropertySaveRequestDTO request) {

		ConfigProperty property = configPropertyRepository
		        .findByPropertyKey(request.getPropertyKey())
		        .map(p -> toEntity(request, p))
		        .orElseGet(() -> toEntity(request, null));

		LinkedHashSet<ConfigPropertyValue> newValues = toValueEntitySet(request.getValues());

		property.clearAndAddValues(newValues);
		
		ConfigProperty saved = configPropertyRepository.save(property);

		return toResponseDTO(saved);
	}

	@Transactional(readOnly = true)
	public List<ConfigPropertyResponseDTO> getAllProperties() {
		return configPropertyRepository.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ConfigPropertyResponseDTO> getPropertiesByCategory(String category) {
		if (category == null || category.isBlank()) {
			return getAllProperties();
		}
		return configPropertyRepository.findByCategoryOrderByPropertyKeyAsc(category.trim()).stream()
				.map(this::toResponseDTO).collect(Collectors.toList());
	}

	private ConfigPropertyValue toEntityValue(ConfigPropertyValueDTO dto) {
		return ConfigPropertyValue.builder().valueKey(dto.getValueKey().trim()).valueLabel(dto.getValueLabel().trim())
				.build();
	}

	private ConfigPropertyResponseDTO toResponseDTO(ConfigProperty property) {
		List<ConfigPropertyValueDTO> values = property.getAllowedValues().stream().map(this::toValueDTO)
				.collect(Collectors.toList());

		return new ConfigPropertyResponseDTO(property.getCategory(), property.getLabel(), property.getPropertyKey(),
				values);
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
		return property;
	}

	private LinkedHashSet<ConfigPropertyValue> toValueEntitySet(List<ConfigPropertyValueDTO> values) {
		return values == null ? new LinkedHashSet<>()
				: values.stream().filter(Objects::nonNull).map(this::toEntityValue) // method reference you already have
						.collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	@Transactional
    public void reloadDefaults() {
        configPropertyRepository.deleteAllInBatch();
        Resource resource = new ClassPathResource("db/data/default-config-data.sql");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.execute(dataSource);
    }

}
