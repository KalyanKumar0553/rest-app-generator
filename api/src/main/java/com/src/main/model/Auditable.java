package com.src.main.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
	@CreatedBy
	@Column(name = "created_by", updatable = false)
	String createdBy;
	@Column(name = "created_on", updatable = false)
	LocalDateTime createdOn;
	@LastModifiedBy
	String lastModifiedBy;
	LocalDateTime lastModifiedOn;
	String timezoneID;

	@PrePersist
	public void prePersist() {
		this.createdOn = LocalDateTime.now();
		this.lastModifiedOn = LocalDateTime.now();
		this.timezoneID = ZoneId.systemDefault().getId();
	}

	@PreUpdate
	public void preUpdate() {
		this.lastModifiedOn = LocalDateTime.now();
		this.timezoneID = ZoneId.systemDefault().getId();
	}

	public Auditable() {
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public LocalDateTime getCreatedOn() {
		return this.createdOn;
	}

	public String getLastModifiedBy() {
		return this.lastModifiedBy;
	}

	public LocalDateTime getLastModifiedOn() {
		return this.lastModifiedOn;
	}

	public String getTimezoneID() {
		return this.timezoneID;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreatedOn(final LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public void setLastModifiedBy(final String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public void setLastModifiedOn(final LocalDateTime lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public void setTimezoneID(final String timezoneID) {
		this.timezoneID = timezoneID;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof Auditable)) return false;
		final Auditable other = (Auditable) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$createdBy = this.getCreatedBy();
		final Object other$createdBy = other.getCreatedBy();
		if (this$createdBy == null ? other$createdBy != null : !this$createdBy.equals(other$createdBy)) return false;
		final Object this$createdOn = this.getCreatedOn();
		final Object other$createdOn = other.getCreatedOn();
		if (this$createdOn == null ? other$createdOn != null : !this$createdOn.equals(other$createdOn)) return false;
		final Object this$lastModifiedBy = this.getLastModifiedBy();
		final Object other$lastModifiedBy = other.getLastModifiedBy();
		if (this$lastModifiedBy == null ? other$lastModifiedBy != null : !this$lastModifiedBy.equals(other$lastModifiedBy)) return false;
		final Object this$lastModifiedOn = this.getLastModifiedOn();
		final Object other$lastModifiedOn = other.getLastModifiedOn();
		if (this$lastModifiedOn == null ? other$lastModifiedOn != null : !this$lastModifiedOn.equals(other$lastModifiedOn)) return false;
		final Object this$timezoneID = this.getTimezoneID();
		final Object other$timezoneID = other.getTimezoneID();
		if (this$timezoneID == null ? other$timezoneID != null : !this$timezoneID.equals(other$timezoneID)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof Auditable;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $createdBy = this.getCreatedBy();
		result = result * PRIME + ($createdBy == null ? 43 : $createdBy.hashCode());
		final Object $createdOn = this.getCreatedOn();
		result = result * PRIME + ($createdOn == null ? 43 : $createdOn.hashCode());
		final Object $lastModifiedBy = this.getLastModifiedBy();
		result = result * PRIME + ($lastModifiedBy == null ? 43 : $lastModifiedBy.hashCode());
		final Object $lastModifiedOn = this.getLastModifiedOn();
		result = result * PRIME + ($lastModifiedOn == null ? 43 : $lastModifiedOn.hashCode());
		final Object $timezoneID = this.getTimezoneID();
		result = result * PRIME + ($timezoneID == null ? 43 : $timezoneID.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "Auditable(createdBy=" + this.getCreatedBy() + ", createdOn=" + this.getCreatedOn() + ", lastModifiedBy=" + this.getLastModifiedBy() + ", lastModifiedOn=" + this.getLastModifiedOn() + ", timezoneID=" + this.getTimezoneID() + ")";
	}
}
