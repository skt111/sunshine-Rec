---
trigger: model_decision
description: 修改核心高频变更文件（对话管线节点、实时通信、注册表/状态机、核心 Controller 等）时生效。受保护文件清单由项目定制层维护（见 §1）。
---

# 核心文件变更保护 (Core File Change Watch)

> 本规则定义修改"核心高频变更文件"时的强制验证步骤。
> **受保护清单由 `/complete-harness` 或人工按 §1 方法校准后填写**（init 生成时不预填业务文件——核心文件随项目演化，禁止模板猜测）。

## 1. 受保护文件清单

> 📌 **校准方法**（生成后由 complete-harness/维护者执行一次）：
> 1. 以 git 历史热点识别核心文件：`git log --since=180.days --name-only --pretty=format: | sort | uniq -c | sort -rn | head -30`（30/90/180 天窗口交叉验证）
> 2. 按架构职责圈定：状态机主图/节点管线、WebSocket/流式路由、注册表与配置源、核心 Controller、前端通信 composable/状态 store/卡片注册表、规划载体（plans.json）
> 3. 聚合装配入口与单业务页面组件**不纳入**核心边界
> 4. 清单填写完成后，与本规则 §2 的 plans.json 附加要求一并生效

| 层级 | 文件 | 核心职责 | 影响范围 |
|-----|------|---------|---------|
| （待校准填写） | | | |

## 2. 修改前强制验证步骤（plans.json 修改附加要求）

> plans.json（若启用 plans 体系）修改附加要求:
> 1. 修改后按 `plans.schema.json` 校验（状态枚举含 revised/blocked、必填字段、parent-children 引用），并运行 `node .qoder/plans/validate-plans.js`（跨字段约束：activePlan 状态/parent 无环/children 镜像/updated 一致），确认 `kanban.html` 渲染无断裂
> 2. **activePlan 约束**: 必须指向 `status ∈ {active, in-progress}` 的 plan
> 3. **父子关系单权威**: parent/children 以 plans.json 为唯一权威，plan 详情文件 frontmatter 不维护（避免双写漂移）
> 4. 更新任一 plan 时同步更新根级 `updated` 字段

## 3. 修改前强制验证步骤（受保护文件通用）

修改受保护文件时**必须**依次执行：

| 步骤 | 动作 | 说明 |
|-----|------|------|
| 1 | 通读目标文件全文 | 禁止凭记忆/局部视图修改 |
| 2 | 通读关联上下文 | 调用方/被调方/数据契约（VO/DTO/协议文档） |
| 3 | 修改后构建验证 | 端级规则定义的标准命令（mvn/npm/uv 等） |
| 4 | 追加/更新单测 | 覆盖修改点与既有行为的回归用例 |
| 5 | 跨端契约核对 | 若影响对外接口，同步更新 api-contract 对应文件 |
| 6 | 变更点自检 | 输出"变更点→影响面→验证结果"三段式说明 |

> ⚠️ 修改 plans.json 且属于核心文件场景时，同时满足 §2 与 §3。
