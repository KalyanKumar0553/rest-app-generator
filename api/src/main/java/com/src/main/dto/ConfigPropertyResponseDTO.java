package com.src.main.dto;

import java.util.List;

public class ConfigPropertyResponseDTO {
	private String category;
	private String label;
	private String propertyKey;
	private String currentValueKey;
	private List<ConfigPropertyValueDTO> values;

	public ConfigPropertyResponseDTO(final String category, final String label, final String propertyKey, final String currentValueKey, final List<ConfigPropertyValueDTO> values) {
		this.category = category;
		this.label = label;
		this.propertyKey = propertyKey;
		this.currentValueKey = currentValueKey;
		this.values = values;
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

	public String getCurrentValueKey() {
		return this.currentValueKey;
	}

	public List<ConfigPropertyValueDTO> getValues() {
		return this.values;
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

	public void setCurrentValueKey(final String currentValueKey) {
		this.currentValueKey = currentValueKey;
	}

	public void setValues(final List<ConfigPropertyValueDTO> values) {
		this.values = values;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ConfigPropertyResponseDTO)) return false;
		final ConfigPropertyResponseDTO other = (ConfigPropertyResponseDTO) o;
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
		final Object this$currentValueKey = this.getCurrentValueKey();
		final Object other$currentValueKey = other.getCurrentValueKey();
		if (this$currentValueKey == null ? other$currentValueKey != null : !this$currentValueKey.equals(other$currentValueKey)) return false;
		final Object this$values = this.getValues();
		final Object other$values = other.getValues();
		if (this$values == null ? other$values != null : !this$values.equals(other$values)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ConfigPropertyResponseDTO;
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
		final Object $currentValueKey = this.getCurrentValueKey();
		result = result * PRIME + ($currentValueKey == null ? 43 : $currentValueKey.hashCode());
		final Object $values = this.getValues();
		result = result * PRIME + ($values == null ? 43 : $values.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ConfigPropertyResponseDTO(category=" + this.getCategory() + ", label=" + this.getLabel() + ", propertyKey=" + this.getPropertyKey() + ", currentValueKey=" + this.getCurrentValueKey() + ", values=" + this.getValues() + ")";
	}
}
