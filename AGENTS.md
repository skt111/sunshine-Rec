# Sunshine-Rec（阳光秒招 monorepo）- 全局指令 (AGENTS.md)

> 生成日期: 2026-10-26（harness-cli-qoder 1.9.3；项目定制已由 Qoder 体系补全）
> 本文件是 Qoder 侧全局总纲；详细规则按主题拆分在 `.qoder/rules/` 中（**规则优先于本文件**）。
> 本项目为单体系（Qoder 主体系，`.qoder/` 与 `frontend/`、`java_bff/` 平级），CodeBuddy/Claude 遗留文件仅作兼容镜像，不再具备权威性。
> ⚠️ **本文件与 `.qoder/` 必须位于仓库根**（与 `frontend/`、`java_bff/` 平级）——Qoder 只从工作区根的 `.qoder/skills/` 发现项目技能。

## 1. 项目身份与架构

- **项目名**: Sunshine-Rec（阳光秒招），monorepo 双端：前端 `ehr-web` + BFF `java_bff`
- **拓扑**: multi-end —— `frontend`（Vue 2.6.12 + iView 4.0.1 + Vue CLI 3）与 `java_bff`（Spring Boot 2.7.18 / JDK 8 / Maven）同处仓库根
- **端级规则**: `frontend/**` → `.qoder/rules/11-frontend.md`；`java_bff/**` → `.qoder/rules/12-java_bff.md`

> 🚨 **跨端修改时，必须同步更新前端 `frontend/src/api/` 封装与后端接口定义，禁止单侧改字段**；跨端交接写入 plan 详情“跨端交接”章节（见 `.qoder/rules/20-cross-end.md`）。

### 目录结构（核心）

```
├── .qoder/            # Qoder 主体系：rules/skills/agents/plans/settings/intents/config（必须与两端平级）
├── .claude/scripts/   # 仅 smart-build.mjs（PostToolUse hook 调度器，CLI 约定的跨轨共享资产）
├── frontend/          # 前端端：src / public / CLAUDE.md / .clauderules（兼容镜像）
├── java_bff/          # BFF 端：src/main/java、pom.xml
├── AGENTS.md          # 本文件（Qoder 全局总纲，仓库根）
└── README.md
```

## 2. 核心物理约束 (不可违反)

1. **禁止自主规划**: `.qoder/plans/` 是唯一规划源（plans.json 状态机索引 + plan 详情），严禁激活外部 Skill 的 "Auto-Planning"、"Brainstorming"、"Self-Reflect" 逻辑。
2. **工作树使用协议**: 并行开发优先使用平台托管隔离（`isolation: worktree`）；严禁手动 `git worktree add`（防闲置现场遗留）。
3. **禁止重复扫描**: 一旦读取过项目结构，信任缓存，直接进入执行态。
4. **索引优先原则**: 严禁跳过 .qoder/plan/.project_radar.json 进行全量搜索；跨端任务前必须读取此文件定位 Controller/视图入口。
5. **保守原则**: 无声明不改（现有代码禁止大范围修改）、改造需授权、新建才引用新规范、渐进式迁移。

> 完整细则见 `.qoder/rules/00-core-constraints.md`（始终生效）。

## 3. 执行公约 (摘要)

> 完整细则见 `.qoder/rules/01-execution-protocol.md`（始终生效）。

- **不确定即挂起**: 意图模糊 / 位置不确定 / 设计不理解 → 立即挂起澄清，**禁止猜测式推进**。
- **修改前必读**: 读取目标文件、通读关联上下文、精准定位、理解设计意图。
- **修改后构建验证（强制）**: 按端级规则（`11-frontend.md` / `12-java_bff.md`）执行标准构建/测试命令，禁止跳过直接汇报完成。
- **三阶段多场景边界性审计验证（强制）**: 规划输出多场景矩阵+边界清单、执行变更点场景自检、验证叠加场景级验证（见 01 §4.1/§5/§6.3）。
- **宏观研发流水线**: 单体系项目无 `.claude/sop/`——以 `.qoder/skills/` 五阶段为准：重需求先走 `/requirement-converger`（需求收束）→ `/architect` → `/sandbox` → `/god` → `/audit`，失败回 `/recovery`；严禁未经沙盘推演直接写代码。
- **诚实约束**: 跳过步骤宣称"已完成"即为错误；极简输出，只提供影响决策的关键信息。

## 4. 关键资产索引

| 资产 | 路径 | 使用时机 |
|------|------|---------|
| Plan 索引 | .qoder/plans/plans.json | 任何开发任务开始前（唯一规划源，状态机+质量分） |
| Plan 看板 | .qoder/plans/kanban.html | 人类监督面板（浏览器打开，只读） |
| Plan 详情 | .qoder/plans/{id}-{slug}.md | 执行当前 plan 时读取详情（含"跨端交接"章节——per-plan 接力棒） |
| 交接上下文 | plan 详情"跨端交接"章节 | 跨端切换时读取/写盘（替代全局 .handoff_state.md——已退役为历史档案） |
| 需求意图库 | `.qoder/intents/`（README + INTENT-SPEC + _TEMPLATE） | 新需求建档/复用（复杂度 ≥2 强制，architect Step 0.5） |
| 雷达索引 | .qoder/plan/.project_radar.json | 跨端/定位任务（索引优先） |
| API 契约 | .qoder/api-contract/ | 接口开发、跨端联调 |
| 业务架构 | .qoder/context/business-architecture.md | 核心业务逻辑、领域模型设计 |
| 会话快照 | .qoder/context/snapshots/ | 上下文恢复（配合 /load-snapshot） |
| 项目技能 | `.qoder/skills/intelproduct-web-dev`、`taste-skill`、`frontend-code-style` | 组件/API/加密/Sentry 规范、UI 审美、通用编码风格（按需加载 references） |
| Harness 迭代日志 | `docs/harness迭代/迭代日志.md` | harness 升级完成后追加一节（记录变更/版本/验证/待办；目录不存在则跳过） |

