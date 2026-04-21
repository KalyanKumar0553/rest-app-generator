package com.src.main.cdn.config;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.src.main.cdn.service.CdnUploadLimitResolver;

@Primary
@Component
public class CdnConfigPropertyUploadLimitResolver implements CdnUploadLimitResolver {

	private static final String MAX_FILES_KEY = "azure.cdn.max_files";
	private static final String MAX_FILE_SIZE_KEY = "azure.cdn.max_file_size";
	private static final int DEFAULT_MAX_FILES = 50;
	private static final int DEFAULT_MAX_FILE_SIZE_MB = 2;
	private static final long DEFAULT_MAX_VIDEO_FILE_SIZE_BYTES = 10L * 1024L * 1024L;

	private final CdnStorageProperties properties;

	public CdnConfigPropertyUploadLimitResolver(CdnStorageProperties properties) {
		this.properties = properties;
	}

	@Override
	public int maxFiles() {
		return positiveOrDefault(properties.getMaxFiles(), DEFAULT_MAX_FILES);
	}

	@Override
	public long maxImageFileSizeBytes() {
		int defaultMegabytes = bytesToMegabytes(properties.getMaxFileSizeBytes(), DEFAULT_MAX_FILE_SIZE_MB);
		int maxMegabytes = positiveOrDefault(defaultMegabytes, DEFAULT_MAX_FILE_SIZE_MB);
		return maxMegabytes * 1024L * 1024L;
	}

	@Override
	public long maxVideoFileSizeBytes() {
		return properties.getMaxVideoFileSizeBytes() > 0
				? properties.getMaxVideoFileSizeBytes()
				: DEFAULT_MAX_VIDEO_FILE_SIZE_BYTES;
	}

	private int positiveOrDefault(int value, int defaultValue) {
		return value > 0 ? value : defaultValue;
	}

	private int bytesToMegabytes(long bytes, int defaultValue) {
		if (bytes <= 0) {
			return defaultValue;
		}
		long megabytes = bytes / (1024L * 1024L);
		return megabytes > 0 ? (int) megabytes : defaultValue;
	}
}
