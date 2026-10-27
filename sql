-- =============================================================
-- 供给端雷达 · 数据库初始化脚本
-- 目标库：MySQL `sunshine`　字符集：utf8mb4 / utf8mb4_general_ci
-- 执行方式：客户端选中 sunshine 库后整段执行
-- =============================================================

-- T1. 高校站点表
CREATE TABLE IF NOT EXISTS `university_site` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT            COMMENT '高校ID',
  `school_code`      VARCHAR(32)   NOT NULL                           COMMENT '高校唯一编码',
  `school_name`      VARCHAR(128)  NOT NULL                           COMMENT '高校名称',
  `school_city`      VARCHAR(64)   NOT NULL                           COMMENT '所在城市',
  `province`         VARCHAR(64)   DEFAULT NULL                       COMMENT '省份',
  `site_url`         VARCHAR(512)  NOT NULL DEFAULT ''                COMMENT '就业网站入口',
  `adapter_type`     VARCHAR(32)   NOT NULL DEFAULT 'GENERIC'         COMMENT 'GENERIC/SPECIFIC',
  `adapter_key`      VARCHAR(64)   DEFAULT NULL                       COMMENT '专用适配器标识',
  `enabled`          TINYINT(1)    NOT NULL DEFAULT 1                 COMMENT '1启用采集 0停用',
  `crawl_status`     VARCHAR(16)   NOT NULL DEFAULT 'NEVER'           COMMENT 'NEVER/SUCCESS/PARTIAL/FAILED',
  `last_crawl_at`    DATETIME      DEFAULT NULL                       COMMENT '最近采集时间',
  `last_fail_reason` VARCHAR(1024) DEFAULT NULL                       COMMENT '最近失败原因',
  `remark`           VARCHAR(512)  DEFAULT NULL                       COMMENT '备注',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间（应用层刷新）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_code` (`school_code`),
  KEY `idx_city` (`school_city`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_crawl_status` (`crawl_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='高校站点表';

-- T2. 招聘活动表
CREATE TABLE IF NOT EXISTS `employment_event` (
  `id`                    BIGINT        NOT NULL AUTO_INCREMENT       COMMENT '活动ID',
  `school_id`             BIGINT        NOT NULL                      COMMENT '高校ID（→ university_site.id）',
  `school_name`           VARCHAR(128)  NOT NULL                      COMMENT '高校名称（冗余）',
  `school_city`           VARCHAR(64)   NOT NULL                      COMMENT '高校所在地（冗余）',
  `event_title`           VARCHAR(512)  NOT NULL                      COMMENT '活动名称',
  `event_type`            VARCHAR(32)   NOT NULL DEFAULT 'UNKNOWN'    COMMENT 'TALK/JOB_FAIR/RECRUITMENT_EVENT/EMPLOYMENT_SERVICE/OTHER_EMPLOYMENT_ACTIVITY/UNKNOWN',
  `event_type_original`   VARCHAR(128)  DEFAULT NULL                  COMMENT '原始活动类型',
  `venue_type`            VARCHAR(16)   NOT NULL DEFAULT 'UNKNOWN'    COMMENT 'ON_CAMPUS/OFF_CAMPUS/ONLINE/HYBRID/UNKNOWN',
  `standard_category`     VARCHAR(32)   NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT 'ON_CAMPUS_TALK/OFF_CAMPUS_TALK/ON_CAMPUS_FAIR/OFF_CAMPUS_FAIR/OTHER/PENDING_REVIEW',
  `event_date`            DATE          DEFAULT NULL                  COMMENT '活动日期',
  `start_time`            VARCHAR(16)   DEFAULT NULL                  COMMENT '开始时间',
  `end_time`              VARCHAR(16)   DEFAULT NULL                  COMMENT '结束时间',
  `venue_name`            VARCHAR(256)  DEFAULT NULL                  COMMENT '举办场馆',
  `event_address`         VARCHAR(512)  DEFAULT NULL                  COMMENT '详细地点',
  `organizer`             VARCHAR(256)  DEFAULT NULL                  COMMENT '主办单位',
  `company_names`         VARCHAR(1024) DEFAULT NULL                  COMMENT '相关企业（逗号分隔）',
  `registration_deadline` DATETIME      DEFAULT NULL                  COMMENT '报名截止',
  `registration_url`      VARCHAR(512)  DEFAULT NULL                  COMMENT '报名链接',
  `event_description`     TEXT          DEFAULT NULL                  COMMENT '活动说明',
  `source_list_url`       VARCHAR(512)  DEFAULT NULL                  COMMENT '来源列表页',
  `source_url`            VARCHAR(1024) DEFAULT NULL                  COMMENT '来源详情页',
  `source_published_at`   DATETIME      DEFAULT NULL                  COMMENT '来源发布时间',
  `crawled_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  `last_seen_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近发现时间',
  `event_status`          VARCHAR(16)   NOT NULL DEFAULT 'UNKNOWN'    COMMENT 'UPCOMING/ONGOING/FINISHED/CANCELLED/POSTPONED/UNKNOWN',
  `verification_status`   VARCHAR(16)   NOT NULL DEFAULT 'PENDING'    COMMENT 'PENDING/VERIFIED/REJECTED/NEED_REVIEW',
  `remote_id`             VARCHAR(128)  DEFAULT NULL                  COMMENT '网站稳定活动ID',
  `fingerprint`           VARCHAR(64)   NOT NULL                      COMMENT '去重指纹',
  `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间（应用层刷新）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fingerprint` (`fingerprint`),
  UNIQUE KEY `uk_school_remote` (`school_id`, `remote_id`),
  KEY `idx_school` (`school_id`),
  KEY `idx_date` (`event_date`),
  KEY `idx_category` (`standard_category`),
  KEY `idx_event_status` (`event_status`),
  KEY `idx_verification` (`verification_status`),
  KEY `idx_last_seen` (`last_seen_at`),
  KEY `idx_published` (`source_published_at`),
  KEY `idx_school_date` (`school_id`, `event_date`),
  KEY `idx_city` (`school_city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招聘活动表';

-- T3. 采集任务表
CREATE TABLE IF NOT EXISTS `crawl_task` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT           COMMENT '任务ID',
  `task_type`         VARCHAR(16)   NOT NULL                          COMMENT 'ALL/SPECIFIED（采集范围）',
  `target_school_ids` VARCHAR(1024) DEFAULT NULL                      COMMENT '目标高校ID列表（逗号分隔）',
  `status`            VARCHAR(16)   NOT NULL DEFAULT 'PENDING'        COMMENT 'PENDING/RUNNING/SUCCESS/PARTIAL/FAILED',
  `started_at`        DATETIME      DEFAULT NULL                      COMMENT '开始时间',
  `finished_at`       DATETIME      DEFAULT NULL                      COMMENT '结束时间',
  `total_count`       INT           NOT NULL DEFAULT 0                COMMENT '处理高校数',
  `success_count`     INT           NOT NULL DEFAULT 0                COMMENT '成功高校数',
  `fail_count`        INT           NOT NULL DEFAULT 0                COMMENT '失败高校数',
  `error_summary`     TEXT          DEFAULT NULL                      COMMENT '错误摘要',
  `triggered_by`      VARCHAR(64)   DEFAULT NULL                      COMMENT '触发者（用户名/SCHEDULED）',
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采集任务表';

-- T4. 活动变更记录表
CREATE TABLE IF NOT EXISTS `employment_event_history` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT                COMMENT '记录ID',
  `event_id`      BIGINT      NOT NULL                               COMMENT '活动ID（→ employment_event.id）',
  `crawl_task_id` BIGINT      DEFAULT NULL                           COMMENT '来源任务ID（→ crawl_task.id）',
  `change_type`   VARCHAR(32) NOT NULL                               COMMENT 'CREATED/UPDATED/CANCELLED/POSTPONED',
  `field_name`    VARCHAR(64) DEFAULT NULL                           COMMENT '变更字段（CREATED 时为空）',
  `old_value`     TEXT        DEFAULT NULL                           COMMENT '旧值',
  `new_value`     TEXT        DEFAULT NULL                           COMMENT '新值',
  `changed_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_event` (`event_id`),
  KEY `idx_event_changed` (`event_id`, `changed_at`),
  KEY `idx_task` (`crawl_task_id`),
  KEY `idx_changed` (`changed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动变更记录表';

-- =============================================================
-- 初始化：54 所高校（幂等 INSERT IGNORE）
-- =============================================================
INSERT IGNORE INTO `university_site`
(`school_code`,`school_name`,`school_city`,`province`,`site_url`,`adapter_type`,`enabled`,`remark`) VALUES
('BUAA','北京航空航天大学','北京','北京','https://career.buaa.edu.cn/','GENERIC',1,NULL),
('USTB','北京科技大学','北京','北京','http://job.ustb.edu.cn','GENERIC',1,'核验工具未能稳定读取，建议复测'),
('BIT','北京理工大学','北京','北京','http://job.bit.edu.cn/','GENERIC',1,NULL),
('BUPT','北京邮电大学','北京','北京','https://job.bupt.edu.cn/','GENERIC',1,'核验工具未能稳定读取'),
('THU','清华大学','北京','北京','http://career.tsinghua.edu.cn','GENERIC',1,NULL),
('PKU','北京大学','北京','北京','https://scc.pku.edu.cn/home.action','GENERIC',1,NULL),
('RUC','中国人民大学','北京','北京','https://career.ruc.edu.cn/','GENERIC',1,NULL),
('UIBE','对外经济贸易大学','北京','北京','https://career.uibe.edu.cn/','GENERIC',1,NULL),
('CUPL','中国政法大学','北京','北京','https://scc.cupl.edu.cn/a/home','GENERIC',1,'核验工具未能稳定读取'),
('CUFE','中央财经大学','北京','北京','http://scc.cufe.edu.cn/','GENERIC',1,NULL),
('BJTU','北京交通大学','北京','北京','http://job.bjtu.edu.cn/frontpage/bjtu/html/index.html','GENERIC',1,NULL),
('NANKAI','南开大学','天津','天津','https://career.nankai.edu.cn/','SPECIFIC',1,'首批试采；页面可能需 JS 渲染'),
('TJU','天津大学','天津','天津','https://job.tju.edu.cn/','GENERIC',1,NULL),
('ZUEL','中南财经政法大学','武汉','湖北','https://jyzx.zuel.edu.cn/','GENERIC',1,'页面可能需要 JavaScript 渲染'),
('NCEPU','华北电力大学','北京','北京','http://job.ncepu.edu.cn/','GENERIC',1,NULL),
('BNU','北京师范大学','北京','北京','http://career.bnu.edu.cn','GENERIC',1,NULL),
('BJFU','北京林业大学','北京','北京','http://job.bjfu.edu.cn/frontpage/bjfu/html/index.html','GENERIC',1,'核验工具未能稳定读取'),
('CUGB','中国地质大学（北京）','北京','北京','http://jiuye.cugb.edu.cn','GENERIC',1,NULL),
('CUMTB','中国矿业大学（北京）','北京','北京','https://jy.cumtb.edu.cn/','SPECIFIC',1,'首批试采'),
('BJUT','北京工业大学','北京','北京','https://job.bjut.edu.cn/','GENERIC',1,NULL),
('BUCT','北京化工大学','北京','北京','https://career.buct.edu.cn/','GENERIC',1,NULL),
('CAU','中国农业大学','北京','北京','https://scc.cau.edu.cn/frontpage/cau/html/index.html','GENERIC',1,'核验工具未能稳定读取'),
('HIT','哈尔滨工业大学','哈尔滨','黑龙江','http://career.hit.edu.cn/','GENERIC',1,NULL),
('XIDIAN','西安电子科技大学','西安','陕西','https://job.xidian.edu.cn/','GENERIC',1,'直接抓取曾超时'),
('JLU','吉林大学','长春','吉林','http://jdjyw.jlu.edu.cn','GENERIC',1,NULL),
('DUT','大连理工大学','大连','辽宁','http://job.dlut.edu.cn/index.html','GENERIC',1,'本次访问超时，建议复测'),
('XJTU','西安交通大学','西安','陕西','http://job.xjtu.edu.cn/','GENERIC',1,NULL),
('CUEB','首都经济贸易大学','北京','北京','https://jy.cueb.edu.cn/','GENERIC',1,NULL),
('CAUC','中国民航大学','天津','天津','https://cauc.bysjy.com.cn/','GENERIC',1,'首批试采'),
('SCCM','四川传媒学院','成都','四川','http://jy.cdysxy.com/','GENERIC',1,'核验工具未能稳定读取'),
('TJGONG','天津工业大学','天津','天津','https://job.tiangong.edu.cn/','GENERIC',1,'首批试采'),
('LZU','兰州大学','兰州','甘肃','https://job.lzu.edu.cn/job/index','GENERIC',1,'核验工具未能稳定读取'),
('SCU','四川大学','成都','四川','https://jy.scu.edu.cn/','GENERIC',1,NULL),
('SWUFE','西南财经大学','成都','四川','https://job.swufe.edu.cn/','GENERIC',1,'首批试采'),
('WHU','武汉大学','武汉','湖北','https://xsjy.whu.edu.cn/','GENERIC',1,'本次返回网关错误，建议复测'),
('CQU','重庆大学','重庆','重庆','https://www.job.cqu.edu.cn/','GENERIC',1,NULL),
('CQNU','重庆师范大学','重庆','重庆','https://cqnu.cqbys.com/','GENERIC',1,'核验工具未能稳定读取'),
('SNNU','陕西师范大学','西安','陕西','http://job.snnu.edu.cn/','GENERIC',1,'本次访问超时'),
('SYSU','中山大学','广州','广东','https://career.sysu.edu.cn/','GENERIC',1,'曾返回 403，需浏览器复测'),
('JNU','暨南大学','广州','广东','http://jndx.bysjy.com.cn/','GENERIC',1,'页面可能需要 JavaScript 渲染'),
('NJU','南京大学','南京','江苏','http://job.nju.edu.cn/#!/home','GENERIC',1,'页面可能需要 JavaScript 渲染'),
('FUDAN','复旦大学','上海','上海','https://career.fudan.edu.cn/','GENERIC',1,NULL),
('SUFE','上海财经大学','上海','上海','https://career.sufe.edu.cn/career/index','GENERIC',1,'首批试采'),
('SDU','山东大学','济南','山东','https://job.sdu.edu.cn/index.htm','GENERIC',1,'原清单城市为山东省，已按所在地规范为济南'),
('BFSU','北京外国语大学','北京','北京','https://jyzd.bfsu.edu.cn/','GENERIC',1,'核验工具未能稳定读取'),
('CUC','中国传媒大学','北京','北京','https://jy.cuc.edu.cn/','GENERIC',1,NULL),
('CFAU','外交学院','北京','北京','','GENERIC',0,'就业指导中心无官网；电话 010-68323945；邮箱 wjxybys@126.com'),
('ZJU','浙江大学','杭州','浙江','https://www.career.zju.edu.cn/jyxt/jyweb/webIndex.zf','GENERIC',1,'原清单城市未填写，已按所在地规范为杭州'),
('BTBU','北京工商大学','北京','北京','https://gsbys.btbu.edu.cn/frontpage/btbu/html/index.html','GENERIC',1,NULL),
('SJTU','上海交通大学','上海','上海','https://www.job.sjtu.edu.cn/','GENERIC',1,'曾超时'),
('MUC','中央民族大学','北京','北京','https://career.muc.edu.cn/','SPECIFIC',1,'首批试采'),
('USTC','中国科学技术大学','合肥','安徽','https://www.job.ustc.edu.cn/','GENERIC',1,'原清单省份为安徽，已按所在地规范为合肥'),
('ZZU','郑州大学','郑州','河南','https://job.zzu.edu.cn/','GENERIC',1,'首批试采'),
('TYUT','太原理工大学','太原','山西','https://career.tyut.edu.cn/','GENERIC',1,'首批试采');

-- 校验
SELECT COUNT(*) AS school_total FROM university_site;                        -- 期望 54
SELECT COUNT(*) AS enabled_total FROM university_site WHERE enabled = 1;    -- 期望 53
SELECT COUNT(*) AS trial_total FROM university_site WHERE remark LIKE '%首批试采%'; -- 期望 9






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




-------------------------


-- =============================================================
-- 雇主品牌雷达 v1.0：重点企业公开笔记（小红书）阶段一最小闭环
-- 依《重点企业小红书公开笔记热点监测方案（合规版）》第五章数据模型
-- 规则：全部 CREATE TABLE IF NOT EXISTS，可重复执行，不删除既有数据
-- 执行：mysql -uroot -p123456 < social-brand-init.sql
-- =============================================================

USE sunshine;

-- -------------------------------------------------------------
-- T1. 企业账号表（账号性质与核验状态必须显式记录，不得默认认定为官方）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `social_account` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '账号ID',
  `company_id`         BIGINT       NOT NULL                           COMMENT '企业ID（→ target_company.id）',
  `platform`           VARCHAR(32)  NOT NULL DEFAULT 'XIAOHONGSHU'     COMMENT '平台标识',
  `account_name`       VARCHAR(128) NOT NULL                           COMMENT '账号显示名称',
  `home_url`           VARCHAR(512) DEFAULT NULL                       COMMENT '主页链接（平台内无网页直链时为空）',
  `account_nature`     VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN'         COMMENT 'OFFICIAL_ENTERPRISE/OFFICIAL_RECRUIT/EMPLOYEE_PERSONAL/PUBLIC/UNKNOWN',
  `verified_status`    VARCHAR(16)  NOT NULL DEFAULT 'PENDING'         COMMENT 'VERIFIED/PENDING/FAILED',
  `verify_basis`       VARCHAR(512) DEFAULT NULL                       COMMENT '核验依据（截图/官方页面/媒体报道）',
  `verified_at`        DATETIME     DEFAULT NULL                       COMMENT '核验时间',
  `company_confidence` DECIMAL(3,2) DEFAULT NULL                       COMMENT '企业关联置信度 0.00-1.00',
  `enabled`            TINYINT(1)   NOT NULL DEFAULT 1                 COMMENT '1启用 0停用',
  `remark`             VARCHAR(512) DEFAULT NULL                       COMMENT '备注',
  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间（应用层刷新）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_account` (`platform`, `account_name`),
  KEY `idx_company` (`company_id`),
  KEY `idx_verified` (`verified_status`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='重点企业社媒账号';

-- -------------------------------------------------------------
-- T2. 公开笔记表（note_url 为唯一追溯依据；不存正文全文与媒体文件）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `social_note` (
  `id`                  BIGINT        NOT NULL AUTO_INCREMENT          COMMENT '笔记ID',
  `platform`            VARCHAR(32)   NOT NULL DEFAULT 'XIAOHONGSHU'   COMMENT '平台标识',
  `note_url`            VARCHAR(1024) NOT NULL                         COMMENT '原始链接（唯一追溯依据）',
  `title`               VARCHAR(512)  NOT NULL DEFAULT ''              COMMENT '标题',
  `content_summary`     VARCHAR(1000) DEFAULT NULL                     COMMENT '必要简短摘要（不存全文）',
  `published_at`        DATETIME      DEFAULT NULL                     COMMENT '发布时间',
  `first_observed_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次发现时间',
  `last_observed_at`    DATETIME      DEFAULT NULL                     COMMENT '最近观察时间',
  `company_id`          BIGINT        NOT NULL                         COMMENT '关联企业（→ target_company.id）',
  `account_id`          BIGINT        DEFAULT NULL                     COMMENT '关联账号（→ social_account.id，可为空）',
  `owner_type`          VARCHAR(16)   NOT NULL DEFAULT 'UNKNOWN'       COMMENT 'OFFICIAL/USER_DISCUSSION/UNKNOWN',
  `source_type`         VARCHAR(32)   NOT NULL DEFAULT 'MANUAL'        COMMENT 'MANUAL/AUTHORIZED_API/AUTHORIZED_SERVICE/PUBLIC_PAGE',
  `verification_status` VARCHAR(16)   NOT NULL DEFAULT 'PENDING'       COMMENT 'VERIFIED/PENDING/FAILED',
  `topic_tags`          VARCHAR(256)  DEFAULT NULL                     COMMENT '主题标签（逗号分隔）',
  `missing_reason`      VARCHAR(256)  DEFAULT NULL                     COMMENT '指标缺失原因（如：收藏数未公开）',
  `remark`              VARCHAR(512)  DEFAULT NULL                     COMMENT '备注',
  `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间（应用层刷新）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_url` (`note_url`(700)),
  KEY `idx_company_publish` (`company_id`, `published_at`),
  KEY `idx_account` (`account_id`),
  KEY `idx_status` (`verification_status`),
  KEY `idx_owner` (`owner_type`),
  KEY `idx_published` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='重点企业公开笔记';

-- -------------------------------------------------------------
-- T3. 互动指标快照表（缺失字段留 NULL，禁止用 0 或点赞数顶替）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `social_note_metric_snapshot` (
  `id`                 BIGINT      NOT NULL AUTO_INCREMENT             COMMENT '快照ID',
  `note_id`            BIGINT      NOT NULL                            COMMENT '笔记ID（→ social_note.id）',
  `like_count`         INT         DEFAULT NULL                        COMMENT '点赞数（缺失为 NULL）',
  `favorite_count`     INT         DEFAULT NULL                        COMMENT '收藏数（缺失为 NULL）',
  `comment_count`      INT         DEFAULT NULL                        COMMENT '评论数（缺失为 NULL）',
  `share_count`        INT         DEFAULT NULL                        COMMENT '分享数（来源提供时）',
  `view_count`         INT         DEFAULT NULL                        COMMENT '浏览/曝光量（来源提供时）',
  `metric_observed_at` DATETIME    NOT NULL                            COMMENT '指标观察时间',
  `metric_source`      VARCHAR(64) NOT NULL                            COMMENT '指标来源（如 MANUAL_2026-10-27）',
  `created_at`         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_observed` (`note_id`, `metric_observed_at`),
  KEY `idx_observed` (`metric_observed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公开笔记互动指标快照';

-- =============================================================
-- 账号候选清单（仅登记“候选”，未核验不得当作官方账号）
-- 说明：账号名称来自公开内容中的自述/提及，核验状态一律 PENDING，
--       业务取得主页截图并填写 verify_basis 后再置为 VERIFIED。
-- 幂等：uk_platform_account + INSERT IGNORE
-- =============================================================
INSERT IGNORE INTO `social_account`
(`company_id`, `platform`, `account_name`, `home_url`, `account_nature`, `verified_status`, `verify_basis`, `company_confidence`, `enabled`, `remark`)
SELECT c.id, 'XIAOHONGSHU', t.account_name, NULL, 'UNKNOWN', 'PENDING', '候选账号：待主页截图核验', NULL, 1, '候选账号，未核验不计入已核验账号'
FROM `target_company` c
JOIN (
  SELECT '中国平安' AS company_name, '中国平安招聘' AS account_name
  UNION ALL SELECT '腾讯', '腾讯招聘'
  UNION ALL SELECT '美团', '美团招聘'
) t ON t.company_name = c.company_name;

-- 校验
SELECT a.id, c.company_name, a.account_name, a.verified_status, a.enabled
FROM `social_account` a
JOIN `target_company` c ON c.id = a.company_id
ORDER BY a.id;





































