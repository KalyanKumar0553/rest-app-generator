package com.src.main.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.src.main.auth.model.AuthRoute;
public interface AuthRouteRepository extends JpaRepository<AuthRoute, java.util.UUID> {
	@Query("""
			select r
			from AuthRoute r
			where r.active = true
			  and r.authorityName is not null
			order by r.priority asc, length(r.pathPattern) desc, r.pathPattern asc
			""")
	List<AuthRoute> findActiveRoutes();
}
