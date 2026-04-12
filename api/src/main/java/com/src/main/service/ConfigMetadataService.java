package com.src.main.service;

import java.util.List;
import java.util.Optional;
import com.src.main.dto.ConfigPropertyResponseDTO;
import com.src.main.dto.ConfigPropertySaveRequestDTO;
import com.src.main.dto.SingleConfigEntryRequestDTO;

public interface ConfigMetadataService {
	ConfigPropertyResponseDTO saveOrUpdateProperty(ConfigPropertySaveRequestDTO request);

	List<ConfigPropertyResponseDTO> getAllProperties();

	List<ConfigPropertyResponseDTO> getPropertiesByCategory(String category);

	ConfigPropertyResponseDTO updateCurrentValue(SingleConfigEntryRequestDTO request);

	boolean isPropertyEnabled(String propertyKey, boolean defaultValue);

	Optional<String> getPropertyCurrentValue(String propertyKey);

	Optional<Integer> getPropertyCurrentIntValue(String propertyKey);

	void reloadDefaults();
}
