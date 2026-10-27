# 附录C：设计参考

> **定位**: 背景性设计参考（架构 / 联动 / 视觉基线），**不构成契约**。具体需求以主文档与附录A/B 为准。

---

## 1. 架构总览

```
前端(Vue2+iView)
  └─ /api/supply-radar/*  ──►  Controller（统一壳）
                                 ├─ SchoolController / EventController / StatisticsController / CrawlTaskController
                                 ├─ Service（查询 / 采集编排 / 分类 / 去重）
                                 ├─ Mapper（MyBatis-Plus）
                                 └─ Crawler（Adapter 体系：GENERIC + SPECIFIC）
                                        ├─ Fetcher（HttpClient / Jsoup）
                                        ├─ Renderer（Playwright）
                                        └─ Scheduler（@Scheduled）
数据库 MySQL `sunshine`（4 表）
外部：54 所高校站点（只读采集）
```

## 2. 前端组件拆分（对应 PRD §八）

| 组件 | 职责 |
|------|------|
| `SupplyRadarPage` | 主页面容器（编排筛选/统计/列表状态） |
| `RadarStatistics` | 顶部 5 张统计卡片 |
| `SchoolFilter` | 高校搜索/多选/城市 |
| `EventCategoryTabs` | 6 分类页签（带数量） |
| `EventList` | 列表容器（分页/排序/视图切换） |
| `EventCard` | 单条活动卡片 |
| `EventDetailDrawer` | 活动详情抽屉（11 项） |
| `SchoolGroupedView` | 高校分组视图 |
| `CrawlTaskPanel` | 采集任务面板（进度） |
| `CrawlStatusTable` | 高校采集状态表 |

> 单文件 ≤400 行；`name` 必填；`scss + scoped`。

## 3. 关键联动机制

```
筛选条件变化
   ├─► GET /statistics（同筛选） ─► 统计卡片 + 页签数量
   └─► GET /events（同筛选）     ─► 列表 + 分页

分类页签点击 ─► category 参数 ─► 重新请求 events（statistics 分类计数保持全量口径）

立即更新 ─► POST /crawl-tasks ─► taskId ─► 轮询 GET /crawl-tasks/{id}
                                            └─► 终态 ─► 刷新 statistics/events/页头

视图切换 ─► 列表视图（events 分页） ⇄ 分组视图（按 school_id 聚合）
```

## 4. 数据表关系简图（背景速览）

```
university_site (1) ──< (N) employment_event (1) ──< (N) employment_event_history
                                       ▲
crawl_task (1) ────────────────────────┘ (history 记录来源任务)
```

> 契约级关系与基数以 **附录A L2** 为准。

## 5. 视觉基线（设计参考——强制对齐主文档 §3.4）

| 令牌 | 值 |
|------|-----|
| 主色 | `#F58220`（橙） |
| 页面背景 | 暖白 `#FAF9F7` / 浅灰 `#F5F6F8` |
| 卡片背景 | `#FFFFFF` |
| 重要数据 | `#F58220` |
| 文字主色 | 深灰 `#333333` |
| 辅助文字 | `#8C8C8C` |
| 危险/取消 | `#E64545` |
| 成功 | `#52C41A` |

- 风格：现代企业级 SaaS 数据平台；信息层次清晰，列表单页 20 条（数据密度以此为准），桌面端（≥1366×768）优先。
- 分类标签配色：校内宣讲会（橙）/ 校外宣讲会（蓝）/ 校内双选会（绿）/ 校外双选会（紫）/ 其他（灰）/ 待核验（红）。
- **无既有像素级设计稿** → 视觉细节可自行设计，但须遵守上述令牌与 §3.4 规范（留白已登记）。

## 6. 核心设计原则

1. 单一数据源：统计/列表/页签计数全部后端计算，前端不本地累加。
2. 契约先行：前后端以字段名（camelCase）+ 响应壳冻结，禁单侧漂移。
3. 采集柔性：单校失败隔离、去重更新、不删原数据、不绕访问控制。
4. 关注点分离：查询服务与采集服务解耦，采集可独立调度/重跑。
5. 可观测：站点状态、任务统计、失败原因全程留痕（`university_site` + `crawl_task`）。

## 7. 关键联动点速查

| 联动关系 | 实现方式 |
|---------|---------|
| 筛选 → 统计+列表 | 同一筛选参数集，两请求并行 |
| 分类页签 → 列表 | `category` 参数 |
| 任务 → 页面刷新 | 轮询终态后统一刷新 |
| 活动更新 → 变更历史 | 事务内双写 |
| 站点采集 → 状态回写 | 任务结束更新 `crawl_status` |

> 本附录为背景性设计参考，具体需求以主文档与附录A/B 为准。
