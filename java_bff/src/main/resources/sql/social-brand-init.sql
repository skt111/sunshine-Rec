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
