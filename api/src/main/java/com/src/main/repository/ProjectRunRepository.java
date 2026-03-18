package com.src.main.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.src.main.model.ProjectRunEntity;
import com.src.main.repository.query.ProjectRunQueries;
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ProjectRunRepository extends JpaRepository<ProjectRunEntity, UUID> {

	long countByProjectIdAndType(UUID projectId, ProjectRunType type);

	@Query(ProjectRunQueries.COUNT_USER_RUNS_IN_PERIOD)
	long countUserRunsInPeriod(String ownerId, ProjectRunType type, OffsetDateTime from, OffsetDateTime to);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
	List<ProjectRunEntity> findByStatusAndTypeOrderByCreatedAtAsc(ProjectRunStatus status, ProjectRunType type,
			Pageable pageable);

	ProjectRunEntity findTopByProjectIdAndTypeOrderByCreatedAtDesc(UUID projectId, ProjectRunType type);

	@Query(ProjectRunQueries.FIND_BY_ID_WITH_PROJECT)
	java.util.Optional<ProjectRunEntity> findByIdWithProject(@Param("runId") UUID runId);

	List<ProjectRunEntity> findByStatusAndUpdatedAtBefore(ProjectRunStatus status, OffsetDateTime before);

	@Query(ProjectRunQueries.FIND_BY_PROJECT_ID_ORDER_BY_CREATED_AT_ASC)
	List<ProjectRunEntity> findByProjectIdOrderByCreatedAtAsc(@Param("projectId") UUID projectId);

	long deleteByProjectId(UUID projectId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
	@Query(ProjectRunQueries.FIND_NEXT_BATCH_FOR_PROCESSING)
	List<ProjectRunEntity> findNextBatchForProcessing(@Param("status") ProjectRunStatus status,
			@Param("type") ProjectRunType type, Pageable pageable);

}
