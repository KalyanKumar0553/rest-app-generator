package com.src.main.workflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

@Service
public class ProjectArchiveService {

	public byte[] zipDirectory(Path root) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(out)) {
			Files.walk(root)
					.filter(Files::isRegularFile)
					.sorted(Comparator.naturalOrder())
					.forEach(path -> writeEntry(root, path, zipOutputStream));
		}
		return out.toByteArray();
	}

	public void deleteDirectoryQuietly(Path root) {
		if (root == null) {
			return;
		}
		try {
			Files.walk(root)
					.sorted(Comparator.reverseOrder())
					.forEach(path -> {
						try {
							Files.deleteIfExists(path);
						} catch (IOException ignored) {
						}
					});
		} catch (IOException ignored) {
		}
	}

	private void writeEntry(Path root, Path file, ZipOutputStream zipOutputStream) {
		try {
			String entryName = root.relativize(file).toString().replace("\\", "/");
			zipOutputStream.putNextEntry(new ZipEntry(entryName));
			try (InputStream inputStream = Files.newInputStream(file)) {
				inputStream.transferTo(zipOutputStream);
			}
			zipOutputStream.closeEntry();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
