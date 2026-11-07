-- The pre-multi-company schema may use a generated Hibernate index name.
SET @legacy_index = (SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'tb_user'
      AND column_name = 'email' AND seq_in_index = 1 AND non_unique = 0
      AND index_name <> 'PRIMARY' LIMIT 1);
SET @ddl = IF(@legacy_index IS NULL, 'SELECT 1', CONCAT('ALTER TABLE tb_user DROP INDEX `', @legacy_index, '`'));
PREPARE migration_stmt FROM @ddl;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;
