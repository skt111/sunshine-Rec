# 供给端雷达 API 契约（v1.0）

> 定位：`task-001` 实现与联调的**唯一接口契约**。需求来源：需求包 v1.0 §6.2 / §3.3 / 附录A。
> 冻结日期：2026-10-26 ｜ 变更须走 CR（禁止注释登记式偏差）
> 基础路径：`/api/supply-radar`（前端 dev 经 vue.config.js `/api` proxy）

## 0. 通用约定

### 0.1 响应壳（唯一）

```json
{ "status": "success", "message": "", "result": { } }
```

| status | 含义 | HTTP |
|--------|------|------|
| `success` | 业务成功 | 200 |
| `fail` | 业务失败（参数/不存在/异常） | 200（服务异常可 500） |
| `401` | 未登录 | 200 或 401 |
| `403` | 无权限 | 200 或 403 |
| `304` | 会话/权限跳转（沿用现有） | 200 |

### 0.2 分页结构

```json
{ "status":"success","message":"","result":{ "list":[], "total":0, "page":1, "size":20, "totalPages":0 } }
```

- `page ≥ 1`；`1 ≤ size ≤ 100`（越界 → `status=fail`）
- `sortBy ∈ {eventDate, lastSeenAt, sourcePublishedAt}`；`sortDirection ∈ {asc, desc}`，默认 `eventDate asc`
- `event_date IS NULL` 排序置尾：`ORDER BY (event_date IS NULL), event_date ASC`

### 0.3 字段命名

- JSON 字段一律 `camelCase`；DB 列 `snake_case`
- 非同名映射显式 DTO：`university_site.id → schoolId`、`crawl_status → lastCrawlStatus`

### 0.4 枚举（单源：Java enum + 前端 constants）

| 枚举 | 取值 |
|------|------|
| `eventType` | TALK / JOB_FAIR / RECRUITMENT_EVENT / EMPLOYMENT_SERVICE / OTHER_EMPLOYMENT_ACTIVITY / UNKNOWN |
| `venueType` | ON_CAMPUS / OFF_CAMPUS / ONLINE / HYBRID / UNKNOWN |
| `standardCategory` | ON_CAMPUS_TALK / OFF_CAMPUS_TALK / ON_CAMPUS_FAIR / OFF_CAMPUS_FAIR / OTHER / PENDING_REVIEW |
| `eventStatus` | UPCOMING / ONGOING / FINISHED / CANCELLED / POSTPONED / UNKNOWN |
| `verificationStatus` | PENDING / VERIFIED / REJECTED / NEED_REVIEW |
| `crawlTaskStatus` | PENDING / RUNNING / SUCCESS / PARTIAL / FAILED |
| `schoolCrawlStatus` | NEVER / SUCCESS / PARTIAL / FAILED |

> **页签 4 个（v1.1）**：全部活动 / 宣讲会 / 双选会 / 其他活动 —— 按 `eventType` 聚合，**不再区分校内/校外**（`standardCategory` 仅保留作展示标签，不参与页签筛选）。
> 「其他活动」= `excludeEventTypes=TALK,JOB_FAIR`。

### 0.5 鉴权

- 除公开探测外，全部端点需登录态；无 token → `status=401`
- 前端使用**独立 axios 实例**（不挂全局拦截器），自行注入 `token`/`userToken`

---

## 1. `GET /api/supply-radar/schools`

**入参**：`city`（可选）、`keyword`（可选）、`enabled`（可选 0/1）

**result**：数组
```json
[{ "schoolId":1,"schoolCode":"PKU","schoolName":"北京大学","schoolCity":"北京","province":"北京",
   "siteUrl":"https://scc.pku.edu.cn/home.action","adapterType":"GENERIC","enabled":1,
   "lastCrawlAt":"2026-10-26 08:00:00","lastCrawlStatus":"SUCCESS","lastFailReason":null }]
```

**一致性**：强一致；**安全**：参数化。

---

## 2. `GET /api/supply-radar/events`

**入参**：`schoolIds`（多值/逗号）、`city`、`category`、`eventType`、`venueType`、`startDate`、`endDate`、`status`、`verificationStatus`、`keyword`、`page`、`size`、`sortBy`、`sortDirection`、`groupBy`（可选 `school`）

**参数语义（强制）**：
- `category=OTHER` **等价于** `standard_category IN ('OTHER','PENDING_REVIEW')`（与页签计数口径一致）
- `category=ALL` 或不传 = 不限制分类
- `startDate` / `endDate` 格式 `yyyy-MM-dd`（闭区间，基于 `event_date`）
- `keyword` 匹配 `event_title` 与 `school_name`（LIKE，参数化）
- `groupBy` 仅接受 `school` 或不传；其他值 → `status=fail`
- `schoolIds` 支持逗号分隔字符串或重复参数

### 2.1 默认（列表）模式

`result`：分页结构，`list` 元素：
```json
{ "id":10001,"schoolId":6,"schoolName":"北京大学","schoolCity":"北京",
  "eventTitle":"2027 届校园招聘宣讲会","eventType":"TALK","venueType":"ON_CAMPUS",
  "standardCategory":"ON_CAMPUS_TALK","eventDate":"2026-10-27","startTime":"14:00","endTime":"16:00",
  "venueName":"校内某教学楼","eventAddress":null,"eventStatus":"UPCOMING",
  "verificationStatus":"PENDING","sourceUrl":"https://...","crawledAt":"2026-10-26 08:00:00",
  "lastSeenAt":"2026-10-26 08:00:00" }
```
`total` = **活动总数**

