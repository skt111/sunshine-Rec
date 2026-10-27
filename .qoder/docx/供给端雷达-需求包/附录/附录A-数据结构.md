# 附录A：数据结构契约（MySQL `sunshine`）

> **配套材料**: 主文档 §五（表清单）+ 原始 PRD §五（建议字段）
> **生成方式**: PRD 建议字段 → 结构化设计；来源性质 = **需求设计（新建表，非现有表反推）**
> **数据源分区**: 全部为 MySQL `sunshine` 库，**本次新建**（无现有表对接）
> **读写模式**: 🚨 本次允许 DDL（新建 4 表）；运行时正常读写
> **物理属性基线**: 全表 `ENGINE=InnoDB`、`DEFAULT CHARSET=utf8mb4`、`COLLATE=utf8mb4_general_ci`、时区语义 `Asia/Shanghai`、主键 `BIGINT AUTO_INCREMENT`
> **触发分级判定**: L1 必做（4 表）；L2 触发（表链 ≥2 表：活动⋈高校、历史⋈活动）；L3 触发（统计聚合/分类计数/分组聚合）；历史表专项**不触发**（全部新建，无现存表接入）

---

## 1. L1 Schema 层（4 张表——行数 = 主文档 §5.2 表清单）

### 1.1 `university_site` — 高校站点表

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 高校 ID（分组关联键） |
| `school_code` | VARCHAR(32) | NOT NULL, UK | 高校唯一编码（如 `PKU` / `ZZU`） |
| `school_name` | VARCHAR(128) | NOT NULL | 高校名称 |
| `school_city` | VARCHAR(64) | NOT NULL | 所在城市（如 `北京`/`武汉`） |
| `province` | VARCHAR(64) | DEFAULT NULL | 省份（可空） |
| `site_url` | VARCHAR(512) | NOT NULL | 就业网站入口 URL |
| `adapter_type` | VARCHAR(32) | NOT NULL DEFAULT 'GENERIC' | `GENERIC`/`SPECIFIC`（见 §3.7.8） |
| `adapter_key` | VARCHAR(64) | DEFAULT NULL | 专用适配器标识（如 `ZZU`/`CAUC`） |
| `enabled` | TINYINT(1) | NOT NULL DEFAULT 1 | 1-启用采集 0-停用 |
| `crawl_status` | VARCHAR(16) | NOT NULL DEFAULT 'NEVER' | `NEVER`/`SUCCESS`/`PARTIAL`/`FAILED`（§3.7.7） |
| `last_crawl_at` | DATETIME | DEFAULT NULL | 最近采集时间 |
| `last_fail_reason` | VARCHAR(1024) | DEFAULT NULL | 最近失败原因 |
| `remark` | VARCHAR(512) | DEFAULT NULL | 备注（来自高校清单） |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 更新时间（应用层刷新，禁 ON UPDATE） |

**索引**: `idx_city(school_city)`、`idx_enabled(enabled)`、`idx_crawl_status(crawl_status)`。
**唯一键**: `uk_school_code(school_code)`。

---

