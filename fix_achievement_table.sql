-- 修复 achievement 表的 condition 字段
USE tiku_db;

-- 删除旧表（如果存在数据，这会丢失数据！）
DROP TABLE IF EXISTS user_achievement;
DROP TABLE IF EXISTS achievement;

-- Hibernate 会自动重新创建表



