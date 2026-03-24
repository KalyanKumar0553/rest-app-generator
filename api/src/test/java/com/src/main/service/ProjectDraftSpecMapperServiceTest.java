package com.src.main.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.src.main.testsupport.ProjectDraftFixtures;

class ProjectDraftSpecMapperServiceTest {

	private final ProjectDraftSpecMapperService service = new ProjectDraftSpecMapperService();

	@Test
	void buildSpec_withValidJavaDraft_mapsCanonicalGeneratorSpec() {
		Map<String, Object> spec = service.buildSpec(ProjectDraftFixtures.minimalJavaDraft());

		Map<String, Object> app = map(spec.get("app"));
		assertThat(app.get("name")).isEqualTo("Customer API");
		assertThat(app.get("artifactId")).isEqualTo("customer-api");
		assertThat(spec.get("database")).isEqualTo("POSTGRES");
		assertThat(spec.get("dbType")).isEqualTo("SQL");
		assertThat(spec.get("enableOpenapi")).isEqualTo(true);
		assertThat(spec.get("enableActuator")).isEqualTo(true);
		assertThat(spec.get("enableLombok")).isEqualTo(true);
		assertThat(spec.get("packages")).isEqualTo("technical");
		assertThat(list(spec.get("dependencies"))).containsExactly("spring-web", "spring-data-jpa", "postgresql");

		List<Map<String, Object>> models = listOfMaps(spec.get("models"));
		assertThat(models).hasSize(1);
		assertThat(models.get(0)).containsEntry("name", "Customer");
		assertThat(models.get(0)).containsKey("rest");
		assertThat(models.get(0)).containsKey("rest-spec-name");

		List<Map<String, Object>> restSpecs = listOfMaps(spec.get("rest-spec"));
		assertThat(restSpecs).extracting(item -> item.get("name")).contains("Customers", "CustomerAdmin");

		Map<String, Object> actuator = map(spec.get("actuator"));
		Map<String, Object> actuatorEndpoints = map(actuator.get("endpoints"));
		assertThat(list(actuatorEndpoints.get("include"))).containsExactly("health", "metrics");
		Map<String, Object> actuatorProfiles = map(actuator.get("profiles"));
		assertThat(actuatorProfiles).containsKey("dev");

		String yaml = service.buildYaml(ProjectDraftFixtures.minimalJavaDraft());
		assertThat(yaml).contains("artifactId: \"customer-api\"");
		assertThat(yaml).contains("database: \"POSTGRES\"");
		assertThat(yaml).contains("rest-spec:");
	}

	@Test
	void buildSpec_withEmptyDraft_appliesDefaultsAndOmitsEmptyCollections() {
		Map<String, Object> spec = service.buildSpec(ProjectDraftFixtures.emptyDraft());

		Map<String, Object> app = map(spec.get("app"));
		assertThat(app.get("name")).isEqualTo("demo-app");
		assertThat(app.get("groupId")).isEqualTo("io.bootrid");
		assertThat(spec.get("database")).isEqualTo("POSTGRES");
		assertThat(spec.get("models")).isNull();
		assertThat(spec.get("dtos")).isNull();
		assertThat(spec.get("enums")).isNull();
		assertThat(spec.get("dependencies")).isNull();
		assertThat(spec).doesNotContainKey("rest-spec");
	}

	@Test
	void buildSpec_withNodeDraft_usesNodeSectionAndDisablesJavaOnlyFlags() {
		Map<String, Object> spec = service.buildSpec(ProjectDraftFixtures.minimalNodeDraft());

		assertThat(spec.get("enableOpenapi")).isEqualTo(false);
		assertThat(spec.get("enableActuator")).isEqualTo(false);
		assertThat(spec.get("enableLombok")).isEqualTo(false);
		assertThat(spec.get("packages")).isEqualTo("technical");
		assertThat(spec).doesNotContainKey("actuator");

		Map<String, Object> node = map(spec.get("node"));
		assertThat(node.get("packageManager")).isEqualTo("pnpm");
		assertThat(node.get("port")).isEqualTo(3030);
		assertThat(node.get("docker")).isEqualTo(true);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> map(Object value) {
		return (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private List<Object> list(Object value) {
		return (List<Object>) value;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> listOfMaps(Object value) {
		return (List<Map<String, Object>>) value;
	}
}
