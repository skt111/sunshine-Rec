-- =============================================================
-- 雇主品牌雷达 · 数据来源核验（阶段一）
-- 目的：把「能不能自动读取」变成可查证据，而不是口头判断。
-- 规则：只做 robots.txt 判定与单次可达性探测，不绕过登录/验证码/风控。
-- 执行：mysql -uroot -p123456 < social-brand-sources.sql
-- =============================================================

USE sunshine;

-- -------------------------------------------------------------
-- T1. 数据来源登记表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `social_data_source` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `source_type`      VARCHAR(32)  NOT NULL COMMENT 'MANUAL/AUTHORIZED_API/AUTHORIZED_SERVICE/PUBLIC_PAGE',
  `label`            VARCHAR(64)  NOT NULL COMMENT '来源名称',
  `authorized`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否取得授权',
  `allowed_fields`   VARCHAR(512) DEFAULT NULL COMMENT '授权字段范围',
  `robots_decision`  VARCHAR(32)  DEFAULT NULL COMMENT 'ALLOW/DISALLOW/NOT_APPLICABLE',
  `status`           VARCHAR(24)  NOT NULL DEFAULT 'NOT_CONFIGURED' COMMENT 'ENABLED/NOT_CONFIGURED/DISABLED_BY_POLICY',
  `conclusion`       VARCHAR(512) DEFAULT NULL COMMENT '当前结论',
  `last_checked_at`  DATETIME     DEFAULT NULL,
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='社媒数据来源登记';

-- -------------------------------------------------------------
-- T2. 来源核验记录（每次探测留痕）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `social_source_check` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `source_type`      VARCHAR(32)  NOT NULL,
  `target_url`       VARCHAR(512) NOT NULL,
  `robots_url`       VARCHAR(512) DEFAULT NULL,
  `robots_decision`  VARCHAR(32)  DEFAULT NULL COMMENT 'ALLOW/DISALLOW/NOT_APPLICABLE',
  `robots_rule`      VARCHAR(256) DEFAULT NULL COMMENT '命中的规则原文',
  `http_status`      INT          DEFAULT NULL COMMENT '未发起请求时为空',
  `final_url`        VARCHAR(512) DEFAULT NULL,
  `login_required`   TINYINT(1)   DEFAULT NULL,
  `block_markers`    VARCHAR(256) DEFAULT NULL COMMENT '命中的拦截特征词',
  `conclusion`       VARCHAR(32)  NOT NULL COMMENT 'ALLOWED/DISALLOWED_BY_ROBOTS/LOGIN_REQUIRED/BLOCKED/UNKNOWN',
  `remark`           VARCHAR(512) DEFAULT NULL,
  `checked_at`       DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_source_checked` (`source_type`, `checked_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='社媒来源核验记录';

-- -------------------------------------------------------------
-- 来源登记（幂等）
-- -------------------------------------------------------------
INSERT INTO `social_data_source`
(`source_type`, `label`, `authorized`, `allowed_fields`, `robots_decision`, `status`, `conclusion`)
VALUES
('MANUAL', '人工辅助录入', 0, '页面可见的标题/链接/点赞/收藏/评论', 'NOT_APPLICABLE', 'ENABLED',
 '业务在小红书 App/网页端人工查看后录入；不涉及自动访问'),
('AUTHORIZED_API', '平台授权接口', 0, NULL, NULL, 'NOT_CONFIGURED',
 '尚未取得小红书开放平台/商业数据接口授权，字段可得性待核验'),
('AUTHORIZED_SERVICE', '授权第三方数据服务', 0, NULL, NULL, 'NOT_CONFIGURED',
 '尚未签约授权数据服务，授权范围与数据保留要求待确认'),
('PUBLIC_PAGE', '公开页面自动读取', 0, NULL, 'DISALLOW', 'DISABLED_BY_POLICY',
 'robots.txt 对通用 UA 全站 Disallow，且未登录访问 /explore 返回 302；按合规边界不启用自动读取')
ON DUPLICATE KEY UPDATE `label` = VALUES(`label`), `status` = VALUES(`status`), `conclusion` = VALUES(`conclusion`);

-- -------------------------------------------------------------
-- 初始核验证据（2026-10-27 实测，供审计追溯）
-- -------------------------------------------------------------
INSERT INTO `social_source_check`
(`source_type`, `target_url`, `robots_url`, `robots_decision`, `robots_rule`, `http_status`, `final_url`,
 `login_required`, `block_markers`, `conclusion`, `remark`, `checked_at`)
VALUES
('PUBLIC_PAGE', 'https://www.xiaohongshu.com/robots.txt', 'https://www.xiaohongshu.com/robots.txt',
 'DISALLOW', 'User-agent: * → Disallow: /', 200, 'https://www.xiaohongshu.com/robots.txt',
 0, NULL, 'DISALLOWED_BY_ROBOTS',
 'robots 明确“User-agent: * Disallow: /”，仅对 Baidu/Bing/360/Sogou 等搜索引擎放开 /explore', '2026-10-27 15:20:00'),
('PUBLIC_PAGE', 'https://www.xiaohongshu.com/explore', 'https://www.xiaohongshu.com/robots.txt',
 'DISALLOW', 'User-agent: * → Disallow: /', 302, 'https://www.xiaohongshu.com/explore',
 NULL, '302 跳转（未登录）', 'DISALLOWED_BY_ROBOTS',
 '单次可达性探测：未登录直接 302；叠加 robots 全站禁止，自动读取路径不可用', '2026-10-27 15:20:00')
ON DUPLICATE KEY UPDATE `checked_at` = VALUES(`checked_at`);

-- 校验
SELECT source_type, label, authorized, robots_decision, status, LEFT(conclusion, 40) AS conclusion
FROM `social_data_source` ORDER BY `id`;
SELECT source_type, target_url, http_status, conclusion, checked_at
FROM `social_source_check` ORDER BY `id`;
