package com.src.main.sm.executor.crud;

import java.util.LinkedHashMap;
import java.util.Map;

public class CrudGenerationUnit {

	private final String entityName;
	private final String idName;
	private final String idType;
	private final String idTypeImport;
	private final String modelPackage;
	private final String repositoryPackage;
	private final String repositoryClass;

	public CrudGenerationUnit(String entityName, String idName, String idType, String idTypeImport, String modelPackage,
			String repositoryPackage) {
		this.entityName = entityName;
		this.idName = idName;
		this.idType = idType;
		this.idTypeImport = idTypeImport;
		this.modelPackage = modelPackage;
		this.repositoryPackage = repositoryPackage;
		this.repositoryClass = entityName + "Repository";
	}

	public String getRepositoryPackage() {
		return repositoryPackage;
	}

	public String getRepositoryClass() {
		return repositoryClass;
	}

	public Map<String, Object> toTemplateModel() {
		Map<String, Object> model = new LinkedHashMap<>();
		model.put("entityName", entityName);
		model.put("idName", idName);
		model.put("idType", idType);
		model.put("idTypeImport", idTypeImport);
		model.put("modelPackage", modelPackage);
		model.put("repositoryPackage", repositoryPackage);
		model.put("repositoryClass", repositoryClass);
		return model;
	}
}
