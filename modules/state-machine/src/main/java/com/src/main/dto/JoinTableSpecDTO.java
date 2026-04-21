package com.src.main.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinTableSpecDTO {
	private String name;
	private List<JoinColumnSpecDTO> joinColumns;
	private List<JoinColumnSpecDTO> inverseJoinColumns;
	private Boolean uniquePair;
	private String onDelete; // NONE|CASCADE|SET_NULL (DDL hint)

	public String getName() {
		return this.name;
	}

	public List<JoinColumnSpecDTO> getJoinColumns() {
		return this.joinColumns;
	}

	public List<JoinColumnSpecDTO> getInverseJoinColumns() {
		return this.inverseJoinColumns;
	}

	public Boolean getUniquePair() {
		return this.uniquePair;
	}

	public String getOnDelete() {
		return this.onDelete;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setJoinColumns(final List<JoinColumnSpecDTO> joinColumns) {
		this.joinColumns = joinColumns;
	}

	public void setInverseJoinColumns(final List<JoinColumnSpecDTO> inverseJoinColumns) {
		this.inverseJoinColumns = inverseJoinColumns;
	}

	public void setUniquePair(final Boolean uniquePair) {
		this.uniquePair = uniquePair;
	}

	public void setOnDelete(final String onDelete) {
		this.onDelete = onDelete;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof JoinTableSpecDTO)) return false;
		final JoinTableSpecDTO other = (JoinTableSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$uniquePair = this.getUniquePair();
		final Object other$uniquePair = other.getUniquePair();
		if (this$uniquePair == null ? other$uniquePair != null : !this$uniquePair.equals(other$uniquePair)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$joinColumns = this.getJoinColumns();
		final Object other$joinColumns = other.getJoinColumns();
		if (this$joinColumns == null ? other$joinColumns != null : !this$joinColumns.equals(other$joinColumns)) return false;
		final Object this$inverseJoinColumns = this.getInverseJoinColumns();
		final Object other$inverseJoinColumns = other.getInverseJoinColumns();
		if (this$inverseJoinColumns == null ? other$inverseJoinColumns != null : !this$inverseJoinColumns.equals(other$inverseJoinColumns)) return false;
		final Object this$onDelete = this.getOnDelete();
		final Object other$onDelete = other.getOnDelete();
		if (this$onDelete == null ? other$onDelete != null : !this$onDelete.equals(other$onDelete)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof JoinTableSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $uniquePair = this.getUniquePair();
		result = result * PRIME + ($uniquePair == null ? 43 : $uniquePair.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $joinColumns = this.getJoinColumns();
		result = result * PRIME + ($joinColumns == null ? 43 : $joinColumns.hashCode());
		final Object $inverseJoinColumns = this.getInverseJoinColumns();
		result = result * PRIME + ($inverseJoinColumns == null ? 43 : $inverseJoinColumns.hashCode());
		final Object $onDelete = this.getOnDelete();
		result = result * PRIME + ($onDelete == null ? 43 : $onDelete.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "JoinTableSpecDTO(name=" + this.getName() + ", joinColumns=" + this.getJoinColumns() + ", inverseJoinColumns=" + this.getInverseJoinColumns() + ", uniquePair=" + this.getUniquePair() + ", onDelete=" + this.getOnDelete() + ")";
	}

	public JoinTableSpecDTO(final String name, final List<JoinColumnSpecDTO> joinColumns, final List<JoinColumnSpecDTO> inverseJoinColumns, final Boolean uniquePair, final String onDelete) {
		this.name = name;
		this.joinColumns = joinColumns;
		this.inverseJoinColumns = inverseJoinColumns;
		this.uniquePair = uniquePair;
		this.onDelete = onDelete;
	}

	public JoinTableSpecDTO() {
	}
}
