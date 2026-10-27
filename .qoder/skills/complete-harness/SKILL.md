---
name: complete-harness
description: 将项目工程化配置补全为可用体系（AGENTS.md + .qoder/ 或 CODEBUDDY.md + .codebuddy/）。适用于: init/codebuddy 骨架生成后的内容补全（技术栈/命令/红线/词汇表）、从 Claude Code 配置迁移（特例）、Qoder↔CodeBuddy 跨体系同步。init 骨架生成后必须执行本技能完成内容落地
---

## References

| 锚点 | 位置 | 用途 |
|------|------|------|
| 触发方式与时机 | 触发方式 (L25) + 触发时机 (L32) | 何时触发补全、各场景触发条件 |
| 阶段总览与 5 Phase | 阶段总览 (L42) | SCAN→DECIDE→MAP→ADAPT→VERIFY 五阶段流水线 |
| SCAN 扫描与 DECIDE 决策 | Phase 1 (L48) + Phase 2 (L72) | 双源感知扫描、技术栈提取、前置决策与完成态短路 |
| MAP 映射表 | Phase 3 (L86) | 骨架→补全主表、Qoder↔CodeBuddy、Claude→Qoder 映射 |
| ADAPT 适配规则 | Phase 4 (L127) | rules frontmatter 三模式、字段差异、settings.local 合并 |
| VERIFY 验证与坑位 | Phase 5 (L155) + 已知坑位 (L170) | 验证清单全过才算完成、实战教训与输出物清单 |

---

# Complete Harness 配置补全 Skill V2.0.0（通用版）

> **定位**: `harness-qoder init` / `harness-qoder codebuddy` 生成骨架后的 AI 补全闭环。**源可以是任意形态**（存量 .claude 配置 / 全新项目 / 现有骨架），**目标可以是 Qoder 或 CodeBuddy 体系**。本技能负责将骨架补全为可用配置（SCAN → DECIDE → MAP → ADAPT → VERIFY）。
> **方法来源**: Golden Example 全量迁移实战 + 双体系初始化实战（双轨并行 + 核心子集 + 独立审计验证）。
> **参考实例**: Golden Example 仓库（双轨）；纯 CodeBuddy 单体系实例（如 talent-pool）。

## 触发方式（会话内）

- 用户输入 `/complete-harness`（新命令），或
- 用户在 init/codebuddy 后说"补全配置 / 完善 harness 配置 / 完成迁移"，或
- 旧项目仍引用 `/migrate-claude-to-qoder`（V1 兼容别名，等价触发），或
- 扫描发现规则仍为模板占位内容（`（补充）` 等）时

## 触发时机

| 场景 | 触发条件 |
|------|---------|
| init 后补全（**默认**） | 已运行 `harness-qoder init` 生成骨架，需补全内容 |
| codebuddy 后同步 | 已运行 `harness-qoder codebuddy` 生成 .codebuddy/，需补全并保持两侧一致 |
| 新项目从零补全 | 无任何源配置，从实际代码扫描技术栈/命令填充（无 .claude 可扫） |
| Claude 迁移（特例） | 项目从 Claude Code 切换，需建立对应体系（源为 .claude/ 时） |
| 完整性校验 | 已补全过，怀疑遗漏（对照实际代码与清单查缺） |

## 阶段总览（5 Phase）

```
SCAN(扫描) → DECIDE(决策) → MAP(映射) → ADAPT(适配) → VERIFY(验证)
```

## Phase 1: SCAN 资产清单扫描（双源感知）

**分支 A — 源为存量 .claude 配置（迁移场景）**：运行 CLI 生成三态对照表：

```powershell
harness-qoder scan --project_path=<项目根> --report
# 或免安装: npx harness-cli-qoder scan --project_path=<项目根> --report
```

> 三态判定 ✅ MIGRATED / 🔗 REFERENCED / ❌ TODO；若尚未安装 CLI：`npm install -g harness-cli-qoder`（内网源 http://10.9.138.23:4873/）。
> 手动核对源资产清单（子端也要扫）：根/子端 CLAUDE.md、`.claude/settings*.json`、agents/commands/skills、`.mcp.json`、`.claudecodeignore`。

**分支 B — 无源（新项目/骨架态）**：执行**技术栈扫描**，从实际代码提取真实信息：

| 扫描目标 | 提取项 | 落地位置 |
|---------|--------|---------|
| `{端}/pom.xml` 或 `build.gradle` | java.version / spring-boot.version / spring-cloud.version / 持久层组件版本 | 子端规则 §1 技术栈 + project-structure.json |
| `{端}/package.json` | dependencies 核心版本（vue/react/脚手架）、scripts（serve/build/lint/test） | 子端规则 §1 技术栈 + 本地命令清单 |
| `{端}/pyproject.toml` / `requirements.txt` | 框架版本、包管理器（uv/pip） | 子端规则 §1 |
| `{端}/README.md` / `CLAUDE.md`（若存在） | 启动方式、架构说明、业务术语 | §2 架构红线 / §4 词汇表 |
| `{体系}/config/project-structure.json` | 栈目录映射与实际目录是否一致 | 不一致则修正 |

