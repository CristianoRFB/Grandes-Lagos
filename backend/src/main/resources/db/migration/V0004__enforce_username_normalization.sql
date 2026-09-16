alter table iam_user
    add constraint ck_iam_user_normalized_username
    check (normalized_username = lower(btrim(username)));
