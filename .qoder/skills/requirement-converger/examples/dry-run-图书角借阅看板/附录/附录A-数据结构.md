# 附录A：数据结构

> **配套材料**：本附录为《图书角借阅看板需求文档》的附录A，提供数据表字段级定义。
> **数据源分区**：§1 为 HR 库现存表（**只读**）；§2 为本应用新建表（本次唯一 DDL 授权范围）。
> **来源标注**：§1 来源为 HR 库 DDL（由 DBA 提供现网脚本核对——留白 L3）；§2 为本需求新定义。

---

## 1. HR 库现存表（只读）

### 1.1 employee — 员工信息表

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| oprid | VARCHAR(64) | PK | 员工域账号（全系统唯一标识，借阅人以此关联） |
| emp_name | VARCHAR(64) | NOT NULL | 员工姓名 |
| dept_name | VARCHAR(128) | 可空 | 所属部门（展示用） |
| email | VARCHAR(128) | 可空 | 邮箱（OA 通知兜底渠道预留） |

索引：uk_oprid(oprid)。

> **来源**: HR 库 DDL 提取（摘录最小字段集）；字段级与现网一致性核对为留白 L3（DBA 提供现网脚本，核对后方可定稿）。
> **读写**: 🚨 只读——本应用禁止任何写入/DDL 操作。

---

## 2. 本应用新建表（DDL 授权范围）

### 2.1 book_loan — 借阅记录表

| 字段名 | 类型 | 约束/默认值 | 说明 |
|--------|------|------------|------|
| id | BIGINT | AUTO_INCREMENT PK | 主键 |
| book_id | VARCHAR(64) | NOT NULL | 图书编号（图书角实体书唯一编号） |
| book_name | VARCHAR(128) | NOT NULL | 书名（登记时录入快照） |
| borrower_oprid | VARCHAR(64) | NOT NULL（索引 idx_borrower） | 借阅人员工账号（关联 employee.oprid，应用层校验存在性） |
| borrow_date | DATE | NOT NULL | 借出日期 |
| due_date | DATE | NOT NULL | 应还日期（默认为借出日期 + 14 天，可修改） |
| return_date | DATE | 可空 | 实际归还日期（NULL=未归还） |
| status | VARCHAR(16) | NOT NULL DEFAULT 'BORROWED'（索引 idx_status） | 借阅状态：BORROWED-在借 / RETURNED-已归还 |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | 更新时间（应用层显式写入） |

索引：idx_status(status)、idx_borrower(borrower_oprid)、idx_due(due_date)。

**在借唯一性约束（应用层）**：同一 book_id 且 status='BORROWED' 至多一条——由服务层在借出登记时拦截（“该书未归还，禁止重复借出”）；重复归还同一记录返回幂等成功（见主文档 §六 行为要求 1）。

**枚举值**：status ∈ {BORROWED, RETURNED}（全枚举，无其他取值）。
