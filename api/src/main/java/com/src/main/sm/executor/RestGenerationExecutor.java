package com.src.main.sm.executor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import org.springframework.statemachine.ExtendedState;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.main.common.util.StringUtils;
import com.src.main.dto.AppSpecDTO;
import com.src.main.dto.ModelSpecDTO;
import com.src.main.dto.StepResult;
import com.src.main.sm.config.StepExecutor;
import com.src.main.sm.executor.common.GenerationLanguage;
import com.src.main.sm.executor.common.GenerationLanguageResolver;
import com.src.main.sm.executor.common.JavaNamingUtils;
import com.src.main.sm.executor.rest.RestControllerGenerator;
import com.src.main.sm.executor.rest.RestGenerationSupport;
import com.src.main.sm.executor.rest.RestGenerationUnit;
import com.src.main.sm.executor.rest.RestRepositoryGenerator;
import com.src.main.sm.executor.rest.RestSharedSupportGenerator;
import com.src.main.sm.executor.rest.RestServiceGenerator;
import com.src.main.util.ProjectMetaDataConstants;

@Component
public class RestGenerationExecutor implements StepExecutor {

	private final RestControllerGenerator controllerGenerator;
	private final RestServiceGenerator serviceGenerator;
	private final RestRepositoryGenerator repositoryGenerator;
	private final RestSharedSupportGenerator sharedSupportGenerator;
	private final ObjectMapper mapper = new ObjectMapper();

	public RestGenerationExecutor(RestControllerGenerator controllerGenerator, RestServiceGenerator serviceGenerator,
			RestRepositoryGenerator repositoryGenerator, RestSharedSupportGenerator sharedSupportGenerator) {
		this.controllerGenerator = controllerGenerator;
		this.serviceGenerator = serviceGenerator;
		this.repositoryGenerator = repositoryGenerator;
		this.sharedSupportGenerator = sharedSupportGenerator;
	}