> ⚠️ 版本号必须**磁盘提取**（mvn/pip 可解析的 properties 引用也接受），禁止凭印象填写；提取后与现有声明比对，差异即补全项。

## Phase 2: DECIDE 前置决策（必须询问用户，禁止擅自决定）

> **完成态短路**: 若 Phase 1 复扫无差异且 VERIFY 清单全过，直接输出 "✅ 已完成，无需处理"。
> **骨架态提示**: 若 rules 仍为模板占位内容（技术栈/红线/词汇表未补全），即使无差异也须继续 MAP/ADAPT——以磁盘内容为准。

| 决策项 | 选项 | 推荐 |
|--------|------|------|
| 目标体系 | Qoder（AGENTS.md+.qoder/）/ CodeBuddy（CODEBUDDY.md+.codebuddy/）/ 双体系 | 双体系（codebuddy 自动适配，两侧同源） |
| 体系策略（源为 .claude 时） | 双轨并行（保留 .claude/ 不动）/ 完全接管 / 混合 | **双轨并行**（回滚安全，易变资产共享） |
| 补全范围 | 核心子集（技术栈+命令+红线+词汇表+hooks）/ 全量 / 最小 | **核心子集** |
| 易变资产 | 路径引用（共享 {体系}/plan 等单点维护）/ 复制副本 | **路径引用** |

决策后输出体系边界声明（无 .claude 时）或双轨边界声明（有 .claude 时）写入总纲，消除"配置状态不明"。

## Phase 3: MAP 映射表

### 3.1 骨架 → 补全（所有场景通用，主表）

| 骨架占位 | 补全来源 | 补全动作 |
|---------|---------|---------|
| 子端规则 §1 技术栈 | Phase 1 技术栈扫描 | 版本化声明（如 "Java 1.8 / Spring Boot 2.7.18"），禁止泛化标签（"前端 (Vue/React)"） |
| 本地命令清单 `（补充）安装依赖...` | package.json scripts / pom 插件 | 填充真实命令（mvn compile/spring-boot:run、npm install/serve/build/lint），保留熔断红线 |
| §2 架构红线 `（补充）...` | 子端 README/CLAUDE.md/实际代码 | 提取 API 隔离/路径别名/契约对齐等红线 |
| §4 业务词汇表 `（补充）...` | 代码/文档 | 统一术语与对应变量名 |
| 易变资产路径（BUILD_PLAN/雷达/契约/交接） | 体系探测 | 三态落位：双轨 `.claude/plan` / CodeBuddy `.codebuddy/plan` / 纯 Qoder `.qoder/plan`，**禁止死引用** |
| 总纲子端列表/技术栈声明 | 子端检测 | 与子端规则 §1 保持一致（版本、组件） |

### 3.2 Qoder → CodeBuddy（codebuddy 适配后的同步补全）

| CodeBuddy 资产 | 适配规则 |
|---------------|---------|
| `CODEBUDDY.md` | 从 AGENTS.md 复制 + 路径/入口替换 |
| `rules/` | **剥离 frontmatter**（paths/trigger 机制不支持）→ 始终加载 + 头部场景标注 |
| `skills/` `commands/` `agents/` | 同源直接复制（生态同源） |
| `settings.json` | 直接复用（Claude 系兼容子集） |
| `scripts/project-scanner.js` | 复制 + 雷达输出路径指向 `.codebuddy/plan/` |

> 两侧一致性要求：**任何一侧的内容补全必须同步另一侧**（子端规则技术栈、总纲声明、路径引用），codebuddy --force 重跑仅处理模板级替换，不覆盖手工补全内容。

### 3.3 Claude → Qoder（仅源为 .claude 时适用）

| Claude 元素 | Qoder 元素 | 处理方式 |
|------------|-----------|---------|
| 根 `CLAUDE.md` | `AGENTS.md` + `.qoder/rules/` | 总纲进 AGENTS.md；约束按主题拆分 rules |
| 子端 `CLAUDE.md` | `.qoder/rules/{NN}-{end}.md` | 每子端一条 `paths:` 规则，正文提取技术栈/红线/词汇表/交接协议 |
| `.claude/settings.json` | `.qoder/settings.json` | permissions 映射；enabledPlugins/enabledMcpjsonServers **无对应物，跳过并声明** |
| `.claude/settings.local.json` | `.qoder/settings.local.json` | permissions 合并 + hooks 迁移；**gitignore 掉** |
| `.claude/agents/*.md` | `.qoder/agents/{name}.md` | frontmatter 兼容，可直接复用全文 |
| `.claude/commands/*.md` | `.qoder/commands/{name}.md` | 补 `---` YAML frontmatter；"@Claude Code" 改写 |
| `.claude/skills/{name}.md` | `.qoder/skills/{name}/SKILL.md` | 单文件转目录式；配套 references/audit/prompts 一并复制 |
| 子端 `.claude/skills/` | rules "战术底座"章节 | **不复制**，路径引用 |
| `.claude/sop/plan/context/templates/api-contract/` | 引用资产 | **不复制**，总纲 §资产索引 + 路径引用 |
| 根 `.mcp.json` | 原生兼容 | 零改动；`enabledMcpjsonServers` → `mcp__{server}__*` |
| `.claudecodeignore` | 无 | Qoder 使用 .gitignore 语义，不迁移 |