### 1.2 `employment_event` — 招聘活动表

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 内部活动 ID |
| `school_id` | BIGINT | NOT NULL | 发布高校 ID（→ university_site.id） |
| `school_name` | VARCHAR(128) | NOT NULL | 高校名称（冗余，便于展示/筛选） |
| `school_city` | VARCHAR(64) | NOT NULL | 高校所在地（冗余） |
| `event_title` | VARCHAR(512) | NOT NULL | 活动名称 |
| `event_type` | VARCHAR(32) | NOT NULL DEFAULT 'UNKNOWN' | `TALK`/`JOB_FAIR`/`RECRUITMENT_EVENT`/`EMPLOYMENT_SERVICE`/`OTHER_EMPLOYMENT_ACTIVITY`/`UNKNOWN`（§3.7.1） |
| `event_type_original` | VARCHAR(128) | DEFAULT NULL | 学校原始活动类型文本 |
| `venue_type` | VARCHAR(16) | NOT NULL DEFAULT 'UNKNOWN' | `ON_CAMPUS`/`OFF_CAMPUS`/`ONLINE`/`HYBRID`/`UNKNOWN`（§3.7.2） |
| `standard_category` | VARCHAR(32) | NOT NULL DEFAULT 'PENDING_REVIEW' | `ON_CAMPUS_TALK`/`OFF_CAMPUS_TALK`/`ON_CAMPUS_FAIR`/`OFF_CAMPUS_FAIR`/`OTHER`/`PENDING_REVIEW`（§3.7.3） |
| `event_date` | DATE | DEFAULT NULL | 活动日期（时间筛选口径基准） |
| `start_time` | VARCHAR(16) | DEFAULT NULL | 开始时间（如 `14:00`，保留原文容错） |
| `end_time` | VARCHAR(16) | DEFAULT NULL | 结束时间 |
| `venue_name` | VARCHAR(256) | DEFAULT NULL | 举办场馆 |
| `event_address` | VARCHAR(512) | DEFAULT NULL | 详细地点 |
| `organizer` | VARCHAR(256) | DEFAULT NULL | 主办单位 |
| `company_names` | VARCHAR(1024) | DEFAULT NULL | 相关企业名称（逗号分隔） |
| `registration_deadline` | DATETIME | DEFAULT NULL | 报名截止时间 |
| `registration_url` | VARCHAR(512) | DEFAULT NULL | 报名链接 |
| `event_description` | TEXT | DEFAULT NULL | 活动说明（清洗后文本/HTML） |
| `source_list_url` | VARCHAR(512) | DEFAULT NULL | 来源列表页 |
| `source_url` | VARCHAR(1024) | DEFAULT NULL | 来源详情页 |
| `source_published_at` | DATETIME | DEFAULT NULL | 来源发布时间 |
| `crawled_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 采集时间 |
| `last_seen_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 最近发现时间 |
| `event_status` | VARCHAR(16) | NOT NULL DEFAULT 'UNKNOWN' | `UPCOMING`/`ONGOING`/`FINISHED`/`CANCELLED`/`POSTPONED`/`UNKNOWN`（§3.7.4） |
| `verification_status` | VARCHAR(16) | NOT NULL DEFAULT 'PENDING' | `PENDING`/`VERIFIED`/`REJECTED`/`NEED_REVIEW`（§3.7.5） |
| `remote_id` | VARCHAR(128) | DEFAULT NULL | 网站稳定活动 ID（可空） |
| `fingerprint` | VARCHAR(64) | NOT NULL | 去重指纹（见下 D2 口径） |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 更新时间（应用层刷新） |

**索引**: `idx_school(school_id)`、`idx_date(event_date)`、`idx_category(standard_category)`、`idx_event_status(event_status)`、`idx_verification(verification_status)`、`idx_last_seen(last_seen_at)`、`idx_published(source_published_at)`、`idx_school_date(school_id, event_date)`、`idx_city(school_city)`。
**唯一键**: `uk_fingerprint(fingerprint)`；`uk_school_remote(school_id, remote_id)`（remote_id 为 NULL 时允许多行，MySQL 语义）。

**去重指纹口径（D2）**: `fingerprint = MD5(CONCAT(school_id, '|', COALESCE(source_url,''), '|', event_title, '|', COALESCE(event_date,''), '|', COALESCE(venue_name,'')))`。优先使用 `remote_id` 命中。

---

