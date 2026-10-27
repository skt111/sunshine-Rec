---
id: task-001
title: 供给端雷达：高校招聘活动采集与展示
created: 2026-10-26
updated: 2026-10-26
exploration: null
contracts:
  - ".qoder/api-contract/supply-radar-api.md"
---

# task-001 供给端雷达：高校招聘活动采集与展示

> 需求来源包: `.qoder/docx/供给端雷达-需求包/` **v1.0**（REQ-STATS verdict=pass）
> 意图文档: `.qoder/intents/supply-radar-university-recruitment-crawl.md`（status=confirmed）
> 复杂度: **9/10（复杂）** ｜ 质量评分: **23/24（绿灯）** ｜ 验收级别: **L3**
> **修订状态**: R1 修复 7 项 P0 → 复验 P0=2（评分卡数字、前端 401）→ **R2 已修复**

## 一、需求分析

在现有「阳光校招情报与人才战略决策系统」中，把已存在的**供给端雷达 Mock 页面**改造为真实数据页面，并新建后端采集与查询能力：自动采集 54 所高校就业网公开的宣讲会/双选会/就业活动，入库 MySQL `sunshine`，通过 REST 端点供前端展示。

### 1.1 关键现状偏差（规划依据）

| # | 需求包假设 | 实际现状（证据） | 处置 |
|---|-----------|----------------|------|
| 1 | 新建页面 + 顶部页签 | `dashboard/SupplyRadar.vue` 已存在（单行压缩源码，纯 Mock），由 `MainPage.vue:9` 按 `activeNav` 挂载；导航为 `DashboardSidebar.vue:11` **硬编码 items** | 按 PRD §2.1「已存在则直接接入」→ **改造现有页面**；菜单项由用户在后台配置（不动硬编码 sidebar） |
| 2 | 分类页签 6 个 | 现有 Mock 为 7 个（`PENDING_REVIEW` 单列，`mockSupplyRadar.js:31` 有 7 个 label 键） | ✅ 已裁决 A：改 **6 个页签**；`PENDING_REVIEW` 并入「其他活动」，但**保留其展示标签**（卡片/详情仍需显示） |
| 3 | `eventStatus` 枚举 | 现有中文「未开始/进行中/待核验」 | 改造对齐枚举；`'待核验'` 不是合法 eventStatus，改由 `verificationStatus` 承载 |
| 4 | `sourcePublishedAt`/`lastSeenAt` | 现有 `publishedAt`/`updatedAt` | 改造对齐契约 |
| 5 | 数据来自后端 | 现有 `mockSupplyRadar.js` 纯 Mock；`pageSize=8` 本地分页 | 移除 Mock 引用，接真实 API + 后端分页 |
| 6 | 后端已有业务框架 | `Main.java` 无 `@SpringBootApplication`；`application.yml` 无 datasource；pom 无 jsoup/playwright；且 pom 含 **mybatis-plus Boot2 starter / dynamic-datasource / Nacos / Redis / Spring AI**，mainClass 指向不存在的 `TicketCoreApplication` | W0 补骨架 + **处置既有自动配置**（见 P1-4 修复） |
| 7 | 前端 401 跳转可用 | `router.js:13-85` 中 `sessionFail`/`Authority` 路由**整段被注释**；`axios.js:75-79` 会 `router.replace({name:'sessionFail'})` 抛错 | 改用**封装层兜底提示**（不依赖命名路由），见 P3 |

## 二、需求基线（S5 调研产物）

```yaml
需求基线:
  域引用: "域缺口登记（.qoder/context/ 为空，无 domain-index.json / business-architecture.md）"
  复用决策: "PARTIAL — 复用 dashboard/SupplyRadar.vue（UI 骨架/交互/样式）+ SupplyDetailDrawer.vue（抽屉骨架）+ dashboard.css；改造：Mock 移除、字段对齐、统计改后端、新增采集面板、组件拆分。asset-catalog 缺失，复用裁决依据代码实读。"
  风格声明: "未生成（style-profile.md 缺失）——以 dashboard.css + 需求包附录C §5 设计令牌为准"
  表清单: "university_site, employment_event, crawl_task, employment_event_history"
  确认记录: ["Step 0.5 intent 建档确认（2026-10-26）", "Step 2 资产复用确认（2026-10-26，菜单=后端数据驱动）", "Step 4 表清单确认（2026-10-26，MySQL sunshine + 附录A 4 表）", "分类页签裁决（2026-10-26，A=6 个页签）", "R1 修复确认（2026-10-26，7 项 P0 全部处置）"]
```

## 三、复杂度评估与拆分判定

| 维度 | 判断 | 分 |
|------|------|----|
| 跨端范围 | 前端 + 后端（2 端） | 1 |
| 文件影响 | 后端 ~40 + 前端 ~10 > 8 | 2 |
| 新实体/表 | 4 张 | 2 |
| 业务规则 | 状态机 + 分类规则 + 54 站点外部联动 | 2 |
| 安全影响 | 外部采集 + 登录态校验 | 2 |
| **总分** | **9 / 10 → 复杂** | |

**硬触发**：新建 4 表 ✓ / ≥3 独立功能点 ✓ / 外部系统对接 ✓ → 拆分成立。

### 3.1 依赖图（R1 修复：消除 P1↔P2 双向依赖）

