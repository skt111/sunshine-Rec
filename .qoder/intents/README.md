# .qoder/intents/ — 需求意图资产库

> **定位**: 新需求在进入 architect/plan mode 前的**结构化入参沉淀位**（intent 前置层，task-103 W1）。
> **性质**: 项目需求资产库——统一管理"谁在什么场景要解决什么问题"，长期作为**理解项目的资产**（需求史 = 项目"为什么存在"的档案）。

## 与 harness 体系的关系

```
自然语言需求 ──采访──▶ intent 文档（本目录，confirmed）──入参──▶ architect ──▶ plan（plans.json）
                                                                              │
                                                        god 执行 / audit 溯源（防 scope creep）
```

- **architect 入口**: 复杂度 ≥2 的新需求先在此建档（5 要素），architect 以 intent 为入参；
- **plan↔intent 引用**: plan 登记时经 `plans.json` plan 行的 `intent` 字段（schema V2 起）指向本目录文档，intent 侧回填 `plan` 字段一次——双向互指由 V-Intent 反向一致性校验兜底；
- **消费端**: god 上下文加载读取、audit 核对"实现 vs intent"。

## 端到端流转 SOP（单点入口——细则留在原位，本表为导航权威）

> 各文件反向链回本表；例外处理与边界见下方例外表。

| 步 | 执行者 | 产物 | 校验点 | 细则位 |
|----|--------|------|--------|--------|
| 新需求触发 | 任一发起者（用户/工单） | — | — | — |
| 采访建档 | 澄清对话执行 AI（architect 模式内归 Step 0.5；模式外主 Agent） | intent 文档（5 要素 + frontmatter） | INTENT-SPEC QUALITY 五判据 | 02-clarification 澄清产物协议 |
| 发起者签收 | 发起者/用户 | status → confirmed | 确认闭环 | 02 协议 §3 |
| 入参检查 | architect Step 0.5 | 三态判定 | 已有 confirmed intent 才放行；draft → 挂起 | architect SKILL §0.5 |
| spec 生成 | architect §1.9 | plan 详情「需求基线」yaml | V-A7 机器校验（无 REPLACE_ME/裁决有据）；生成判据 Q1-Q5 | architect SKILL §1.9 + references/spec-quality.md + validate-plans.js §6.4 |
| plan 设计 | architect §2-6 | 评分卡 ≥19 绿灯 | 质量门禁 | architect SKILL + scorecard.md |
| plan 登记 | architect | plans.json 行（含 `intent` 字段回填） | V-Intent 引用/缺口/状态/反向 1:N | validate-plans.js §6.7 |
| 执行许可 | sandbox 蓝军 | 报告凭证 | P0=0 绿灯（complexity ≥4 强制） | sandbox SKILL |
| 执行 | god | 代码 + checkpoint | 意图锚点加载 + 构建验证 | god SKILL 阶段 2/4 |
| 验收 | audit | 报告 | 实现 vs intent 4 清单 + 基线核对 | audit SKILL 意图核对块 |

**例外表（边界处置）**:

| 例外 | 处置 |
|------|------|
| 轻量需求（复杂度 <2） | 直通免建档（architect §0.5 第三态；§1.5 正式评估 <2 回退——双闸容错） |
| 存量需求（intent 体系启用前） | 不追溯建档；V-Intent 缺口告警豁免（created ≤ 2026-09-09） |
| todo 型 harness 工程 | 沿用 V-A7 todo 豁免 |
| **平台 Plan mode 导入** | 登记桥前置协议：Step 0.5 三态 → 建档 confirmed → 回填 `intent` → 豁免须详情注记（architect 挂起点平台导入前置协议） |
| 孤儿 intent（draft 长期无 plan 认领） | 滞留不阻断，人工定期裁决 |
| 弃采/废弃 | 用户弃采裁决或 architect 建档时声明 → status archived（不删除） |
| 意图变更 | 更新 intent 文档（git + updated 双键版本化）→ 关联 plan 置 revised |

## 与领域资产（DDD）的联动

- intent frontmatter 的 `domain` 字段**对齐 `.claude/context/domain-index.json` 的 `domain` 域名称**（实际域示例：`业务办理域`（keywords 含休假/leave）、`员工关怀域`（含生日/birthday））；harness 工程需求用 `harness 工程域`；
- intent 的 domain 未在 domain-index 建档 → 记**域缺口**（回流 inventory，与 architect §1.9 域定位四态衔接）；
- `business-architecture.md` 域节可按需回链关联 intent（域档案的"活需求侧"）；
- domain-architect 深化轮可将本目录按域聚合，作为域档案的**需求侧输入源**（档案写"现状"，intent 写"诉求"）。

## 命名规范

```
{域前缀}-{slug}-{YYYY-MM-DD}.md
```

- 域前缀示例: `harness-` / `leave-` / `attendance-` / `birthday-` / `team-care-` …
- slug: 英文短横线，一句话概括需求
- 日期: 建档日；同日同域多需求加后缀 `-2`、`-3`

## 生命周期（轻量，不入 plans.json 状态机）

| 状态 | 含义 | 进入条件 |
|------|------|---------|
| `draft` | 捕获中 | 采访进行中/未确认 |
| `confirmed` | 发起者已确认，可进 architect | 5 要素对齐、发起者裁决 |
| `archived` | 被取代或废弃 | 不删除——历史资产保留（需求史） |

存量 docs/ 需求文档**不追溯迁移**（豁免）；本目录启用日（2026-09-09）起新需求强制建档。

> **archived 触发方**: 用户弃采裁决（对话中声明需求废弃/不做了）或 architect 建档时声明；无自动回收——历史资产保留。孤儿 intent（draft 长期无 plan 认领）滞留不阻断，由人工定期裁决。

## 文件清单

- `_TEMPLATE-intent.md` — 建档模板（复制后填写，勿直接修改）
- `{域前缀}-{slug}-{YYYY-MM-DD}.md` — 真实需求档案
