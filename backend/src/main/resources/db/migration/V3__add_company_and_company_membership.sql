CREATE TABLE company (
    id BIGINT NOT NULL AUTO_INCREMENT,
    legal_name VARCHAR(150) NOT NULL,
    trade_name VARCHAR(150) NOT NULL,
    document_number VARCHAR(14),
    slug VARCHAR(80) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    active BOOLEAN NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    currency CHAR(3) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_company_document_number UNIQUE (document_number),
    CONSTRAINT uk_company_slug UNIQUE (slug)
);

CREATE TABLE company_membership (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_company_membership_company_user UNIQUE (company_id, user_id),
    CONSTRAINT fk_company_membership_company FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_company_membership_user FOREIGN KEY (user_id) REFERENCES tb_user (id)
);

CREATE INDEX idx_company_membership_user_active
    ON company_membership (user_id, active);

INSERT INTO company (
    legal_name,
    trade_name,
    document_number,
    slug,
    active,
    timezone,
    currency,
    created_at,
    updated_at
)
VALUES (
    'Empresa padrão migrada',
    'Empresa padrão',
    NULL,
    'default',
    true,
    'America/Sao_Paulo',
    'BRL',
    NOW(6),
    NOW(6)
);

INSERT INTO company_membership (
    company_id,
    user_id,
    role,
    active,
    created_at,
    updated_at
)
SELECT
    company.id,
    administrator.user_id,
    CASE
        WHEN EXISTS (
            SELECT 1
            FROM user_permission
            INNER JOIN permission ON permission.id = user_permission.permission_id
            WHERE user_permission.user_id = administrator.user_id
              AND permission.description = 'ROLE_OWNER'
        ) THEN 'COMPANY_OWNER'
        WHEN EXISTS (
            SELECT 1
            FROM user_permission
            INNER JOIN permission ON permission.id = user_permission.permission_id
            WHERE user_permission.user_id = administrator.user_id
              AND permission.description = 'ROLE_ADMIN'
        ) THEN 'ADMIN'
        ELSE 'OPERATOR'
    END,
    tb_user.active,
    NOW(6),
    NOW(6)
FROM administrator
INNER JOIN tb_user ON tb_user.id = administrator.user_id
INNER JOIN company ON company.slug = 'default';
