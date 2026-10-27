-- =============================================================
-- 供给端雷达 · 数据库初始化脚本
-- 目标库：MySQL `sunshine`　字符集：utf8mb4 / utf8mb4_general_ci
-- 来源：需求包 v1.0 附录A-数据结构.md（L1/L2/L3 + DDL）
-- 生成日期：2026-10-26
-- 执行方式：在目标库下执行（先 USE sunshine; 或客户端选中该库）
-- 说明：全部使用 CREATE TABLE IF NOT EXISTS，可重复执行，不删除既有数据
-- =============================================================

-- 若需显式选库（按需取消注释）：
-- USE `sunshine`;

-- -------------------------------------------------------------
-- T1. 高校站点表
-- -------------------------------------------------------------
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

-- -------------------------------------------------------------
-- T2. 招聘活动表
-- -------------------------------------------------------------
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
  `fingerprint`           VARCHAR(64)   NOT NULL                      COMMENT '去重指纹 md5(school_id|source_url|title|date|venue)',
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

-- -------------------------------------------------------------
-- T3. 采集任务表
-- -------------------------------------------------------------
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

-- -------------------------------------------------------------
-- T4. 活动变更记录表
-- -------------------------------------------------------------
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
-- 初始化数据：54 所高校（来源：需求包附录/高校清单-54所.md）
-- 幂等：school_code 唯一键 + INSERT IGNORE，可重复执行
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
('SDU','山东大学','济南','山东','https://job.sdu.edu.cn/index.htm','GENERIC',1,'原清单城市为山东省，已按学校所在地规范为济南'),
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

-- =============================================================
-- 校验（可选执行）
-- =============================================================
-- SELECT COUNT(*) AS school_total FROM university_site;          -- 期望 54
-- SELECT COUNT(*) AS enabled_total FROM university_site WHERE enabled = 1; -- 期望 53
-- SELECT COUNT(*) AS trial_total FROM university_site WHERE remark LIKE '%首批试采%'; -- 期望 9
