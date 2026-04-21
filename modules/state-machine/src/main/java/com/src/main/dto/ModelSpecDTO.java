package com.src.main.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelSpecDTO {
	private String name;
	private String schema;
	private String tableName;
	private Boolean addRestEndpoints;
	private Boolean addCrudOperations;
	private ClassMethodsSpecDTO classMethods;
	private OptionsSpecDTO options;
	private IdSpecDTO id;
	private List<List<String>> uniqueConstraints;
	private List<FieldSpecDTO> fields;
	private List<RelationSpecDTO> relations;

	public String getName() {
		return this.name;
	}

	public String getSchema() {
		return this.schema;
	}

	public String getTableName() {
		return this.tableName;
	}

	public Boolean getAddRestEndpoints() {
		return this.addRestEndpoints;
	}

	public Boolean getAddCrudOperations() {
		return this.addCrudOperations;
	}

	public ClassMethodsSpecDTO getClassMethods() {
		return this.classMethods;
	}

	public OptionsSpecDTO getOptions() {
		return this.options;
	}

	public IdSpecDTO getId() {
		return this.id;
	}

	public List<List<String>> getUniqueConstraints() {
		return this.uniqueConstraints;
	}

	public List<FieldSpecDTO> getFields() {
		return this.fields;
	}

	public List<RelationSpecDTO> getRelations() {
		return this.relations;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setSchema(final String schema) {
		this.schema = schema;
	}

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	public void setAddRestEndpoints(final Boolean addRestEndpoints) {
		this.addRestEndpoints = addRestEndpoints;
	}

	public void setAddCrudOperations(final Boolean addCrudOperations) {
		this.addCrudOperations = addCrudOperations;
	}

	public void setClassMethods(final ClassMethodsSpecDTO classMethods) {
		this.classMethods = classMethods;
	}

	public void setOptions(final OptionsSpecDTO options) {
		this.options = options;
	}

	public void setId(final IdSpecDTO id) {
		this.id = id;
	}

	public void setUniqueConstraints(final List<List<String>> uniqueConstraints) {
		this.uniqueConstraints = uniqueConstraints;
	}

	public void setFields(final List<FieldSpecDTO> fields) {
		this.fields = fields;
	}

	public void setRelations(final List<RelationSpecDTO> relations) {
		this.relations = relations;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof ModelSpecDTO)) return false;
		final ModelSpecDTO other = (ModelSpecDTO) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$addRestEndpoints = this.getAddRestEndpoints();
		final Object other$addRestEndpoints = other.getAddRestEndpoints();
		if (this$addRestEndpoints == null ? other$addRestEndpoints != null : !this$addRestEndpoints.equals(other$addRestEndpoints)) return false;
		final Object this$addCrudOperations = this.getAddCrudOperations();
		final Object other$addCrudOperations = other.getAddCrudOperations();
		if (this$addCrudOperations == null ? other$addCrudOperations != null : !this$addCrudOperations.equals(other$addCrudOperations)) return false;
		final Object this$name = this.getName();
		final Object other$name = other.getName();
		if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
		final Object this$schema = this.getSchema();
		final Object other$schema = other.getSchema();
		if (this$schema == null ? other$schema != null : !this$schema.equals(other$schema)) return false;
		final Object this$tableName = this.getTableName();
		final Object other$tableName = other.getTableName();
		if (this$tableName == null ? other$tableName != null : !this$tableName.equals(other$tableName)) return false;
		final Object this$classMethods = this.getClassMethods();
		final Object other$classMethods = other.getClassMethods();
		if (this$classMethods == null ? other$classMethods != null : !this$classMethods.equals(other$classMethods)) return false;
		final Object this$options = this.getOptions();
		final Object other$options = other.getOptions();
		if (this$options == null ? other$options != null : !this$options.equals(other$options)) return false;
		final Object this$id = this.getId();
		final Object other$id = other.getId();
		if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
		final Object this$uniqueConstraints = this.getUniqueConstraints();
		final Object other$uniqueConstraints = other.getUniqueConstraints();
		if (this$uniqueConstraints == null ? other$uniqueConstraints != null : !this$uniqueConstraints.equals(other$uniqueConstraints)) return false;
		final Object this$fields = this.getFields();
		final Object other$fields = other.getFields();
		if (this$fields == null ? other$fields != null : !this$fields.equals(other$fields)) return false;
		final Object this$relations = this.getRelations();
		final Object other$relations = other.getRelations();
		if (this$relations == null ? other$relations != null : !this$relations.equals(other$relations)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof ModelSpecDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $addRestEndpoints = this.getAddRestEndpoints();
		result = result * PRIME + ($addRestEndpoints == null ? 43 : $addRestEndpoints.hashCode());
		final Object $addCrudOperations = this.getAddCrudOperations();
		result = result * PRIME + ($addCrudOperations == null ? 43 : $addCrudOperations.hashCode());
		final Object $name = this.getName();
		result = result * PRIME + ($name == null ? 43 : $name.hashCode());
		final Object $schema = this.getSchema();
		result = result * PRIME + ($schema == null ? 43 : $schema.hashCode());
		final Object $tableName = this.getTableName();
		result = result * PRIME + ($tableName == null ? 43 : $tableName.hashCode());
		final Object $classMethods = this.getClassMethods();
		result = result * PRIME + ($classMethods == null ? 43 : $classMethods.hashCode());
		final Object $options = this.getOptions();
		result = result * PRIME + ($options == null ? 43 : $options.hashCode());
		final Object $id = this.getId();
		result = result * PRIME + ($id == null ? 43 : $id.hashCode());
		final Object $uniqueConstraints = this.getUniqueConstraints();
		result = result * PRIME + ($uniqueConstraints == null ? 43 : $uniqueConstraints.hashCode());
		final Object $fields = this.getFields();
		result = result * PRIME + ($fields == null ? 43 : $fields.hashCode());
		final Object $relations = this.getRelations();
		result = result * PRIME + ($relations == null ? 43 : $relations.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "ModelSpecDTO(name=" + this.getName() + ", schema=" + this.getSchema() + ", tableName=" + this.getTableName() + ", addRestEndpoints=" + this.getAddRestEndpoints() + ", addCrudOperations=" + this.getAddCrudOperations() + ", classMethods=" + this.getClassMethods() + ", options=" + this.getOptions() + ", id=" + this.getId() + ", uniqueConstraints=" + this.getUniqueConstraints() + ", fields=" + this.getFields() + ", relations=" + this.getRelations() + ")";
	}

	public ModelSpecDTO(final String name, final String schema, final String tableName, final Boolean addRestEndpoints, final Boolean addCrudOperations, final ClassMethodsSpecDTO classMethods, final OptionsSpecDTO options, final IdSpecDTO id, final List<List<String>> uniqueConstraints, final List<FieldSpecDTO> fields, final List<RelationSpecDTO> relations) {
		this.name = name;
		this.schema = schema;
		this.tableName = tableName;
		this.addRestEndpoints = addRestEndpoints;
		this.addCrudOperations = addCrudOperations;
		this.classMethods = classMethods;
		this.options = options;
		this.id = id;
		this.uniqueConstraints = uniqueConstraints;
		this.fields = fields;
		this.relations = relations;
	}

	public ModelSpecDTO() {
	}
}
