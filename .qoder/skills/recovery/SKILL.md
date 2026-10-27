---
name: recovery
description: 断点恢复模式 - 在中断后重建上下文，恢复现场
---

# Recovery Skill - 断点恢复模式入口

气闸舱现场还原器。在中断后重建上下文。

## 触发时机

- 人类执行了 /clear 清理记忆后
- 第二天重新打开电脑后
- 流程意外中断需要接力时
- 会话切换需要恢复现场时

## 执行流程

### 1. 读取大盘（A 阶段：会话锚点优先，禁全局最新猜测）

读取 `.qoder/plans/plans.json`：
- **定位来源（按序）**: ①会话显式声明（用户说"继续哪个"或传入 planId）→ ②in-progress 集合：**列出全部 in-progress plan（id/updated/标题）供用户选择**（主流 = 多会话多 plan，恢复的必须是"本会话的 plan"而非全局最新）→ ③plans.json activePlan（仅当无 in-progress 时作为推荐展示）
- **读取切片**: 定位后只读该 plan 的行 + plan 详情（"跨端交接"章节承载接力上下文），禁止全量 JSON 入上下文
- **校验定位状态**: 若定位的 plan 非 `active`/`in-progress` → 按状态处理：`revised` → 报告待修订（回迁由 sandbox 执行）；`blocked` → 检查详情记录原因；`draft` → 报告未激活（评分 ≥19 后才可激活）；`done`/`archived` → **异常报告**（终态不可置回，要求改指其他 plan）；状态机所有者见 x-rules.stateMachine.owners
- **blocked 解阻检查**: 若定位的 plan 为 `blocked`，检查其 plan 详情记录的原因：
  - 原因 = 依赖未满足（如子 plan 依赖父级 done）→ 依赖已 `done` 则建议置回 `in-progress`（blockedResolve）
  - 原因 = 失败熔断/构建连续失败（TDD 熔断/失败闭环产物）→ **无依赖可查，提示人工解阻后回 god 阶段 3**（失败型 blocked 无自动解阻路径）
  - 原因 = **修复回路超限（escalation 型——x-rules.repairLoop 达上限裁决清单）** → **读取座标: plan 详情「修复轮次记录」节超限行 + 裁决清单全文（唯一权威；检查点「裁决记录」节/账本行为引用镜像）** → 提示人工裁决；**批准继续** → 置回 `in-progress` 并在 plan 详情「修复轮次记录」节追加处置行 + 账本裁决行（**含计数重置声明**——新一轮独立计数）；**批准终止** → 按人工裁决处置 + 在 plan 详情「修复轮次记录」节追加处置行 + 账本裁决行（状态写入遵循既有 owner 规则: done 仅 audit 通过方 / archived 仅归档流程）
- **revised 处理**: 若存在 `revised` plan，在恢复报告中标注"待修订"并建议回 architect 修正后重新沙盘验证；回迁 active 由 sandbox 绿灯统一执行（唯一回迁路径，见 x-rules.stateMachine.revised）
- 降级：`plans.json` 不存在 → 搜索 `.qoder/plans/*.md` 按更新时间排序

### 2. 读取交接上下文（A 阶段：per-plan 交接章节替代全局 handoff）

读取定位 plan 的详情文件"跨端交接"章节：
- 获取上一端留下的微观上下文（API 契约、状态字典等）
- `.handoff_state.md` 已退役为历史档案（2026-10-26 起不再增量写入），不参与当前恢复

### 3. 账本行扫描（V1.3.0——断点定位）

读取定位 plan 的检查点（`checkpoint-{planId}-*.md` 按 updated 取最新）正文「执行账本」尾行与 plan 详情「修复轮次记录」节：
- 输出断点: 最近事件行（`task-complete / fix-round / ruling / blocked`）+ 修复轮次 `R{n}`（上限/计数口径见 x-rules.repairLoop）
- blocked/超限状态判定: 尾行为 `blocked` 或「修复轮次记录」节存在超限行 → 按 §1 blocked 解阻检查（第三类）处理（裁决清单读取座标见其条款）
- 扫描规则: 见 god 检查点规范——账本行容忍 `- ` 前缀（正则 `^-?\s*{ISO}`）

