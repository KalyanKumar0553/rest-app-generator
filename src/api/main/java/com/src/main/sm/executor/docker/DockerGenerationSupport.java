package com.src.main.sm.executor.docker;

import java.util.Locale;
import java.util.Map;

import com.src.main.common.util.StringUtils;

public final class DockerGenerationSupport {
	private static final Map<String, String> DB_SERVICE_BLOCKS = Map.ofEntries(
			Map.entry("MSSQL", """
					  mssql:
					    image: mcr.microsoft.com/mssql/server:2022-latest
					    environment:
					      - ACCEPT_EULA=yes
					      - MSSQL_PID=express
					      - MSSQL_SA_PASSWORD=P4ssword!
					    ports:
					      - "1434:1433"
					"""),
			Map.entry("MYSQL", """
					  mysql:
					    image: mysql:8.4
					    environment:
					      - MYSQL_ROOT_PASSWORD=root
					      - MYSQL_DATABASE=basicdb
					    ports:
					      - "3307:3306"
					"""),
			Map.entry("MARIADB", """
					  mariadb:
					    image: mariadb:11
					    environment:
					      - MARIADB_ROOT_PASSWORD=root
					      - MARIADB_DATABASE=basicdb
					    ports:
					      - "3308:3306"
					"""),
			Map.entry("ORACLE", """
					  oracle:
					    image: gvenzl/oracle-free:23-slim
					    environment:
					      - ORACLE_PASSWORD=oracle
					    ports:
					      - "1522:1521"
					"""),
			Map.entry("POSTGRES", """
					  postgres:
					    image: postgres:16
					    environment:
					      - POSTGRES_DB=basicdb
					      - POSTGRES_USER=postgres
					      - POSTGRES_PASSWORD=postgres
					    ports:
					      - "5432:5432"
					"""),
			Map.entry("MONGODB", """
					  mongodb:
					    image: mongo:7
					    environment:
					      - MONGO_INITDB_DATABASE=basicdb
					    ports:
					      - "27017:27017"
					"""),
			Map.entry("DERBY", """
					  derby:
					    image: aceberg/derbydb:latest
					    environment:
					      - DERBY_DB=basicdb
					      - DERBY_USER=postgres
					      - DERBY_PASSWORD=postgres
					    ports:
					      - "1527:1527"
					"""),
			Map.entry("H2", """
					  h2:
					    image: oscarfonts/h2:latest
					    environment:
					      - H2_OPTIONS=-ifNotExists
					    ports:
					      - "1521:1521"
					      - "8181:81"
					"""),
			Map.entry("HSQL", """
					  hsql:
					    image: blacklabelops/hsqldb:latest
					    environment:
					      - HSQLDB_DATABASE=basicdb
					      - HSQLDB_USER=postgres
					      - HSQLDB_PASSWORD=postgres
					    ports:
					      - "9001:9001"
					"""));

	private DockerGenerationSupport() {
	}

	public static boolean isDockerComposeEnabled(Object raw) {
		if (raw == null) {
			return false;
		}
		if (raw instanceof Boolean enabled) {
			return enabled;
		}
		String normalized = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
		return "true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "y".equals(normalized);
	}

	public static String toServiceName(String artifactId) {
		String base = StringUtils.firstNonBlank(artifactId, "app").toLowerCase(Locale.ROOT);
		String cleaned = base.replaceAll("[^a-z0-9_-]", "-").replaceAll("-+", "-");
		if (cleaned.isBlank()) {
			return "app";
		}
		return cleaned;
	}

	public static String resolveJarGlob(String buildTool) {
		if ("gradle".equalsIgnoreCase(StringUtils.firstNonBlank(buildTool, ""))) {
			return "build/libs/*.jar";
		}
		return "target/*.jar";
	}

	public static String resolveDbServiceBlock(String database) {
		String normalized = StringUtils.firstNonBlank(database, "").trim().toUpperCase(Locale.ROOT);
		if (normalized.isBlank() || "NONE".equals(normalized) || "OTHER".equals(normalized)) {
			return "";
		}
		return DB_SERVICE_BLOCKS.getOrDefault(normalized, "");
	}
}