### 1.3 `crawl_task` — 采集任务表

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 任务 ID |
| `task_type` | VARCHAR(16) | NOT NULL | `ALL`/`SPECIFIED`（采集范围）；触发来源由 `triggered_by` 区分（用户名 / `SCHEDULED`） |
| `target_school_ids` | VARCHAR(1024) | DEFAULT NULL | 目标高校 ID 列表（逗号分隔；ALL 时为空） |
| `status` | VARCHAR(16) | NOT NULL DEFAULT 'PENDING' | `PENDING`/`RUNNING`/`SUCCESS`/`PARTIAL`/`FAILED`（§3.7.6） |
| `started_at` | DATETIME | DEFAULT NULL | 开始时间 |
| `finished_at` | DATETIME | DEFAULT NULL | 结束时间 |
| `total_count` | INT | NOT NULL DEFAULT 0 | 处理高校数 |
| `success_count` | INT | NOT NULL DEFAULT 0 | 成功高校数 |
| `fail_count` | INT | NOT NULL DEFAULT 0 | 失败高校数 |
| `error_summary` | TEXT | DEFAULT NULL | 错误摘要 |
| `triggered_by` | VARCHAR(64) | DEFAULT NULL | 触发者（用户/定时） |
| `created_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**: `idx_status(status)`、`idx_created(created_at)`。

---

### 1.4 `employment_event_history` — 活动变更记录表

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| `id` | BIGINT | PK AUTO_INCREMENT | 记录 ID |
| `event_id` | BIGINT | NOT NULL | 活动 ID（→ employment_event.id） |
| `crawl_task_id` | BIGINT | DEFAULT NULL | 来源采集任务 ID（→ crawl_task.id） |
| `change_type` | VARCHAR(32) | NOT NULL | `CREATED`/`UPDATED`/`CANCELLED`/`POSTPONED` |
| `field_name` | VARCHAR(64) | DEFAULT NULL | 变更字段名（CREATED 时为空） |
| `old_value` | TEXT | DEFAULT NULL | 旧值 |
| `new_value` | TEXT | DEFAULT NULL | 新值 |
| `changed_at` | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 变更时间 |

**索引**: `idx_event(event_id)`、`idx_event_changed(event_id, changed_at)`（详情“最近变更”取 Top1）、`idx_task(crawl_task_id)`、`idx_changed(changed_at)`。

---

## 2. L2 关系层（3 条关联）

| # | 关系 | 左表.列 | 右表.列 | 基数 | 连接语义 | 用途 | 物理注记 |
|---|------|---------|---------|------|---------|------|---------|
| R1 | 活动 ↔ 高校 | `employment_event.school_id` | `university_site.id` | N:1 | LEFT JOIN | 分组视图/按校筛选/采集状态关联 | 单库·索引 `idx_school` 已建 |
| R2 | 变更 ↔ 活动 | `employment_event_history.event_id` | `employment_event.id` | N:1 | LEFT JOIN | 详情抽屉"最近变更记录" | 单库·索引 `idx_event` 已建 |
| R3 | 变更 ↔ 任务 | `employment_event_history.crawl_task_id` | `crawl_task.id` | N:1 | LEFT JOIN | 追溯变更来源任务 | 单库·索引 `idx_task` 已建 |

> 均为单库应用层关联，**无物理外键**（D8）；关联列均有索引。

---

## 3. L3 口径层（6 条——触发：聚合/分类计数/分组/分页）

> 口径写死（语义层），执行形态不锁——参考 SQL 允许等价改写，**等价性以口径为准**。

### Q1 监测高校数（`statistics.monitoredSchoolCount`）
- 表链: `university_site`
- 口径: 启用采集的高校计数
- 参考 SQL:
```sql
SELECT COUNT(*) FROM university_site WHERE enabled = 1;
```
- 性能注记: 走 `idx_enabled`。

### Q2 活动总数（`statistics.totalEventCount`）
- 表链: `employment_event`
- 口径: 满足当前筛选条件的活动总数（**不含分类页签单选限制**，与列表同筛选）
- **无日期规则**: 时间筛选为“不限”时不过滤日期（含 `event_date IS NULL`）；启用时间范围时 `event_date BETWEEN` 自动排除 NULL（与主文档 §4.1.3(c) 一致）
- 参考 SQL:
```sql
SELECT COUNT(*) FROM employment_event
WHERE (:timeFilterEnabled = 0 OR event_date BETWEEN :startDate AND :endDate)
  AND (:city IS NULL OR school_city = :city)
  AND (:schoolIdsEmpty = 1 OR school_id IN (:schoolIds))
  AND (:venueType IS NULL OR venue_type = :venueType)
  AND (:eventStatus IS NULL OR event_status = :eventStatus)
  AND (:verificationStatus IS NULL OR verification_status = :verificationStatus)
  AND (:keyword IS NULL OR event_title LIKE CONCAT('%', :keyword, '%'));
