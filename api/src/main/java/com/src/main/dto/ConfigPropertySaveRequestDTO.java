package com.src.main.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class ConfigPropertySaveRequestDTO {
	@NotBlank(message = "Category must not be blank")
	@Size(max = 100, message = "Category can contain up to 100 characters")
	private String category;
	@NotBlank(message = "Label must not be blank")
	@Size(max = 200, message = "Label can contain up to 200 characters")
	private String label;
	@NotBlank(message = "Property key must not be blank")
	@Size(max = 300, message = "Property key can contain up to 300 characters")
	private String propertyKey;
	@NotEmpty(message = "Allowed values list must not be empty")
	@Valid
	private List<ConfigPropertyValueDTO> values;
	@Size(max = 200, message = "Current value can contain up to 200 characters")
	private String currentValueKey;

	public ConfigPropertySaveRequestDTO() {
	}

	public String getCategory() {
		return this.category;
	}

	public String getLabel() {
		return this.label;
	}

	public String getPropertyKey() {
		return this.propertyKey;
	}

	public List<ConfigPropertyValueDTO> getValues() {
		return this.values;
	}

	public String getCurrentValueKey() {
		return this.currentValueKey;
	}

	public void setCategory(final String category) {
		this.category = category;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setPropertyKey(final String propertyKey) {
		this.propertyKey = propertyKey;
	}

	public void setValues(final List<ConfigPropertyValueDTO> values) {
		this.values = values;
	}

	public void setCurrentValueKey(final String currentValueKey) {
		this.currentValueKey = currentValueKey;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ConfigPropertySaveRequestDTO)) return false;
		final ConfigPropertySaveRequestDTO other = (ConfigPropertySaveRequestDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$category = this.getCategory();
		final Object other$category = other.getCategory();
		if (this$category == null ? other$category != null : !this$category.equals(other$category)) return false;
		final Object this$label = this.getLabel();
		final Object other$label = other.getLabel();
		if (this$label == null ? other$label != null : !this$label.equals(other$label)) return false;
		final Object this$propertyKey = this.getPropertyKey();
		final Object other$propertyKey = other.getPropertyKey();
		if (this$propertyKey == null ? other$propertyKey != null : !this$propertyKey.equals(other$propertyKey)) return false;
		final Object this$values = this.getValues();
		final Object other$values = other.getValues();
		if (this$values == null ? other$values != null : !this$values.equals(other$values)) return false;
		final Object this$currentValueKey = this.getCurrentValueKey();
		final Object other$currentValueKey = other.getCurrentValueKey();
		if (this$currentValueKey == null ? other$currentValueKey != null : !this$currentValueKey.equals(other$currentValueKey)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ConfigPropertySaveRequestDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $category = this.getCategory();
		result = result * PRIME + ($category == null ? 43 : $category.hashCode());
		final Object $label = this.getLabel();
		result = result * PRIME + ($label == null ? 43 : $label.hashCode());
		final Object $propertyKey = this.getPropertyKey();
		result = result * PRIME + ($propertyKey == null ? 43 : $propertyKey.hashCode());
		final Object $values = this.getValues();
		result = result * PRIME + ($values == null ? 43 : $values.hashCode());
		final Object $currentValueKey = this.getCurrentValueKey();
		result = result * PRIME + ($currentValueKey == null ? 43 : $currentValueKey.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ConfigPropertySaveRequestDTO(category=" + this.getCategory() + ", label=" + this.getLabel() + ", propertyKey=" + this.getPropertyKey() + ", values=" + this.getValues() + ", currentValueKey=" + this.getCurrentValueKey() + ")";
	}
}
