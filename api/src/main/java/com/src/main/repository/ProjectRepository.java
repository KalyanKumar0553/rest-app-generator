package com.src.main.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.model.ProjectEntity;
import com.src.main.repository.query.ProjectQueries;

import jakarta.persistence.LockModeType;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
	@Query(ProjectQueries.FIND_ACCESSIBLE_PROJECTS)
	List<ProjectEntity> findAccessibleProjects(@Param("userId") String userId);

	@Query(ProjectQueries.FIND_ACCESSIBLE_PROJECTS_BY_USER_KEYS)
	List<ProjectEntity> findAccessibleProjectsByUserKeys(@Param("userKeys") Collection<String> userKeys);

	@Query(ProjectQueries.FIND_WITH_CONTRIBUTORS_BY_ID)
	Optional<ProjectEntity> findWithContributorsById(@Param("projectId") UUID projectId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(ProjectQueries.FIND_WITH_CONTRIBUTORS_BY_ID_FOR_UPDATE)
	Optional<ProjectEntity> findWithContributorsByIdForUpdate(@Param("projectId") UUID projectId);

	@Query(ProjectQueries.FIND_ALL_WITH_CONTRIBUTORS)
	List<ProjectEntity> findAllWithContributors();

	@Query(ProjectQueries.EXISTS_BY_OWNER_ID_IN_AND_NAME_IGNORE_CASE)
	boolean existsByOwnerIdInAndNameIgnoreCase(@Param("ownerKeys") Collection<String> ownerKeys,
			@Param("projectName") String projectName,
			@Param("excludedProjectId") UUID excludedProjectId);

	Optional<ProjectEntity> findByInviteToken(String inviteToken);
}
