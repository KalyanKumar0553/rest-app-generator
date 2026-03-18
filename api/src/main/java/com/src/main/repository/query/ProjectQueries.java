package com.src.main.repository.query;

public final class ProjectQueries {

	private ProjectQueries() {
	}

	public static final String FIND_ACCESSIBLE_PROJECTS = """
			select distinct p
			from ProjectEntity p
			left join p.contributors c
			where p.ownerId = :userId or c.userId = :userId
			order by p.updatedAt desc
			""";

	public static final String FIND_ACCESSIBLE_PROJECTS_BY_USER_KEYS = """
			select distinct p
			from ProjectEntity p
			left join p.contributors c
			where p.ownerId in :userKeys or c.userId in :userKeys
			order by p.updatedAt desc
			""";

	public static final String FIND_WITH_CONTRIBUTORS_BY_ID = """
			select distinct p
			from ProjectEntity p
			left join fetch p.contributors c
			where p.id = :projectId
			""";

	public static final String FIND_ALL_WITH_CONTRIBUTORS = """
			select distinct p
			from ProjectEntity p
			left join fetch p.contributors c
			""";

	public static final String EXISTS_BY_OWNER_ID_IN_AND_NAME_IGNORE_CASE = """
			select (count(p) > 0)
			from ProjectEntity p
			where p.ownerId in :ownerKeys
			  and lower(trim(p.name)) = lower(trim(:projectName))
			  and (:excludedProjectId is null or p.id <> :excludedProjectId)
			""";
}
