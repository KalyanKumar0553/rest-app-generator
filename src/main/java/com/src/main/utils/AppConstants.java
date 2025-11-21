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
  String TPL_MAIN = "templates/project/main.mustache";
  String TPL_DTO = "templates/dto/class.java.mustache";
  String TPL_VALIDATION_FIELD_MATCH = "templates/validation/field_match.mustache";
  String TPL_VALIDATION_FIELD_MATCH_VALIDATOR = "templates/validation/field_match_validator.mustache";
  String TPL_VALIDATION_CONDITIONAL_REQUIRED = "templates/validation/conditional_required.mustache";
  String TPL_VALIDATION_CONDITIONAL_REQUIRED_VALIDATOR = "templates/validation/conditional_required_validator.mustache";
  
  String ROOT_DIR = "rootDir";
  String GROUP_ID = "groupId";
  String ARTIFACT_ID = "artifactId";
  String NAME = "name";
  String DESCRIPTION = "description";
  String PACKAGE_NAME = "packageName";
  String BOOT_VERSION = "bootVersion";
  String VERSION = "version";
  String GENERATOR = "generator";
  String JDK_VERSION = "jdkVersion";
  String PACKAGING = "packaging";
  String DEPENDENCIES = "dependencies";
  String EXTRAS_OPENAPI = "extras.openapi";
  String EXTRAS_ANGULAR_INTEGRATION = "extras.angularIntegration";
  String BUILD_TOOL = "buildTool"; // "maven" | "gradle"
  String DEFAULT_BUILD_TOOL = "maven";
  String MAVEN_URL =  "/solrsearch/select?q=%s&rows=1&wt=json";
  String TPL_VALIDATION_MESSAGES = "templates/project/messages.properties.mustache";
  String GRADLE_MUSTACHE = "/templates/project/build.gradle.kts.mustache";
  String TPL_README = "templates/project/README.md.mustache";
  String TPL_GITIGNORE = "templates/project/gitignore.mustache";


}
