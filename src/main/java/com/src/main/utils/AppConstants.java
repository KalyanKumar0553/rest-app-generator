package com.src.main.utils;

public interface AppConstants {
  
  String API_ROOT = "/api";
  String API_PROJECTS = API_ROOT + "/projects";
  String PATH_ID = "/{id}";
  String PATH_DOWNLOAD = "/download";

  String HDR_CONTENT_DISPOSITION = "Content-Disposition";
  String MEDIA_TYPE_OCTET = "application/octet-stream";
  String DISP_ATTACHMENT_FMT = "attachment; filename=\"%s.zip\"";

  String PROP_SCHEDULE_MS = "app.scheduler.ms";
  String PROP_PARALLEL_PICK = "batch.parallelPick";
  String PROP_EXEC_CORE = "batch.executor.corePool";
  String PROP_EXEC_MAX = "batch.executor.maxPool";
  String PROP_EXEC_QUEUE = "batch.executor.queueCapacity";

  String DEFAULT_ARTIFACT = "generated-api";
  String DEFAULT_GROUP = "com.example";
  String DEFAULT_VERSION = "0.0.1-SNAPSHOT";
  String THREAD_PREFIX = "proj-job-";
  
  String TPL_POM = "templates/project/pom.mustache";
  String TPL_APP_YML = "templates/project/application.yml.mustache";
  String TPL_MAIN = "templates/project/mainApp.java.mustache";
  String TPL_DTO = "templates/dto/class.java.mustache";
  String TPL_VALIDATION_MESSAGES = "templates/project/messages.mustache";
  String TPL_VALIDATION_FIELD_MATCH = "validation/field_match.mustache";
  String TPL_VALIDATION_FIELD_MATCH_VALIDATOR = "templates/validation/field_match_validator.mustache";
  String TPL_VALIDATION_CONDITIONAL_REQUIRED = "templates/validation/conditional_required.mustache";
  String TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR = "templates/validation/conditional_required_validator.mustache";

}
