package com.src.main.cdn.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3UploadModuleConfig {
}
