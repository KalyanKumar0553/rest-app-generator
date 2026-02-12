package com.src.main.sm.executor.rest;

import java.util.LinkedHashMap;
import java.util.Map;

public class RestGenerationUnit {

	private final String entityName;
	private final String idName;
	private final String idType;
	private final String endpointPath;
	private final String modelPackage;
	private final String repositoryPackage;
	private final String servicePackage;
	private final String controllerPackage;
	private final String repositoryClass;
	private final String serviceClass;
	private final String controllerClass;

	public RestGenerationUnit(String entityName, String idName, String idType, String endpointPath, String modelPackage,
			String repositoryPackage, String servicePackage, String controllerPackage) {
		this.entityName = entityName;
		this.idName = idName;
		this.idType = idType;
		this.endpointPath = endpointPath;
		this.modelPackage = modelPackage;
		this.repositoryPackage = repositoryPackage;
		this.servicePackage = servicePackage;
		this.controllerPackage = controllerPackage;
		this.repositoryClass = entityName + "Repository";
		this.serviceClass = entityName + "Service";
		this.controllerClass = entityName + "Controller";
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

	public String getEndpointPath() {
		return endpointPath;
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

	public String getRepositoryClass() {
		return repositoryClass;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public String getControllerClass() {
		return controllerClass;
	}

	public Map<String, Object> toTemplateModel() {
		Map<String, Object> model = new LinkedHashMap<>();
		model.put("entityName", entityName);
		model.put("idName", idName);
		model.put("idType", idType);
		model.put("endpointPath", endpointPath);
		model.put("modelPackage", modelPackage);
		model.put("repositoryPackage", repositoryPackage);
		model.put("servicePackage", servicePackage);
		model.put("controllerPackage", controllerPackage);
		model.put("repositoryClass", repositoryClass);
		model.put("serviceClass", serviceClass);
		model.put("controllerClass", controllerClass);
		return model;
	}
}
