insert into iam_role(code) values ('R01') on conflict do nothing;
insert into iam_capability(code) values ('analytics.read'),('audit.read'),('identity.manage'),('policy.manage') on conflict do nothing;
insert into iam_role_capability(role_id, capability_id) select r.id,c.id from iam_role r cross join iam_capability c where r.code='R01' and c.code in ('analytics.read','audit.read','identity.manage','policy.manage') on conflict do nothing;
insert into iam_user(username,password_hash,enabled) values ('p01-admin','{noop}p01-admin',true) on conflict do nothing;
insert into iam_user_role_grant(user_id,role_id,scope_type,scope_ref) select u.id,r.id,'GLOBAL',null from iam_user u,iam_role r where u.username='p01-admin' and r.code='R01' on conflict do nothing;