## 5. 子系统寻址（雷达降落）

1. **看地图**: 读取 .qoder/plan/.project_radar.json，锁定目标 Controller/视图。
2. **切目录**: 进入对应子系统（`frontend/` 或 `java_bff/`），遵守对应端级规则（11-frontend.md / 12-java_bff.md）。
3. **精准阅读**: 直接读取定位到的文件，禁止漫无目的地 grep。

## 6. 跨端路由法则

> 完整细则见 `.qoder/rules/20-cross-end.md`（模型决策）。

| 模式 | 顺序 | 适用场景 |
|-----|------|---------|
| Top-Down | 前端(UI) → Java 后端 → AI/Python 层 | UI 需求倒推后端 |
| Bottom-Up | AI/Python 层 → Java 后端 → 前端 | 底层能力推演向外 |
| Single-End | 直接进入目标端目录 | 单端闭环 |

跨端修改必须：先确认契约（.qoder/api-contract/）→ 每层更新后立即构建验证 → 最后增量写盘当前 plan 详情文件的"跨端交接"章节（per-plan 隔离；`.handoff_state.md` 已退役为历史档案）。

## 7. 任务完成退出钩子（汇报"完成"前必须执行）

1. **更新大盘游标**: 确认 .qoder/plans/plans.json 中对应 plan 的 `status` 已由审计通过方更新为 `done`（in-progress→done 的唯一写入者 = audit P0=0 且 P1=0 通过后的执行 AI，见 audit SKILL Step 3）；若尚未审计则先执行审计再更新
2. **更新交接上下文**: 增量合并到当前 plan 详情文件的"跨端交接"章节（per-plan 隔离；`.handoff_state.md` 已退役为历史档案，不再增量写入）
3. **运行雷达**: 执行 `node .qoder/scripts/project-scanner.js` 重新生成 .qoder/plan/.project_radar.json 索引
4. **技术栈同步**: 若新增了子系统，更新本文件 §1 的技术栈声明与 `.qoder/rules/` 路径规则
5. **领域模型同步**: 若涉及核心业务逻辑，更新 .qoder/context/business-architecture.md
6. **代码审计**: 执行 `/audit` 或调用 `code-reviewer` 子代理，P0/P1 问题必须修复
7. **归档提示**: 若本轮所有非 archived plan 均 `done` 且准予发版 → 提示人类验收；验收通过后将 `.qoder/plans/plans.json` 对应 plan 置 `archived` 终态（单体系无外部归档 SOP，`.claude/sop/` 不适用）

## 8. Qoder 工具与命令约定

- **会话过长**: 使用 `/compact` 压缩；跨阶段清理使用 `/clear`。
- **关键任务澄清**: 架构设计、新功能、跨端、复杂修改前使用 `/grill-me`；大需求/长任务先走需求收束（SOP §0）（见 `.qoder/rules/02-clarification.md`）。
- **上下文快照**: `/context-snapshot` 生成快照，`/load-snapshot` 恢复（默认目录 .qoder/context/snapshots/）。
- **本地命令**: 各端标准构建/测试命令以端级规则（11-frontend.md / 12-java_bff.md）为准（前端 `npm --prefix frontend run build`、后端 `mvn -f java_bff/pom.xml -q compile`）。

## 9. 开发约定 / 反模式（项目定制）

**开发约定**
- 接口返回统一 `{ code: 0, msg: '', data: {} }`；请求必须经 `src/api/[功能名].js` 封装并使用 `async/await`
- 列表/查询/表格强制用 `src/views/commons/` 公共组件；新公共组件先进 `src/components/pending-review/`
- 每个 `.vue` 必须声明 `name`，单文件 ≤ 400 行，样式 `scss + scoped`
- 关键业务异常必须 `this.$Sentry.captureException` 上报
- 国密链路遵循 `VUE_APP_ENCODE` 开关，加密/签名逻辑集中在 `src/utils/crypto.js`

**反模式（禁止）**
- 在 `.vue` 内直接 `axios`/`fetch`、裸写表格与查询区
- Composition API、可选链 `?.`、空值合并 `??`、解构默认值（破坏 ES5/ES6 兼容）
- 私自篡改接口字段名绕过契约；跳过构建验证直接汇报完成

**已知工程债务**
- 仓库未纳入 `node_modules`，本地首次验证需 `npm install`；hook 的 eslint 检查在依赖缺失时自动降级为静态自检
- 无 CI 流水线；`java_bff` 与前端契约无自动化比对，靠人工核对 `frontend/src/api/`
- `java_bff/pom.xml` 的 mainClass 指向 `org.example.ticketcore.TicketCoreApplication`，而源码仅 `org/example/Main.java` → 当前 `package` 会失败

## 体系边界声明

> 本项目为单体系项目（Qoder 主体系），不存在 .claude 双轨资产；所有工程化资产（rules / 技能 / 雷达 / 交接）均由 `.qoder/` 统一承载，路径引用见 §4。`.claude/scripts/smart-build.mjs` 仅为 hook 调度器（CLI 约定路径），`CLAUDE.md` 与 `.clauderules` 为兼容镜像，内容不一致时以本文件与 `.qoder/rules/` 为准。