```

### Q3 近期宣讲会 / 近期双选会（`recentTalkCount` / `recentFairCount`）
- 表链: `employment_event`
- 口径: 在 Q2 同一筛选基础上，按标准分类分组计数；"近期" = 所选时间范围内（默认 `event_date >= CURDATE()`）
- 参考 SQL:
```sql
SELECT standard_category, COUNT(*) AS cnt
FROM employment_event
WHERE event_date BETWEEN :startDate AND :endDate
  /* 其余同 Q2 筛选 */
GROUP BY standard_category;
```
- 映射: `recentTalkCount = ON_CAMPUS_TALK + OFF_CAMPUS_TALK`；`recentFairCount = ON_CAMPUS_FAIR + OFF_CAMPUS_FAIR`。

### Q4 分类页签计数（`categoryCounts`）
- 表链: `employment_event`
- 口径: 与 Q2 同筛选，按 `standard_category` 聚合；`all` = 全部；`other` = `OTHER` + `PENDING_REVIEW`
- 参考 SQL: 同 Q3 的 GROUP BY（返回全分类计数，前端映射为 6 个页签值）
- 性能注记: 走 `idx_category`。

### Q5 高校分组聚合（分组视图）
- 表链: `employment_event ⋈ university_site`（LEFT JOIN）
- 口径: 按 `school_id` 分组，统计当前筛选条件（不含 school 筛选时）下每校活动数 + 关联高校采集状态 + 前 3 条近期活动
- 参考 SQL（主体）:
```sql
SELECT e.school_id, e.school_name, e.school_city,
       COUNT(*) AS event_count,
       u.last_crawl_at, u.crawl_status
FROM employment_event e
LEFT JOIN university_site u ON u.id = e.school_id
WHERE e.event_date BETWEEN :startDate AND :endDate
  /* 其余同 Q2 筛选（排除 schoolIds） */
GROUP BY e.school_id, e.school_name, e.school_city, u.last_crawl_at, u.crawl_status
ORDER BY event_count DESC;
```
- 近期活动摘要（前 3 条）: 对分组结果批量反查（`school_id IN (...)` 一次查询取每组 Top3），**禁 N+1**。
- 🚨 分组键必须 `school_id`，禁止校名模糊匹配。

### Q6 活动列表分页与排序
- 表链: `employment_event`
- 口径: 同 Q2 筛选；排序白名单 `eventDate|lastSeenAt|sourcePublishedAt`，方向 `asc|desc`；默认 `event_date ASC`；后端分页
- 参考 SQL:
```sql
SELECT * FROM employment_event
WHERE /* 同 Q2 筛选 */
ORDER BY
  CASE WHEN :sortBy = 'eventDate' AND :dir = 'asc'  THEN event_date END ASC,
  CASE WHEN :sortBy = 'eventDate' AND :dir = 'desc' THEN event_date END DESC,
  CASE WHEN :sortBy = 'lastSeenAt' THEN last_seen_at END /* 方向同理 */,
  id ASC
LIMIT :size OFFSET :offset;
```
- 安全: `:sortBy`/`:dir` 必须经白名单校验（防注入），禁止直接拼接。
- 性能注记: `idx_school_date`、`idx_last_seen`。

---

## 4. 建表 DDL（可执行——交付物 D3）

```sql
-- 供给端雷达 建表脚本（MySQL 8.x / 5.7+）
-- 库：sunshine ｜ 字符集：utf8mb4 ｜ 生成日期：2026-10-26

