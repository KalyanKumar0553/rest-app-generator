package com.src.main.mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.src.main.dto.ConfigPropertyResponseDTO;
import com.src.main.dto.ConfigPropertySaveRequestDTO;
import com.src.main.dto.ConfigPropertyValueDTO;
import com.src.main.model.ConfigProperty;
import com.src.main.model.ConfigPropertyValue;

@Component
public class ConfigMetadataMapper {
	public ConfigPropertyValue toEntityValue(ConfigPropertyValueDTO dto) {
		return ConfigPropertyValue.builder().valueKey(dto.getValueKey().trim()).valueLabel(dto.getValueLabel().trim()).build();
	}

	public ConfigPropertyResponseDTO toResponseDTO(ConfigProperty property) {
		List<ConfigPropertyValueDTO> values = property.getAllowedValues().stream().map(this::toValueDTO).collect(Collectors.toList());
		return new ConfigPropertyResponseDTO(property.getCategory(), property.getLabel(), property.getPropertyKey(), property.getCurrentValueKey(), values);
	}

	public ConfigPropertyValueDTO toValueDTO(ConfigPropertyValue value) {
		ConfigPropertyValueDTO dto = new ConfigPropertyValueDTO();
		dto.setValueKey(value.getValueKey());
		dto.setValueLabel(value.getValueLabel());
		return dto;
	}

	public ConfigProperty toEntity(ConfigPropertySaveRequestDTO request, ConfigProperty existing, String normalizedCurrentValue) {
		ConfigProperty property = existing != null ? existing : new ConfigProperty();
		property.setCategory(request.getCategory().trim());
		property.setLabel(request.getLabel().trim());
		property.setPropertyKey(request.getPropertyKey().trim());
		property.setCurrentValueKey(normalizedCurrentValue);
		return property;
	}

	public LinkedHashSet<ConfigPropertyValue> toValueEntitySet(List<ConfigPropertyValueDTO> values) {
		return values == null ? new LinkedHashSet<>() : values.stream().filter(Objects::nonNull).map(this::toEntityValue).collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
