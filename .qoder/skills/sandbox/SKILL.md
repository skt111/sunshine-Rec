---
name: sandbox
description: 沙盘验证模式 - 独立审计 plan 逻辑完整性和流程闭环
---

## References

| 锚点 | 位置 | 用途 |
|------|------|------|
| 触发时机与参数 | §触发时机 + §参数 | 何时启动 sandbox、--skip-typo/--lightweight 参数 |
| 行为约束与报告规范 | §行为约束 + §报告写盘规范 | 禁止修改业务代码、独立审计、写盘路径/命名/签名 |
| 审计重点与分级 | §审计重点 + [audit/sandbox-checklist.md](./audit/sandbox-checklist.md) | P0/P1/P2 维度定义、详细检查清单 |
| 执行流程 Step 0-3 | §执行流程 + [prompts/sandbox-agent.md](./prompts/sandbox-agent.md) | 场景判定→蓝军 Agent→等待结果→处理结果的完整链路 |
| 错误处理 | §错误处理 | 超时/降级/报告写入失败(不授权绿灯)容错、SendMessage Fallback |
| 结果路由与 God 衔接 | §处理审计结果 + [references/god-handoff.md](./references/god-handoff.md) | P0/P1/P2 结果判定、revised 打回与回迁、绿灯授权与 P0 阻断路由 |
| 历史变更详情 | [references/changelog.md](./references/changelog.md) | V2.4.0 / V2.3.0 代际明细 |

---

# Sandbox Skill V2.7.3

> **版本**: V2.7.3 | **更新**: 2026-09-30 | **核心**: 会话隔离 + 独立审计 + planId 凭证 + 结构行协议 + revised 回迁 + 需求包引用（条件触发） + 复验轻量/输入协议/P2 简写 + 修复轮次计数与超限出口（x-rules.repairLoop） + Red Flags 反合理化表 + **P1 询问自治变体** + **环境接线完备性检查（V2.7.3——2026-09-30 跨项目失败复盘吸收）**

---

## 触发时机

- plan 完成后，执行 `/god` 前
- **architect 激活前置（2026-09-07 加强遵循）**: complexity ≥4 或跨端 plan 在 architect 置 `active` 前（绿灯 = 执行许可唯一签发点，报告写盘为凭证）
- 发现 plan 逻辑漏洞需要验证时
- 跨端功能的规划阶段验证

## 参数

| 参数 | 说明 | 示例 |
|-----|------|------|
| `--skip-typo` | 跳过文案修改验证 | `/sandbox --skip-typo` |
| `--lightweight` | 轻量验证（仅 P2） | `/sandbox --lightweight` |

## 行为约束

- 🚨 **禁止修改业务代码**
- 审计 Agent 必须独立（`Explore` 类型）
- 报告必须物理写盘（路径/命名/签名规范见下）
- P0 问题必须阻断执行

---

## 报告写盘规范（唯一权威，god 验签按此定位）

- **路径**: `.qoder/skills/sandbox/reports/`
- **命名**: `{YYYY-MM-DD}-{planId}-{topic}-沙盘审计.md`（planId 必须携带——god 验签按 planId 精确匹配；历史无 planId 报告不迁移，存量豁免）
- **签名 frontmatter**（写入文件头部，含 plan_id 绑定）:
```yaml
---
plan_id: "{planId}"              # 本次沙盘的 plan（god 验签的匹配键）
signatures:
  sandbox_agent: "{agentId}"   # 独立蓝军 Agent ID；主 Agent 降级执行时填 "self" 并标注 [非独立审计]
  timestamp: "{ISO8601}"
---
```
- **内容**: 审计结果分级（P0/P1/P2）+ 每条【问题】【证据(文件:行号)】【建议】+ 验证级别 + 通过/不通过结论；**P2 例外（V2.6.3）**: P2 记录仅需【编号、描述、源文件、代码位置】四字段（本句为 P2 豁免优先于上述全量表述的显式例外——与 audit issue-record-spec 对齐）
- **机器可断言结构（task-106 W3 · 2026-09-10）**: 报告**尾部**（正文之后）必须附结构行：

