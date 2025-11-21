package com.src.main.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.Validate;

/**
 * Utility methods for converting package names to filesystem paths and related
 * path helpers.
 */
public final class PathUtils {

	private PathUtils() {
	}

	/**
	 * Converts a base Java package (e.g. "com.src.main.model") into a filesystem
	 * path relative to the Java source folder (src/main/java).
	 *
	 * Example:
	 * 
	 * <pre>
	 * PathUtils.javaSrcPathFromPackage("com.src.main.model")
	 * // -> src/main/java/com/src/main/model
	 * </pre>
	 */
	public static Path javaSrcPathFromPackage(String packageName) {
		Validate.notBlank(packageName, "packageName must not be blank");
		String normalized = packageName.replace('.', '/');
		return Paths.get("src", "main", "java", normalized);
	}

	/**
	 * Converts a fully qualified class name (e.g. com.src.main.model.User) into its
	 * expected .java file path under src/main/java.
	 *
	 * @param fullyQualifiedName package + class
	 * @return Path like src/main/java/com/src/main/model/User.java
	 */
	public static Path javaFilePath(String fullyQualifiedName) {
		Validate.notBlank(fullyQualifiedName, "fullyQualifiedName must not be blank");
		String path = fullyQualifiedName.replace('.', '/') + ".java";
		return Paths.get("src", "main", "java").resolve(path);
	}

	/**
	 * Converts a package name into its directory path (no src/main/java prefix).
	 * Example: "com.src.main.utils" → "com/src/main/utils"
	 */
	public static String packageToDir(String packageName) {
		Validate.notBlank(packageName, "packageName must not be blank");
		return packageName.replace('.', '/');
	}

	/**
	 * Returns path to the main resources folder.
	 */
	public static Path resourcesDir() {
		return Paths.get("src", "main", "resources");
	}

	/**
	 * Returns path to the test sources folder.
	 */
	public static Path testJavaDir() {
		return Paths.get("src", "test", "java");
	}
}
