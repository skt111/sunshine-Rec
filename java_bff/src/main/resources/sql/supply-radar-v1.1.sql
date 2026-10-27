-- =============================================================
-- 供需侧雷达 v1.1 数据库增量（按《供需侧雷达开发提示词》七、数据库设计）
-- 方案：单表扩展（保留 employment_event，补齐文档要求字段 + 新增 target_company）
-- 执行：mysql -uroot -p123456 < supply-radar-v1.1.sql
-- =============================================================

USE sunshine;

-- 7.1 university_site 补 school_tag（985/211）
ALTER TABLE university_site
    ADD COLUMN school_tag VARCHAR(32) NULL COMMENT '院校标签：985/211/其他' AFTER school_name;

UPDATE university_site SET school_tag = '985'
WHERE school_code IN ('PKU','THU','RUC','BUAA','BIT','CAU','BNU');
UPDATE university_site SET school_tag = '211'
WHERE school_code IN ('BUPT','CUC','USTB');

-- 7.2 target_company 重点企业配置（名单配置化，不硬编码）
CREATE TABLE IF NOT EXISTS target_company (
    id                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '企业ID',
    company_name        VARCHAR(128) NOT NULL COMMENT '标准企业名称',
    company_aliases     VARCHAR(512) NULL COMMENT '别名（逗号分隔）',
    company_category    VARCHAR(32)  NOT NULL COMMENT 'INSURANCE/FINANCE/INTERNET',
    recruitment_entity  VARCHAR(128) NULL COMMENT '默认招聘主体（总行/总部）',
    enabled             TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
    notes               VARCHAR(500) NULL COMMENT '备注（如仅监测总行）',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_company_name (company_name),
    KEY idx_category (company_category),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='重点监测企业';

-- 首批企业（3.1 保险竞品 / 3.2 头部金融 / 3.3 头部互联网）
INSERT INTO target_company (company_name, company_aliases, company_category, recruitment_entity, enabled, notes) VALUES
('中国人寿',       '国寿,中国人寿保险',                        'INSURANCE', '总部', 1, NULL),
('中国平安',       '平安,平安保险,平安人寿,平安产险',            'INSURANCE', '集团总部', 1, NULL),
('中国人民保险集团','人保,中国人保,人保财险,人保寿险',            'INSURANCE', '集团总部', 1, NULL),
('中国太平洋保险', '太保,中国太保,太平洋保险,太保寿险',          'INSURANCE', '集团总部', 1, NULL),
('友邦保险',       '友邦,AIA',                                'INSURANCE', '中国区总部', 1, NULL),
('泰康保险集团',   '泰康,泰康人寿,泰康资产,泰康健投',            'INSURANCE', '集团总部', 1, NULL),
('招商银行',       '招行,CMB',                                'FINANCE',   '总行', 1, '仅总行校招计为正式，分行活动需标注'),
('华泰证券',       '华泰,华泰联合证券',                        'FINANCE',   '总部', 1, NULL),
('广发证券',       '广发,广发基金',                            'FINANCE',   '总部', 1, NULL),
('中金公司',       '中金,CICC,中国国际金融',                    'FINANCE',   '总部', 1, NULL),
('腾讯',           'Tencent,腾讯科技,腾讯云',                   'INTERNET',  '集团总部', 1, NULL),
('阿里巴巴',       '阿里,Alibaba,蚂蚁集团,蚂蚁金服,淘宝',        'INTERNET',  '集团总部', 1, NULL),
('百度',           'Baidu,百度在线',                           'INTERNET',  '集团总部', 1, NULL),
('字节跳动',       '字节,ByteDance,抖音,今日头条',              'INTERNET',  '集团总部', 1, NULL),
('京东',           'JD,京东集团,京东科技',                      'INTERNET',  '集团总部', 1, NULL),
('美团',           'Meituan,美团点评',                         'INTERNET',  '集团总部', 1, NULL),
('小米',           'Xiaomi,小米集团',                          'INTERNET',  '集团总部', 1, NULL),
('网易',           'NetEase,网易游戏,网易有道',                 'INTERNET',  '集团总部', 1, NULL),
('快手',           'Kuaishou,快手科技',                        'INTERNET',  '集团总部', 1, NULL),
('华为',           'Huawei,华为技术,华为云',                    'INTERNET',  '集团总部', 1, NULL)
ON DUPLICATE KEY UPDATE company_aliases = VALUES(company_aliases);

-- 7.3/7.4 宣讲会 + 双选会 字段扩展（主题单表 + event_type 区分）
ALTER TABLE employment_event
    ADD COLUMN company_id               BIGINT       NULL COMMENT '企业ID→target_company.id（仅宣讲会）' AFTER event_title,
    ADD COLUMN company_name             VARCHAR(128) NULL COMMENT '标准企业名（仅宣讲会）' AFTER company_id,
    ADD COLUMN company_category         VARCHAR(32)  NULL COMMENT '企业类别 INSURANCE/FINANCE/INTERNET' AFTER company_name,
    ADD COLUMN recruitment_entity       VARCHAR(128) NULL COMMENT '招聘主体/子公司（公开明确时）' AFTER company_category,
    ADD COLUMN registration_start_at    DATETIME     NULL COMMENT '企业报名开始时间（双选会）' AFTER registration_deadline,
    ADD COLUMN registration_method      VARCHAR(256) NULL COMMENT '报名方式' AFTER registration_start_at,
    ADD COLUMN registration_requirements VARCHAR(1000) NULL COMMENT '企业报名条件' AFTER registration_method,
    ADD COLUMN registration_status      VARCHAR(20)  NOT NULL DEFAULT 'NOT_PUBLISHED'
        COMMENT 'NOT_OPEN/OPEN/CLOSING_SOON/CLOSED/NOT_PUBLISHED/CANCELLED/NEEDS_REVIEW' AFTER registration_requirements,
    ADD COLUMN original_title           VARCHAR(512) NULL COMMENT '原始标题（未清洗）' AFTER event_title,
    ADD COLUMN original_time_text       VARCHAR(256) NULL COMMENT '原始时间文本' AFTER start_time,
    ADD COLUMN original_category        VARCHAR(128) NULL COMMENT '原始栏目名称' AFTER event_type_original,
    ADD KEY idx_company (company_id),
    ADD KEY idx_reg_deadline (registration_deadline),
    ADD KEY idx_reg_status (registration_status);

-- 7.5 crawl_task 统计字段（第 246 行要求：候选/新增/更新/重复/待核验）
ALTER TABLE crawl_task
    ADD COLUMN candidate_count      INT NOT NULL DEFAULT 0 COMMENT '候选记录数' AFTER total_count,
    ADD COLUMN updated_count        INT NOT NULL DEFAULT 0 COMMENT '更新数' AFTER success_count,
    ADD COLUMN duplicate_count      INT NOT NULL DEFAULT 0 COMMENT '重复数' AFTER updated_count,
    ADD COLUMN pending_review_count INT NOT NULL DEFAULT 0 COMMENT '待核验数' AFTER duplicate_count;

-- 学生变更历史表补字段级追溯（已有表，补索引）
ALTER TABLE employment_event_history
    ADD KEY idx_event_changed (event_id, changed_at);
