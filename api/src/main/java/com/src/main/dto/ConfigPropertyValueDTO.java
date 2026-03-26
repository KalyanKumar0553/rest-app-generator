package com.src.main.dto;

public class ConfigPropertyValueDTO {
	private String valueKey;
	private String valueLabel;

	public String getValueKey() {
		return this.valueKey;
	}

	public String getValueLabel() {
		return this.valueLabel;
	}

	public void setValueKey(final String valueKey) {
		this.valueKey = valueKey;
	}

	public void setValueLabel(final String valueLabel) {
		this.valueLabel = valueLabel;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ConfigPropertyValueDTO)) return false;
		final ConfigPropertyValueDTO other = (ConfigPropertyValueDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$valueKey = this.getValueKey();
		final Object other$valueKey = other.getValueKey();
		if (this$valueKey == null ? other$valueKey != null : !this$valueKey.equals(other$valueKey)) return false;
		final Object this$valueLabel = this.getValueLabel();
		final Object other$valueLabel = other.getValueLabel();
		if (this$valueLabel == null ? other$valueLabel != null : !this$valueLabel.equals(other$valueLabel)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ConfigPropertyValueDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $valueKey = this.getValueKey();
		result = result * PRIME + ($valueKey == null ? 43 : $valueKey.hashCode());
		final Object $valueLabel = this.getValueLabel();
		result = result * PRIME + ($valueLabel == null ? 43 : $valueLabel.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ConfigPropertyValueDTO(valueKey=" + this.getValueKey() + ", valueLabel=" + this.getValueLabel() + ")";
	}

	public ConfigPropertyValueDTO() {
	}

	public ConfigPropertyValueDTO(final String valueKey, final String valueLabel) {
		this.valueKey = valueKey;
		this.valueLabel = valueLabel;
	}
}
