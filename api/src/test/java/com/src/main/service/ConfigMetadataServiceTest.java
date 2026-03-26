package com.src.main.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.src.main.dto.SingleConfigEntryRequestDTO;
import com.src.main.model.ConfigProperty;
import com.src.main.model.ConfigPropertyValue;
import com.src.main.repository.ConfigPropertyRepository;

@ExtendWith(MockitoExtension.class)
class ConfigMetadataServiceTest {

	@Mock
	private ConfigPropertyRepository configPropertyRepository;

	@Mock
	private DataSource dataSource;

	@InjectMocks
	private ConfigMetadataService service;

	@Test
	void updateCurrentValue_acceptsConfiguredBooleanValueKey() {
		ConfigProperty property = new ConfigProperty();
		property.setCategory("FEATURES");
		property.setLabel("AI Labs");
		property.setPropertyKey("app.feature.ai-labs.enabled");
		LinkedHashSet<ConfigPropertyValue> allowedValues = new LinkedHashSet<>();
		allowedValues.add(ConfigPropertyValue.builder()
				.valueKey("true")
				.valueLabel("Enabled")
				.build());
		allowedValues.add(ConfigPropertyValue.builder()
				.valueKey("false")
				.valueLabel("Disabled")
				.build());
		property.setAllowedValues(allowedValues);

		when(configPropertyRepository.findByPropertyKey("app.feature.ai-labs.enabled"))
				.thenReturn(Optional.of(property));
		when(configPropertyRepository.save(property)).thenReturn(property);

		SingleConfigEntryRequestDTO request = new SingleConfigEntryRequestDTO(
				"FEATURES",
				"app.feature.ai-labs.enabled",
				"true");

		service.updateCurrentValue(request);

		ArgumentCaptor<ConfigProperty> propertyCaptor = ArgumentCaptor.forClass(ConfigProperty.class);
		verify(configPropertyRepository).save(propertyCaptor.capture());
		assertThat(propertyCaptor.getValue().getCurrentValueKey()).isEqualTo("true");
	}
}
