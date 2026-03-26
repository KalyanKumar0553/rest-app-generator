package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionsSpecDTO {
	private boolean auditing;
	private boolean softDelete;
	private boolean entity;
	private boolean immutable;
	private boolean naturalIdCache;
	private LombokSpecDTO lombok;

	public OptionsSpecDTO(final boolean auditing, final boolean softDelete, final boolean entity, final boolean immutable, final boolean naturalIdCache, final LombokSpecDTO lombok) {
		this.auditing = auditing;
		this.softDelete = softDelete;
		this.entity = entity;
		this.immutable = immutable;
		this.naturalIdCache = naturalIdCache;
		this.lombok = lombok;
	}

	public OptionsSpecDTO() {
	}

	public boolean isAuditing() {
		return this.auditing;
	}

	public boolean isSoftDelete() {
		return this.softDelete;
	}

	public boolean isEntity() {
		return this.entity;
	}

	public boolean isImmutable() {
		return this.immutable;
	}

	public boolean isNaturalIdCache() {
		return this.naturalIdCache;
	}

	public LombokSpecDTO getLombok() {
		return this.lombok;
	}

	public void setAuditing(final boolean auditing) {
		this.auditing = auditing;
	}

	public void setSoftDelete(final boolean softDelete) {
		this.softDelete = softDelete;
	}

	public void setEntity(final boolean entity) {
		this.entity = entity;
	}

	public void setImmutable(final boolean immutable) {
		this.immutable = immutable;
	}

	public void setNaturalIdCache(final boolean naturalIdCache) {
		this.naturalIdCache = naturalIdCache;
	}

	public void setLombok(final LombokSpecDTO lombok) {
		this.lombok = lombok;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof OptionsSpecDTO)) return false;
		final OptionsSpecDTO other = (OptionsSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isAuditing() != other.isAuditing()) return false;
		if (this.isSoftDelete() != other.isSoftDelete()) return false;
		if (this.isEntity() != other.isEntity()) return false;
		if (this.isImmutable() != other.isImmutable()) return false;
		if (this.isNaturalIdCache() != other.isNaturalIdCache()) return false;
		final Object this$lombok = this.getLombok();
		final Object other$lombok = other.getLombok();
		if (this$lombok == null ? other$lombok != null : !this$lombok.equals(other$lombok)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof OptionsSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isAuditing() ? 79 : 97);
		result = result * PRIME + (this.isSoftDelete() ? 79 : 97);
		result = result * PRIME + (this.isEntity() ? 79 : 97);
		result = result * PRIME + (this.isImmutable() ? 79 : 97);
		result = result * PRIME + (this.isNaturalIdCache() ? 79 : 97);
		final Object $lombok = this.getLombok();
		result = result * PRIME + ($lombok == null ? 43 : $lombok.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "OptionsSpecDTO(auditing=" + this.isAuditing() + ", softDelete=" + this.isSoftDelete() + ", entity=" + this.isEntity() + ", immutable=" + this.isImmutable() + ", naturalIdCache=" + this.isNaturalIdCache() + ", lombok=" + this.getLombok() + ")";
	}
}