	@Override
	@SuppressWarnings("unchecked")
	public StepResult execute(ExtendedState data) {
		try {
			Path root = Path.of((String) data.getVariables().get(ProjectMetaDataConstants.ROOT_DIR));
			Map<String, Object> yaml = (Map<String, Object>) data.getVariables().get(ProjectMetaDataConstants.YAML);
			if (yaml == null) {
				return StepResult.error("REST_GENERATION", "YAML not found in extended state.");
			}

			AppSpecDTO spec = mapper.convertValue(yaml, AppSpecDTO.class);
			List<ModelSpecDTO> models = spec.getModels();
			if (models == null || models.isEmpty()) {
				return StepResult.ok(Map.of("status", "Success", "restGeneratedCount", 0));
			}
			List<Map<String, Object>> rawModels = resolveRawModels(yaml);

			String basePackage = StringUtils.firstNonBlank(str(yaml.get("basePackage")),
					(String) data.getVariables().get(ProjectMetaDataConstants.GROUP_ID),
					ProjectMetaDataConstants.DEFAULT_GROUP);
			String packageStructure = StringUtils.firstNonBlank(str(yaml.get("packages")), spec.getPackages(), "technical");
			GenerationLanguage language = GenerationLanguageResolver.resolveFromYaml(yaml);
			boolean noSql = isNoSqlDatabase(yaml);
			Map<String, Map<String, Object>> restSpecByName = resolveRestSpecByName(yaml);
			Map<String, String> restSpecBasePathByName = resolveRestSpecBasePathByName(restSpecByName);
			Map<Integer, String> modelRestSpecNameByIndex = resolveModelRestSpecNameByIndex(yaml);
			String utilPackage = RestGenerationSupport.resolveUtilPackage(basePackage, packageStructure);
			sharedSupportGenerator.generate(root, utilPackage, noSql, language);

			int generatedCount = 0;
			for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
				ModelSpecDTO model = models.get(modelIndex);
				if (!Boolean.TRUE.equals(model.getAddRestEndpoints()) || Boolean.TRUE.equals(model.getAddCrudOperations())) {
					continue;
				}
				String restSpecName = modelRestSpecNameByIndex.get(modelIndex);
				Map<String, Object> mappedRestSpec = restSpecName == null ? Collections.emptyMap() : restSpecByName.getOrDefault(restSpecName, Collections.emptyMap());
				Map<String, Object> rawModel = modelIndex < rawModels.size() ? rawModels.get(modelIndex) : Collections.emptyMap();
				String mappedBasePath = normalizeBasePath(restSpecBasePathByName.get(restSpecName));
				validateControllerOnlyRestConfig(model, rawModel, mappedRestSpec, restSpecName != null);
				Map<String, Object> runtimeConfig = buildRuntimeConfig(model, rawModel, mappedRestSpec, restSpecName != null);
				RestGenerationUnit unit = RestGenerationSupport.buildUnit(model, basePackage, packageStructure, noSql,
						language == GenerationLanguage.KOTLIN, mappedBasePath, runtimeConfig);
				boolean hasServiceLayer = Boolean.TRUE.equals(runtimeConfig.get("hasServiceLayer"));
				if (hasServiceLayer) {
					repositoryGenerator.generate(root, unit, language);
					serviceGenerator.generate(root, unit, language);
				}
				controllerGenerator.generate(root, unit, language);
				generatedCount += 1;
			}

			return StepResult.ok(Map.of("status", "Success", "restGeneratedCount", generatedCount));
		} catch (Exception ex) {
			return StepResult.error("REST_GENERATION", ex.getMessage());
		}
	}

	private static String str(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> resolveRawModels(Map<String, Object> yaml) {
		if (yaml == null) {
			return List.of();
		}
		Object modelsRaw = yaml.get("models");
		if (!(modelsRaw instanceof List<?> list)) {
			return List.of();
		}
		List<Map<String, Object>> models = new ArrayList<>();
		for (Object obj : list) {
			if (obj instanceof Map<?, ?> raw) {
				models.add((Map<String, Object>) raw);
			} else {
				models.add(Collections.emptyMap());
			}
		}
		return models;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Map<String, Object>> resolveRestSpecByName(Map<String, Object> yaml) {
		Map<String, Map<String, Object>> byName = new LinkedHashMap<>();
		if (yaml == null) {
			return byName;
		}
		Object restSpecRaw = yaml.get("rest-spec");
		if (!(restSpecRaw instanceof List<?> restSpecs)) {
			return byName;
		}
		for (Object restSpecObj : restSpecs) {
			if (!(restSpecObj instanceof Map<?, ?> rawMap)) {
				continue;
			}
			Map<String, Object> restSpec = (Map<String, Object>) rawMap;
			String name = StringUtils.trimToNull(str(restSpec.get("name")));
			if (name != null) {
				byName.put(name, restSpec);
			}
		}
		return byName;
	}

	private static Map<String, String> resolveRestSpecBasePathByName(Map<String, Map<String, Object>> restSpecByName) {
		Map<String, String> basePathByName = new LinkedHashMap<>();
		for (Map.Entry<String, Map<String, Object>> entry : restSpecByName.entrySet()) {
			String name = entry.getKey();
			Map<String, Object> restSpec = entry.getValue();
			String basePath = StringUtils.trimToNull(str(restSpec.get("basePath")));
			if (basePath != null) {
				basePathByName.put(name, basePath);
			}
		}
		return basePathByName;
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, String> resolveModelRestSpecNameByIndex(Map<String, Object> yaml) {
		Map<Integer, String> result = new LinkedHashMap<>();
		if (yaml == null) {
			return result;
		}
		Object modelsRaw = yaml.get("models");
		if (!(modelsRaw instanceof List<?> models)) {
			return result;
		}
		for (int index = 0; index < models.size(); index++) {
			Object modelObj = models.get(index);
			if (!(modelObj instanceof Map<?, ?> rawModel)) {
				continue;
			}
			Map<String, Object> model = (Map<String, Object>) rawModel;
			String mappedName = firstMappedSpecName(model);
			if (mappedName != null) {
				result.put(index, mappedName);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static String firstMappedSpecName(Map<String, Object> model) {
		if (model == null) {
			return null;
		}
		String fromFlat = StringUtils.firstNonBlank(
				StringUtils.trimToNull(str(model.get("rest-spec-name"))),
				StringUtils.trimToNull(str(model.get("restSpecName"))),
				StringUtils.trimToNull(str(model.get("restSpec"))));
		if (fromFlat != null) {
			return fromFlat;
		}
		Object restRaw = model.get("rest");
		if (restRaw instanceof Map<?, ?> restMapRaw) {
			Map<String, Object> restMap = (Map<String, Object>) restMapRaw;
			return StringUtils.firstNonBlank(
					StringUtils.trimToNull(str(restMap.get("name"))),
					StringUtils.trimToNull(str(restMap.get("resourceName"))));
		}
		return null;
	}

	private static String normalizeBasePath(String rawBasePath) {
		String path = StringUtils.trimToNull(rawBasePath);
		if (path == null) {
			return null;
		}
		path = path.startsWith("/") ? path : "/" + path;
		if (!path.startsWith("/api/")) {
			path = "/api" + path;
		}
		path = path.replaceAll("/+", "/");
		if (path.endsWith("/") && path.length() > 1) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	@SuppressWarnings("unchecked")
	private static void validateControllerOnlyRestConfig(
			ModelSpecDTO model,
			Map<String, Object> rawModel,
			Map<String, Object> restSpec,
			boolean hasMappedRestSpec) {
		if (hasMappedRestSpec) {
			return;
		}

		Map<String, Object> methods = resolveMethods(restSpec, rawModel);
		String configName = StringUtils.firstNonBlank(
				StringUtils.trimToNull(str(rawModel.get("rest-spec-name"))),
				StringUtils.trimToNull(str(rawModel.get("restSpecName"))),
				StringUtils.trimToNull(str(rawModel.get("restSpec"))),
				StringUtils.trimToNull(str(restSpec.get("name"))),
				StringUtils.trimToNull(model != null ? model.getName() : null),
				"Unknown");

		for (Map.Entry<String, Object> methodEntry : methods.entrySet()) {
			Object entryRaw = methodEntry.getValue();
			if (!isOperationEnabled(entryRaw)) {
				continue;
			}
			Map<String, Object> entry = entryRaw instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Collections.emptyMap();
			boolean requestEmpty = isBlockEmpty(entry.get("request"));
			boolean responseEmpty = isBlockEmpty(entry.get("response"));
			if (requestEmpty && responseEmpty) {
				throw new IllegalArgumentException(
						"Invalid configuration for '" + configName + "': request and response configuration is required for endpoint '"
								+ methodEntry.getKey() + "' when not mapped to entity.");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean isBlockEmpty(Object raw) {
		if (raw == null) {
			return true;
		}
		if (raw instanceof Map<?, ?> mapRaw) {
			Map<Object, Object> map = (Map<Object, Object>) mapRaw;
			if (map.isEmpty()) {
				return true;
			}
			for (Map.Entry<Object, Object> entry : map.entrySet()) {
				Object value = entry.getValue();
				if (!isValueEmpty(value)) {
					return false;
				}
			}
			return true;
		}
		return isValueEmpty(raw);
	}

	@SuppressWarnings("unchecked")
	private static boolean isValueEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof String text) {
			return StringUtils.trimToNull(text) == null;
		}
		if (value instanceof List<?> list) {
			return list.isEmpty();
		}
		if (value instanceof Map<?, ?> mapRaw) {
			Map<Object, Object> map = (Map<Object, Object>) mapRaw;
			if (map.isEmpty()) {
				return true;
			}
			for (Map.Entry<Object, Object> entry : map.entrySet()) {
				if (!isValueEmpty(entry.getValue())) {
					return false;
				}
			}
			return true;
		}
		if (value instanceof Boolean bool) {
			return !bool;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> buildRuntimeConfig(
			ModelSpecDTO model,
			Map<String, Object> rawModel,
			Map<String, Object> restSpec,
			boolean hasMappedRestSpec) {
		Map<String, Object> runtime = new LinkedHashMap<>();
		String entityType = JavaNamingUtils.toJavaTypeName(StringUtils.firstNonBlank(model != null ? model.getName() : null, "Entity"), "Entity");
		String idType = resolveModelIdType(model);
		String idName = resolveModelIdName(model);
		Map<String, Object> methods = resolveMethods(restSpec, rawModel);
		boolean hasServiceLayer = hasMappedRestSpec;
		runtime.put("hasServiceLayer", hasServiceLayer);
		runtime.put("idNamePascalCase", toPascalCase(idName));

		configureOperation(runtime, methods, "list", "List<" + entityType + ">", null, hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "get", entityType, null, hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "create", idType, entityType, hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "update", entityType, entityType, hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "patch", entityType, entityType, hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "delete", idType, null, hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "bulkInsert", "List<" + idType + ">", "List<" + entityType + ">", hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "bulkUpdate", "List<" + entityType + ">", "List<" + entityType + ">", hasServiceLayer, entityType, idType);
		configureOperation(runtime, methods, "bulkDelete", "List<" + idType + ">", "List<" + idType + ">", hasServiceLayer, entityType, idType);
		runtime.put("usesAnyIdPathOperation", anyTrue(runtime, "getEnabled", "updateEnabled", "patchEnabled", "deleteEnabled"));
		return runtime;
	}

	private static boolean anyTrue(Map<String, Object> runtime, String... keys) {
		for (String key : keys) {
			if (Boolean.TRUE.equals(runtime.get(key))) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> resolveMethods(Map<String, Object> restSpec, Map<String, Object> rawModel) {
		Object methodsRaw = restSpec.get("methods");
		if (methodsRaw instanceof Map<?, ?> mapRaw && !mapRaw.isEmpty()) {
			return (Map<String, Object>) mapRaw;
		}
		Object restRaw = rawModel.get("rest");
		if (restRaw instanceof Map<?, ?> restMapRaw) {
			Object inlineMethodsRaw = ((Map<String, Object>) restMapRaw).get("methods");
			if (inlineMethodsRaw instanceof Map<?, ?> inlineMap && !inlineMap.isEmpty()) {
				return (Map<String, Object>) inlineMap;
			}
		}
		Map<String, Object> defaults = new LinkedHashMap<>();
		defaults.put("list", true);
		defaults.put("get", true);
		defaults.put("create", true);
		defaults.put("patch", true);
		defaults.put("delete", true);
		defaults.put("bulkInsert", true);
		defaults.put("bulkUpdate", true);
		defaults.put("bulkDelete", true);
		return defaults;
	}

	@SuppressWarnings("unchecked")
	private static void configureOperation(
			Map<String, Object> runtime,
			Map<String, Object> methods,
			String key,
			String defaultResponseType,
			String defaultRequestType,
			boolean hasServiceLayer,
			String entityType,
			String idType) {
		Object entryRaw = methods.get(key);
		boolean enabled = isOperationEnabled(entryRaw);
		runtime.put(key + "Enabled", enabled);
		if (!enabled) {
			runtime.put(key + "UseService", false);
			return;
		}

		Map<String, Object> entry = entryRaw instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Collections.emptyMap();
		Map<String, Object> request = entry.get("request") instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Collections.emptyMap();
		Map<String, Object> response = entry.get("response") instanceof Map<?, ?> raw ? (Map<String, Object>) raw : Collections.emptyMap();

		String requestType = defaultRequestType;
		if (defaultRequestType != null) {
			requestType = resolveRequestType(key, request, defaultRequestType);
			runtime.put(key + "RequestType", requestType);
		}

		String responseType = resolveResponseType(key, response, defaultResponseType);
		runtime.put(key + "ResponseType", responseType);
		String responseMode = resolveControllerResponseMode(response);
		boolean returnsResponseEntity = "RESPONSE_ENTITY".equals(responseMode);
		runtime.put(key + "ReturnsResponseEntity", returnsResponseEntity);
		runtime.put(key + "ControllerReturnType", returnsResponseEntity
				? "ResponseEntity<" + responseType + ">"
				: responseType);
		applyMapperFlags(runtime, key, requestType, entityType, idType);
		applyOperationTypeFlags(runtime, key, requestType, responseType, entityType, idType);
		applyBatchOptions(runtime, key, entry);

		boolean useService = hasServiceLayer && isServiceCompatible(key, requestType, responseType, entityType, idType);
		runtime.put(key + "UseService", useService);
	}

	@SuppressWarnings("unchecked")
	private static void applyBatchOptions(Map<String, Object> runtime, String key, Map<String, Object> entry) {
		Map<String, Object> batch = entry != null && entry.get("batch") instanceof Map<?, ?> rawBatch
				? (Map<String, Object>) rawBatch
				: Collections.emptyMap();
		switch (key) {
		case "bulkInsert" -> {
			int batchSize = readPositiveInt(batch.get("batchSize"), 1);
			boolean asyncEnabled = readBoolean(batch.get("enableAsyncMode")) || readBoolean(batch.get("asyncProcessing"));
			runtime.put("bulkInsertBatchSize", batchSize);
			runtime.put("bulkInsertUseBatching", batchSize > 1);
			runtime.put("bulkInsertAsyncEnabled", asyncEnabled);
		}
		case "bulkUpdate" -> {
			int batchSize = readPositiveInt(batch.get("batchSize"), 1);
			boolean asyncEnabled = readBoolean(batch.get("enableAsyncMode")) || readBoolean(batch.get("asyncProcessing"));
			String updateMode = str(batch.get("updateMode"));
			String validationStrategy = str(batch.get("validationStrategy"));
			String optimisticLockHandling = str(batch.get("optimisticLockHandling"));

			boolean patchMode = "PATCH".equalsIgnoreCase(StringUtils.firstNonBlank(updateMode, "PUT"));
			boolean skipDuplicates = "SKIP_DUPLICATES".equalsIgnoreCase(StringUtils.firstNonBlank(validationStrategy, "VALIDATE_ALL_FIRST"));
			boolean validateAllFirst = !skipDuplicates;
			boolean skipConflicts = "SKIP_CONFLICTS".equalsIgnoreCase(StringUtils.firstNonBlank(optimisticLockHandling, "FAIL_ON_CONFLICT"));
			boolean failOnConflict = !skipConflicts;

			runtime.put("bulkUpdateBatchSize", batchSize);
			runtime.put("bulkUpdateUseBatching", batchSize > 1);
			runtime.put("bulkUpdateAsyncEnabled", asyncEnabled);
			runtime.put("bulkUpdateModePatch", patchMode);
			runtime.put("bulkUpdateModePut", !patchMode);
			runtime.put("bulkUpdateValidationSkipDuplicates", skipDuplicates);
			runtime.put("bulkUpdateValidationValidateAllFirst", validateAllFirst);
			runtime.put("bulkUpdateOptimisticSkipConflicts", skipConflicts);
			runtime.put("bulkUpdateOptimisticFailOnConflict", failOnConflict);
		}
		case "bulkDelete" -> {
			int batchSize = readPositiveInt(batch.get("batchSize"), 1);
			boolean asyncEnabled = readBoolean(batch.get("enableAsyncMode")) || readBoolean(batch.get("asyncProcessing"));
			String failureStrategy = str(batch.get("failureStrategy"));
			boolean continueOnFailure = "CONTINUE_AND_REPORT_FAILURES".equalsIgnoreCase(StringUtils.firstNonBlank(failureStrategy, "STOP_ON_FIRST_ERROR"));
			boolean stopOnFirstError = !continueOnFailure;
			boolean allowIncludeDeletedParam = readBoolean(batch.get("allowIncludeDeletedParam"));

			runtime.put("bulkDeleteBatchSize", batchSize);
			runtime.put("bulkDeleteUseBatching", batchSize > 1);
			runtime.put("bulkDeleteAsyncEnabled", asyncEnabled);
			runtime.put("bulkDeleteContinueAndReportFailures", continueOnFailure);
			runtime.put("bulkDeleteStopOnFirstError", stopOnFirstError);
			runtime.put("bulkDeleteAllowIncludeDeletedParam", allowIncludeDeletedParam);
		}
		default -> {
		}
		}
	}

	private static int readPositiveInt(Object raw, int defaultValue) {
		if (raw == null) {
			return defaultValue;
		}
		try {
			int parsed = Integer.parseInt(String.valueOf(raw).trim());
			return parsed > 0 ? parsed : defaultValue;
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	private static boolean readBoolean(Object raw) {
		if (raw == null) {
			return false;
		}
		if (raw instanceof Boolean bool) {
			return bool;
		}
		String normalized = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
	}

	private static void applyMapperFlags(
			Map<String, Object> runtime,
			String key,
			String requestType,
			String entityType,
			String idType) {
		if (requestType == null) {
			return;
		}
		switch (key) {
		case "create", "update", "patch" -> {
			boolean needsMapper = !entityType.equals(requestType) && !idType.equals(requestType);
			runtime.put(key + "RequestMapperEnabled", needsMapper);
			if (needsMapper) {
				runtime.put(key + "RequestDtoType", requestType);
				runtime.put(key + "RequestMapperClass", buildRequestMapperClass(requestType));
			}
		}
		case "bulkInsert", "bulkUpdate" -> {
			String elementType = extractListElementType(requestType);
			boolean needsMapper = elementType != null && !entityType.equals(elementType) && !idType.equals(elementType);
			runtime.put(key + "RequestMapperEnabled", needsMapper);
			if (needsMapper) {
				runtime.put(key + "RequestDtoElementType", elementType);
				runtime.put(key + "RequestMapperClass", buildRequestMapperClass(elementType));
			}
		}
		default -> {
		}
		}
	}

	private static void applyOperationTypeFlags(
			Map<String, Object> runtime,
			String key,
			String requestType,
			String responseType,
			String entityType,
			String idType) {
		if ("list".equals(key)) {
			runtime.put("listResponseIsEntityList", ("List<" + entityType + ">").equals(responseType));
			runtime.put("listResponseIsEntityPage", ("Page<" + entityType + ">").equals(responseType));
			return;
		}
		if ("create".equals(key)) {
			runtime.put("createResponseIsId", idType.equals(responseType));
			runtime.put("createResponseIsEntity", entityType.equals(responseType));
			return;
		}
		if ("delete".equals(key)) {
			runtime.put("deleteResponseIsId", idType.equals(responseType));
			runtime.put("deleteResponseIsVoid", "Void".equals(responseType));
			return;
		}
		if ("bulkInsert".equals(key)) {
			runtime.put("bulkInsertRequestIsEntityList", ("List<" + entityType + ">").equals(requestType));
			runtime.put("bulkInsertResponseIsIdList", ("List<" + idType + ">").equals(responseType));
			runtime.put("bulkInsertResponseIsEntityList", ("List<" + entityType + ">").equals(responseType));
			return;
		}
		if ("bulkDelete".equals(key)) {
			runtime.put("bulkDeleteRequestIsIdList", ("List<" + idType + ">").equals(requestType));
			runtime.put("bulkDeleteResponseIsIdList", ("List<" + idType + ">").equals(responseType));
			runtime.put("bulkDeleteResponseIsVoid", "Void".equals(responseType));
		}
	}

	private static boolean isOperationEnabled(Object entryRaw) {
		if (entryRaw == null) {
			return false;
		}
		if (entryRaw instanceof Boolean bool) {
			return bool;
		}
		return true;
	}

	private static String resolveRequestType(String key, Map<String, Object> request, String defaultType) {
		if (request == null || request.isEmpty()) {
			return JavaNamingUtils.toJavaTypeExpression(defaultType, "Request");
		}
		String explicitType = StringUtils.trimToNull(str(request.get("type")));
		if (explicitType != null) {
			return JavaNamingUtils.toJavaTypeExpression(explicitType, "Request");
		}
		String dtoName = StringUtils.trimToNull(str(request.get("dtoName")));
		if (dtoName != null) {
			if (key.startsWith("bulk") && !dtoName.startsWith("List<")) {
				return JavaNamingUtils.toJavaTypeExpression("List<" + dtoName + ">", "Request");
			}
			return JavaNamingUtils.toJavaTypeExpression(dtoName, "Request");
		}
		return JavaNamingUtils.toJavaTypeExpression(defaultType, "Request");
	}

	private static String resolveResponseType(String key, Map<String, Object> response, String defaultType) {
		if (response == null || response.isEmpty()) {
			return JavaNamingUtils.toJavaTypeExpression(defaultType, "Response");
		}
		String dtoName = StringUtils.trimToNull(str(response.get("dtoName")));
		if (dtoName == null) {
			return JavaNamingUtils.toJavaTypeExpression(defaultType, "Response");
		}
		if ("list".equals(key)) {
			if (dtoName.startsWith("Page<") || dtoName.startsWith("List<")) {
				return JavaNamingUtils.toJavaTypeExpression(dtoName, "Response");
			}
			return JavaNamingUtils.toJavaTypeExpression("List<" + dtoName + ">", "Response");
		}
		if (key.startsWith("bulk")) {
			return JavaNamingUtils.toJavaTypeExpression(dtoName.startsWith("List<") ? dtoName : "List<" + dtoName + ">", "Response");
		}
		return JavaNamingUtils.toJavaTypeExpression(dtoName, "Response");
	}

	private static String resolveControllerResponseMode(Map<String, Object> response) {
		if (response == null || response.isEmpty()) {
			return "RESPONSE_ENTITY";
		}
		String raw = StringUtils.trimToNull(str(response.get("responseType")));
		if (raw == null) {
			return "RESPONSE_ENTITY";
		}
		String normalized = raw.trim().toUpperCase(Locale.ROOT);
		if ("DTO_DIRECT".equals(normalized) || "CUSTOM_WRAPPER".equals(normalized)) {
			return normalized;
		}
		return "RESPONSE_ENTITY";
	}

	private static boolean isServiceCompatible(String key, String requestType, String responseType, String entityType, String idType) {
		return switch (key) {
		case "list" -> ("List<" + entityType + ">").equals(responseType) || ("Page<" + entityType + ">").equals(responseType);
		case "get" -> entityType.equals(responseType);
		case "create" -> isEntityOrMappableRequestType(requestType, entityType, idType) && (entityType.equals(responseType) || idType.equals(responseType));
		case "update", "patch" -> isEntityOrMappableRequestType(requestType, entityType, idType) && entityType.equals(responseType);
		case "delete" -> "Void".equals(responseType) || idType.equals(responseType);
		case "bulkInsert", "bulkUpdate" -> isEntityOrMappableListRequestType(requestType, entityType, idType)
				&& (("List<" + entityType + ">").equals(responseType) || ("List<" + idType + ">").equals(responseType));
		case "bulkDelete" -> ("List<" + idType + ">").equals(requestType)
				&& ("Void".equals(responseType) || ("List<" + idType + ">").equals(responseType));
		default -> false;
		};
	}

	private static boolean isEntityOrMappableRequestType(String requestType, String entityType, String idType) {
		if (requestType == null || requestType.isBlank()) {
			return false;
		}
		if (entityType.equals(requestType)) {
			return true;
		}
		if (idType.equals(requestType)) {
			return false;
		}
		return !requestType.contains("<") && !requestType.contains(">");
	}

	private static boolean isEntityOrMappableListRequestType(String requestType, String entityType, String idType) {
		String elementType = extractListElementType(requestType);
		if (elementType == null || elementType.isBlank()) {
			return false;
		}
		if (entityType.equals(elementType)) {
			return true;
		}
		return !idType.equals(elementType);
	}

	private static String extractListElementType(String requestType) {
		if (requestType == null) {
			return null;
		}
		String trimmed = requestType.trim();
		if (!trimmed.startsWith("List<") || !trimmed.endsWith(">")) {
			return null;
		}
		return trimmed.substring(5, trimmed.length() - 1).trim();
	}

	private static String buildRequestMapperClass(String sourceType) {
		return JavaNamingUtils.toJavaTypeName(sourceType, "Request") + "RequestMapper";
	}

	private static String resolveModelIdType(ModelSpecDTO model) {
		String raw = model != null && model.getId() != null ? StringUtils.trimToNull(model.getId().getType()) : null;
		if (raw == null) {
			return "Long";
		}
		String normalized = raw.toLowerCase(Locale.ROOT);
		return switch (normalized) {
		case "int", "integer" -> "Integer";
		case "long" -> "Long";
		case "boolean" -> "Boolean";
		case "decimal", "bigdecimal" -> "BigDecimal";
		case "uuid" -> "UUID";
		case "date", "localdate" -> "LocalDate";
		case "datetime", "localdatetime" -> "LocalDateTime";
		case "offsetdatetime" -> "OffsetDateTime";
		case "instant" -> "Instant";
		default -> JavaNamingUtils.toJavaTypeExpression(raw, "Id");
		};
	}

	private static String resolveModelIdName(ModelSpecDTO model) {
		String raw = model != null && model.getId() != null ? StringUtils.trimToNull(model.getId().getField()) : null;
		return raw == null ? "id" : raw;
	}

	private static String toPascalCase(String value) {
		if (value == null || value.isBlank()) {
			return "Id";
		}
		String trimmed = value.trim();
		return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
	}

	@SuppressWarnings("unchecked")
	private static boolean isNoSqlDatabase(Map<String, Object> yaml) {
		if (yaml == null) {
			return false;
		}
		Object dbTypeRaw = yaml.get("dbType");
		if (dbTypeRaw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			dbTypeRaw = ((Map<String, Object>) appRaw).get("dbType");
		}
		if (dbTypeRaw != null && "NOSQL".equalsIgnoreCase(String.valueOf(dbTypeRaw).trim())) {
			return true;
		}
		Object databaseRaw = yaml.get("database");
		if (databaseRaw == null && yaml.get("app") instanceof Map<?, ?> appRaw) {
			databaseRaw = ((Map<String, Object>) appRaw).get("database");
		}
		return databaseRaw != null && "MONGODB".equalsIgnoreCase(String.valueOf(databaseRaw).trim());
	}
}
