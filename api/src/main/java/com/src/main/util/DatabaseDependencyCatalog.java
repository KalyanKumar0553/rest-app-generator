package com.src.main.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.src.main.dto.MavenDependencyDTO;

public final class DatabaseDependencyCatalog {

	private static final Map<String, MavenDependencyDTO> BY_DATABASE = new LinkedHashMap<>();

	static {
		BY_DATABASE.put("MSSQL", new MavenDependencyDTO("com.microsoft.sqlserver", "mssql-jdbc", "runtime", false));
		BY_DATABASE.put("MYSQL", new MavenDependencyDTO("com.mysql", "mysql-connector-j", "runtime", false));
		BY_DATABASE.put("MARIADB", new MavenDependencyDTO("org.mariadb.jdbc", "mariadb-java-client", "runtime", false));
		BY_DATABASE.put("ORACLE", new MavenDependencyDTO("com.oracle.database.jdbc", "ojdbc11", "runtime", false));
		BY_DATABASE.put("POSTGRES", new MavenDependencyDTO("org.postgresql", "postgresql", "runtime", false));
		BY_DATABASE.put("MONGODB",
				new MavenDependencyDTO("org.springframework.boot", "spring-boot-starter-data-mongodb", null, false));
		BY_DATABASE.put("DERBY", new MavenDependencyDTO("org.apache.derby", "derby", "runtime", false));
		BY_DATABASE.put("H2", new MavenDependencyDTO("com.h2database", "h2", "runtime", false));
		BY_DATABASE.put("HSQL", new MavenDependencyDTO("org.hsqldb", "hsqldb", "runtime", false));
	}

	private DatabaseDependencyCatalog() {
	}

	public static Optional<MavenDependencyDTO> resolve(String databaseCode) {
		if (databaseCode == null || databaseCode.isBlank()) {
			return Optional.empty();
		}
		String normalized = databaseCode.trim().toUpperCase(Locale.ROOT);
		if ("NONE".equals(normalized) || "OTHER".equals(normalized)) {
			return Optional.empty();
		}
		return Optional.ofNullable(BY_DATABASE.get(normalized));
	}
}
