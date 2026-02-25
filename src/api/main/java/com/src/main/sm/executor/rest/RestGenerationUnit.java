package com.src.main.sm.executor.rest;

import java.util.LinkedHashMap;
import java.util.Map;

public class RestGenerationUnit {

	private final String entityName;
	private final String idName;
	private final String idType;
	private final String idTypeImport;
	private final String endpointPath;
	private final String requestBasePath;
	private final String modelPackage;
	private final String repositoryPackage;
	private final String servicePackage;
	private final String controllerPackage;
	private final String supportPackage;
	private final String entitySupportClass;
	private final String filterSupportClass;
	private final String querySupportClass;
	private final String repositoryClass;
	private final String serviceClass;
	private final String controllerClass;
	private final String allowedSortFieldsLiteral;
	private final boolean noSql;
	private final Map<String, Object> runtimeConfig;

	public RestGenerationUnit(String entityName, String idName, String idType, String idTypeImport, String endpointPath, String requestBasePath,
			String modelPackage,
			String repositoryPackage, String servicePackage, String controllerPackage, String supportPackage,
			String allowedSortFieldsLiteral, boolean noSql, Map<String, Object> runtimeConfig) {
		this.entityName = entityName;
		this.idName = idName;
		this.idType = idType;
		this.idTypeImport = idTypeImport;
		this.endpointPath = endpointPath;
		this.requestBasePath = requestBasePath;
		this.modelPackage = modelPackage;
		this.repositoryPackage = repositoryPackage;
		this.servicePackage = servicePackage;
		this.controllerPackage = controllerPackage;
		this.supportPackage = supportPackage;
		this.allowedSortFieldsLiteral = allowedSortFieldsLiteral;
		this.repositoryClass = entityName + "Repository";
		this.serviceClass = entityName + "Service";
		this.controllerClass = entityName + "Controller";
		this.entitySupportClass = "RestEntityUtils";
		this.filterSupportClass = "RestFilterUtils";
		this.querySupportClass = "RestQueryUtils";
		this.noSql = noSql;
		this.runtimeConfig = runtimeConfig == null ? new LinkedHashMap<>() : new LinkedHashMap<>(runtimeConfig);
	}

	public String getEntityName() {
		return entityName;
	}

	public String getIdName() {
		return idName;
	}

	public String getIdType() {
		return idType;
	}

	public String getIdTypeImport() {
		return idTypeImport;
	}

	public String getEndpointPath() {
		return endpointPath;
	}

	public String getRequestBasePath() {
		return requestBasePath;
	}

	public String getModelPackage() {
		return modelPackage;
	}

	public String getRepositoryPackage() {
		return repositoryPackage;
	}

	public String getServicePackage() {
		return servicePackage;
	}

	public String getControllerPackage() {
		return controllerPackage;
	}

	public String getSupportPackage() {
		return supportPackage;
	}

	public String getEntitySupportClass() {
		return entitySupportClass;
	}

	public String getFilterSupportClass() {
		return filterSupportClass;
	}

	public String getQuerySupportClass() {
		return querySupportClass;
	}

	public String getRepositoryClass() {
		return repositoryClass;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public String getControllerClass() {
		return controllerClass;
	}

	public String getAllowedSortFieldsLiteral() {
		return allowedSortFieldsLiteral;
	}

	public boolean isNoSql() {
		return noSql;
	}

	public Map<String, Object> getRuntimeConfig() {
		return new LinkedHashMap<>(runtimeConfig);
	}

	public Map<String, Object> toTemplateModel() {
		Map<String, Object> model = new LinkedHashMap<>();
		model.put("entityName", entityName);
		model.put("idName", idName);
		model.put("idType", idType);
		model.put("idTypeImport", idTypeImport);
		model.put("endpointPath", endpointPath);
		model.put("requestBasePath", requestBasePath);
		model.put("modelPackage", modelPackage);
		model.put("repositoryPackage", repositoryPackage);
		model.put("servicePackage", servicePackage);
		model.put("controllerPackage", controllerPackage);
		model.put("supportPackage", supportPackage);
		model.put("entitySupportClass", entitySupportClass);
		model.put("filterSupportClass", filterSupportClass);
		model.put("querySupportClass", querySupportClass);
		model.put("repositoryClass", repositoryClass);
		model.put("serviceClass", serviceClass);
		model.put("controllerClass", controllerClass);
		model.put("allowedSortFieldsLiteral", allowedSortFieldsLiteral);
		model.put("noSql", noSql);
		model.putAll(runtimeConfig);
		return model;
	}
}
