-- Установим переменные
SET @index_name := 'idx_path';
SET @table_name := 'page';
SET @schema_name := DATABASE(); -- Текущая БД

--Используется information_schema.STATISTICS для проверки существования индекса.
SELECT COUNT(*) INTO @index_exists
FROM information_schema.STATISTICS
WHERE table_schema = @schema_name
  AND table_name = @table_name
  AND index_name = @index_name;

SET @sql_create := IF(@index_exists = 0,
    CONCAT('CREATE INDEX ', @index_name, ' ON ', @table_name, ' (path(255))'),
    'SELECT "Index already exists"');

PREPARE stmt FROM @sql_create;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