```
P0 基础设施（共享层，最先）
 ├─ Result / 异常 / 登录拦截
 ├─ enums(7) / entity(4) / mapper(4)        ← 实体与 Mapper 上移 P0（原在 P2，导致 P1 无法编译）
 ├─ CrawlTrigger 接口 + TaskTriggerResult + NoopCrawlTrigger
 │    签名：`TaskTriggerResult trigger(Long taskId, String taskType, List<Long> schoolIds)`
 │    装配：`CrawlExecutor implements CrawlTrigger`（P1）；NoopCrawlTrigger 由 `@Configuration` 的 `@Bean @ConditionalOnMissingBean(CrawlTrigger.class)` 声明（P0）
 │    `TaskTriggerResult` 定义于 `crawler/CrawlTrigger.java`（嵌套类）
 ├─ CrawlProperties（8 个 `supply-radar.crawl.*` 键绑定）← **上移 P0**（P1-N2：P2 调度器需读 cron/enabled）
 ├─ SQL 复用 supply-radar-init.sql（已执行）
 └─ 前端 api/supplyRadar.js + constants/supplyRadar.js + /api proxy
        │
        ├──► P1 采集层（仅依赖 P0）
        └──► P2 API 层（仅依赖 P0；CrawlTrigger 由 P1 提供实现，缺失时 Noop 兜底）
                     │
                     └──► P3 前端改造（依赖 P0 + P2）
```

> **CrawlScheduler 归属 P2**（R2 修复 P1-13）：调度器需要「创建任务 + 获取全局锁」（属 `CrawlTaskService`/`CrawlTaskLocker`），若放 P1 会造成 P1→P2 依赖回潮。故 P1 只提供 `CrawlExecutor`（执行单次任务体），P2 提供调度与任务创建。

| 子阶段 | 依赖数 | 说明 |
|--------|--------|------|
| P0 | 0 | 共享资产最早交付 |
| P1 | 1（P0） | 用 P0 的 entity/mapper/enums 写库 |
| P2 | 1（P0） | 用 P0 的 entity/mapper + `CrawlTrigger` 接口 |
| P3 | 2（P0 + P2） | 用 P0 的 api 封装 + P2 的端点契约 |

> 依赖数全部 ≤2，符合 x-rules.dependencyLimit；P1/P2 **编码可并行**，P2 启动采集时若无 P1 实现则走 `NoopCrawlTrigger`（返回"采集模块未就绪"），不阻塞 API 开发。

**执行形态**：单会话串行推进（P0→P1→P2→P3），不启用 worktree 并行。

## 四、探索引用

N/A — 数据建模直接采用需求包 `附录A-数据结构.md`（L1/L2/L3 + 可执行 DDL），DDL 已执行入库。

## 五、现状扫描（证据）

| 层 | 文件:位置 | 发现 |
|----|----------|------|
| 前端入口 | `views/mainPage/MainPage.vue:9` | `SupplyRadar` 按 `activeNav` 挂载；`openSource` 用 `window.open`（缺 `rel`） |
| 前端页面 | `dashboard/SupplyRadar.vue:1-13` | **单行压缩源码**，纯 Mock；`pageSize=8` 本地分页；`schoolGroups` 本地聚合 |
| 前端抽屉 | `dashboard/SupplyDetailDrawer.vue:1,3` | 依赖 mock `categoryLabels`；`companyNames.join('、')` |
| 前端导航 | `dashboard/DashboardSidebar.vue:11` | 硬编码 items（非后端 menuList） |
| 前端路由 | `router/generateRouter.js:19-44`、`router.js:13-85,91` | `sessionFail`/`Authority` 路由被注释 |
| 请求层 | `plugins/axios.js:13,31-34,75-79` | `baseURL='/'`；注入 token；401/403 跳命名路由（会抛错） |
| API 封装 | `api/dataRequst.js:41-62` | `getList` 在无配置时走 SM4 分支，路径拼接有风险 |
| 构建 | `vue.config.js:88-103` | 仅代理 `/ehr`，**无 `/api`** |
| 后端入口 | `java/org/example/Main.java:3` | **无 `@SpringBootApplication`** |
| 后端依赖 | `pom.xml:117-132,191-195,202-207,223-226,265-275,306` | Nacos/dynamic-datasource/mybatis-plus(Boot2)/Redis/Spring AI；mainClass 错误；**无 jsoup/playwright** |
| 后端配置 | `application.yml:1-33` | **无 datasource**；有 Spring AI + Redis vectorstore |
| 数据层 | `resources/sql/supply-radar-init.sql` | 4 表 DDL + 54 校 INSERT，**已执行** |
| 测试 | `java_bff/src/test` | **不存在** |

## 六、实现路径

**推荐**：P0 冻结共享层 → P1/P2 编码并行 → P3 收口。证据：P0 交付 `entity/mapper/enums/Result/CrawlTrigger/api 封装` 是 P1/P2/P3 的唯一契约面（附录A + 需求 §3.3）。

**备选**：单波次全量。不推荐——P1 采集调优（54 站点）会阻塞 P2/P3 验证。

## 七、API 契约草案

> 引用需求包 v1.0 §6.2 冻结的 **7 个端点**（单源化）。

| # | 端点 | 用途 |
|---|------|------|
| 1 | `GET /api/supply-radar/schools` | 高校列表 |
| 2 | `GET /api/supply-radar/events` | 活动列表（分页）**+ `groupBy=school` 分组模式**（R1 修复 P0-3） |
| 3 | `GET /api/supply-radar/statistics` | 统计指标 |
| 4 | `GET /api/supply-radar/events/{id}` | 活动详情 + `changes[]` |
| 5 | `POST /api/supply-radar/crawl-tasks` | 启动采集 |
| 6 | `GET /api/supply-radar/crawl-tasks/{taskId}` | 查询任务 |
| 7 | `GET /api/supply-radar/crawl-status` | 采集状态 |

