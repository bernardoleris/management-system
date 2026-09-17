INSERT INTO permission (id, description) VALUES
    (2, 'ROLE_ADMIN'),
    (3, 'ROLE_OPERATOR');

INSERT INTO tb_user (
    id, first_name, last_name, email, active,
    account_non_expired, account_non_locked, credentials_non_expired, created_at
) VALUES
    (3, 'Admin', 'Role', 'admin.role@test.com', true, true, true, true, NOW()),
    (4, 'Operator', 'Role', 'operator.role@test.com', true, true, true, true, NOW());

INSERT INTO user_permission (user_id, permission_id) VALUES
    (3, 2),
    (4, 3);

INSERT INTO administrator (id, user_id, login, password, last_login) VALUES
    (2, 3, 'admin-role', '$2a$10$a8wbxBxvisKr1SVN1Ly.6eyEYIuhtVZP9NSoYs3CZnq9vPyFP65Lu', NULL),
    (3, 4, 'operator-role', '$2a$10$a8wbxBxvisKr1SVN1Ly.6eyEYIuhtVZP9NSoYs3CZnq9vPyFP65Lu', NULL);
