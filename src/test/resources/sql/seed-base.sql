INSERT INTO tb_user (
    id,
    first_name,
    last_name,
    email,
    active,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    created_at
)
VALUES (
    1,
    'Admin',
    'Test',
    'admin@test.com',
    true,
    true,
    true,
    true,
    NOW()
);

INSERT INTO permission (id, description)
VALUES (1, 'ROLE_OWNER');

INSERT INTO user_permission (user_id, permission_id)
VALUES (1, 1);

INSERT INTO administrator (id, user_id, login, password, last_login)
VALUES (
    1,
    1,
    'admin',
    '$2a$10$a8wbxBxvisKr1SVN1Ly.6eyEYIuhtVZP9NSoYs3CZnq9vPyFP65Lu',
    NULL
);
