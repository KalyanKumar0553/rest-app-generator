package com.src.main.dto;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldSpecDTO {
	private String name;
	private String type;
	private Boolean naturalId;
	private ColumnSpecDTO column;
	private JpaSpecDTO jpa;
	private List<ConstraintDTO> constraints;
	private Map<String, String> messages;

	public String getName() {
		return this.name;
	}

	public String getType() {
		return this.type;
	}

	public Boolean getNaturalId() {
		return this.naturalId;
	}

	public ColumnSpecDTO getColumn() {
		return this.column;
	}

	public JpaSpecDTO getJpa() {
		return this.jpa;
	}

	public List<ConstraintDTO> getConstraints() {
		return this.constraints;
	}

	public Map<String, String> getMessages() {
		return this.messages;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public void setNaturalId(final Boolean naturalId) {
		this.naturalId = naturalId;
	}

	public void setColumn(final ColumnSpecDTO column) {
		this.column = column;
	}

	public void setJpa(final JpaSpecDTO jpa) {
		this.jpa = jpa;
	}

	public void setConstraints(final List<ConstraintDTO> constraints) {
		this.constraints = constraints;
	}

	public void setMessages(final Map<String, String> messages) {
		this.messages = messages;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof FieldSpecDTO)) return false;
		final FieldSpecDTO other = (FieldSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$naturalId = this.getNaturalId();
		final Object other$naturalId = other.getNaturalId();
		if (this$naturalId == null ? other$naturalId != null : !this$naturalId.equals(other$naturalId)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$type = this.getType();
		final Object other$type = other.getType();
		if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
		final Object this$column = this.getColumn();
		final Object other$column = other.getColumn();
		if (this$column == null ? other$column != null : !this$column.equals(other$column)) return false;
		final Object this$jpa = this.getJpa();
		final Object other$jpa = other.getJpa();
		if (this$jpa == null ? other$jpa != null : !this$jpa.equals(other$jpa)) return false;
		final Object this$constraints = this.getConstraints();
		final Object other$constraints = other.getConstraints();
		if (this$constraints == null ? other$constraints != null : !this$constraints.equals(other$constraints)) return false;
		final Object this$messages = this.getMessages();
		final Object other$messages = other.getMessages();
		if (this$messages == null ? other$messages != null : !this$messages.equals(other$messages)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof FieldSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $naturalId = this.getNaturalId();
		result = result * PRIME + ($naturalId == null ? 43 : $naturalId.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $type = this.getType();
		result = result * PRIME + ($type == null ? 43 : $type.hashCode());
		final Object $column = this.getColumn();
		result = result * PRIME + ($column == null ? 43 : $column.hashCode());
		final Object $jpa = this.getJpa();
		result = result * PRIME + ($jpa == null ? 43 : $jpa.hashCode());
		final Object $constraints = this.getConstraints();
		result = result * PRIME + ($constraints == null ? 43 : $constraints.hashCode());
		final Object $messages = this.getMessages();
		result = result * PRIME + ($messages == null ? 43 : $messages.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "FieldSpecDTO(name=" + this.getName() + ", type=" + this.getType() + ", naturalId=" + this.getNaturalId() + ", column=" + this.getColumn() + ", jpa=" + this.getJpa() + ", constraints=" + this.getConstraints() + ", messages=" + this.getMessages() + ")";
	}

	public FieldSpecDTO(final String name, final String type, final Boolean naturalId, final ColumnSpecDTO column, final JpaSpecDTO jpa, final List<ConstraintDTO> constraints, final Map<String, String> messages) {
		this.name = name;
		this.type = type;
		this.naturalId = naturalId;
		this.column = column;
		this.jpa = jpa;
		this.constraints = constraints;
		this.messages = messages;
	}

	public FieldSpecDTO() {
	}
}