**`groupBy=school` 模式（不新增端点，保持 7 个）**：
- 入参同 `events`（`schoolIds` 可选；分组模式下作为过滤而非分组键）
- `result`：`{ list: [ { schoolId, schoolName, schoolCity, eventCount, lastCrawlAt, lastCrawlStatus, recentEvents[3] } ], total }`
- **`total` 语义（R2 修复 P2-4）**：分组模式 = **高校组数**；列表模式 = 活动总数。两模式 `page/size` 分页作用于各自的 list
- **展开数据源（R2 修复 P1-14）**：分组卡片「展开」时调用 `GET /events?schoolIds={id}&category={当前页签}&{其余筛选}`，**不依赖 limited `recentEvents[3]`**
- 口径：附录A Q5（按 `school_id` 分组；前 3 条批量反查，禁 N+1）
- 内部两个模式**共用 `EventQueryBuilder`**（R1 修复 P1-8）
- **参数校验（R2 修复 P2-4）**：`page ≥ 1`；`1 ≤ size ≤ 100`（越界→`status=fail`）；`sortBy` 只能取 `eventDate|lastSeenAt|sourcePublishedAt`

**实现约束**：
- 响应壳 `{status,message,result}`；`status: success|fail|401|403|304`
- 分页 `result.{list,total,page,size,totalPages}`；`size` 默认 20
- 非同名映射显式 DTO（`id→schoolId`、`crawl_status→lastCrawlStatus`）
- 枚举单源（P0 `enums/` + 前端 `constants/supplyRadar.js`）
- 查询全参数化；`sortBy`/`sortDirection` 走**枚举白名单**（防注入）
- 未登录返回 `Result.status=401`（P0 `LoginInterceptor`）

## 八、运行配置契约（R1 修复 P1-7：12 键全量）

