package com.src.main.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationSpecDTO {

	public enum Type {
		OneToOne, OneToMany, ManyToOne, ManyToMany;
	}

	private String name;
	private Type type;
	private String target;
	// OneToMany-specific
	private String mappedBy;
	private List<String> cascade;
	private Boolean orphanRemoval;
	private String orderBy;
	private OrderColumnDTO orderColumn;
	// ManyToOne-specific
	private Boolean optional;
	private JoinColumnSpecDTO joinColumn;
	// ManyToMany-specific
	private JoinTableSpecDTO joinTable;

	public String getName() {
		return this.name;
	}

	public Type getType() {
		return this.type;
	}

	public String getTarget() {
		return this.target;
	}

	public String getMappedBy() {
		return this.mappedBy;
	}

	public List<String> getCascade() {
		return this.cascade;
	}

	public Boolean getOrphanRemoval() {
		return this.orphanRemoval;
	}

	public String getOrderBy() {
		return this.orderBy;
	}

	public OrderColumnDTO getOrderColumn() {
		return this.orderColumn;
	}

	public Boolean getOptional() {
		return this.optional;
	}

	public JoinColumnSpecDTO getJoinColumn() {
		return this.joinColumn;
	}

	public JoinTableSpecDTO getJoinTable() {
		return this.joinTable;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setType(final Type type) {
		this.type = type;
	}

	public void setTarget(final String target) {
		this.target = target;
	}

	public void setMappedBy(final String mappedBy) {
		this.mappedBy = mappedBy;
	}

	public void setCascade(final List<String> cascade) {
		this.cascade = cascade;
	}

	public void setOrphanRemoval(final Boolean orphanRemoval) {
		this.orphanRemoval = orphanRemoval;
	}

	public void setOrderBy(final String orderBy) {
		this.orderBy = orderBy;
	}

	public void setOrderColumn(final OrderColumnDTO orderColumn) {
		this.orderColumn = orderColumn;
	}

	public void setOptional(final Boolean optional) {
		this.optional = optional;
	}

	public void setJoinColumn(final JoinColumnSpecDTO joinColumn) {
		this.joinColumn = joinColumn;
	}

	public void setJoinTable(final JoinTableSpecDTO joinTable) {
		this.joinTable = joinTable;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof RelationSpecDTO)) return false;
		final RelationSpecDTO other = (RelationSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$orphanRemoval = this.getOrphanRemoval();
		final Object other$orphanRemoval = other.getOrphanRemoval();
		if (this$orphanRemoval == null ? other$orphanRemoval != null : !this$orphanRemoval.equals(other$orphanRemoval)) return false;
		final Object this$optional = this.getOptional();
		final Object other$optional = other.getOptional();
		if (this$optional == null ? other$optional != null : !this$optional.equals(other$optional)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$type = this.getType();
		final Object other$type = other.getType();
		if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
		final Object this$target = this.getTarget();
		final Object other$target = other.getTarget();
		if (this$target == null ? other$target != null : !this$target.equals(other$target)) return false;
		final Object this$mappedBy = this.getMappedBy();
		final Object other$mappedBy = other.getMappedBy();
		if (this$mappedBy == null ? other$mappedBy != null : !this$mappedBy.equals(other$mappedBy)) return false;
		final Object this$cascade = this.getCascade();
		final Object other$cascade = other.getCascade();
		if (this$cascade == null ? other$cascade != null : !this$cascade.equals(other$cascade)) return false;
		final Object this$orderBy = this.getOrderBy();
		final Object other$orderBy = other.getOrderBy();
		if (this$orderBy == null ? other$orderBy != null : !this$orderBy.equals(other$orderBy)) return false;
		final Object this$orderColumn = this.getOrderColumn();
		final Object other$orderColumn = other.getOrderColumn();
		if (this$orderColumn == null ? other$orderColumn != null : !this$orderColumn.equals(other$orderColumn)) return false;
		final Object this$joinColumn = this.getJoinColumn();
		final Object other$joinColumn = other.getJoinColumn();
		if (this$joinColumn == null ? other$joinColumn != null : !this$joinColumn.equals(other$joinColumn)) return false;
		final Object this$joinTable = this.getJoinTable();
		final Object other$joinTable = other.getJoinTable();
		if (this$joinTable == null ? other$joinTable != null : !this$joinTable.equals(other$joinTable)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof RelationSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $orphanRemoval = this.getOrphanRemoval();
		result = result * PRIME + ($orphanRemoval == null ? 43 : $orphanRemoval.hashCode());
		final Object $optional = this.getOptional();
		result = result * PRIME + ($optional == null ? 43 : $optional.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $type = this.getType();
		result = result * PRIME + ($type == null ? 43 : $type.hashCode());
		final Object $target = this.getTarget();
		result = result * PRIME + ($target == null ? 43 : $target.hashCode());
		final Object $mappedBy = this.getMappedBy();
		result = result * PRIME + ($mappedBy == null ? 43 : $mappedBy.hashCode());
		final Object $cascade = this.getCascade();
		result = result * PRIME + ($cascade == null ? 43 : $cascade.hashCode());
		final Object $orderBy = this.getOrderBy();
		result = result * PRIME + ($orderBy == null ? 43 : $orderBy.hashCode());
		final Object $orderColumn = this.getOrderColumn();
		result = result * PRIME + ($orderColumn == null ? 43 : $orderColumn.hashCode());
		final Object $joinColumn = this.getJoinColumn();
		result = result * PRIME + ($joinColumn == null ? 43 : $joinColumn.hashCode());
		final Object $joinTable = this.getJoinTable();
		result = result * PRIME + ($joinTable == null ? 43 : $joinTable.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "RelationSpecDTO(name=" + this.getName() + ", type=" + this.getType() + ", target=" + this.getTarget() + ", mappedBy=" + this.getMappedBy() + ", cascade=" + this.getCascade() + ", orphanRemoval=" + this.getOrphanRemoval() + ", orderBy=" + this.getOrderBy() + ", orderColumn=" + this.getOrderColumn() + ", optional=" + this.getOptional() + ", joinColumn=" + this.getJoinColumn() + ", joinTable=" + this.getJoinTable() + ")";
	}

	public RelationSpecDTO(final String name, final Type type, final String target, final String mappedBy, final List<String> cascade, final Boolean orphanRemoval, final String orderBy, final OrderColumnDTO orderColumn, final Boolean optional, final JoinColumnSpecDTO joinColumn, final JoinTableSpecDTO joinTable) {
		this.name = name;
		this.type = type;
		this.target = target;
		this.mappedBy = mappedBy;
		this.cascade = cascade;
		this.orphanRemoval = orphanRemoval;
		this.orderBy = orderBy;
		this.orderColumn = orderColumn;
		this.optional = optional;
		this.joinColumn = joinColumn;
		this.joinTable = joinTable;
	}

	public RelationSpecDTO() {
	}
}
