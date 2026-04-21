package com.src.main.cdn.service;

import org.springframework.stereotype.Component;

import com.src.main.cdn.config.CdnStorageProperties;

@Component
public class DefaultCdnUploadLimitResolver implements CdnUploadLimitResolver {

	private static final int DEFAULT_MAX_FILES = 50;
	private static final long DEFAULT_MAX_IMAGE_FILE_SIZE_BYTES = 2L * 1024L * 1024L;
	private static final long DEFAULT_MAX_VIDEO_FILE_SIZE_BYTES = 10L * 1024L * 1024L;

	private final CdnStorageProperties properties;

	public DefaultCdnUploadLimitResolver(CdnStorageProperties properties) {
		this.properties = properties;
	}

	@Override
	public int maxFiles() {
		return properties.getMaxFiles() > 0 ? properties.getMaxFiles() : DEFAULT_MAX_FILES;
	}

	@Override
	public long maxImageFileSizeBytes() {
		return properties.getMaxFileSizeBytes() > 0 ? properties.getMaxFileSizeBytes() : DEFAULT_MAX_IMAGE_FILE_SIZE_BYTES;
	}

	@Override
	public long maxVideoFileSizeBytes() {
		return properties.getMaxVideoFileSizeBytes() > 0 ? properties.getMaxVideoFileSizeBytes() : DEFAULT_MAX_VIDEO_FILE_SIZE_BYTES;
	}
}
