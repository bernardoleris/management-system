UPDATE address
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE category
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE brand
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE supplier
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE product
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE client
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE sale
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE stock_movements
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

UPDATE alert
SET company_id = (SELECT id FROM company WHERE slug = 'default')
WHERE company_id IS NULL;

ALTER TABLE address MODIFY company_id BIGINT NOT NULL;
ALTER TABLE category MODIFY company_id BIGINT NOT NULL;
ALTER TABLE brand MODIFY company_id BIGINT NOT NULL;
ALTER TABLE supplier MODIFY company_id BIGINT NOT NULL;
ALTER TABLE product MODIFY company_id BIGINT NOT NULL;
ALTER TABLE client MODIFY company_id BIGINT NOT NULL;
ALTER TABLE sale MODIFY company_id BIGINT NOT NULL;
ALTER TABLE stock_movements MODIFY company_id BIGINT NOT NULL;
ALTER TABLE alert MODIFY company_id BIGINT NOT NULL;