```
REPORT-STATS: plan_id={planId} P0={n} P1={n} P2={n} VERDICT={pass|fail}
```

  - **VERDICT 自洽（强制）**: `P0>0 → VERDICT=fail`（P0 零才可 pass；P1 裁量归用户，不影响 pass）；
  - **多轮规范**: 结构行**恒为最新一轮状态**——修订复验后**更新原行**（全文件至多一条，禁止追加第二条）；
  - **消费方**: god 验签结构行解析优先；validate-plans.js §6.8 V-Report 断言；
  - **存量豁免**: 2026-09-10 前报告无结构行不追溯（仅验文件存在 + plan_id 绑定）。

  完整示例（蓝军照抄；其余内容不限格式）:

```markdown
（报告正文……）

---
REPORT-STATS: plan_id=task-XXX P0=0 P1=2 P2=7 VERDICT=pass
```

---

## 核心原则

| 原则 | 说明 |
|-----|------|
| **独立审计** | Subagent 执行，禁止自我审计 |
| **物理写盘** | 报告先写入文件，再发送消息 |
| **简单至上** | Prompt 简洁明确，检查清单外置引用 |

### 规划载体来源判定

| 来源 | 审计执行者 | 原因 |
|-----|-----------|------|
| 当前会话编写 | **独立 Agent**（必须） | 角色隔离 |
| 其他会话/人类编写 | 主 Agent（可） | 已有独立视角 |
| 无法判定 | **独立 Agent**（必须） | 宁可过度审计 |

**规划载体定位（A 阶段：会话隔离）**:
1. **planId 由调用方显式传入**（/sandbox {planId} 或会话内已确定）——禁止从全局 activePlan 间接寻址（多会话并发时会被其他会话改写）
2. **读取切片**: 只读 plans.json 中该 plan 的行（按 id 提取）+ plan 详情文件，禁止全量 JSON 入上下文
3. 若 plan 有 `parent`（子 plan）→ 校验父 plan 的 children 依赖是否闭环（无环、无缺失）
4. 核验 plan 详情/内容的方案质量评分卡是否诚实：对照 plan 文本逐项复核得分，发现虚高（自评 2 分但无证据）→ 报告中标注 [评分卡失实]
5. **核验复杂度评分合理性（防低估）**: 对照 architect §1.5 五维标准复核 plan 的 complexity 值；若发现明显低估（如跨 3 端却评 ≤6）→ 升级验证级别为完整验证 + 拆分完整性，并标注 [复杂度低估]
6. `plans.json` 不存在 → 询问用户

---

## 审计重点

> **核心价值**: 具体功能逻辑规则流转验证，而非公共校验

| 级别 | 维度 | 说明 |
|-----|------|------|
| **P0** | 业务逻辑流转 | 流程闭环、分支完整、边界覆盖 |
| **P0** | 迭代计划逻辑 | 依赖关系、执行顺序、前置条件 |
| **P1** | 状态机流转 | 死锁检测、变量覆盖风险 |
| **P1** | 环境接线完备性（V2.7.3） | 计划中每项环境/外部依赖是否有"期望值 + 可执行落值与验证方式"——仅登记期望值而无落值动作 → 列为挂起条件/执行前必办（"确认≠接线"——2026-09-30 复盘吸收） |
| **P2** | 协议对齐 | 入参/出参闭环、命名一致 |

> **详细检查清单**: [audit/sandbox-checklist.md](./audit/sandbox-checklist.md)

> **需求包引用（条件触发 · 2026-09-16）**: plan/需求基线引用需求包（requirement-converger 产出）时——包留白表「必须对齐」未落定项 → **挂起条件候选**；包自检报告 `REQ-STATS` `fail` → 拒审回退收束（`conditional` ⚠️ 项并入沙盘复核重点）。

