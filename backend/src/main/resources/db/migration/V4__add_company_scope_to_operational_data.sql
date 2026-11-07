ALTER TABLE address ADD COLUMN company_id BIGINT NULL;
ALTER TABLE category ADD COLUMN company_id BIGINT NULL;
ALTER TABLE brand ADD COLUMN company_id BIGINT NULL;
ALTER TABLE supplier ADD COLUMN company_id BIGINT NULL;
ALTER TABLE product ADD COLUMN company_id BIGINT NULL;
ALTER TABLE client ADD COLUMN company_id BIGINT NULL;
ALTER TABLE sale ADD COLUMN company_id BIGINT NULL;
ALTER TABLE stock_movements ADD COLUMN company_id BIGINT NULL;
ALTER TABLE alert ADD COLUMN company_id BIGINT NULL;

UPDATE address CROSS JOIN company
SET address.company_id = company.id
WHERE company.slug = 'default';

UPDATE category CROSS JOIN company
SET category.company_id = company.id
WHERE company.slug = 'default';

UPDATE brand CROSS JOIN company
SET brand.company_id = company.id
WHERE company.slug = 'default';

UPDATE supplier CROSS JOIN company
SET supplier.company_id = company.id
WHERE company.slug = 'default';

UPDATE product CROSS JOIN company
SET product.company_id = company.id
WHERE company.slug = 'default';

UPDATE client CROSS JOIN company
SET client.company_id = company.id
WHERE company.slug = 'default';

UPDATE sale CROSS JOIN company
SET sale.company_id = company.id
WHERE company.slug = 'default';

UPDATE stock_movements CROSS JOIN company
SET stock_movements.company_id = company.id
WHERE company.slug = 'default';

UPDATE alert CROSS JOIN company
SET alert.company_id = company.id
WHERE company.slug = 'default';

-- Drop the legacy single-column unique keys by column, not by a generated
-- Hibernate index name. Existing installations may have different names.
SET @legacy_index = (SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'category'
      AND column_name = 'name' AND seq_in_index = 1 AND non_unique = 0
      AND index_name <> 'PRIMARY' LIMIT 1);
SET @ddl = IF(@legacy_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE category DROP INDEX `', @legacy_index, '`'));
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @legacy_index = (SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'brand'
      AND column_name = 'name' AND seq_in_index = 1 AND non_unique = 0
      AND index_name <> 'PRIMARY' LIMIT 1);
SET @ddl = IF(@legacy_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE brand DROP INDEX `', @legacy_index, '`'));
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @legacy_index = (SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'supplier'
      AND column_name = 'cnpj' AND seq_in_index = 1 AND non_unique = 0
      AND index_name <> 'PRIMARY' LIMIT 1);
SET @ddl = IF(@legacy_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE supplier DROP INDEX `', @legacy_index, '`'));
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @legacy_index = (SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'product'
      AND column_name = 'barcode' AND seq_in_index = 1 AND non_unique = 0
      AND index_name <> 'PRIMARY' LIMIT 1);
SET @ddl = IF(@legacy_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE product DROP INDEX `', @legacy_index, '`'));
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

SET @legacy_index = (SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'client'
      AND column_name = 'document_number' AND seq_in_index = 1 AND non_unique = 0
      AND index_name <> 'PRIMARY' LIMIT 1);
SET @ddl = IF(@legacy_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE client DROP INDEX `', @legacy_index, '`'));
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

ALTER TABLE address
    ADD CONSTRAINT fk_address_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD INDEX idx_address_company (company_id);

ALTER TABLE category
    ADD CONSTRAINT fk_category_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD CONSTRAINT uk_category_company_name UNIQUE (company_id, name);

ALTER TABLE brand
    ADD CONSTRAINT fk_brand_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD CONSTRAINT uk_brand_company_name UNIQUE (company_id, name);

ALTER TABLE supplier
    ADD CONSTRAINT fk_supplier_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD CONSTRAINT uk_supplier_company_cnpj UNIQUE (company_id, cnpj),
    ADD INDEX idx_supplier_company_active (company_id, active);

ALTER TABLE product
    ADD CONSTRAINT fk_product_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD CONSTRAINT uk_product_company_barcode UNIQUE (company_id, barcode),
    ADD INDEX idx_product_company_active_name (company_id, active, name);

ALTER TABLE client
    ADD CONSTRAINT fk_client_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD CONSTRAINT uk_client_company_document UNIQUE (company_id, document_number);

ALTER TABLE sale
    ADD CONSTRAINT fk_sale_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD INDEX idx_sale_company_date_status (company_id, date, status);

ALTER TABLE stock_movements
    ADD CONSTRAINT fk_stock_movement_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD INDEX idx_stock_movement_company_date_type (company_id, date, movement_type);

ALTER TABLE alert
    ADD CONSTRAINT fk_alert_company FOREIGN KEY (company_id) REFERENCES company (id),
    ADD INDEX idx_alert_company_active_read (company_id, active, is_read);
