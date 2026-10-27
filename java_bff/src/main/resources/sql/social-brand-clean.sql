-- =============================================================
-- 雇主品牌雷达 · 大模型数据清洗（阶段一）
-- 目的：把采集来的脏标题（含作者名/时间/点赞数）清洗成可读标题，
--       并给出归属类型与主题标签；原值留档，可追溯可回滚。
-- 执行：mysql -uroot -p123456 < social-brand-clean.sql
-- =============================================================

USE sunshine;

-- 幂等加列（MySQL 8 无 ADD COLUMN IF NOT EXISTS，用 information_schema 判断）
DROP PROCEDURE IF EXISTS `add_col_if_absent`;
DELIMITER $$
CREATE PROCEDURE `add_col_if_absent`(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_ddl VARCHAR(512))
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_col
  ) THEN
    SET @s = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
    PREPARE stmt FROM @s;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL add_col_if_absent('social_note', 'title_raw',
  '`title_raw` VARCHAR(512) DEFAULT NULL COMMENT ''清洗前原始标题'' AFTER `title`');
CALL add_col_if_absent('social_note', 'clean_source',
  '`clean_source` VARCHAR(16) DEFAULT NULL COMMENT ''清洗方式：LLM/RULE/NONE'' AFTER `missing_reason`');
CALL add_col_if_absent('social_note', 'clean_basis',
  '`clean_basis` VARCHAR(512) DEFAULT NULL COMMENT ''清洗依据与置信度'' AFTER `clean_source`');
CALL add_col_if_absent('social_note', 'cleaned_at',
  '`cleaned_at` DATETIME DEFAULT NULL COMMENT ''清洗时间'' AFTER `clean_basis`');

DROP PROCEDURE IF EXISTS `add_col_if_absent`;

-- 校验
SELECT column_name, column_type, column_comment
FROM information_schema.columns
WHERE table_schema = 'sunshine' AND table_name = 'social_note'
  AND column_name IN ('title', 'title_raw', 'clean_source', 'clean_basis', 'cleaned_at')
ORDER BY ordinal_position;
