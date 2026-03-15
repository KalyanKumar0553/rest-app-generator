package com.src.main.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.src.main.auth.model.AuthRoute;
import com.src.main.auth.model.RoleType;

public interface AuthRouteRepository extends JpaRepository<AuthRoute, java.util.UUID> {
	@Query("""
			select r
			from AuthRoute r
			join fetch r.role role
			where r.active = true
			  and role.active = true
			  and role.type = :roleType
			order by r.priority asc, length(r.pathPattern) desc, r.pathPattern asc
			""")
	List<AuthRoute> findActiveRoutesByRoleType(@Param("roleType") RoleType roleType);
}
