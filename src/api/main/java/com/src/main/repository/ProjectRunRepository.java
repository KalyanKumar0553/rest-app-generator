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
import com.src.main.util.ProjectRunStatus;
import com.src.main.util.ProjectRunType;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface ProjectRunRepository extends JpaRepository<ProjectRunEntity, UUID> {

	long countByProjectIdAndType(UUID projectId, ProjectRunType type);

	@Query("select count(r) from ProjectRunEntity r where r.ownerId = :ownerId and r.type = :type and r.createdAt >= :from and r.createdAt < :to")
	long countUserRunsInPeriod(String ownerId, ProjectRunType type, OffsetDateTime from, OffsetDateTime to);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
	List<ProjectRunEntity> findByStatusAndTypeOrderByCreatedAtAsc(ProjectRunStatus status, ProjectRunType type,
			Pageable pageable);

	ProjectRunEntity findTopByProjectIdAndTypeOrderByCreatedAtDesc(UUID projectId, ProjectRunType type);

	List<ProjectRunEntity> findByStatusAndUpdatedAtBefore(ProjectRunStatus status, OffsetDateTime before);

	List<ProjectRunEntity> findByProjectIdOrderByCreatedAtAsc(UUID projectId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
	@Query("SELECT r FROM ProjectRunEntity r WHERE r.status = :status AND r.type   = :type ORDER BY r.createdAt ASC")
	List<ProjectRunEntity> findNextBatchForProcessing(@Param("status") ProjectRunStatus status,
			@Param("type") ProjectRunType type, Pageable pageable);

}
