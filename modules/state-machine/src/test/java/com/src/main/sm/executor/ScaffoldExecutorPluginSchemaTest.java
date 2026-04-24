package com.src.main.sm.executor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.statemachine.support.DefaultExtendedState;

import com.src.main.dto.MavenDependencyDTO;
import com.src.main.service.DependencyResolver;
import com.src.main.util.GradleWrapperInstaller;
import com.src.main.util.InitializrGradleGenerator;
import com.src.main.util.InitializrPomGenerator;
import com.src.main.util.ProjectMetaDataConstants;

class ScaffoldExecutorPluginSchemaTest {

	@TempDir
	Path tempDir;

	@Test
	void generatesSingleInitialPluginSchemaMigrationForSelectedModules() throws Exception {
		ScaffoldExecutor executor = new ScaffoldExecutor(
				new NoopDependencyResolver(),
				new InitializrPomGenerator(),
				new InitializrGradleGenerator(),
				new GradleWrapperInstaller(),
				new TemplateEngine());

		DefaultExtendedState state = new DefaultExtendedState();
		state.getVariables().put(ProjectMetaDataConstants.ROOT_DIR, tempDir);
		state.getVariables().put(ProjectMetaDataConstants.GROUP_ID, "com.example");
		state.getVariables().put(ProjectMetaDataConstants.ARTIFACT_ID, "demo-app");
		state.getVariables().put(ProjectMetaDataConstants.VERSION, "0.0.1-SNAPSHOT");
		state.getVariables().put(ProjectMetaDataConstants.NAME, "Demo App");
		state.getVariables().put(ProjectMetaDataConstants.DESCRIPTION, "Demo description");
		state.getVariables().put(ProjectMetaDataConstants.JDK_VERSION, "17");
		state.getVariables().put(ProjectMetaDataConstants.BOOT_VERSION, "3.3.5");
		state.getVariables().put(ProjectMetaDataConstants.PACKAGING, "jar");
		state.getVariables().put(ProjectMetaDataConstants.BUILD_TOOL, "maven");
		state.getVariables().put(ProjectMetaDataConstants.GENERATOR, "groovy");
		state.getVariables().put(ProjectMetaDataConstants.PACKAGE_NAME, "com.example");
		state.getVariables().put("yaml", minimalJavaYaml());

		executor.execute(state);

		Path migration = tempDir.resolve("src/main/resources/rest-app-db/migration/V100__initial_plugin_schema.sql");
		assertTrue(Files.exists(migration), "expected consolidated plugin schema migration to be generated");

		String sql = Files.readString(migration);
		assertTrue(sql.contains("-- module: rbac"));
		assertTrue(sql.contains("-- module: auth"));
		assertTrue(sql.contains("-- module: subscription"));
		assertTrue(sql.contains("-- module: cdn"));
		assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS roles"));
		assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS users"));
		assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS subscription_plan"));
		assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS cdn_image_upload_draft"));
		assertTrue(sql.contains("-- module: rbac-seed"));
		assertTrue(sql.contains("'ROLE_REPORT_VIEWER'"));
		assertTrue(sql.contains("'report.read'"));
		assertTrue(sql.contains("INSERT INTO role_permissions (role_name, permission_name)"));
		assertTrue(sql.contains("INSERT INTO routes (id, path_pattern, http_method, authority_name, priority, active)"));
		assertTrue(sql.contains("'/api/v1/reports/**'"));
		assertTrue(sql.contains("'GET'"));
		assertFalse(sql.contains("'ROLE_SWAGGER_ADMIN'"));
		assertFalse(sql.contains("V102__auth_schema"));
	}

	private static Map<String, Object> minimalJavaYaml() {
		return Map.of(
			"runtime", Map.of("active", "java"),
			"app", Map.of("language", "java"),
			"core", Map.of(
				"app", Map.of(
					"groupId", "com.example",
					"artifactId", "demo-app",
					"name", "Demo App"),
				"modules", Map.of(
					"selected", List.of("rbac", "auth", "subscription", "cdn"),
					"config", Map.of(
						"rbac", Map.of(
							"defaultRole", "ROLE_REPORT_VIEWER",
							"roles", List.of(
								Map.of(
									"code", "ROLE_REPORT_VIEWER",
									"displayName", "Report Viewer",
									"description", "Read-only reporting role",
									"systemRole", true,
									"active", true)),
							"permissions", List.of(
								Map.of(
									"code", "report.read",
									"displayName", "Read Reports",
									"description", "Read reporting data",
									"category", "REPORTS",
									"active", true)),
							"rolePermissions", List.of(
								Map.of(
									"roleCode", "ROLE_REPORT_VIEWER",
									"permissionCodes", List.of("report.read"))),
							"routes", List.of(
								Map.of(
									"pathPattern", "/api/v1/reports/**",
									"httpMethod", "GET",
									"authorities", List.of("report.read"),
									"priority", 25,
									"active", true)))))));
	}

	private static final class NoopDependencyResolver implements DependencyResolver {

		@Override
		public List<MavenDependencyDTO> resolveForMaven(List<String> idsOrGavs, String bootVersion, boolean includeOpenApi) {
			return List.of();
		}

		@Override
		public List<String> resolveForGradle(List<String> idsOrGavs, String bootVersion, boolean includeOpenApi) {
			return List.of();
		}
	}
}
