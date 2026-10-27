# Architect Skill - Detailed Examples

> 本文件为 architect skill 的详细示例参考，避免主 skill Token 膨胀。
> ⚠️ 示例中的业务场景与文件名为占位示例，**按实际项目替换**（结构与方法论通用）。

## 示例 1: 跨端新功能架构规划

### 用户需求
"实现文档智能问答功能，用户输入问题后系统从检索库检索相关文档片段并生成回答"

### 执行过程

```markdown
## 架构规划报告

### 需求分析
- 核心需求：用户提问 → 检索 → LLM 生成回答 → 返回结果
- 边界确认：
  - 问题长度限制：≤ 500 字符
  - 返回片段数：Top-K = 5
  - 超时控制：≤ 10 秒
- 涉及端：frontend (Vue) → Java BFF → Python AI (向量库 + LLM)

### 现状扫描
**前端层**:
- `src/views/AskPage.vue` - 问答主页面
- `src/api/search.js` - 现有检索 API 调用

**Java 层**:
- `SearchController.java` - 检索路由入口
- `SearchService.java` - 业务逻辑层
- `SearchVO.java` - 响应 VO

**Python 层**:
- `retrieval_service.py` - 向量检索服务
- `llm_service.py` - LLM 调用服务

### 实现路径
**推荐路径** (Top-Down):
1. 前端设计问答 UI 组件
2. 定义 API 契约 (Request/Response)
3. Java BFF 实现问答接口
4. Python 实现检索 + 生成逻辑
5. 跨端联调验证

**证据支撑**:
- 现状扫描发现已有 `retrieval_service.py` 和 `llm_service.py`，可复用现有能力
- `SearchController.java` 已有检索路由，扩展 `/ask` 接口侵入性最小

**备选路径** (Bottom-Up):
- 先实现 Python 核心能力，再向外推演
- 证据: 无明显优势，现状扫描显示前端和 Java 层结构清晰，Top-Down 更高效
- 缺点: 前端等待时间长

### API 契约草案

**POST /api/search/ask**

Request:
```json
{
  "question": "string (≤500字符)",
  "scopeId": "string",
  "topK": 5
}
```

Response:
```json
{
  "answer": "string",
  "sources": [
    {
      "docId": "string",
      "fragment": "string",
      "score": 0.95
    }
  ],
  "latency": 1234
}
```

### 任务拆解
[ ] 阶段 1: 前端问答组件开发 (AskPage.vue)
[ ] 阶段 2: Java BFF 问答接口实现 (SearchController.ask)
[ ] 阶段 3: Python 向量检索优化 (retrieval_service.search)
[ ] 阶段 4: Python LLM 生成集成 (llm_service.generate)
[ ] 阶段 5: 跨端联调与性能测试
```

**⏸️ 挂起等待批准**: API 契约草案已输出，等待用户确认后继续

---

## 示例 2: 单端 Bug 排查架构分析

### 用户需求
"用户反馈列表加载很慢，需要排查优化"

### 执行过程

```markdown
## 架构规划报告

### 需求分析
- 问题现象：列表加载耗时 > 5 秒
- 排查范围：前端渲染 + 后端查询 + 数据库索引
- 优化目标：加载时间 < 1 秒

### 现状扫描
**调用链溯源**:
```
ListPage.vue
  → GET /api/list
    → ListController.list()
      → ListService.findAll()
        → ListRepository.findAll()
          → SELECT * FROM business_table
```

**发现的问题**:
1. 前端：列表渲染未使用虚拟滚动，1000+ 条数据全量渲染
   - 证据: `ListPage.vue:45` 使用 `v-for` 无虚拟滚动
2. 后端：查询未使用分页，一次性加载全表
   - 证据: `ListService.java:32` 直接调用 `findAll()` 无分页参数
3. 数据库：`created_at` 字段无索引，排序效率低
   - 证据: `SELECT * FROM business_table ORDER BY created_at DESC` 全表扫描

### 实现路径
**推荐路径** (侵入性最小):
1. 数据库：添加 `created_at` 索引
2. 后端：引入分页查询
3. 前端：引入虚拟滚动组件

**证据支撑**:
- 数据库优化最简单，现状扫描发现 `created_at` 是排序字段
- 后端已有分页依赖（扫描构建文件发现），无需引入新依赖
- 前端 UI 框架支持虚拟滚动组件

### 任务拆解
[ ] 阶段 1: 数据库索引优化
[ ] 阶段 2: 后端分页查询实现
[ ] 阶段 3: 前端虚拟滚动改造
[ ] 阶段 4: 性能验证 (目标 < 1 秒)
```

**简化路径**: 单端优化，无需 API 契约推演，可直接进入执行

---

## 示例 3: 信息缺失挂起场景 (NEW)

### 用户需求
"实现用户权限管理系统"

### 执行过程

```markdown
## 架构规划报告

### 需求分析
- 核心需求：用户权限管理系统
- 边界确认：❌ 边界不明确

### 现状扫描
**发现问题**:
- 需求过于模糊，无法确定范围
- 未明确是 RBAC、ABAC 还是简单权限控制
- 未明确权限粒度（菜单级、按钮级、数据级）

### ⏸️ 信息缺失挂起

**缺失信息**:
1. 权限模型类型：
   - RBAC (基于角色)
   - ABAC (基于属性)
   - 简单权限控制

2. 权限粒度：
   - 菜单级
   - 按钮级
   - 数据级

3. 用户规模：
   - 小型系统 (<100 用户)
   - 中型系统 (100-1000 用户)
   - 大型系统 (>1000 用户)

**建议**: 调用 grill-me 进行结构化追问，澄清缺失信息
```

**AI 向用户询问**:
> 需要澄清以下信息才能继续规划：
>
> 1. **权限模型类型**：您需要 RBAC (基于角色)、ABAC (基于属性) 还是简单权限控制？
>    - 推荐: RBAC - 适合大多数业务场景，易于理解和维护
>
> 2. **权限粒度**：需要控制到哪个级别？
>    - 推荐: 按钮级 - 平衡安全性和开发成本
>
> 是否需要启动追问模式 (grill-me) 详细澄清每个决策点？