---

## 执行流程

### Step 0: 场景判定

**解析 Task 元数据（从 plans.json，非 plan frontmatter——单权威）**:

| 标签 | 含义 | 验证级别 |
|-----|------|---------|
| `complexity ≤3` | 简单 | 简化验证（P0 + P2） |
| `complexity 4-6` | 中等 | 完整验证（P0+P1+P2） |
| `complexity ≥7` | 复杂 | 完整验证 + 拆分完整性 |
| `parent` 非空 | 子 plan | 完整验证 + 父依赖检查 |
| 外部对接 | 涉及外部系统 | 完整验证 + 契约先行检查 |
| `status=revised` | 修订后复验 | **复验轻量（V2.6.3）**: 修订点+原问题清单逐项闭环；绿灯后回迁 active |

**判定逻辑**:

```
IF 标签包含 "typo" OR "文案修改":
    → 跳过验证，绿灯授权

ELIF 涉及 ≥ 2 个子系统:
    → 完整验证（P0+P1+P2）

ELIF Bug 修复 AND 跨端:
    → 完整验证

ELIF 配置修改:
    → 轻量验证（仅 P2）

ELIF status = revised（修订后复验）:
    → 复验轻量: 范围=修订点+原问题清单（V2.6.3）；新面问题→该 plan 原判定验证级别全量（含附加检查项）

ELIF complexity ≥7 OR 子 plan:
    → 完整验证 + 拆分完整性检查

ELSE:
    → 简化验证（P0 + P2）
```

---

**Red Flags 反合理化表（V2.7.1——跳过条款旁对照）**

| 借口 | 现实 |
|------|------|
| "typo 而已不用沙盘" | ≤3 行纯文案才算 typo（01 §2：typo ≤3 行；"纯文案"为本表收严口径）；改了逻辑/配置就不是——走对应验证级别 |
| "文案修改直接跳过" | "typo OR 文案修改→跳过"以 Step 0 判定表为准；文案含逻辑语义变化（状态码/字段名）→ 不属纯文案 |
| "上轮刚沙盘过这轮不用" | 判定看**本轮变更面**：触达方案逻辑 → 完整验证（复验轻量仅限 revised 复验轮） |

---

### Step 1: 启动蓝军 Agent

**Agent 配置（planId 显式传入，禁止子代理读全局游标）**:

```json
{
  "subagent_type": "Explore",
  "prompt": "见 prompts/sandbox-agent.md（必须附 planId 参数）",
  "name": "sandbox-blue-team"
}
```

**输入协议（V2.6.3——事实输入，非结论）**:

1. planId + plan 文件路径（必传）；
2. **方案变更面锚点**（评分卡/拆分/契约章节路径；缺失时蓝军按 plan 全文扫描并标注 [推断模式]）；
3. 验证级别（Step 0 判定结果——复验轮取首轮判定，不重判）；
4. **复验模式标记**（首轮/复验；复验时必传「首轮报告路径」——原问题清单载体；缺失→按 reports 目录 planId 前缀定位兜底，仍不可得→回退首轮全量模式）。

**关键约束**:
- Agent 类型必须为 `Explore`（只读，无修改能力）
- **必须传入 planId**（子代理第 1 步按 planId 读对应 plan，不从 activePlan 间接寻址——并发下会被其他会话改写）
- 不传入编码时的上下文、假设、决策过程
- 审计 Agent 从零读取，无记忆污染

---

### Step 2: 等待审计结果

**超时处理**:

```
首次超时（180秒/3分钟）:
  → 询问用户选择:
    [A] 降级执行（主 Agent 执行，标注[非独立审计]）
    [B] 继续等待（+180秒/+3分钟）

二次超时（+180秒/+3分钟）:
  → 询问用户选择:
    [A] 降级执行
    [B] 继续等待（+180秒/+3分钟）
    [C] 跳过审计（风险自担，需确认）
```

