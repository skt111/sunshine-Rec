---
trigger: model_decision
description: 涉及 2 个及以上子系统（root）的跨端修改、接口契约变更、全栈联调、子系统定位任务
---

# 跨端路由与契约法则

> 来源: `.claude/CLAUDE.md` §2/§3 | 跨端任务时生效

## 1. 子系统寻址（雷达降落）

1. **看地图**: 读取 .qoder/plan/.project_radar.json，锁定目标 Controller/视图。
2. **切目录**: 进入对应子系统（`frontend/` 或 `java_bff/`），遵守路径规则（11-frontend.md / 12-java_bff.md）。
3. **精准阅读**: 直接读取定位到的文件，禁止漫无目的地 grep。

## 2. 路由模式

| 模式 | 顺序 | 适用场景 |
|-----|------|---------|
| Top-Down | 前端(UI) → Java 后端 → AI/Python 层 | UI 需求倒推后端 |
| Bottom-Up | AI/Python 层 → Java 后端 → 前端 | 底层能力推演向外 |
| Single-End | 直接进入目标目录 | 单端闭环 |

## 3. 跨端修改强制要求

1. **契约先行**: 开发前确认 .qoder/api-contract/ 中对应契约（通过 .qoder/plan/.project_radar.json 定位 contract_file）。
2. **同步更新**: 跨层修改必须同步更新对应的 VO/DTO 类与接口定义（前端 VO ↔ Java DTO ↔ Python Schema）。
3. **逐层验证**: 每层更新后立即构建验证（`mvn compile` / `uv run pytest` / `npm run build`）。
4. **变更确认**: 启动前输出跨端变更确认，格式如下：

```markdown
🔄 **跨端变更确认**
本次修改涉及: `[Python / Java / 前端(JS/TS)]`
- 将依次更新各层代码，每层更新后立即构建验证
```

5. **契约同步**: 接口变更必须同步更新 .qoder/api-contract/ 契约文档。
6. **交接写盘**: 层间切换时增量合并到 当前 plan 详情文件"跨端交接"章节（禁止覆盖），记录期望的南向/北向接口路径与 DTO/VO 结构。
7. **命名一致性（强制）**: 跨端字段命名以 Java 端 `@JsonNaming(SnakeCaseStrategy)` 输出为准（全链路 snake_case，含手工 Map key）；前端 TS Interface 与后端 VO 逐字段对齐，严禁两端命名分裂（历史教训：taskName/task_name 混用致字段取空）。

## 4. 强制澄清

跨端新功能开发前必须明确 API 契约（见 `.qoder/rules/02-clarification.md`），严禁跳过澄清直接执行。
