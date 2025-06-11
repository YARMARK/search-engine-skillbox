-- Индекс для колонки `path` длиной до 255 символов
CREATE INDEX idx_path ON page(path(255));