> **设置原因**: Subagent 执行探查任务可能需要较长时间，最短等待时间设为 3 分钟

**消息接收**:

```
审计完成 | 报告路径: {path} | P0: {n} | P1: {n} | P2: {n} | 通过: {是/否} | 验证级别: {级别}
```

---

### Step 3: 处理审计结果

> **复验轻量范围声明（V2.6.3）**: `status=revised` 的复验轮——报告须逐项闭环原问题清单（引用原编号）；发现新面问题（修订点+原清单之外）→ 升级至原判定验证级别全量重审；**修复轮次计数与上限见 x-rules.repairLoop**（`R{n}` 写 plan 详情「修复轮次记录」节）。

**结果判定**:

| 条件 | 动作 |
|-----|------|
| P0 > 0（未达上限） | 🔴 阻断执行，读取报告，**修复轮次计数**（写 plan 详情「修复轮次记录」节——行式 `R{n}: {日期} {触发方} {P0数} {处置}`；n=本 plan 生命周期累计轮次，sandbox 修订+audit 修复合并计数；**append-only 核验**: 复验前验节完整性，缺失/被改写→不判首轮，单链处置: 依账本镜像补录+人工确认，无法补录/确认→按已达上限口径处置）；**将 `plans.json` 中该 plan 的 `status` 更新为 `revised`**（状态机见 plans.schema.json x-rules），打回修正（修正 plan 文件 → /sandbox 重新验证）；**要求 architect 重评 qualityScore**（修订后旧分失真，见 x-rules.stateMachine.owners.to_revised） |
| P0 > 0 且达上限（escalation，见 x-rules.repairLoop） | 🔴 输裁决清单（逐项: 修复/延后/推翻 + 理由 + 若错代价 = Ruling 三要素）→ **落盘（唯一权威 = plan 详情）**: 「修复轮次记录」节追加超限行（`R{n}: {日期} {触发方} 超限 {P0数} {处置}`——append-only）+ 裁决清单全文随行落底；检查点存在时账本追加 `blocked` 行（引用镜像）→ **置 `blocked`**（owners.to_blocked 授权；并在 plan 详情记录原因）→ 升级人工（**不自动收敛**；回转路径: recovery blockedResolve②——人工裁决，读取座标见其条款） |
| P0 = 0 且 P1 > 0 | 🟡 询问用户是否修复 P1 问题（**自治授权变体（V2.7.2）**: 显式授权下不即时询问——落账为 Ruling（检查点账本 ruling 行 + plan 详情「裁决记录」节镜像）+ 报告「**待裁决清单（P1 事后裁决）**」节集中呈现供用户事后裁决；**防伪**: 无落账记录 = 视为未授权（同 god 口径）；**P0 不可授权**——保底不动；同 audit 口径） |
| P0 = 0 且 P1 = 0 | 🟢 绿灯授权；若该 plan 当前为 `revised`（修订后复验），**将 `plans.json` 中该 plan 的 `status` 置回 `active`**（唯一回迁路径，见 plans.schema.json x-rules）并在 plan 详情「修复轮次记录」节追加处置行（`R{n}: {日期} sandbox 回迁 active`——append-only——写入时机矩阵（god 账本节）第 2 类），输出下一步: /god |
| 评分卡失实 | 🟡 标注，询问用户是否要求 architect 重新自评 |
| 复杂度低估 | 🟡 标注并升级验证级别（已在 Step 1 处理），建议 architect 修正 complexity 值 |

**输出格式**:

