# 附录A：数据结构（员工入职引导平台）

> 配套材料：主文档《员工入职引导平台需求文档》v1.0 ｜ 生成方式：需求定义（本包唯一权威——非现网 DDL 转载） ｜ 数据源分区：§1 HR 库（现存只读）/ §2-§11 平台库（本次新建 10 表）
> 读写模式声明：🚨 §1 employee **只读**（禁写入/DDL）；§2-§11 为本次**唯一 DDL 授权范围**（新建 10 表），其余严禁任何 DDL。
> 字符集约定：新建表 utf8mb4（统一 utf8mb4_general_ci——避免跨表 collation 混用）。

## 1. HR 库（现存只读）

### 1.1 employee — 员工信息

> ⚠️ **留白 L3**：本表为现网表，字段清单**待 DBA 提供现网 DDL 后定稿**；以下为需求使用面所需字段的暂拟描述（开发前必须与现网核对，以现网为准）。

| 字段名（暂拟） | 类型 | 约束/默认值 | 说明 |
|---------------|------|------------|------|
| oprid | VARCHAR(64) | PK | 域账号（全平台人员唯一标识） |
| name | VARCHAR(64) | — | 姓名（联查展示） |
| dept | VARCHAR(64) | — | 部门（**主管数据范围过滤依据**——现网字段名以核对为准） |
| （其余字段按现网核对） | — | — | 本平台仅读取上述使用面字段，其余不涉及 |

索引：按 oprid（PK）。本轮**无任何写入**。

## 2. 引导任务（平台库·新建）

### 2.1 onboarding_task_template — 任务模板

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| template_name | VARCHAR(64) | NOT NULL, UK uk_name | 模板名称（全局唯一） |
| position_code | VARCHAR(32) | NOT NULL | 适用岗位编码（键值集合见主文档 §九 键名表维护说明） |
| items | JSON | NOT NULL | 任务条目数组：[{item_name, description, due_days, sort_order}，due_days≥1，数组长度≥1] |
| is_active | TINYINT | NOT NULL DEFAULT 1 | 1-启用 0-停用（停用不影响已有实例） |
| created_by | VARCHAR(64) | NOT NULL | 创建人 oprid |
| created_at / updated_at | DATETIME | NOT NULL | 应用层显式维护（禁止 ON UPDATE 隐式刷新） |

索引：uk_name(template_name)。说明：条目以 JSON 存储（模板粒度小、成组读取；生成实例时解析为快照）。

### 2.2 onboarding_task — 任务实例（模板快照）

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| pairing_id | BIGINT | NOT NULL, idx | 关联 onboarding_pairing.id（配对=入职启动点） |
| oprid | VARCHAR(64) | NOT NULL, idx | 新人域账号 |
| task_name | VARCHAR(64) | NOT NULL | 任务名称（生成时快照） |
| description | VARCHAR(255) | NULL | 任务说明 |
| due_date | DATE | NOT NULL | 截止日 = 入职日 + due_days（快照） |
| task_status | VARCHAR(16) | NOT NULL DEFAULT 'PENDING' | PENDING/IN_PROGRESS/DONE（终态 DONE 不可逆） |
| done_at | DATETIME | NULL | 完成时间 |
| created_at / updated_at | DATETIME | NOT NULL | 应用层显式维护 |

索引：idx_pairing(pairing_id)、idx_oprid(oprid)。说明：**快照语义**——模板后续修改不影响本表已有行。

### 2.3 onboarding_material — 材料记录（元数据）

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| oprid | VARCHAR(64) | NOT NULL, idx | 材料归属人（新人） |
| material_type | VARCHAR(32) | NOT NULL | ID_CARD/EDUCATION/CONTRACT（枚举） |
| file_name | VARCHAR(255) | NOT NULL | 原始文件名（**列表仅返回元数据**） |
| file_size | BIGINT | NOT NULL | 字节数 |
| storage_key | VARCHAR(255) | NOT NULL | 存储标识（**永不直接下发**——下载走鉴权流式接口） |
| material_status | VARCHAR(16) | NOT NULL DEFAULT 'SUBMITTED' | SUBMITTED/APPROVED/REJECTED |
| reject_reason | VARCHAR(200) | NULL | 驳回原因（REJECTED 时必填） |
| resubmit_count | INT | NOT NULL DEFAULT 0 | 重申次数 |
| reviewed_by | VARCHAR(64) | NULL | 审核人 oprid |
| reviewed_at | DATETIME | NULL | 审核时间 |
| created_at / updated_at | DATETIME | NOT NULL | 应用层显式维护 |

索引：idx_oprid_type(oprid, material_type)。

## 3. 导师配对（平台库·新建）

