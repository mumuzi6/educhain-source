-- 为user表的tags字段添加全文索引
-- 注意：此操作可能需要一些时间完成，具体取决于表大小

-- 添加全文索引
ALTER TABLE user ADD FULLTEXT INDEX idx_tags_fulltext (tags) WITH PARSER ngram;

-- 如果MySQL版本不支持ngram解析器，可以使用以下语句
-- ALTER TABLE user ADD FULLTEXT INDEX idx_tags_fulltext (tags);

-- 确认索引已添加
SHOW INDEX FROM user WHERE Key_name = 'idx_tags_fulltext'; 