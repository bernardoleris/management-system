INSERT INTO company (
    legal_name, trade_name, document_number, slug, active, timezone, currency, created_at, updated_at
) VALUES (
    'Empresa B LTDA', 'Empresa B', '98765432000198', 'empresa-b', true, 'America/Sao_Paulo', 'BRL', NOW(), NOW()
);

INSERT INTO tb_user (
    id, first_name, last_name, email, active,
    account_non_expired, account_non_locked, credentials_non_expired, created_at
) VALUES (
    10, 'Owner', 'Empresa B', 'owner.empresa-b@test.com', true, true, true, true, NOW()
);

INSERT INTO administrator (id, user_id, login, password, last_login)
VALUES (
    10, 10, 'owner-empresa-b', '$2a$10$a8wbxBxvisKr1SVN1Ly.6eyEYIuhtVZP9NSoYs3CZnq9vPyFP65Lu', NULL
);

INSERT INTO company_membership (company_id, user_id, role, active, created_at, updated_at)
SELECT id, 10, 'COMPANY_OWNER', true, NOW(), NOW()
FROM company
WHERE slug = 'empresa-b';

INSERT INTO category (id, name, company_id)
SELECT 2, 'Category From Company B', id
FROM company
WHERE slug = 'empresa-b';
