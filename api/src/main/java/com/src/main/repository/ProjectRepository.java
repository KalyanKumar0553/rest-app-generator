package com.src.main.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.model.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
	@Query("""
			select distinct p
			from ProjectEntity p
			left join p.contributors c
			where p.ownerId = :userId or c.userId = :userId
			order by p.updatedAt desc
			""")
	List<ProjectEntity> findAccessibleProjects(@Param("userId") String userId);

	@Query("""
			select distinct p
			from ProjectEntity p
			left join p.contributors c
			where p.ownerId in :userKeys or c.userId in :userKeys
			order by p.updatedAt desc
			""")
	List<ProjectEntity> findAccessibleProjectsByUserKeys(@Param("userKeys") Collection<String> userKeys);

	@Query("""
			select distinct p
			from ProjectEntity p
			left join fetch p.contributors c
			where p.id = :projectId
			""")
	Optional<ProjectEntity> findWithContributorsById(@Param("projectId") UUID projectId);
}