### 4. 代码寻址

结合上述信息：
- 全局搜索定位当前正在修改的核心文件
- 确认需要继续修改的文件清单

## 行为约束

- 🚨 **严禁立刻开始写代码！**
- 必须先输出《现场恢复报告》
- 等待人类回复"批准"后，再进入对应模式

## 恢复确认协议 (Recovery Confirmation Protocol)

**标准恢复**：恢复后必须输出：

```
📍 **现场恢复报告**
- 当前进度: [Task X - Step Y]
- 上游契约: [关键数据结构]
- 账本断点: [执行账本尾行（类型+明细）| 修复轮次 R{n}]
- 待处理问题: [列表]
- 建议模式: [God Mode / Auditor Mode]

[⏸️ 恢复确认] 请回复"批准"后继续
```

**只读模式**：用户声明 `--readonly` 或仅查看进度时：
- 输出恢复报告后直接结束
- 不等待批准，不进入执行模式

🚨 **未经确认直接开始工作 = 恢复失败**

## 输出格式

```
## 现场恢复报告

### 宏观进度
- 当前 plan: [plan ID - 阶段 Y]
- 进度状态: [进行中/待验收/已阻塞]
- 质量门禁: [qualityScore/24（<19 需回 architect 补分）]
- 账本断点: [执行账本尾行（类型+明细）| 修复轮次 R{n}（无则 N/A）]

### 上游契约数据
[关键 API 契约 / 状态字典]

### 当前修改文件
| 文件 | 状态 | 说明 |
|-----|------|------|
| ... | 已完成/进行中 | ... |

### 下一步建议
[建议以 God Mode / Auditor Mode 继续]

---
等待人类批准后进入对应模式。
```

## 示例

### 示例 1: 标准恢复流程

**场景**: 用户执行 `/clear` 后重新唤醒

**AI 输出**:
```markdown
📍 **现场恢复报告**

### 宏观进度
- 当前阶段: Task 3 - Java BFF 实现阶段
- 进度状态: 进行中 (Step 2/4)

### 上游契约数据
- API 契约: `/api/knowledge/search` 已定义
- 请求结构: `KnowledgeSearchRequest { query: string, moduleCode: string }`
- 响应结构: `KnowledgeSearchResponse { results: List<KnowledgeVO> }`

### 当前修改文件
| 文件 | 状态 | 说明 |
|-----|------|------|
| `KnowledgeController.java` | 已完成 | 入口 Controller |
| `KnowledgeService.java` | 进行中 | 核心业务逻辑 |
| `KnowledgeVO.java` | 已完成 | 响应 VO |

### 下一步建议
建议以 **God Mode** 继续，完成 `KnowledgeService.java` 的剩余逻辑。

---

[⏸️ 恢复确认] 请回复"批准"后继续
```

### 示例 2: 只读模式恢复

**场景**: 用户使用 `--readonly` 参数仅查看进度

**AI 输出**:
```markdown
📍 **现场恢复报告 (只读模式)**

### 宏观进度
- 当前阶段: Task 5 - 审计阶段
- 进度状态: 待验收

### 审计结果摘要
- P0 问题: 0 个
- P1 问题: 2 个
- P2 问题: 5 个

### 下一步建议
建议以 **Auditor Mode** 进行最终审计。

---

*只读模式结束，未进入执行模式。*
```

## 版本信息

| 属性 | 值 |
|-----|-----|
| 版本号 | V1.3.0 |
| 更新时间 | 2026-09-22 |
| 变更摘要 | V1.3.0（09-22）：执行账本消费——§3 账本行扫描（断点定位）；blocked 解阻第三类分支（修复回路超限——人工裁决驱动，批准继续/终止两分支）+ 恢复报告增"账本断点"字段。V1.2.0（09-03）：A 阶段会话隔离：定位改会话显式/in-progress 列表选择（禁全局最新猜测）；读取切片化；跨端交接上下文改读 plan 详情章节 |
