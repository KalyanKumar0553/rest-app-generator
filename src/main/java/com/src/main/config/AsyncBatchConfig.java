package com.src.main.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.src.main.utils.AppConstants;

@Configuration
public class AsyncBatchConfig {

  @Bean
  public TaskExecutor projectTaskExecutor(Environment env) {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(Integer.parseInt(env.getProperty(AppConstants.PROP_EXEC_CORE, "4")));
    ex.setMaxPoolSize(Integer.parseInt(env.getProperty(AppConstants.PROP_EXEC_MAX, "8")));
    ex.setQueueCapacity(Integer.parseInt(env.getProperty(AppConstants.PROP_EXEC_QUEUE, "100")));
    ex.setThreadNamePrefix(AppConstants.THREAD_PREFIX);
    ex.initialize();
    return ex;
  }

}