| 键 | 默认 | 敏感 |
|----|------|------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/sunshine?useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_general_ci&serverTimezone=Asia/Shanghai` | 否 |
| `spring.datasource.driver-class-name` | `com.mysql.cj.jdbc.Driver` | 否 |
| `spring.datasource.username` | 环境变量 `SPRING_DATASOURCE_USERNAME` | **是** |
| `spring.datasource.password` | 环境变量 `SPRING_DATASOURCE_PASSWORD` | **是** |
| `mybatis-plus.configuration.map-underscore-to-camel-case` | `true`（**需求 §9.1 冻结键，不得删**） | 否 |
| `supply-radar.crawl.enabled` | `true` | 否 |
| `supply-radar.crawl.cron` | `0 0 8,18 * * ?` | 否 |
| `supply-radar.crawl.request-interval-ms` | `2000` | 否 |
| `supply-radar.crawl.timeout-ms` | `30000` | 否 |
| `supply-radar.crawl.max-retry` | `2` | 否 |
| `supply-radar.crawl.max-schools-per-task` | `60` | 否 |
| `supply-radar.crawl.playwright-enabled` | `true` | 否 |
| `supply-radar.crawl.user-agent` | `SunshineRecBot/1.0 (+contact)` | 否 |

> 共 **13 行** = 需求 §9.1 冻结 12 键 + `driver-class-name`（附加）。字符集用 `characterEncoding=UTF-8` + `connectionCollation=utf8mb4_general_ci`（Connector/J 不接受 `characterEncoding=utf8mb4`）。
> `CrawlProperties` 仅绑定其中 **8 个 `supply-radar.crawl.*` 键**（其余为 datasource/mybatis 键，由 Boot 直接读取）。

**既有自动配置处置（R1 修复 P1-4 + R2 补强）**：
- `mybatis-plus-boot-starter`（Boot2 坐标）→ 改为 `mybatis-plus-spring-boot3-starter`（Boot3 兼容）
- Nacos config/discovery + bootstrap → 本地关闭：`spring.cloud.nacos.config.enabled=false`、`spring.cloud.nacos.discovery.enabled=false`，**必须加 `spring.cloud.nacos.config.import-check.enabled=false`**（否则 Boot 2.4+ 抛 `No spring.config.import`）
- Spring AI → 用**实键**关闭：`spring.ai.openai.chat.enabled=false`、`spring.ai.openai.embedding.enabled=false`、`spring.ai.vectorstore.redis.enabled=false`
- `dynamic-datasource` → 本次用 plain datasource，设 `spring.datasource.dynamic.enabled=false`；若无该开关则 `@SpringBootApplication(exclude=DynamicDataSourceAutoConfiguration.class)`
- `spring-boot-maven-plugin` mainClass → `org.example.SupplyRadarApplication`
- 🔴 **硬编码密钥清除（R2 修复 P1-11）**：`application.yml:11,30` 现有明文 `spring.ai.openai.api-key` / `llm.api-key` → 改为 `${LLM_API_KEY:}`，**并建议轮换该密钥**；P0 门禁加 grep：`sk-|api-key:\s*sk` 在受控文件命中数=0
- 每项处置在 P0 落地后须有**启动冒烟证据**（日志无自动配置报错）

**P0 出口硬门禁（R1 修复 P1-3，全部可执行）**：

| 门禁 | 命令/断言 | 期望 |
|------|----------|------|
| DB 连通 | `select 1` | 返回 1 |
| 表结构 | `show tables` 过滤 4 表 | =4 |
| 高校数据 | `select count(*) from university_site` | =54（其中 CFAU 无官网 `enabled=0`） |
| 启用数 | `... where enabled=1` | =53（54 − 外交学院） |
| 登录态（P0 出口） | `LoginInterceptorTest`（MockMvc 打探针端点） | 无 token → 401 |
| 登录态（P2 出口） | 无 token 请求 `/events` | `status=401` |
| Playwright | `mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`后执行 `PlaywrightSmokeTest`（启动并关闭 Chromium） | 无异常 |
| 外网 | 任取 3 校 HTTP GET | 至少 1 所 200 含正文 |
| 端口 | 8080/8088/3306 占用探测 | 先探占用者身份，禁盲杀 |
| 启动 | `mvn spring-boot:run` | 启动成功且无自动配置报错 |
| 密钥扫描 | grep `sk-|api-key:\s*sk` 于受控文件 | 命中数=0 |

> L2/L3 未达成时登记「挂起 + 责任方 + 解除证据」，**不得静默跳过**。

## 九、方案贯通审计与评分卡（R1 重评）

| 域 | 审计项 | 状态 | 得分 | 落点证据 |
|----|--------|------|------|---------|
| A | 表覆盖度 | ✅ | 2 | 附录A 4 表 ↔ §5.2；`supply-radar-init.sql:16-125` |
| A | 注释完整率 | ✅ | 2 | 执行版 SQL **全列 COMMENT**（`supply-radar-init.sql:16-125`） |
| A | 约束+命名 | ✅ | 2 | PK/UK/9 索引齐备，snake_case |
| B | 贯通度 | ✅ | 2 | 依赖图 §3.1 修复后闭环（采集写→API 读→前端展示） |
| B | 边界完整 | ✅ | 2 | §6.3 B1~B10 + §十 边界清单 |
| B | 契约对齐 | ✅ | 2 | §十四 13 项 Mock 映射 + #14 适配器补充 |
| C | 连通性 | ✅ | 2 | 流程 6 分组由 `groupBy=school` 承载（§七） |
| C | 完备性 | ✅ | 2 | 空态/错误/超时/降级（需求 §4.1.9、§11.2） |
| C | 自洽性 | ⚠️ | 1 | 页签 6 与 `PENDING_REVIEW` 展示标签已分离（§十四 #5）；R3 已消解配置键集（8 crawl 键）/依赖归属（CrawlProperties 上移 P0）/总分数字；保留 1 分保守 |
| D | 覆盖度（7 类） | ✅ | 2 | §十 场景矩阵 |
| D | 过度设计 | ✅ | 2 | 无 AI/推送/图表 |
| D | 拆分合理 | ✅ | 2 | §3.1 依赖图（依赖数 0/1/1/2，均 ≤2） |
| **总分** | | | **23/24** | 绿灯（≥19）｜plans.json 登记 **23** |

> 证据口径：A2 依据执行版 SQL 全列 COMMENT；B1 依据依赖图闭环；B3 依据 §十四 13 项映射表；C1 依据 `groupBy=school` 落点；D3 依据依赖数 0/1/1/2。C3 保留 1 分扣减（保守），**有效 23/24**。

## 十、场景矩阵与边界清单

| 类别 | 场景 | 预期行为 | 验证方式 |
|------|------|---------|---------|
| 正常 | 采集→入库→列表→详情 | 全链路可见 | L3 |
| 异常 | 单校 403 | 记失败，他校继续，任务 PARTIAL | L3 |
| 极限 | 100/页 + 10 万级 | 后端分页 <1s | L3 |
| 空值 | 无数据 / 字段缺失 | 空态文案 / 「暂无公开信息」 | L2 |
| 并发 | 重复点「立即更新」 | 返回既有 taskId（**MySQL `GET_LOCK` + 条件更新**） | L2 |
| 权限 | 无 token 访问 | `status=401` + 前端兜底提示 | L2 |
| 超时 | 采集超时 / 轮询 100 次 | 记失败 / 提示稍后查看 | L3 |

**边界清单**：

| 类别 | 边界 | 预期行为 | 验证 |
|------|------|---------|------|
| 边界值 | `eventDate IS NULL` | 仅「不限」可见，打「日期待核」；前端 null-safe 格式化 | L3 |
| 边界值 | 重复采集 | fingerprint 命中→更新，不新增 | L3 计数 |
| 边界值 | `venueType=HYBRID` | `PENDING_REVIEW` + `NEED_REVIEW` | L1 单测 |
| 依赖缺失 | Playwright 未装 | 降级 Jsoup，写 `last_fail_reason` | L2 日志 |
| 依赖缺失 | 外网部分不可达 | PARTIAL | L3 |
| 竞态 | 双请求建任务 | **MySQL `GET_LOCK('supply_radar_crawl',0)`**：**专用连接**持有，任务全程不归还连接池，`finally` 中 `RELEASE_LOCK` + `close`；锁被占→返回当前 RUNNING taskId | L2 |
| 崩溃恢复 | RUNNING 任务超 **60 分钟**（>NF-P3 的 30 分钟全量上限，防误杀） | `CrawlTaskLocker.recoverStale()`（由 `ApplicationRunner` + `CrawlScheduler` 调用）置 FAILED + `error_summary`，释放锁 | L3 手工模拟 |
| 排序边界 | `event_date IS NULL` 记录 | 排序按 `ORDER BY (event_date IS NULL), event_date ASC`，NULL 置尾（R2 修复 P2-5） | L1 单测 |

**反向用例**：若按「新建独立页面 + 顶部页签」实现 → 重复添加导航并重建已存在页面，违反 PRD §2.1 → **拒绝**。
**反向用例 2**：若按「前端本地聚合分组」实现 → 后端分页后仅覆盖当前页，且违反 R2「分页后端处理」→ **拒绝**。

## 十一、验收级别声明

**L3**（MySQL 已就绪 + 外部采集链路）。
被测形态：正常（DB+网络+Playwright）/ 降级（无 Playwright→纯 Jsoup）/ 受限（部分外网不可达→PARTIAL）。
缺 L3 环境：L0/L1/L2 本地必达，L3 登记挂起。

## 十二、任务拆解（R2 修复后）

### P0 基础设施与共享层（无依赖）
- [ ] `pom.xml`：+`jsoup 1.17.2` +`playwright 1.46.0`（版本锁定）；mybatis-plus 改 Boot3 starter；mainClass 改 `SupplyRadarApplication`
- [ ] `application.yml`：datasource(UTF-8+utf8mb4 collation) + **13 配置键** + 关闭 Nacos(含 import-check)/SpringAI(实键)/Redis-vector/dynamic + **删除明文 api-key 改 `${LLM_API_KEY:}`**
- [ ] `SupplyRadarApplication.java` ✚（`@SpringBootApplication` + `@MapperScan` + `@EnableScheduling` + `@EnableAsync`）
- [ ] `config/WebMvcConfig.java` ✚（注册登录拦截器）
- [ ] `common/LoginInterceptor.java` ✚（token 非空 + 格式校验；**若项目无 JWT 密钥则仅存在性校验，必须登记为临时方案/风险**，不宣称“沿用现有鉴权”）
- [ ] `common/Result.java` ✚、`common/GlobalExceptionHandler.java` ✚
- [ ] `config/AsyncConfig.java` ✚（有界线程池 + 拒绝策略）
- [ ] `supplyradar/enums/` ✚（EventType/VenueType/StandardCategory/EventStatus/VerificationStatus/CrawlTaskStatus/SchoolCrawlStatus）
- [ ] `supplyradar/entity/` ✚（4 实体）
- [ ] `supplyradar/mapper/` ✚（4 Mapper）
- [ ] `crawler/CrawlTrigger.java` ✚（含嵌套 `TaskTriggerResult`，签名 `trigger(Long taskId, String taskType, List<Long> schoolIds)`）+ `crawler/NoopCrawlTrigger.java` ✚（由 `@Configuration` 的 `@Bean @ConditionalOnMissingBean` 声明）
- [ ] `crawler/config/CrawlProperties.java` ✚（**8 个 `supply-radar.crawl.*` 键绑定**；上移 P0）
- [ ] `test/.../LoginInterceptorTest.java` ✚（MockMvc 探针 → 401）
- [ ] `test/.../PlaywrightSmokeTest.java` ✚（启闭 Chromium 冒烟）
- [ ] `resources/sql/supply-radar-init.sql`：**复核复用**（不新建 01/02）
- [ ] `frontend/src/api/supplyRadar.js` ✚（**独立 `axios.create({baseURL:'/api'})` 实例**，自行注入 token，**不挂全局拦截器**；捕获 401 → 提示“登录已失效”；7 方法显式 `params`）
- [ ] `.qoder/api-contract/supply-radar-api.md` ✚（7 端点 + groupBy 变体 + 响应壳/分页/枚举口径）
- [ ] `frontend/src/constants/supplyRadar.js` ✚（6 页签 + PENDING_REVIEW 展示标签 + 枚举中文映射）
- [ ] `vue.config.js`：+`/api` dev proxy
- [ ] **P0 出口门禁全部通过**（§八 表格）

### P1 采集层（依赖 P0）
- [ ] `crawler/adapter/{SiteAdapter,GenericAdapter,SpecificAdapter,AdapterRegistry}.java` ✚（**Registry 以 `school_code` 为键**；SPECIFIC 未实现→回退 GENERIC 并记日志）
- [ ] `crawler/fetch/PageFetcher.java` ✚（robots/Crawl-delay/间隔 2s/超时 30s/退避重试/charset 判定）
- [ ] `crawler/render/PlaywrightRenderer.java` ✚（降级处理）
- [ ] `crawler/discover/ColumnDiscoverer.java` ✚（关键词白名单扫描，E2）
- [ ] `crawler/parse/EventParser.java` ✚
- [ ] `crawler/clean/HtmlCleaner.java` ✚（B9）
- [ ] `crawler/classify/{EventClassifier,EventTypeResolver,VenueTypeResolver,EventStatusResolver}.java` ✚（§3.7.9 全 6 种 venueType）
- [ ] `crawler/dedup/FingerprintCalculator.java` ✚（D2 公式）
- [ ] `crawler/persist/EventUpserter.java` ✚（同事务 upsert + history）
- [ ] `crawler/CrawlExecutor.java` ✚（`implements CrawlTrigger`；单校失败隔离 + finally 终态）
- [ ] `test/.../{EventClassifierTest,EventUpserterTest}.java` ✚
- [ ] 9 校试采

### P2 API 层（依赖 P0）
- [ ] `supplyradar/query/{EventQuery,EventQueryBuilder}.java` ✚（统计/列表/分组共用）
- [ ] `supplyradar/dto/{SchoolVO,EventVO,EventDetailVO,ChangeVO,StatisticsVO,CrawlTaskVO,GroupVO}.java` ✚（**EventDetailVO 显式 11 字段**：含 `companyNames`/`organizer`/`registrationDeadline`/`registrationUrl`/`eventDescription`/`sourceListUrl`/`sourcePublishedAt`）
- [ ] `supplyradar/service/{SchoolService,EventService,GroupService,StatisticsService,CrawlTaskService}.java` ✚
- [ ] `supplyradar/service/CrawlTaskLocker.java` ✚（**MySQL `GET_LOCK` + 条件更新 + `recoverStale()` 60 分钟阈值**）
- [ ] `crawler/CrawlScheduler.java` ✚（**归 P2**：定时创建任务 + 调 `CrawlTrigger`）
- [ ] `supplyradar/config/StaleTaskRecoveryRunner.java` ✚（`ApplicationRunner` → `recoverStale()`）
- [ ] `supplyradar/controller/{SchoolController,EventController,StatisticsController,CrawlTaskController}.java` ✚（7 端点 + groupBy）
- [ ] `test/.../{StatisticsServiceTest,EventControllerTest,CrawlTaskServiceTest}.java` ✚

### P3 前端改造（依赖 P0 + P2）
- [ ] `dashboard/SupplyRadar.vue`：改为**编排容器**（Mock 移除、调 API）
- [ ] `dashboard/supply/SupplyRadarStats.vue` ✚（5 卡）
- [ ] `dashboard/supply/SupplyRadarFilter.vue` ✚（8 筛选 + 多选 + 重置）
- [ ] `dashboard/supply/SupplyRadarTabs.vue` ✚（6 页签 + 数量）
- [ ] `dashboard/supply/SupplyEventCard.vue` ✚（11 字段 + 三态）
- [ ] `dashboard/supply/SupplySchoolGroup.vue` ✚（分组视图 + 展开）
- [ ] `dashboard/supply/SupplyPager.vue` ✚（后端分页 + 3 排序）
- [ ] `dashboard/supply/CrawlTaskPanel.vue` ✚（任务 + 高校状态表）
- [ ] `dashboard/SupplyDetailDrawer.vue`：字段对齐（11 项 + `changes[]` + null-safe）
- [ ] `dashboard/mockSupplyRadar.js`：隔离（不再引用）
- [ ] `MainPage.vue`：`openSource` 改 `window.open(url,'_blank','noopener,noreferrer')`
- [ ] **401 验收点**：模拟无 token → 独立 axios 实例捕获并提示，**不触发全局 `router.replace('sessionFail')`**
- [ ] **轮询验收点**：3s/次、最多 100 次、`document.hidden` 暂停
- [ ] 单 `.vue` ≤400 行检查

## 十三、文件面清单（R2 修复）

| 阶段 | 文件（✚=新建） |
|------|---------------|
| P0 | `pom.xml`、`resources/application.yml`、`SupplyRadarApplication.java`✚、`config/WebMvcConfig.java`✚、`config/AsyncConfig.java`✚、`common/{Result,GlobalExceptionHandler,LoginInterceptor}.java`✚、`supplyradar/enums/*.java`✚(7)、`supplyradar/entity/*.java`✚(4)、`supplyradar/mapper/*.java`✚(4)、`crawler/{CrawlTrigger,NoopCrawlTrigger}.java`✚、`crawler/config/CrawlProperties.java`✚、`test/.../{LoginInterceptorTest,PlaywrightSmokeTest}.java`✚、`frontend/src/api/supplyRadar.js`✚、`frontend/src/constants/supplyRadar.js`✚、`vue.config.js`、`.qoder/api-contract/supply-radar-api.md`✚ |
| P1 | `org/example/crawler/` 下**除** `CrawlTrigger.java`/`NoopCrawlTrigger.java`/`config/CrawlProperties.java`/`CrawlScheduler.java` 之外的其余文件✚（约 13 文件）、`test/.../{EventClassifierTest,EventUpserterTest}.java`✚ |
| P2 | `org/example/supplyradar/{query,dto,service,controller,config}/**`✚（约 21 文件）、`crawler/CrawlScheduler.java`✚、`test/.../{StatisticsServiceTest,EventControllerTest,CrawlTaskServiceTest}.java`✚ |
| P3 | `dashboard/SupplyRadar.vue`、`dashboard/supply/*.vue`✚(7)、`dashboard/SupplyDetailDrawer.vue`、`dashboard/mockSupplyRadar.js`、`views/mainPage/MainPage.vue` |

> P0 已含 entity/mapper，P1/P2 文件面**零相交**；P3 仅触 dashboard 与 MainPage。

## 十四、Mock → 契约字段映射表（13 项 Mock 映射 + #14 适配器补充）

| # | 现状（Mock） | 契约要求 | 类型/NULL 策略 | 改造文件 |
|---|-------------|---------|---------------|---------|
| 1 | `eventStatus:'未开始'` 等中文 | `UPCOMING/ONGOING/FINISHED/CANCELLED/POSTPONED/UNKNOWN` | 枚举映射（前端常量） | SupplyEventCard / Drawer |
| 2 | `eventStatus:'待核验'`（非法值） | 归属 `verificationStatus=NEED_REVIEW` + `standardCategory=PENDING_REVIEW` | 拆分两字段 | 卡片/抽屉 |
| 3 | `venueType:'线上/线下/线上线下混合'` | `ONLINE/ON_CAMPUS/OFF_CAMPUS/HYBRID/UNKNOWN` | 枚举映射 | 卡片/抽屉 |
| 4 | `verificationStatus:'待人工核验/已核验'` | `PENDING/VERIFIED/REJECTED/NEED_REVIEW` | 枚举映射 | 抽屉 |
| 5 | `categoryLabels` 7 键 | 6 页签 + `PENDING_REVIEW` **展示标签保留** | 常量拆分：`TABS`(6) / `CATEGORY_LABELS`(7) | constants/supplyRadar.js |
| 6 | `companyNames: []` 数组 | `VARCHAR(1024)` 逗号分隔字符串 | 展示前归一：`split(/[，,、\s]+/)`（R2 修复 P2-7） | Drawer |
| 7 | `eventDate` 恒有值 `.slice(5)` | 允许 NULL | null-safe 显示「日期待核」 | 卡片/抽屉 |
| 8 | 详情直接用列表对象 | `GET /events/{id}` + 11 项 | 异步加载 + loading | Drawer |
| 9 | `pageSize: 8` 本地分页 | 默认 20，后端分页 | 删本地 slice | Pager |
| 10 | 5 个筛选 | 8 筛选 + `schoolIds` 多选 + 3 排序 | 新增城市/形式/核验/多选 | Filter |
| 11 | `changeNote` 字符串 | `changes[]`（changeType/fieldName/oldValue/newValue/changedAt） | 列表渲染 | Drawer |
| 12 | `openSource` → `window.open` 无 rel | `rel="noopener noreferrer"` | 改 `window.open(url,'_blank','noopener,noreferrer')` | MainPage |
| 13 | `publishedAt` / `updatedAt` | `sourcePublishedAt` / `lastSeenAt` | 字段重命名 | 卡片/抽屉 |
| 14 | `adapter_type='SPECIFIC'` 但 `adapter_key=NULL` | Registry 按 `school_code` 分派 | 回填或按 code 解析（R2 修复 P1-12） | P1 AdapterRegistry |

## 十五、修复轮次记录（append-only）

| 轮次 | 日期 | 触发方 | P0 数 | 处置 |
|------|------|--------|-------|------|
| R1 | 2026-10-26 | sandbox | 7 | 修复 P0-1~P0-7（依赖图/评分卡/分组/登录态/枚举/SQL/组件拆分）并重评 qualityScore=22 |
| R2 | 2026-10-26 | sandbox 复验 | 2 | 修复 P0-2（合计 24→23、三值统一）/ P0-4（独立 axios 实例绕开全局 401）；并修复 P1-1/4/7/11/12/13/14/15 与 P2-3/4/5/7/8；qualityScore=23 |
| R3 | 2026-10-26 | sandbox 复验 | 0 | **P0=0 通过**；同步修复深化项 P1-N1（401 门禁分阶段）/ N2（CrawlProperties 上移 P0）/ N3（签名加 taskId）/ N4（GET_LOCK 连接生命周期）与 P2-N1~N10 |

### R1 处置明细

| P0 | 修复落点 |
|----|---------|
| P0-1 | §3.1 依赖图：entity/mapper 上移 P0；新增 `CrawlTrigger` 接口解耦 P1↔P2；依赖数统一 0/1/1/2 |
| P0-2 | §九 重评（并显式说明 C3 保守扣分，plans.json 记 22） |
| P0-3 | §七 `GET /events?groupBy=school`；P2 增 `GroupService/GroupVO`；P3 增 `SupplySchoolGroup.vue` |
| P0-4 | P0 增 `LoginInterceptor`+`WebMvcConfig`；P3 用封装层 401 兜底（不依赖 sessionFail 命名路由） |
| P0-5 | P0 增 `enums/`(7) + `frontend/src/constants/supplyRadar.js` |
| P0-6 | P0 SQL 改"复核复用 `supply-radar-init.sql`"，不新建 01/02 |
| P0-7 | P3 拆为 1 编排页 + 7 子组件 + 抽屉，单文件 ≤400 行 |

## 十六、跨端交接

> 本节在 god 阶段增量更新。

- **本端完成状态**：**全部完成（done，2026-10-27）** —— P0/P1/P2/P3 均已实现，审计 `P0=0 / P1=0`，实际联调验证通过（前端 8093 ↔ 后端 8080）；P2 处置见 §十九
- **下游需要的契约字段**：需求包 §6.2 + §3.3；本 plan §七 + §十四 映射表；`.qoder/api-contract/supply-radar-api.md`
- **遗留风险**：
  1. ✅ 已处置：Nacos / Spring AI / dynamic-datasource 关闭 + 关闭 compatibility-verifier（Boot 3.5.3 vs Spring Cloud 2024.0.0）；
  2. ⚠️ 明文 LLM API Key **按用户要求保留**（已登记裁决）；需注意仓库访问控制与后续轮换；
  3. Playwright 内核未安装且依赖暂注释 → P1 解决（D:\repository1 无该包，需配镜像或手动安装）；
  4. 登录校验当前为存在性校验（伪造非空 token 可通过）→ 临时方案风险已登记；
  5. `DashboardSidebar` 为硬编码菜单，新页签需改代码或后续接入后端 menuList；
  6. Spring Cloud 2024.0.0 + Spring Cloud Alibaba 2023.0.3.2 与 Boot 3.5.3 版本矩阵不匹配 → 若后续启用 Nacos/配置中心需重新评估。

## 十七、验证证据

> 检查点：`.qoder/skills/audit/checkpoints/checkpoint-task-001-20261026T2048.md`

### P0（2026-10-26）

| 验证项 | 结论 | 关键结果 |
|--------|------|---------|
| L0 构建 | ✅ 通过 | `mvn install` BUILD SUCCESS（01:03 min） |
| L1 测试 | ✅ 通过 | Tests run: 2, Failures: 0, Errors: 0 |
| L2 启动冒烟 | ✅ 通过 | Started SupplyRadarApplication in 15.711s |
| L2 功能面探活 | ✅ 通过 | 无 token `/api/probe` → `{"status":"401"}` |
| 实例回收 | ✅ 完成 | 8080 端口已释放 |
| Playwright 冒烟 | ⚠️ 未触发 | 内核未安装且依赖暂注释（P1 解除） |

**被测形态**：正常（DB 就绪；Nacos / Spring AI embedding+vectorstore / dynamic-datasource 均关闭）
**未验证面**：Playwright 内核、真实采集链路（P1）

### P1 / P2 / P3

（待执行后追加）

### P1~P3 终态（2026-10-27）

| 验证项 | 结论 | 关键结果 |
|--------|------|---------|
| L0 构建 | ✅ 通过 | `mvn package -DskipTests` BUILD SUCCESS |
| L1 测试 | ✅ 通过 | Tests run: 16, Failures: 0, Errors: 0（新增 RegistrationStatusResolverTest 7 用例） |
| L2 启动冒烟 | ✅ 通过 | Started SupplyRadarApplication in 18.54s |
| L2 功能面探活 | ✅ 通过 | `/statistics` → all=2904 / talk=1835 / fair=1067 / other=2 |
| 前端构建 | ✅ 通过 | ESLint 0 error；dev server Compiled successfully |
| 采集实测 | ✅ 通过 | 10 所高校入库 2904 条（TALK 1837 / JOB_FAIR 1067 / 其他 2） |
| 联调 | ✅ 通过 | 登录态 401 根因定位（LoginInterceptor）+ auth 开关可本地联调 |

**未验证面**：定时调度（CrawlScheduler 仅代码就绪，未等 cron 触发）、社会品牌模块 UI（本轮未改）

## 十九、交付摘要与遗留（2026-10-27 收口）

### 交付内容

- **采集**：10 所北京高校（PKU/THU/RUC/BUAA/BIT/CAU/BNU/BUPT/CUC/USTB），3 类适配器（FRONTPAGE / THU_CIC / PLAYWRIGHT_HTML）+ Playwright 渲染（SPA/WAF 绕过）
- **数据**：`employment_event` 2904 条；`target_company` 20 家重点企业（保险 6 / 金融 4 / 互联网 10）
- **后端**：事件/统计/高校/企业/采集任务 8 端点；报名七态解析；企业识别（含假阳性排除）
- **前端**：4 类型页签（全部/宣讲/双选/其他）+ 企业/报名筛选 + 高校分组视图 + 详情抽屉
- **P2**：12 项中修复 8 / 失效 1 / 本就满足 1 / 保留 2（见下）

### P2 处置（详见 `.qoder/skills/audit/reports/audit-task-001-20261027-联调.md` §六）

修复：职业指导归类、指纹去 URL、remoteId 提取、跨年日期、词表单源、分组 LIMIT、契约 v1.1、CrawlScheduler
失效：线上双选 standardCategory（页签改 eventType）
保留：默认库口令（支持 env 覆盖）、文件超 400 行（技术债）

### 额外修复的阻断问题

`@MapperScan` 仅扫 `supplyradar.mapper` → 重新打包后 socialbrand 的 Mapper 无 Bean，**应用无法启动**；已修为双包扫描。

### 遗留风险

1. 明文 LLM API Key（用户裁决保留，需仓库访问控制 + 后续轮换）
2. 登录拦截器仅校验 token 存在性（伪造非空 token 可通过）
3. `DashboardSidebar` 硬编码菜单
4. 默认库口令 `root/123456`（生产必须用 `SPRING_DATASOURCE_PASSWORD`）
5. 大文件未拆（CrawlExecutor 463 / EventParser 512 / SocialBrandService 720）

### 运行方式

```powershell
# 后端（本地联调跳过登录校验）
java -jar java_bff\target\java_bff-1.0-SNAPSHOT.jar --supply-radar.auth.enabled=false
# 前端
npm --prefix frontend run serve   # http://localhost:8093
```

## 十八、需求变更 v1.1（2026-10-27 用户确认）

### 18.1 采集范围收窄

仅采集 **10 所北京高校**（其余 44 所 `enabled=0`）：

| # | 代码 | 高校 | 类型 |
|---|------|------|------|
| 1 | PKU | 北京大学 | 985 |
| 2 | THU | 清华大学 | 985 |
| 3 | RUC | 中国人民大学 | 985 |
| 4 | BUAA | 北京航空航天大学 | 985 |
| 5 | BIT | 北京理工大学 | 985 |
| 6 | CAU | 中国农业大学 | 985 |
| 7 | BNU | 北京师范大学 | 985 |
| 8 | BUPT | 北京邮电大学 | 211（指定加入） |
| 9 | CUC | 中国传媒大学 | 211（指定加入） |
| 10 | USTB | 北京科技大学 | 211（指定加入） |

> 中央民族大学（MUC）**排除**。

### 18.2 采集口径变更（宣讲会 vs 双选会）

| 类型 | 核心关注 | 必填字段 | 不需要 |
|------|---------|---------|--------|
| **宣讲会（TALK）** | **哪家公司**、在**哪个学校**、**什么时候** | `company_names`（公司）、`event_date`、`start_time`、`school_id` | — |
| **双选会（JOB_FAIR）** | **报名时间**、**地点** | `registration_deadline`、`venue_name`/`event_address`、`event_date` | `company_names`（不检索参会企业） |
| 组团招聘 | 同双选会 | 同上 | 同上 |

### 18.3 重点公司白名单（宣讲会识别用）

| 类别 | 公司 |
|------|------|
| 保险竞品 | 中国人寿/国寿、中国平安/平安、中国人保/人保、中国太保/太保、友邦、泰康 |
| 头部金融 | 招商银行/招行、华泰证券/华泰、广发证券/广发、中金公司/中金 |
| 头部互联网 | 字节跳动/字节、腾讯、阿里巴巴/阿里/蚂蚁、百度、美团、京东、拼多多、网易、华为、小米 |

### 18.4 实现进度

- [x] `target_company` 表 + 20 家企业（保险 6 / 金融 4 / 互联网 10）
- [x] `employment_event` 补 11 字段（company_*/registration_*/original_*）
- [x] `university_site.school_tag`、`crawl_task` 4 个统计字段
- [x] `CompanyResolver`：白名单匹配（标题全名→标题别名→正文全名→正文别名，5min 缓存）
- [x] `RegistrationStatusResolver`：7 态（NOT_OPEN/OPEN/CLOSING_SOON/CLOSED/NOT_PUBLISHED/CANCELLED/NEEDS_REVIEW），72h 阀值
- [x] 宣讲会无公司 → `verification_status=NEEDS_REVIEW`
- [x] 双选会不采集企业名单（`company_names` 置 null）
- [x] 报名开始/截止/方式/条件提取
- [x] 栏目收敛：招聘会类优先，单校最多 5 个栏目
- [x] 列表卡片：无年份日期（MM-DD）+ 标题正则兑底
- [x] 正文选择器排除弹窗/导航块
- [ ] 前端「公司」筛选 + 双选会报名时间列（待做）
- [ ] API 前缀 `/api/supply-demand-radar/` + company-talks / job-fairs 分组接口（待做）
- [ ] 前端两个一级页签（供给侧 / 需求侧）（待做）

### 18.6 已知卡点（北航）

北航 `career.buaa.edu.cn` 为 frontpage 平台 SPA：列表卡片由 XHR 渲染，卡片链接为站内路由，
当前通用解析器只能提到导航链接（组团招聘/招聘会菜单），导致详情页为栏目首页而非活动详情。
待办：抓包确认公开 JSON 接口后写 `adapter_type=BUAA_FRONTPAGE` 专用适配器（或 dump 渲染 HTML 分析卡片选择器）。

### 18.5 验收调整

- E1 由“≥5 所不同结构”调整为：**10 所中至少 5 所采到含公司名的宣讲会 + 至少 3 所采到含报名时间的双选会**
