package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassMethodsSpecDTO {
	private Boolean toString;
	private Boolean hashCode;
	private Boolean equals;
	private Boolean noArgsConstructor;
	private Boolean allArgsConstructor;
	private Boolean builder;

	public Boolean getToString() {
		return this.toString;
	}

	public Boolean getHashCode() {
		return this.hashCode;
	}

	public Boolean getEquals() {
		return this.equals;
	}

	public Boolean getNoArgsConstructor() {
		return this.noArgsConstructor;
	}

	public Boolean getAllArgsConstructor() {
		return this.allArgsConstructor;
	}

	public Boolean getBuilder() {
		return this.builder;
	}

	public void setToString(final Boolean toString) {
		this.toString = toString;
	}

	public void setHashCode(final Boolean hashCode) {
		this.hashCode = hashCode;
	}

	public void setEquals(final Boolean equals) {
		this.equals = equals;
	}

	public void setNoArgsConstructor(final Boolean noArgsConstructor) {
		this.noArgsConstructor = noArgsConstructor;
	}

	public void setAllArgsConstructor(final Boolean allArgsConstructor) {
		this.allArgsConstructor = allArgsConstructor;
	}

	public void setBuilder(final Boolean builder) {
		this.builder = builder;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ClassMethodsSpecDTO)) return false;
		final ClassMethodsSpecDTO other = (ClassMethodsSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$toString = this.getToString();
		final Object other$toString = other.getToString();
		if (this$toString == null ? other$toString != null : !this$toString.equals(other$toString)) return false;
		final Object this$hashCode = this.getHashCode();
		final Object other$hashCode = other.getHashCode();
		if (this$hashCode == null ? other$hashCode != null : !this$hashCode.equals(other$hashCode)) return false;
		final Object this$equals = this.getEquals();
		final Object other$equals = other.getEquals();
		if (this$equals == null ? other$equals != null : !this$equals.equals(other$equals)) return false;
		final Object this$noArgsConstructor = this.getNoArgsConstructor();
		final Object other$noArgsConstructor = other.getNoArgsConstructor();
		if (this$noArgsConstructor == null ? other$noArgsConstructor != null : !this$noArgsConstructor.equals(other$noArgsConstructor)) return false;
		final Object this$allArgsConstructor = this.getAllArgsConstructor();
		final Object other$allArgsConstructor = other.getAllArgsConstructor();
		if (this$allArgsConstructor == null ? other$allArgsConstructor != null : !this$allArgsConstructor.equals(other$allArgsConstructor)) return false;
		final Object this$builder = this.getBuilder();
		final Object other$builder = other.getBuilder();
		if (this$builder == null ? other$builder != null : !this$builder.equals(other$builder)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ClassMethodsSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $toString = this.getToString();
		result = result * PRIME + ($toString == null ? 43 : $toString.hashCode());
		final Object $hashCode = this.getHashCode();
		result = result * PRIME + ($hashCode == null ? 43 : $hashCode.hashCode());
		final Object $equals = this.getEquals();
		result = result * PRIME + ($equals == null ? 43 : $equals.hashCode());
		final Object $noArgsConstructor = this.getNoArgsConstructor();
		result = result * PRIME + ($noArgsConstructor == null ? 43 : $noArgsConstructor.hashCode());
		final Object $allArgsConstructor = this.getAllArgsConstructor();
		result = result * PRIME + ($allArgsConstructor == null ? 43 : $allArgsConstructor.hashCode());
		final Object $builder = this.getBuilder();
		result = result * PRIME + ($builder == null ? 43 : $builder.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ClassMethodsSpecDTO(toString=" + this.getToString() + ", hashCode=" + this.getHashCode() + ", equals=" + this.getEquals() + ", noArgsConstructor=" + this.getNoArgsConstructor() + ", allArgsConstructor=" + this.getAllArgsConstructor() + ", builder=" + this.getBuilder() + ")";
	}

	public ClassMethodsSpecDTO(final Boolean toString, final Boolean hashCode, final Boolean equals, final Boolean noArgsConstructor, final Boolean allArgsConstructor, final Boolean builder) {
		this.toString = toString;
		this.hashCode = hashCode;
		this.equals = equals;
		this.noArgsConstructor = noArgsConstructor;
		this.allArgsConstructor = allArgsConstructor;
		this.builder = builder;
	}

	public ClassMethodsSpecDTO() {
	}
}
