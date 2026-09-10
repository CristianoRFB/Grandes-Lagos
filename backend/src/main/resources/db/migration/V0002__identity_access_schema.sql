create table iam_user (
 id uuid primary key, username varchar(120) not null, normalized_username varchar(120) not null unique,
 display_name varchar(160) not null, password_hash varchar(255) not null, enabled boolean not null default true,
 created_at timestamptz not null default current_timestamp, updated_at timestamptz not null default current_timestamp
);
create table iam_role (
 id uuid primary key, code varchar(8) not null unique, display_name varchar(160) not null,
 enabled boolean not null default true, system_role boolean not null default true,
 created_at timestamptz not null default current_timestamp, updated_at timestamptz not null default current_timestamp
);
create table iam_capability (code varchar(80) primary key, description varchar(255) not null);
create table iam_role_capability (role_id uuid not null references iam_role(id), capability_code varchar(80) not null references iam_capability(code), primary key (role_id, capability_code));
create table iam_user_role_grant (
 id uuid primary key, user_id uuid not null references iam_user(id), role_id uuid not null references iam_role(id),
 scope_type varchar(32) not null check (scope_type in ('GLOBAL','ORGANIZATION','BUSINESS_UNIT','OPERATIONAL_AREA','WAREHOUSE','COST_CENTER')),
 scope_reference uuid, granted_at timestamptz not null default current_timestamp, revoked_at timestamptz,
 check ((scope_type = 'GLOBAL' and scope_reference is null) or (scope_type <> 'GLOBAL' and scope_reference is not null))
);
create index ix_iam_grant_user_effective on iam_user_role_grant(user_id, revoked_at);
create unique index ux_iam_grant_active on iam_user_role_grant(user_id, role_id, scope_type, coalesce(scope_reference, '00000000-0000-0000-0000-000000000000'::uuid)) where revoked_at is null;
