package com.src.main.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.src.main.dto.JSONResponseDTO;
import com.src.main.dto.ProjectCreateResponseDTO;
import com.src.main.dto.ProjectSummaryDTO;
import com.src.main.exception.GenericException;
import com.src.main.exception.ProjectFetchException;
import com.src.main.model.ProjectEntity;
import com.src.main.repository.ProjectRepository;
import com.src.main.util.AppUtils;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.RequestStatus;
import com.src.main.validation.ProjectRequestValidator;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProjectServiceImpl implements ProjectService {

	private final ProjectRepository repo;
	private final Validator validator;

	static class Input {
		@NotBlank
		String yaml;

		Input(String yaml) {
			this.yaml = yaml;
		}

		public String getYaml() {
			return yaml;
		}
	}

	@Override
	@Transactional
	public ProjectCreateResponseDTO create(String yamlText, String ownerId) {
		try {
			var violations = validator.validate(new Input(yamlText));
			if (!violations.isEmpty())
				throw new ConstraintViolationException(violations);
			if (ownerId == null || ownerId.isBlank()) {
				throw new IllegalArgumentException("Owner is required");
			}

			Map<String, Object> spec;
			try {
				spec = new Yaml().load(yamlText);
				if (spec == null || !(spec instanceof Map))
					throw new IllegalArgumentException("YAML must be a mapping at the root");
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid YAML: " + e.getMessage());
			}
			Map<String, Object> app = (Map<String, Object>) spec.get("app");
			if (app == null)
				throw new IllegalArgumentException("Missing required 'app' section");
			Map<String, String> projectDetails = ProjectRequestValidator.validateProjectData(app);
			ProjectEntity p = ProjectEntity.fromDTO(projectDetails,ownerId,yamlText);
			repo.save(p);
			return new ProjectCreateResponseDTO(p.getId().toString(), ProjectRunStatus.QUEUED.name());
		} catch (Exception e) {
			throw new GenericException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	@Override
	public ResponseEntity<JSONResponseDTO<List<ProjectSummaryDTO>>> fetchProjecs(String ownerId) {
		try {
			List<ProjectEntity> projectEntities = repo.findByOwnerId(ownerId);
			List<ProjectSummaryDTO> projectResponseList = projectEntities.stream().map(ProjectSummaryDTO::toDTO).collect(Collectors.toList());
			return ResponseEntity.ok(AppUtils.getJSONObject(projectResponseList, RequestStatus.PROJECT_FETCH_SUCCESS.getDescription()));
		} catch (Exception e) {
			throw new ProjectFetchException(RequestStatus.PROJECT_FETCH_FAIL);
		}
	}
}
