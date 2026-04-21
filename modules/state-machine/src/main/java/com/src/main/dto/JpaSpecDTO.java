package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JpaSpecDTO {
	private ConvertSpecDTO convert;

	public ConvertSpecDTO getConvert() {
		return this.convert;
	}

	public void setConvert(final ConvertSpecDTO convert) {
		this.convert = convert;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof JpaSpecDTO)) return false;
		final JpaSpecDTO other = (JpaSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$convert = this.getConvert();
		final Object other$convert = other.getConvert();
		if (this$convert == null ? other$convert != null : !this$convert.equals(other$convert)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof JpaSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $convert = this.getConvert();
		result = result * PRIME + ($convert == null ? 43 : $convert.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "JpaSpecDTO(convert=" + this.getConvert() + ")";
	}

	public JpaSpecDTO(final ConvertSpecDTO convert) {
		this.convert = convert;
	}

	public JpaSpecDTO() {
	}
}
