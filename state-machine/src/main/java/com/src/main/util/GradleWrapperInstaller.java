package com.src.main.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class GradleWrapperInstaller {

	public void installWrapper(Path projectRoot, String gradleVersion) {
		Objects.requireNonNull(projectRoot, "projectRoot");
		String version = (gradleVersion == null || gradleVersion.isBlank()) ? "8.10.2" : gradleVersion.trim();

		Path gradleDir = projectRoot.resolve("gradle").resolve("wrapper");
		Path gradlew = projectRoot.resolve("gradlew");
		Path gradlewBat = projectRoot.resolve("gradlew.bat");
		Path wrapperJar = gradleDir.resolve("gradle-wrapper.jar");
		Path wrapperProps = gradleDir.resolve("gradle-wrapper.properties");

		try {
			Files.createDirectories(gradleDir);

			copyClasspath("/templates/gradle/gradlew", gradlew);
			copyClasspath("/templates/gradle/gradlew.bat", gradlewBat);

			trySetExecutable(gradlew);

			ensureWrapperJar(version, wrapperJar);

			String distroType = "bin";
			Properties p = new Properties();
			p.setProperty("distributionBase", "GRADLE_USER_HOME");
			p.setProperty("distributionPath", "wrapper/dists");
			p.setProperty("distributionUrl",
					"https://services.gradle.org/distributions/gradle-" + version + "-" + distroType + ".zip");
			p.setProperty("zipStoreBase", "GRADLE_USER_HOME");
			p.setProperty("zipStorePath", "wrapper/dists");
			writeProps(wrapperProps, p);

		} catch (IOException e) {
			throw new IllegalStateException("Failed to install Gradle Wrapper into project: " + projectRoot, e);
		}
	}

	private static void copyClasspath(String resourcePath, Path dest) throws IOException {
		try (InputStream in = GradleWrapperInstaller.class.getResourceAsStream(resourcePath)) {
			if (in == null) {
				throw new IOException("Missing classpath resource: " + resourcePath);
			}
			Files.createDirectories(dest.getParent());
			try (OutputStream out = Files.newOutputStream(dest, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				in.transferTo(out);
			}
		}
	}

	private static void copyClasspathIfPresent(String resourcePath, Path dest) throws IOException {
		try (InputStream in = GradleWrapperInstaller.class.getResourceAsStream(resourcePath)) {
			if (in == null) {
				return;
			}
			Files.createDirectories(dest.getParent());
			try (OutputStream out = Files.newOutputStream(dest, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				in.transferTo(out);
			}
		}
	}

	private static void ensureWrapperJar(String gradleVersion, Path wrapperJar) throws IOException {
		copyClasspathIfPresent("/templates/gradle/wrapper/gradle-wrapper.jar", wrapperJar);
		if (Files.exists(wrapperJar) && Files.size(wrapperJar) > 0L) {
			return;
		}

		Files.createDirectories(wrapperJar.getParent());
		String url = "https://raw.githubusercontent.com/gradle/gradle/v" + gradleVersion
				+ "/gradle/wrapper/gradle-wrapper.jar";
		try (InputStream in = URI.create(url).toURL().openStream();
				OutputStream out = Files.newOutputStream(wrapperJar, StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			in.transferTo(out);
		}
		if (!Files.exists(wrapperJar) || Files.size(wrapperJar) == 0L) {
			throw new IOException("Failed to provision gradle-wrapper.jar at " + wrapperJar);
		}
	}

	private static void writeProps(Path dest, Properties p) throws IOException {
		try (OutputStream out = Files.newOutputStream(dest, StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			p.store(out, null);
		}
		byte[] bytes = Files.readAllBytes(dest);
		String normalized = new String(bytes, StandardCharsets.ISO_8859_1).replace("\r\n", "\n");
		Files.write(dest, normalized.getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.TRUNCATE_EXISTING);
	}

	private static void trySetExecutable(Path script) {
		try {
			Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
					PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ,
					PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ,
					PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(script, perms);
		} catch (UnsupportedOperationException | IOException ignored) {
		}
	}
}
