package com.src.main.auth.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.auth.model.Role;
import com.src.main.auth.model.RoleType;

public interface RoleRepository extends JpaRepository<Role, String> {
	Optional<Role> findByNameAndTypeAndActiveTrue(String name, RoleType type);
	List<Role> findByTypeOrderByNameAsc(RoleType type);
	List<Role> findByNameIn(Collection<String> names);

	@Query("""
			select distinct ur.roleName
			from UserRole ur
			join ur.role r
			where ur.userId = :userId
			  and r.active = true
			order by ur.roleName asc
			""")
	List<String> findActiveRoleNamesByUserId(@Param("userId") String userId);
}
