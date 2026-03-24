package com.src.main.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ProjectDraftFixtures {

	private ProjectDraftFixtures() {
	}

	public static Map<String, Object> minimalJavaDraft() {
		Map<String, Object> draft = new LinkedHashMap<>();
		draft.put("settings", mapOf(
				"projectGroup", "io.bootrid",
				"projectName", "Customer API",
				"projectDescription", "Customer service",
				"buildType", "gradle",
				"language", "java",
				"frontend", "none"));
		draft.put("database", mapOf(
				"dbType", "SQL",
				"database", "POSTGRES",
				"dbGeneration", "Hibernate (update)",
				"pluralizeTableNames", true));
		draft.put("preferences", mapOf(
				"applFormat", "yaml",
				"packages", "technical",
				"enableOpenAPI", true,
				"enableActuator", true,
				"configureApi", true,
				"enableLombok", true,
				"useDockerCompose", true,
				"profiles", List.of("dev", "prod"),
				"javaVersion", "21",
				"deployment", "Docker"));
		draft.put("controllers", mapOf(
				"enabled", true,
				"config", restConfig("CustomerAdmin", "/api/customer-admin")));
		draft.put("actuator", mapOf(
				"selectedConfiguration", "default",
				"configurations", mapOf(
						"default", List.of("health", "metrics", "unknown"),
						"dev", List.of("env", "beans", "health"))));
		draft.put("dependencies", "spring-web, spring-data-jpa");
		draft.put("selectedDependencies", List.of("postgresql", "spring-web"));
		draft.put("entities", List.of(customerEntity()));
		draft.put("dataObjects", List.of(customerRequestDto()));
		draft.put("relations", List.of());
		draft.put("enums", List.of(mapOf("name", "CustomerStatus", "storage", "STRING", "constants", List.of("ACTIVE", "INACTIVE"))));
		draft.put("mappers", List.of());
		return draft;
	}

	public static Map<String, Object> minimalNodeDraft() {
		Map<String, Object> draft = minimalJavaDraft();
		draft.put("settings", mapOf(
				"projectGroup", "io.bootrid",
				"projectName", "Node API",
				"projectDescription", "Node service",
				"buildType", "gradle",
				"language", "node",
				"frontend", "none",
				"packageManager", "pnpm",
				"serverPort", 3030));
		draft.put("preferences", mapOf(
				"applFormat", "yaml",
				"packages", "domain",
				"enableOpenAPI", true,
				"enableActuator", true,
				"configureApi", true,
				"enableLombok", true,
				"useDockerCompose", true,
				"profiles", List.of("dev"),
				"javaVersion", "21",
				"deployment", "Docker"));
		return draft;
	}

	public static Map<String, Object> emptyDraft() {
		Map<String, Object> draft = new LinkedHashMap<>();
		draft.put("settings", new LinkedHashMap<>());
		draft.put("database", new LinkedHashMap<>());
		draft.put("preferences", new LinkedHashMap<>());
		draft.put("controllers", mapOf("enabled", false, "config", new LinkedHashMap<>()));
		draft.put("dependencies", "");
		draft.put("selectedDependencies", List.of());
		draft.put("entities", List.of());
		draft.put("dataObjects", List.of());
		draft.put("relations", List.of());
		draft.put("enums", List.of());
		draft.put("mappers", List.of());
		return draft;
	}

	public static Map<String, Object> invalidDraftMissingProjectName() {
		Map<String, Object> draft = minimalJavaDraft();
		((Map<String, Object>) draft.get("settings")).put("projectName", "   ");
		return draft;
	}

	public static Map<String, Object> restConfig(String resourceName, String basePath) {
		return mapOf(
				"resourceName", resourceName,
				"basePath", basePath,
				"mapToEntity", true,
				"mappedEntityName", "Customer",
				"methods", mapOf(
						"list", true,
						"get", true,
						"create", true,
						"update", false,
						"patch", true,
						"delete", true,
						"bulkInsert", true,
						"bulkUpdate", true,
						"bulkDelete", true),
				"apiVersioning", mapOf(
						"enabled", true,
						"strategy", "header",
						"headerName", "X-API-VERSION",
						"defaultVersion", "1"),
				"pathVariableType", "UUID",
				"deletion", mapOf(
						"mode", "SOFT",
						"restoreEndpoint", true,
						"includeDeletedParam", true),
				"hateoas", mapOf(
						"enabled", true,
						"selfLink", true,
						"updateLink", true,
						"deleteLink", true),
				"pagination", mapOf(
						"enabled", true,
						"mode", "OFFSET",
						"sortField", "createdAt",
						"sortDirection", "DESC"),
				"searchFiltering", mapOf(
						"keywordSearch", true,
						"jpaSpecification", true,
						"searchableFields", List.of("name", "email")),
				"batchOperations", mapOf(
						"insert", mapOf("batchSize", 250, "enableAsyncMode", false),
						"update", mapOf(
								"batchSize", 100,
								"updateMode", "PATCH",
								"optimisticLockHandling", "SKIP_CONFLICTS",
								"validationStrategy", "SKIP_DUPLICATES",
								"enableAsyncMode", true,
								"asyncProcessing", true),
						"bulkDelete", mapOf(
								"deletionStrategy", "HARD",
								"batchSize", 25,
								"failureStrategy", "CONTINUE_AND_REPORT_FAILURES",
								"enableAsyncMode", true,
								"allowIncludeDeletedParam", true)),
				"requestResponse", mapOf(
						"request", mapOf(
								"list", mapOf("mode", "GENERATE_DTO", "dtoName", "CustomerQuery"),
								"create", mapOf("mode", "GENERATE_DTO", "dtoName", "CustomerRequest"),
								"delete", mapOf("mode", "GENERATE_DTO", "dtoName", "DeleteCustomerRequest"),
								"update", mapOf("mode", "GENERATE_DTO", "dtoName", "CustomerUpdateRequest"),
								"patch", mapOf("mode", "JSON_MERGE_PATCH", "dtoName", "CustomerPatchRequest"),
								"getByIdType", "UUID",
								"deleteByIdType", "UUID",
								"bulkInsertType", "",
								"bulkUpdateType", "",
								"bulkDeleteType", ""),
						"response", mapOf(
								"responseType", "CUSTOM_WRAPPER",
								"dtoName", "CustomerEnvelope",
								"endpointDtos", mapOf(
										"list", "CustomerListEnvelope",
										"get", "CustomerDetailEnvelope",
										"create", "CustomerCreateEnvelope",
										"update", "",
										"patch", "",
										"delete", "",
										"bulkInsert", "",
										"bulkUpdate", "",
										"bulkDelete", ""),
								"responseWrapper", "UPSERT",
								"enableFieldProjection", true,
								"includeHateoasLinks", true)),
				"documentation", mapOf(
						"includeDefaultDocumentation", true,
						"endpoints", mapOf(
								"list", mapOf("description", "List customers", "group", "Customers", "descriptionTags", List.of("list"), "deprecated", false),
								"get", mapOf("description", "Get customer", "group", "Customers", "descriptionTags", List.of("get"), "deprecated", false),
								"create", mapOf("description", "Create customer", "group", "Customers", "descriptionTags", List.of("create"), "deprecated", false),
								"update", mapOf("description", "Update customer", "group", "Customers", "descriptionTags", List.of("update"), "deprecated", false),
								"patch", mapOf("description", "Patch customer", "group", "Customers", "descriptionTags", List.of("patch"), "deprecated", false),
								"delete", mapOf("description", "Delete customer", "group", "Customers", "descriptionTags", List.of("delete"), "deprecated", false),
								"bulkInsert", mapOf("description", "Bulk insert customers", "group", "Customers", "descriptionTags", List.of("bulkInsert"), "deprecated", false),
								"bulkUpdate", mapOf("description", "Bulk update customers", "group", "Customers", "descriptionTags", List.of("bulkUpdate"), "deprecated", false),
								"bulkDelete", mapOf("description", "Bulk delete customers", "group", "Customers", "descriptionTags", List.of("bulkDelete"), "deprecated", false))));
	}

	private static Map<String, Object> customerEntity() {
		List<Map<String, Object>> fields = new ArrayList<>();
		fields.add(mapOf("name", "id", "type", "UUID", "primaryKey", true));
		fields.add(mapOf(
				"name", "name",
				"type", "String",
				"required", true,
				"unique", true,
				"maxLength", 120,
				"constraints", List.of(mapOf("name", "NotBlank"), mapOf("name", "Size", "value", "2", "value2", "120"))));
		fields.add(mapOf(
				"name", "age",
				"type", "Int",
				"constraints", List.of(mapOf("name", "Min", "value", "18"))));

		return mapOf(
				"name", "Customer",
				"addRestEndpoints", true,
				"addCrudOperations", true,
				"mappedSuperclass", false,
				"immutable", false,
				"auditable", true,
				"softDelete", true,
				"naturalIdCache", false,
				"classMethods", mapOf("toString", true, "hashCode", true, "equals", true, "noArgsConstructor", true, "allArgsConstructor", true, "builder", true),
				"fields", fields,
				"restConfig", restConfig("Customers", "/api/customers"));
	}

	private static Map<String, Object> customerRequestDto() {
		return mapOf(
				"name", "CustomerRequest",
				"dtoType", "request",
				"fields", List.of(
						mapOf("name", "name", "type", "String", "jsonProperty", "customer_name", "constraints", List.of(mapOf("name", "NotBlank"))),
						mapOf("name", "age", "type", "Int", "constraints", List.of(mapOf("name", "Min", "value", "18")))),
				"classMethods", mapOf("toString", true, "hashCode", true, "equals", true, "noArgsConstructor", true, "allArgsConstructor", true, "builder", false),
				"mapperEnabled", true,
				"mapperModels", List.of("Customer"));
	}

	public static Map<String, Object> mapOf(Object... items) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 0; i + 1 < items.length; i += 2) {
			map.put(String.valueOf(items[i]), items[i + 1]);
		}
		return map;
	}
}
