create table iam_user (id bigserial primary key, username varchar(120) unique not null, password_hash varchar(255) not null, enabled boolean not null default true);
create table iam_role (id bigserial primary key, code varchar(40) unique not null);
create table iam_capability (id bigserial primary key, code varchar(80) unique not null);
create table iam_role_capability (role_id bigint not null references iam_role(id), capability_id bigint not null references iam_capability(id), primary key(role_id, capability_id));
create table iam_user_role_grant (user_id bigint not null references iam_user(id), role_id bigint not null references iam_role(id), scope_type varchar(32) not null, scope_ref varchar(120), primary key(user_id, role_id, scope_type, scope_ref));
create index ix_iam_grant_user on iam_user_role_grant(user_id);
