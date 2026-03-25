package com.src.main.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.src.main.exception.GenericException;

@Service
public class PluginModuleStorageService {

	private final Path storageRoot;

	public PluginModuleStorageService(@Value("${app.plugin.storage-root:}") String configuredStorageRoot) {
		String root = configuredStorageRoot == null || configuredStorageRoot.isBlank()
				? Path.of(System.getProperty("java.io.tmpdir"), "bootrid-plugin-storage").toString()
				: configuredStorageRoot.trim();
		this.storageRoot = Path.of(root).toAbsolutePath().normalize();
	}

	public StoredPluginArtifact store(String moduleCode, String versionCode, MultipartFile artifact) {
		validateArtifact(artifact);
		try {
			Files.createDirectories(storageRoot);
			String normalizedModuleCode = sanitizeSegment(moduleCode);
			String normalizedVersionCode = sanitizeSegment(versionCode);
			Path moduleDir = storageRoot.resolve(normalizedModuleCode).resolve(normalizedVersionCode);
			Files.createDirectories(moduleDir);
			String fileName = sanitizeFileName(artifact.getOriginalFilename());
			Path targetFile = moduleDir.resolve(fileName);
			try (InputStream inputStream = artifact.getInputStream()) {
				Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
			}
			String checksum = calculateSha256(targetFile);
			return new StoredPluginArtifact(fileName, storageRoot.relativize(targetFile).toString(), checksum, artifact.getSize());
		} catch (IOException ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store plugin artifact.");
		}
	}

	public Path resolve(String storageKey) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin storage key is required.");
		}
		return storageRoot.resolve(storageKey).normalize();
	}

	public record StoredPluginArtifact(String fileName, String storageKey, String checksumSha256, long sizeBytes) {
	}

	private void validateArtifact(MultipartFile artifact) {
		if (artifact == null || artifact.isEmpty()) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin zip file is required.");
		}
		String fileName = artifact.getOriginalFilename();
		if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
			throw new GenericException(HttpStatus.BAD_REQUEST, "Plugin artifact must be a zip file.");
		}
	}

	private String calculateSha256(Path file) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = Files.readAllBytes(file);
			return HexFormat.of().formatHex(digest.digest(bytes));
		} catch (NoSuchAlgorithmException | IOException ex) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to verify plugin artifact.");
		}
	}

	private String sanitizeSegment(String value) {
		return value == null ? "unknown" : value.trim().toLowerCase().replaceAll("[^a-z0-9._-]+", "-");
	}

	private String sanitizeFileName(String value) {
		String sanitized = value == null ? "plugin.zip" : value.replaceAll("[\\\\/]+", "-").trim();
		return sanitized.isBlank() ? "plugin.zip" : sanitized;
	}
}
