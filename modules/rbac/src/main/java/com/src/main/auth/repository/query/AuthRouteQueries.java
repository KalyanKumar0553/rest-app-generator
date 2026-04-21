package com.src.main.auth.repository.query;

public final class AuthRouteQueries {

	private AuthRouteQueries() {
	}

	public static final String FIND_ACTIVE_ROUTES = """
			select r
			from AuthRoute r
			where r.active = true
			  and r.authorityName is not null
			order by r.priority asc, length(r.pathPattern) desc, r.pathPattern asc
			""";
}
