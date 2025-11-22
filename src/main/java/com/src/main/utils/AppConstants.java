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
  
  String MAVEN_URL =  "/solrsearch/select?q=%s&rows=1&wt=json";
  String THREAD_PREFIX = "proj-job-";
}
