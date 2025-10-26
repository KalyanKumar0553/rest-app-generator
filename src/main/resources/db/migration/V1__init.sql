create table projects (
  id uuid primary key,
  yaml text not null,
  status varchar(20) not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  artifact_id varchar(120) not null,
  group_id varchar(200) not null,
  version varchar(50) not null,
  zip bytea,
  error_message text
);

create index idx_projects_status on projects(status);
