package com.src.main.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionsSpecDTO {
	
	private boolean auditing;
	private boolean softDelete;
	private boolean entity;
	private boolean immutable;
	private boolean naturalIdCache;
	private LombokSpecDTO lombok;

	public boolean isAuditing() {
		return Boolean.TRUE.equals(auditing);
	}

	public boolean isSoftDelete() {
		return Boolean.TRUE.equals(softDelete);
	}

	public boolean isEntity() {
		return entity;
	}

	public boolean isImmutable() {
		return Boolean.TRUE.equals(immutable);
	}

	public boolean getAuditing() {
		return auditing;
	}

	public void setAuditing(boolean auditing) {
		this.auditing = auditing;
	}

	public boolean getSoftDelete() {
		return softDelete;
	}

	public void setSoftDelete(boolean softDelete) {
		this.softDelete = softDelete;
	}

	public boolean getEntity() {
		return entity;
	}

	public void setEntity(boolean entity) {
		this.entity = entity;
	}

	public Boolean getImmutable() {
		return immutable;
	}

	public void setImmutable(boolean immutable) {
		this.immutable = immutable;
	}
	
	public Boolean getNaturalIdCache() {
		return naturalIdCache;
	}

	public void setNaturalIdCache(boolean naturalIdCache) {
		this.naturalIdCache = naturalIdCache;
	}

	public LombokSpecDTO getLombok() {
		return lombok;
	}

	public void setLombok(LombokSpecDTO lombok) {
		this.lombok = lombok;
	}

}
