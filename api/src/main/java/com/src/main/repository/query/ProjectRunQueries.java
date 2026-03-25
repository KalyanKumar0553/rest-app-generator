package com.src.main.repository.query;

public final class ProjectRunQueries {

	private ProjectRunQueries() {
	}

	public static final String COUNT_USER_RUNS_IN_PERIOD = """
			select count(r)
			from ProjectRunEntity r
			where r.ownerId = :ownerId
			  and r.type = :type
			  and r.createdAt >= :from
			  and r.createdAt < :to
			""";

	public static final String FIND_BY_ID_WITH_PROJECT = """
			select r
			from ProjectRunEntity r
			join fetch r.project p
			where r.id = :runId
			""";

	public static final String FIND_BY_PROJECT_ID_ORDER_BY_CREATED_AT_ASC = """
			select r
			from ProjectRunEntity r
			join fetch r.project p
			where p.id = :projectId
			order by r.createdAt asc
			""";

	public static final String FIND_NEXT_BATCH_FOR_PROCESSING = """
			select r
			from ProjectRunEntity r
			where r.status = :status
			  and r.type = :type
			order by r.createdAt asc
			""";

	public static final String FIND_NEXT_BATCH_FOR_PROCESSING_NATIVE = """
			select *
			from project_runs
			where status = :status
			  and type = :type
			order by created_at asc
			limit :limit
			for update skip locked
			""";
}
