package com.src.main.cdn.service;

public interface CdnUploadLimitResolver {

	int maxFiles();

	long maxImageFileSizeBytes();

	long maxVideoFileSizeBytes();
}
