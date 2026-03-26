package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvertSpecDTO {
	private String converter;
	private Boolean disableInTests;

	public String getConverter() {
		return this.converter;
	}

	public Boolean getDisableInTests() {
		return this.disableInTests;
	}

	public void setConverter(final String converter) {
		this.converter = converter;
	}

	public void setDisableInTests(final Boolean disableInTests) {
		this.disableInTests = disableInTests;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ConvertSpecDTO)) return false;
		final ConvertSpecDTO other = (ConvertSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$disableInTests = this.getDisableInTests();
		final Object other$disableInTests = other.getDisableInTests();
		if (this$disableInTests == null ? other$disableInTests != null : !this$disableInTests.equals(other$disableInTests)) return false;
		final Object this$converter = this.getConverter();
		final Object other$converter = other.getConverter();
		if (this$converter == null ? other$converter != null : !this$converter.equals(other$converter)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ConvertSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $disableInTests = this.getDisableInTests();
		result = result * PRIME + ($disableInTests == null ? 43 : $disableInTests.hashCode());
		final Object $converter = this.getConverter();
		result = result * PRIME + ($converter == null ? 43 : $converter.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ConvertSpecDTO(converter=" + this.getConverter() + ", disableInTests=" + this.getDisableInTests() + ")";
	}

	public ConvertSpecDTO(final String converter, final Boolean disableInTests) {
		this.converter = converter;
		this.disableInTests = disableInTests;
	}

	public ConvertSpecDTO() {
	}
}