### 2.2 `groupBy=school` 分组模式

`list` 元素：
```json
{ "schoolId":6,"schoolName":"北京大学","schoolCity":"北京","eventCount":128,
  "lastCrawlAt":"2026-10-26 08:00:00","lastCrawlStatus":"SUCCESS",
  "recentEvents":[{"id":10001,"eventTitle":"...","eventDate":"2026-10-27","standardCategory":"ON_CAMPUS_TALK"}] }
```
- 外层仍为分页结构：`{ list, total, page, size, totalPages }`
- `total` = **高校组数**
- `recentEvents` 最多 3 条；批量反查（禁 N+1）
- 分组卡片「展开」→ 调本端点**默认模式**（携带 `schoolIds={id}` + 当前 `category` + 其余筛选）

**错误**：`size` 越界/`sortBy` 非法 → `status=fail`

---

## 3. `GET /api/supply-radar/statistics`

**入参**：同 `events` 的主要筛选（不含 `page/size/sort`）

**result**：
```json
{ "monitoredSchoolCount":53,"totalEventCount":12860,"recentTalkCount":1204,"recentFairCount":386,
  "pendingVerificationCount":72,"lastCrawlAt":"2026-10-26 08:00:00","lastCrawlTaskStatus":"PARTIAL",
  "categoryCounts":{"all":12860,"onCampusTalk":982,"offCampusTalk":222,"onCampusFair":301,"offCampusFair":85,"other":72} }
```

- `lastCrawlTaskStatus` = 任务级（`crawlTaskStatus` 枚举）
- `categoryCounts.other` = `OTHER` + `PENDING_REVIEW`
- 口径：附录A Q1~Q4；**统计与列表必须共用 `EventQueryBuilder`**

---

## 4. `GET /api/supply-radar/events/{id}`

**result**：完整活动字段（**11 项详情要素**）+ `changes[]`

| 详情块 | 字段 |
|--------|------|
| 基本信息 | `eventTitle`、`schoolName`、`schoolCity` |
| 类型与分类 | `eventType`、`venueType`、`standardCategory` |
| 日期时间 | `eventDate`、`startTime`、`endTime` |
| 举办地点 | `venueName`、`eventAddress` |
| 主办与企业 | `organizer`、`companyNames`（逗号分隔） |
| 报名信息 | `registrationDeadline`、`registrationUrl` |
| 活动说明 | `eventDescription` |
| 来源 | `schoolName`、`sourceUrl`、`sourceListUrl` |
| 采集 | `crawledAt`、`lastSeenAt` |
| 核验 | `verificationStatus` |
| 变更 | `changes[]` |

```json
{ "...": "上述字段",
  "changes":[{ "changeType":"UPDATED","fieldName":"event_date","oldValue":"2026-10-26","newValue":"2026-10-27","changedAt":"2026-10-26 08:00:00" }] }
```
**异常**：不存在 → `status=fail`，`message="活动不存在"`

---

## 5. `POST /api/supply-radar/crawl-tasks`

**入参**：
```json
{ "taskType":"ALL","schoolIds":[],"startDate":null,"endDate":null }
```
`taskType ∈ {ALL, SPECIFIED}`（触发来源由 `triggered_by` 区分：用户名 / `SCHEDULED`）

**result**：`{ "taskId": 123, "status": "PENDING" }`

**语义**：受理 ≠ 完成；异步执行，终态以 `/crawl-tasks/{taskId}` 为准
**幂等**：MySQL `GET_LOCK('supply_radar_crawl',0)` 全局锁；锁被占 → 返回当前 RUNNING 的 `taskId`（不重复启动）
**一致性**：任务级最终一致；单活动 upsert + history 强一致（同事务）

---

## 6. `GET /api/supply-radar/crawl-tasks/{taskId}`

**result**：
```json
{ "taskId":123,"taskType":"ALL","status":"PARTIAL","startedAt":"...","finishedAt":"...",
  "totalCount":54,"successCount":51,"failCount":3,"errorSummary":"..." }
```
**轮询**：前端 3s/次、最多 100 次、`document.hidden` 暂停
**超时回收**：RUNNING 超 60 分钟 → `CrawlTaskLocker.recoverStale()` 置 FAILED

---

## 7. `GET /api/supply-radar/crawl-status`

**入参**：`status`（可选）

**result**：
```json
[{ "schoolId":6,"schoolName":"北京大学","schoolCity":"北京",
   "lastCrawlAt":"2026-10-26 08:00:00","lastCrawlStatus":"SUCCESS","lastFailReason":null }]
```
`lastCrawlStatus` = 高校级（`schoolCrawlStatus` 枚举）

---

## 8. 变更记录

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-10-26 | 初版：7 端点 + `groupBy=school` 变体 + 响应壳/分页/枚举口径 |
| v1.1 | 2026-10-27 | 页签 6→4（按 eventType，不区分校内校外）；新增 `excludeEventTypes`（CSV）；statistics 新增 `typeCounts`（all/talk/jobFair/other）；`keyword` 同时匹配 `event_title` 与 `company_name`；`sortBy` 实际支持 `registrationDeadline`（前端未用，保留后端能力） |
