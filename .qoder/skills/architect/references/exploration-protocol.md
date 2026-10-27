# architect 探索与信息收集协议（§1.8）

> 用途: SKILL.md §1.8 全文；复杂度 ≥4 或涉及建表时的数据源确认、表清单核对与 DDL 建模闭环。

**定位**: 为 plan 提供数据建模、逻辑流转等引用信息。让 AI 从"猜"变为"查"——数据源是探索的第一信息约束，缺省即猜测。

**Step A - 数据源确认（必须询问用户，禁止默认）**:

```
## 📋 数据源确认
1. 生产库（只读）: 确认使用现有 MCP 配置？
   - A) 直接查询   B) 先出表清单供核对再查
2. 测试库: 是否有配套测试库？（是否可写，用于 DDL 验证）
3. 权限边界: 可查询 schema 范围？
4. 敏感数据: 结果含敏感字段时是否需脱敏展示？
```

**Step B - 表清单核对闭环（防探查跑偏）**:

```
Oracle MCP 查 all_tables 过滤候选 → 输出核对表：
| 候选表 | 用途判断 | 相关? |
|--------|---------|-------|
| PS_LEAVE_REQUEST | 申请主表 | ✅ |
| PS_EMP           | 员工主数据 | ❌ 已有链路 |
→ 用户确认/纠正后才进入列级 DDL 探查
```

**Step C - DDL 建模闭环（防字段级幻觉）**:

```
产出规范（新表写入 `.qoder/plans/explorations/{topic}-{date}-ddl.sql`）：
- CREATE TABLE + COMMENT ON（表注释 + 每列注释）
- 约束: PK/FK/UNIQUE/CHECK 显式声明
- 索引: 每条附查询路径理由
- 来源标注: 每个字段注明"来自 XX 表 XX 列"或"新增字段（需求 §X 定义）"
```

**产出**: `.qoder/plans/explorations/{topic}-{date}.md`（数据模型/实体关系/状态流转/API 清单/开放问题），plan 通过 frontmatter `exploration` 字段引用（基准目录 `.qoder/plans/explorations/`，validate-plans.js 会校验引用文件存在性）。

**闭环**: 探索(查真实表) → 建模(DDL 全注释) → plan 引用 → God 实施直接用 → Audit 核验实现与 DDL 一致。