CREATE TABLE IF NOT EXISTS university_site (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '高校ID',
  school_code       VARCHAR(32)  NOT NULL                COMMENT '高校唯一编码',
  school_name       VARCHAR(128) NOT NULL                COMMENT '高校名称',
  school_city       VARCHAR(64)  NOT NULL                COMMENT '所在城市',
  province          VARCHAR(64)  DEFAULT NULL            COMMENT '省份',
  site_url          VARCHAR(512) NOT NULL                COMMENT '就业网站入口',
  adapter_type      VARCHAR(32)  NOT NULL DEFAULT 'GENERIC' COMMENT 'GENERIC/SPECIFIC',
  adapter_key       VARCHAR(64)  DEFAULT NULL            COMMENT '专用适配器标识',
  enabled           TINYINT(1)   NOT NULL DEFAULT 1      COMMENT '1启用 0停用',
  crawl_status      VARCHAR(16)  NOT NULL DEFAULT 'NEVER' COMMENT 'NEVER/SUCCESS/PARTIAL/FAILED',
  last_crawl_at     DATETIME     DEFAULT NULL            COMMENT '最近采集时间',
  last_fail_reason  VARCHAR(1024) DEFAULT NULL           COMMENT '最近失败原因',
  remark            VARCHAR(512) DEFAULT NULL            COMMENT '备注',
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_school_code (school_code),
  KEY idx_city (school_city),
  KEY idx_enabled (enabled),
  KEY idx_crawl_status (crawl_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='高校站点表';

CREATE TABLE IF NOT EXISTS employment_event (
  id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  school_id             BIGINT       NOT NULL             COMMENT '高校ID',
  school_name           VARCHAR(128) NOT NULL             COMMENT '高校名称',
  school_city           VARCHAR(64)  NOT NULL             COMMENT '高校所在地',
  event_title           VARCHAR(512) NOT NULL             COMMENT '活动名称',
  event_type            VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN' COMMENT '活动类型',
  event_type_original   VARCHAR(128) DEFAULT NULL         COMMENT '原始活动类型',
  venue_type            VARCHAR(16)  NOT NULL DEFAULT 'UNKNOWN' COMMENT '地点属性',
  standard_category     VARCHAR(32)  NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '标准分类',
  event_date            DATE         DEFAULT NULL         COMMENT '活动日期',
  start_time            VARCHAR(16)  DEFAULT NULL         COMMENT '开始时间',
  end_time              VARCHAR(16)  DEFAULT NULL         COMMENT '结束时间',
  venue_name            VARCHAR(256) DEFAULT NULL         COMMENT '举办场馆',
  event_address         VARCHAR(512) DEFAULT NULL         COMMENT '详细地点',
  organizer             VARCHAR(256) DEFAULT NULL         COMMENT '主办单位',
  company_names         VARCHAR(1024) DEFAULT NULL        COMMENT '相关企业',
  registration_deadline DATETIME     DEFAULT NULL         COMMENT '报名截止',
  registration_url      VARCHAR(512) DEFAULT NULL         COMMENT '报名链接',
  event_description     TEXT         DEFAULT NULL         COMMENT '活动说明',
  source_list_url       VARCHAR(512) DEFAULT NULL         COMMENT '来源列表页',
  source_url            VARCHAR(1024) DEFAULT NULL        COMMENT '来源详情页',
  source_published_at   DATETIME     DEFAULT NULL         COMMENT '来源发布时间',
  crawled_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  last_seen_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近发现时间',
  event_status          VARCHAR(16)  NOT NULL DEFAULT 'UNKNOWN' COMMENT '活动状态',
  verification_status   VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '核验状态',
  remote_id             VARCHAR(128) DEFAULT NULL         COMMENT '网站稳定ID',
  fingerprint           VARCHAR(64)  NOT NULL             COMMENT '去重指纹',
  created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fingerprint (fingerprint),
  UNIQUE KEY uk_school_remote (school_id, remote_id),
  KEY idx_school (school_id),
  KEY idx_date (event_date),
  KEY idx_category (standard_category),
  KEY idx_event_status (event_status),
  KEY idx_verification (verification_status),
  KEY idx_last_seen (last_seen_at),
  KEY idx_published (source_published_at),
  KEY idx_school_date (school_id, event_date),
  KEY idx_city (school_city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='招聘活动表';

CREATE TABLE IF NOT EXISTS crawl_task (
  id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  task_type         VARCHAR(16)  NOT NULL             COMMENT 'ALL/SPECIFIED（采集范围）',
  target_school_ids VARCHAR(1024) DEFAULT NULL         COMMENT '目标高校ID列表',
  status            VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
  started_at        DATETIME     DEFAULT NULL         COMMENT '开始时间',
  finished_at       DATETIME     DEFAULT NULL         COMMENT '结束时间',
  total_count       INT          NOT NULL DEFAULT 0   COMMENT '处理高校数',
  success_count     INT          NOT NULL DEFAULT 0   COMMENT '成功高校数',
  fail_count        INT          NOT NULL DEFAULT 0   COMMENT '失败高校数',
  error_summary     TEXT         DEFAULT NULL         COMMENT '错误摘要',
  triggered_by      VARCHAR(64)  DEFAULT NULL         COMMENT '触发者',
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_status (status),
  KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采集任务表';

CREATE TABLE IF NOT EXISTS employment_event_history (
  id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  event_id       BIGINT      NOT NULL             COMMENT '活动ID',
  crawl_task_id  BIGINT      DEFAULT NULL         COMMENT '来源任务ID',
  change_type    VARCHAR(32) NOT NULL             COMMENT 'CREATED/UPDATED/CANCELLED/POSTPONED',
  field_name     VARCHAR(64) DEFAULT NULL         COMMENT '变更字段',
  old_value      TEXT        DEFAULT NULL         COMMENT '旧值',
  new_value      TEXT        DEFAULT NULL         COMMENT '新值',
  changed_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (id),
  KEY idx_event (event_id),
  KEY idx_event_changed (event_id, changed_at),
  KEY idx_task (crawl_task_id),
  KEY idx_changed (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动变更记录表';
```

## 5. 初始化数据（交付物 D4——54 所高校）

> 完整 54 条 `INSERT` 由开发方依据随包附录 `高校清单-54所.md` 生成（`school_code` 用高校简称拼音/缩写去重）。
> 计数断言：`SELECT COUNT(*) FROM university_site;` = **54**。
> 清单中「未提供官网链接」者（如外交学院）`site_url` 置空串、`enabled=0`、`remark` 记录原因。

示例（结构示范，非全部）：
```sql
INSERT INTO university_site (school_code, school_name, school_city, province, site_url, adapter_type, enabled, remark)
VALUES
('ZZU',   '郑州大学',     '郑州',  '河南', 'https://job.zzu.edu.cn/',        'GENERIC', 1, '清单试采优先站'),
('CAUC',  '中国民航大学', '天津',  '天津', 'https://cauc.bysjy.com.cn/',     'GENERIC', 1, '清单试采优先站'),
('TYUT',  '太原理工大学', '太原',  '山西', 'https://career.tyut.edu.cn/',    'GENERIC', 1, '清单试采优先站'),
('NANKAI','南开大学',     '天津',  '天津', 'https://career.nankai.edu.cn/',  'GENERIC', 1, 'JS 渲染可能'),
('SWUFE', '西南财经大学', '成都',  '四川', 'https://job.swufe.edu.cn/',      'GENERIC', 1, '清单试采优先站');
-- ... 其余 49 所按高校清单补全
```