```
📊 沙盘审计完成

报告路径: {path}
验证级别: {级别}
统计: P0={n}, P1={n}, P2={n}
状态: ✅ 审计通过 / ❌ 审计未通过

[若 P0 > 0（未达上限——修复轮次 R{n}/3）]
🔴 P0 问题阻断:
1. [问题描述]
2. [问题描述]

修正 plan → /sandbox 重新验证

[若 P0 > 0 且达上限（escalation）]
🔴 修复回路超限（R3/3）——裁决清单:
1. [逐项: 修复/延后/推翻 + 理由 + 若错代价]
plans.json: {plan_id} status → blocked
升级人工裁决（不自动收敛）

[若 P0 = 0]
🟢 绿灯授权
下一步: /god
```

---

## 错误处理

| 错误场景 | 处理方式 |
|---------|---------|
| Agent 启动失败 | 询问用户是否降级执行 |
| Agent 超时 | 询问用户：降级/等待/跳过 |
| 报告写入失败 | **不授权绿灯 + 状态动作**：将 `plans.json` 中该 plan 的 `status` 置 `revised` 并标注 [报告写盘失败待重验]（复用 to_revised 所有者，保证"无绿灯必无 active 放行"）；终端输出审计结果并标注 [无报告凭证]；继续执行需用户显式批准（凭证链：写盘成功 → 绿灯 → god 验签） |
| plan 格式错误 | 输出格式示例，请求修正 |
| SendMessage 失败 | 终端输出审计结果（Fallback） |

**SendMessage 失败 Fallback**:

```
IF SendMessage 成功:
    → 完成
ELSE:
    → 终端输出: "审计完成 | 报告路径: {path} | P0: {n} | P1: {n} | P2: {n}"
```

---

## 与 God Skill 衔接

绿灯授权与 P0 阻断的输出路由块见：

> [references/god-handoff.md](./references/god-handoff.md)

---

## 版本信息

| 属性 | 值 |
|-----|-----|
| 版本号 | V2.7.2 |
| 更新时间 | 2026-09-22 |
| 变更摘要 | V2.7.2（09-22）：P1 询问条款自治变体——显式授权下不即时询问（落账 Ruling + 待裁决清单节；P0 不可授权）。V2.7.1（09-22）：Red Flags 反合理化表（跳过条款旁——沙盘捷径场景）。V2.7.0（09-22）：修复回路超限出口（Superpowers breaker 同构）——轮次计数（plan 详情「修复轮次记录」节；sandbox+audit 统一计数含预审计、append-only 核验）+ 达上限 escalation（裁决清单+Ruling+置 blocked+升级人工，不自动收敛）——阈值唯一权威 x-rules.repairLoop；P0 阻断输出块分支化。V2.6.3（09-20）：同构吸收 audit V2.8.0 三机制——复验轻量模式（修订点+原问题清单，新面→原判定级别全量）+ 输入协议（事实输入：锚点/验证级别/复验标记+首轮报告路径）+ P2 简写（四字段对齐 audit）；复验 P2 计数=最新一轮实发现数（确认仍存在的未修原 P2 计入）。V2.6.2（09-16）：需求包引用接线——审计重点增「需求包引用（条件触发）」（留白必须对齐项→挂起候选；REQ-STATS fail→拒审；requirement-converger 联动）。W3（task-106）：报告规范增「机器可断言结构」（REPORT-STATS 尾部结构行 + 多轮规范 + 示例）；sandbox-agent prompt 同步。V2.6.0（09-03）：A 阶段会话隔离 |

> 历史代际明细（V2.4.0 / V2.3.0）见 [references/changelog.md](./references/changelog.md)。

---

## 📚 参考文件 (详细内容)

以下内容已移至独立文件以优化 Token 效率：

- **审计检查清单**: [audit/sandbox-checklist.md](./audit/sandbox-checklist.md)
- **蓝军 Agent Prompt**: [prompts/sandbox-agent.md](./prompts/sandbox-agent.md)
- **God 衔接路由（绿灯/阻断）**: [references/god-handoff.md](./references/god-handoff.md)
- **历史变更详情**: [references/changelog.md](./references/changelog.md)