### 3.1 onboarding_mentor — 导师库

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| oprid | VARCHAR(64) | NOT NULL, UK uk_oprid | 导师工号（employee 存在性校验） |
| dept | VARCHAR(64) | NOT NULL | 部门（展示；来源 employee 联查核对） |
| expertise | VARCHAR(128) | NULL | 擅长领域 |
| is_active | TINYINT | NOT NULL DEFAULT 1 | 1-启用 0-停用（停用不可被新配对选择） |
| created_at / updated_at | DATETIME | NOT NULL | 应用层显式维护 |

说明：**带教负载不入库**——实时计算（pairing 表 ACTIVE 计数）。

### 3.2 onboarding_pairing — 配对记录

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| newcomer_oprid | VARCHAR(64) | NOT NULL | 新人域账号 |
| mentor_id | BIGINT | NOT NULL, idx | 关联 mentor.id |
| start_date | DATE | NOT NULL | 配对起始日（=入职日） |
| pairing_status | VARCHAR(16) | NOT NULL DEFAULT 'ACTIVE' | ACTIVE/CLOSED |
| active_flag | TINYINT | NULL | ACTIVE=1 / CLOSED=NULL（**唯一索引技巧**：MySQL 唯一索引不约束 NULL） |
| closed_at | DATETIME | NULL | 解除时间 |
| created_by | VARCHAR(64) | NOT NULL | 操作人（HR） |
| created_at / updated_at | DATETIME | NOT NULL | 应用层显式维护 |

索引：**uk_active(newcomer_oprid, active_flag)**——同新人同时至多 1 个 ACTIVE（数据库兜底 + 应用层拦截双保险）、idx_mentor(mentor_id)。

### 3.3 onboarding_feedback — 配对互评

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| pairing_id | BIGINT | NOT NULL, idx | 关联 pairing.id |
| direction | VARCHAR(24) | NOT NULL | MENTOR_TO_NEWCOMER / NEWCOMER_TO_MENTOR |
| star | TINYINT | NOT NULL | 1-5 星 |
| content | VARCHAR(500) | NULL | 评价文本 |
| created_by | VARCHAR(64) | NOT NULL | 评价人 oprid |
| created_at | DATETIME | NOT NULL | 提交时间（**不可修改/删除**） |

索引：**uk_pairing_direction(pairing_id, direction)**——每方向至多 1 条。

## 4. 培训提醒（平台库·新建）

### 4.1 onboarding_course — 培训课程

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| course_name | VARCHAR(128) | NOT NULL | 课程名称 |
| lecturer | VARCHAR(64) | NOT NULL | 讲师 |
| course_time | DATETIME | NOT NULL | 开课时间（PUBLISHED 后不可改） |
| location | VARCHAR(128) | NULL | 地点 |
| quota | INT | NOT NULL | 名额（≥1；PUBLISHED 后不可改） |
| course_status | VARCHAR(16) | NOT NULL DEFAULT 'DRAFT' | DRAFT/PUBLISHED/DISABLED |
| created_at / updated_at | DATETIME | NOT NULL | 应用层显式维护 |

### 4.2 onboarding_enrollment — 报名登记

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| course_id | BIGINT | NOT NULL, idx | 关联 course.id |
| oprid | VARCHAR(64) | NOT NULL | 报名人工号（employee 存在性校验） |
| enrolled_at | DATETIME | NOT NULL | 登记时间 |
| created_by | VARCHAR(64) | NOT NULL | 登记操作人（HR） |

索引：**uk_course_oprid(course_id, oprid)**——重复登记拦截；报名数≤名额由应用层校验（先查后写防超额）。

### 4.3 onboarding_reminder_log — 提醒发送记录

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键（**幂等键**——重推不产生重复消息） |
| reminder_type | VARCHAR(24) | NOT NULL | PAIRING_NOTICE / TASK_REMINDER / MATERIAL_REJECTED |
| receiver_oprid | VARCHAR(64) | NOT NULL | 接收人工号 |
| content_summary | VARCHAR(200) | NOT NULL | 内容摘要（落库即摘要，便于列表展示） |
| reminder_status | VARCHAR(16) | NOT NULL DEFAULT 'PENDING' | PENDING/SENT/FAILED |
| fail_reason | VARCHAR(200) | NULL | 失败原因（OA 回执可读映射：不可达/鉴权失败/限流） |
| retry_count | INT | NOT NULL DEFAULT 0 | 重推次数 |
| sent_at | DATETIME | NULL | 发送成功时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |

### 4.4 onboarding_operation_log — 材料操作日志（隐私红线留痕）

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | PK AUTO_INCREMENT | 主键 |
| oprid | VARCHAR(64) | NOT NULL | 操作人 |
| action | VARCHAR(24) | NOT NULL | MATERIAL_APPROVE / MATERIAL_REJECT / MATERIAL_DOWNLOAD |
| target_oprid | VARCHAR(64) | NOT NULL | 材料归属人 |
| material_id | BIGINT | NOT NULL | 关联 material.id |
| created_at | DATETIME | NOT NULL | 操作时间 |

说明：**只增不改不删**（审计用途）；审核与下载必写（主文档 §三.6 强制）。
