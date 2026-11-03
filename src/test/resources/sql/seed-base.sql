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

INSERT INTO company_membership (company_id, user_id, role, active, created_at, updated_at)
SELECT id, 1, 'COMPANY_OWNER', true, NOW(), NOW()
FROM company
WHERE slug = 'default';

INSERT INTO category (id, name, company_id)
SELECT 1, 'Test Category', id FROM company WHERE slug = 'default';

INSERT INTO brand (id, name, company_id)
SELECT 1, 'Test Brand', id FROM company WHERE slug = 'default';

INSERT INTO supplier (id, trade_name, cnpj, phone, email, active, company_id)
SELECT 1, 'Test Supplier', '12345678000199', '11999999999', 'supplier@test.com', true, id
FROM company WHERE slug = 'default';

INSERT INTO product (
    id,
    name,
    description,
    barcode,
    purchase_price,
    sale_price,
    quantity,
    created_at,
    active,
    company_id,
    category_id,
    brand_id,
    supplier_id
)
VALUES (
    1,
    'Test Product',
    'Product used by integration tests',
    '7890000000001',
    10.00,
    20.00,
    10,
    NOW(),
    true,
    (SELECT id FROM company WHERE slug = 'default'),
    1,
    1,
    1
);

INSERT INTO tb_user (
    id,
    first_name,
    last_name,
    email,
    phone,
    active,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    created_at
)
VALUES (
    2,
    'Client',
    'Test',
    'client@test.com',
    '11988888888',
    true,
    true,
    true,
    true,
    NOW()
);

INSERT INTO client (id, user_id, document_type, document_number, birth_date, address_id, company_id)
VALUES (1, 2, 'CPF', '12345678901', '1990-01-01', NULL,
        (SELECT id FROM company WHERE slug = 'default'));