## Phase 4: ADAPT 适配规则

### 4.1 rules frontmatter（Qoder 三模式；CodeBuddy 无此机制）

| 模式 | frontmatter | 用途 |
|------|------------|------|
| 始终生效 | 无 frontmatter（或 `trigger: always_on`） | 全局硬约束 |
| 路径生效 | `paths: [dir/**]` | 子端规则 |
| 模型决策 | `trigger: model_decision` + `description` | 澄清类、跨端类规则 |

> CodeBuddy 侧：无触发机制 → 全部转始终加载，头部标注"原 Qoder paths/model_decision 规则，出现正文所述场景时按正文执行"。

### 4.2 字段与格式差异（源为 Claude 配置时必须改）

| 项目 | 改动 |
|------|------|
| `disable-model-invocation: true` | **删除**（Qoder 无此字段） |
| 无 frontmatter 的技能/命令 | **补** `name` + `description`（必填） |
| hooks matcher | Claude `Edit` → Qoder `Write\|SearchReplace\|edit_file` |
| 权限 `Read(**)` 等 | Qoder 为工具级放行 `Read`/`Glob`/`Grep` |
| 权限通配 | `Bash(npm run:*)` → `Bash(npm run *)` |

### 4.3 子端 settings.local.json 合并原则

- **合并**: 通用可复用命令（npm/pip/uv/curl 健康检查/构建验证/打包）
- **跳过**: `rm -rf`/`rm -f` 等破坏性命令、含绝对路径的一次性历史命令
- 跳过项必须写入边界声明

## Phase 5: VERIFY 验证清单（全过才算完成）

- [ ] 所有 JSON 文件语法通过
- [ ] rules/commands/agents/skills 的 frontmatter 符合目标体系规范
- [ ] **技术栈声明与实际代码一致**（版本号磁盘核实，无泛化标签残留）
- [ ] 本地命令清单真实可用（mvn/npm 命令存在且拼写正确）
- [ ] **所有路径引用经磁盘核实存在**（重点：雷达/契约/交接/BUILD_PLAN 指向实际体系目录，无 .claude 或 .qoder/plan 死引用）
- [ ] 两侧（.qoder/.codebuddy）规则内容一致（若为双体系）
- [ ] hooks 命令可执行且与脚本输入协议匹配
- [ ] `.gitignore` 包含 `.qoder/settings.local.json`（CodeBuddy 项目含 `.codebuddy/settings.local.json`）
- [ ] 边界声明（单体系/双轨）已写入总纲
- [ ] 独立 Agent 审计通过（P0=0；P1 修复或声明）
- [ ] 目标 IDE 中实测 `/skills`、`/agents`、`/commands` 加载
- [ ] `harness-qoder scan --project_path=<项目根>` 复扫：待处理项为 0（或已声明）

## 已知坑位（实战教训）

1. **子端文件漏检**: 文件搜索可能被 .gitignore 影响，必须全盘确认（CLI scan 走全盘不受影响）。
2. **源路径引用可能本来就是坏的**: 引用文件实际不存在，映射后必须磁盘核实。
3. **技能配套文件源缺失**: 源遗留缺文件，双轨保持一致即可，不引入新路径。
4. **退出钩子完整性**: 补全退出钩子时逐条对照，易漏"运行雷达"与"技术栈同步"步骤。
5. **hooks matcher 工具名**: 使用目标 IDE 工具名，Claude 的 `Edit` 在 Qoder 中不存在。
6. **权限映射不完整**: 逐条对照源 allow 列表，合并后逐条核对。
7. **init 骨架≠完成**: init 只生成模板（含占位符章节），本技能 Phase 3/4 的补全才是主体；跳过补全直接使用的配置是空壳。
8. **技术栈版本凭印象填写**: 必须从 pom/package.json 磁盘提取，泛化标签（"Vue/React"）会误导代理选择命令。
9. **行尾差异导致替换失配**: Windows 项目文件多为 CRLF，脚本/手工替换多行文本前先归一化行尾。
10. **--force 覆盖手工补全**: 重跑 init/codebuddy 带 --force 会还原手工补全内容；易变资产（plan/契约）不受影响。

## 输出物清单

```
AGENTS.md / CODEBUDDY.md        # 总纲 + 资产索引 + 退出钩子 + 边界声明
{q体系}/rules/                  # {NN}-{theme}.md（00-01 始终 / 02 决策 / 10+ 子端 / 20 跨端）
{q体系}/settings.json           # 提交级权限
{q体系}/settings.local.json     # 本地权限 + hooks（gitignore）
{q体系}/agents/{name}.md        # 子代理
{q体系}/commands/{name}.md      # 命令
{q体系}/skills/{name}/SKILL.md  # 技能（目录式 + 配套文件）
{q体系}/config/project-structure.json  # 栈目录映射（与实际一致）
{q体系}/plan/.project_radar.json       # 雷达索引（scanner 运行产物）
```
