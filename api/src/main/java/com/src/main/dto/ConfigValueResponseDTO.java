package com.src.main.dto;

public class ConfigValueResponseDTO {
	private String key;
	private String label;

	public String getKey() {
		return this.key;
	}

	public String getLabel() {
		return this.label;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ConfigValueResponseDTO)) return false;
		final ConfigValueResponseDTO other = (ConfigValueResponseDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$key = this.getKey();
		final Object other$key = other.getKey();
		if (this$key == null ? other$key != null : !this$key.equals(other$key)) return false;
		final Object this$label = this.getLabel();
		final Object other$label = other.getLabel();
		if (this$label == null ? other$label != null : !this$label.equals(other$label)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ConfigValueResponseDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $key = this.getKey();
		result = result * PRIME + ($key == null ? 43 : $key.hashCode());
		final Object $label = this.getLabel();
		result = result * PRIME + ($label == null ? 43 : $label.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ConfigValueResponseDTO(key=" + this.getKey() + ", label=" + this.getLabel() + ")";
	}

	public ConfigValueResponseDTO(final String key, final String label) {
		this.key = key;
		this.label = label;
	}
}
