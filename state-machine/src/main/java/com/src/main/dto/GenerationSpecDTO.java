package com.src.main.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerationSpecDTO {

	public enum Strategy {
		IDENTITY, SEQUENCE, UUID, AUTO, NONE;
	}

	private Strategy strategy;
	private String sequenceName;
	private Integer allocationSize;
	private String generatorName;
	private String hibernateUuidStrategy;

	public Strategy getStrategy() {
		return this.strategy;
	}

	public String getSequenceName() {
		return this.sequenceName;
	}

	public Integer getAllocationSize() {
		return this.allocationSize;
	}

	public String getGeneratorName() {
		return this.generatorName;
	}

	public String getHibernateUuidStrategy() {
		return this.hibernateUuidStrategy;
	}

	public void setStrategy(final Strategy strategy) {
		this.strategy = strategy;
	}

	public void setSequenceName(final String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public void setAllocationSize(final Integer allocationSize) {
		this.allocationSize = allocationSize;
	}

	public void setGeneratorName(final String generatorName) {
		this.generatorName = generatorName;
	}

	public void setHibernateUuidStrategy(final String hibernateUuidStrategy) {
		this.hibernateUuidStrategy = hibernateUuidStrategy;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof GenerationSpecDTO)) return false;
		final GenerationSpecDTO other = (GenerationSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$allocationSize = this.getAllocationSize();
		final Object other$allocationSize = other.getAllocationSize();
		if (this$allocationSize == null ? other$allocationSize != null : !this$allocationSize.equals(other$allocationSize)) return false;
		final Object this$strategy = this.getStrategy();
		final Object other$strategy = other.getStrategy();
		if (this$strategy == null ? other$strategy != null : !this$strategy.equals(other$strategy)) return false;
		final Object this$sequenceName = this.getSequenceName();
		final Object other$sequenceName = other.getSequenceName();
		if (this$sequenceName == null ? other$sequenceName != null : !this$sequenceName.equals(other$sequenceName)) return false;
		final Object this$generatorName = this.getGeneratorName();
		final Object other$generatorName = other.getGeneratorName();
		if (this$generatorName == null ? other$generatorName != null : !this$generatorName.equals(other$generatorName)) return false;
		final Object this$hibernateUuidStrategy = this.getHibernateUuidStrategy();
		final Object other$hibernateUuidStrategy = other.getHibernateUuidStrategy();
		if (this$hibernateUuidStrategy == null ? other$hibernateUuidStrategy != null : !this$hibernateUuidStrategy.equals(other$hibernateUuidStrategy)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof GenerationSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $allocationSize = this.getAllocationSize();
		result = result * PRIME + ($allocationSize == null ? 43 : $allocationSize.hashCode());
		final Object $strategy = this.getStrategy();
		result = result * PRIME + ($strategy == null ? 43 : $strategy.hashCode());
		final Object $sequenceName = this.getSequenceName();
		result = result * PRIME + ($sequenceName == null ? 43 : $sequenceName.hashCode());
		final Object $generatorName = this.getGeneratorName();
		result = result * PRIME + ($generatorName == null ? 43 : $generatorName.hashCode());
		final Object $hibernateUuidStrategy = this.getHibernateUuidStrategy();
		result = result * PRIME + ($hibernateUuidStrategy == null ? 43 : $hibernateUuidStrategy.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "GenerationSpecDTO(strategy=" + this.getStrategy() + ", sequenceName=" + this.getSequenceName() + ", allocationSize=" + this.getAllocationSize() + ", generatorName=" + this.getGeneratorName() + ", hibernateUuidStrategy=" + this.getHibernateUuidStrategy() + ")";
	}

	public GenerationSpecDTO(final Strategy strategy, final String sequenceName, final Integer allocationSize, final String generatorName, final String hibernateUuidStrategy) {
		this.strategy = strategy;
		this.sequenceName = sequenceName;
		this.allocationSize = allocationSize;
		this.generatorName = generatorName;
		this.hibernateUuidStrategy = hibernateUuidStrategy;
	}

	public GenerationSpecDTO() {
	}
}
