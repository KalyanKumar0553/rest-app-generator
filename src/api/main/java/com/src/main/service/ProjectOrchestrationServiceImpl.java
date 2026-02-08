package com.src.main.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.model.ProjectEntity;
import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.ProjectRepository;
import com.src.main.repository.ProjectRunRepository;
import com.src.main.util.AppConstants;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

import lombok.AllArgsConstructor;

@Service
public class ProjectOrchestrationServiceImpl implements ProjectOrchestrationService {

	private final ProjectRepository projectRepository;
	private final ProjectRunRepository projectRunRepository;

	@Value("${app.project.max-generates-per-user-per-day:200}")
	private int maxGeneratesPerUserPerDay;

	private ZoneId zoneId = ZoneId.of("Asia/Kolkata");

	public ProjectOrchestrationServiceImpl(ProjectRepository projectRepository,ProjectRunRepository projectRunRepository) {
		this.projectRepository = projectRepository;
		this.projectRunRepository = projectRunRepository;
	}

	@Override
	@Transactional
	public ProjectEntity updateSpec(UUID projectId, String ownerId, String yamlContent) {
		ProjectEntity project = getOwnedProject(projectId, ownerId);
		project.setYaml(yamlContent);
		return project;
	}

	@Override
	@Transactional
	public ProjectRunEntity updateSpecAndGenerate(UUID projectId, String ownerId, String yamlContent) {
		ProjectEntity project = updateSpec(projectId, ownerId, yamlContent);

		ProjectRunEntity latest = projectRunRepository.findTopByProjectIdAndTypeOrderByCreatedAtDesc(projectId,ProjectRunType.GENERATE_CODE);
		if (latest != null && latest.getStatus() == ProjectRunStatus.QUEUED) {
			latest.setStatus(ProjectRunStatus.CANCELLED);
		}
		return createGenerateRun(project, ownerId);
	}

	@Override
	@Transactional
	public ProjectRunEntity generateCode(UUID projectId, String ownerId) {
		ProjectEntity project = getOwnedProject(projectId, ownerId);
		ProjectRunEntity latest = projectRunRepository.findTopByProjectIdAndTypeOrderByCreatedAtDesc(projectId,ProjectRunType.GENERATE_CODE);
		if (latest != null && (latest.getStatus() == ProjectRunStatus.QUEUED || latest.getStatus() == ProjectRunStatus.INPROGRESS)) {
			return latest;
		}
		return createGenerateRun(project, ownerId);
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectEntity getOwnedProject(UUID projectId, String ownerId) {
		ProjectEntity project = projectRepository.findById(projectId)
				.orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
		if (!ownerId.equals(project.getOwnerId())) {
			throw new SecurityException("User not allowed to access this project");
		}
		return project;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProjectRunEntity> getRunsForProject(UUID projectId, String ownerId) {
		ProjectEntity project = getOwnedProject(projectId, ownerId);
		return projectRunRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectRunEntity getRun(UUID runId, String ownerId) {
		ProjectRunEntity run = projectRunRepository.findById(runId)
				.orElseThrow(() -> new IllegalArgumentException("Run not found: " + runId));
		if (!ownerId.equals(run.getOwnerId())) {
			throw new SecurityException("User not allowed to access this run");
		}
		return run;
	}

	@Transactional
	protected ProjectRunEntity createGenerateRun(ProjectEntity project, String ownerId) {
		enforceDailyLimit(ownerId);
		long existingForProject = projectRunRepository.countByProjectIdAndType(project.getId(),ProjectRunType.GENERATE_CODE);
		ProjectRunEntity run = new ProjectRunEntity();
		run.setProject(project);
		run.setOwnerId(ownerId);
		run.setType(ProjectRunType.GENERATE_CODE);
		run.setStatus(ProjectRunStatus.QUEUED);
		run.setRunNumber((int) existingForProject + 1);
		return projectRunRepository.save(run);
	}

	protected void enforceDailyLimit(String ownerId) {
		LocalDate today = LocalDate.now(zoneId);
		OffsetDateTime from = today.atStartOfDay(zoneId).toOffsetDateTime();
		OffsetDateTime to = today.plusDays(1).atStartOfDay(zoneId).toOffsetDateTime();
		long countToday = projectRunRepository.countUserRunsInPeriod(ownerId, ProjectRunType.GENERATE_CODE, from, to);
		if (countToday >= maxGeneratesPerUserPerDay) {
			throw new IllegalStateException("Daily generate limit reached (" + maxGeneratesPerUserPerDay + " per day)");
		}
	}
	
	@Override
	public ResponseEntity<byte[]> download(UUID id) {
		ProjectRunEntity p = projectRunRepository.findById(id)
				.orElseThrow(() -> new java.util.NoSuchElementException("Project Run Not found"));
		if (p.getZip() == null)
			return ResponseEntity.status(202).build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						String.format(AppConstants.DISP_ATTACHMENT_FMT, p.getProject().getArtifact()))
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(p.getZip());
	}

}
