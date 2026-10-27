-- =============================================================
-- 供需侧雷达 v1.2：固化首批 10 所 + 适配器配置（可复现）
-- 修复审计 P1-11：「10 所打通」不可复现（init.sql 仍为 54 所、无 adapter_key）
-- 执行：mysql -uroot -p123456 -e "source <本文件>"
-- =============================================================

USE sunshine;

-- 1) 仅启用首批 10 所（中央民族大学 MUC 显式排除）
UPDATE university_site
SET enabled = CASE
    WHEN school_code IN ('PKU','THU','RUC','BUAA','BIT','CAU','BNU','BUPT','CUC','USTB') THEN 1
    ELSE 0
END;

-- 2) 适配器固化（与代码 FrontpageCrawler / ThuCicCrawler / PlaywrightHtmlCrawler 的 supports 对应）
UPDATE university_site SET adapter_key = 'FRONTPAGE'
WHERE school_code IN ('PKU','BIT','CAU','BNU','USTB','CUC','BUAA','BUPT');

UPDATE university_site SET adapter_key = 'THU_CIC'
WHERE school_code = 'THU';

UPDATE university_site SET adapter_key = 'PLAYWRIGHT_HTML'
WHERE school_code = 'RUC';

-- 3) 院校标签
UPDATE university_site SET school_tag = '985'
WHERE school_code IN ('PKU','THU','RUC','BUAA','BIT','CAU','BNU');
UPDATE university_site SET school_tag = '211'
WHERE school_code IN ('BUPT','CUC','USTB');

-- 4) 校验
SELECT school_code, school_name, school_tag, adapter_key, enabled
FROM university_site
WHERE enabled = 1
ORDER BY id;
