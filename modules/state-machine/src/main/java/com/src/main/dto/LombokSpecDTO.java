package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LombokSpecDTO {
	private Boolean builder;
	private Boolean toString;
	private Boolean equalsAndHashCode;

	public Boolean getBuilder() {
		return this.builder;
	}

	public Boolean getToString() {
		return this.toString;
	}

	public Boolean getEqualsAndHashCode() {
		return this.equalsAndHashCode;
	}

	public void setBuilder(final Boolean builder) {
		this.builder = builder;
	}

	public void setToString(final Boolean toString) {
		this.toString = toString;
	}

	public void setEqualsAndHashCode(final Boolean equalsAndHashCode) {
		this.equalsAndHashCode = equalsAndHashCode;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof LombokSpecDTO)) return false;
		final LombokSpecDTO other = (LombokSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$builder = this.getBuilder();
		final Object other$builder = other.getBuilder();
		if (this$builder == null ? other$builder != null : !this$builder.equals(other$builder)) return false;
		final Object this$toString = this.getToString();
		final Object other$toString = other.getToString();
		if (this$toString == null ? other$toString != null : !this$toString.equals(other$toString)) return false;
		final Object this$equalsAndHashCode = this.getEqualsAndHashCode();
		final Object other$equalsAndHashCode = other.getEqualsAndHashCode();
		if (this$equalsAndHashCode == null ? other$equalsAndHashCode != null : !this$equalsAndHashCode.equals(other$equalsAndHashCode)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof LombokSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $builder = this.getBuilder();
		result = result * PRIME + ($builder == null ? 43 : $builder.hashCode());
		final Object $toString = this.getToString();
		result = result * PRIME + ($toString == null ? 43 : $toString.hashCode());
		final Object $equalsAndHashCode = this.getEqualsAndHashCode();
		result = result * PRIME + ($equalsAndHashCode == null ? 43 : $equalsAndHashCode.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "LombokSpecDTO(builder=" + this.getBuilder() + ", toString=" + this.getToString() + ", equalsAndHashCode=" + this.getEqualsAndHashCode() + ")";
	}

	public LombokSpecDTO(final Boolean builder, final Boolean toString, final Boolean equalsAndHashCode) {
		this.builder = builder;
		this.toString = toString;
		this.equalsAndHashCode = equalsAndHashCode;
	}

	public LombokSpecDTO() {
	}
}
