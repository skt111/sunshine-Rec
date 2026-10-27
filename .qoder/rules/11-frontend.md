---
paths:
- frontend/**
---

# frontend 端规则（Vue 2.6 + iView 4.0 + Vue CLI 3）

> 生成日期: 2026-10-26 | 技术栈: 前端 (Vue/React) → 实测 **Vue 2.6.12 + iView 4.0.1** | 仅当访问 `frontend/**` 下文件时生效
> 来源: `/complete-harness` 按 `frontend/CLAUDE.md`、`frontend/.clauderules`、`package.json` 实际内容补全

## 1. 技术栈与物理环境

- **项目**: IntelProduct Web（阳光秒招前端），包名 `ehr-web`，根目录 `frontend/`
- **框架**: Vue 2.6.12，严格 **Options API**（禁用 Composition API）
- **UI**: iView 4.0.1（`view-design`）；优先 `src/components/` 公共封装，其次原生组件
- **构建**: Vue CLI 3（webpack）
- **样式**: SCSS + `scoped`，全局变量引自 `@/assets/common.scss`
- **语言约束**: 兼容 ES5/ES6，禁用可选链 `?.`、空值合并 `??`、解构默认值
- **本地命令清单**（在仓库根执行，或先 `cd frontend`）:
  - 安装依赖: `npm --prefix frontend install`
  - 开发启动: `npm --prefix frontend run serve`（`--mode development`）
  - 生产构建: `npm --prefix frontend run build`（`--mode production`）
  - 测试环境构建: `npm --prefix frontend run int`（`--mode test`）
  - 代码检查: `npm --prefix frontend run lint`
  - 🚨 **熔断红线**: 构建报错且 2 次无法修复 → 立即向人类求助，严禁带病交接

## 2. 架构红线

- **页面视图**: `src/views/[模块名]/[页面名].vue`；公共页面组件: `src/views/commons/`
- **公共组件优先**: 列表/查询/表格必须用 `ListSearch` / `EditTable` / `ApproveFlow` / `FuzzyMagnify`，禁止裸写表格与查询区
- **新公共组件**: 先进 `src/components/pending-review/`，Code Review 通过后方可转正
- **API 隔离**: 网络请求必须封装在 `src/api/[功能名].js`；禁止在 `.vue` 内直接 `axios`/`fetch`；统一 `async/await`
- **接口返回壳**: 统一 `{ code: 0, msg: '', data: {} }`
- **组件名**: 每个 Vue 组件必须显式声明 `name`
- **文件体积**: 单个 `.vue` 控制在 400 行内，超出必须拆分
- **异常监控**: 关键业务异常必须 `this.$Sentry.captureException`；接口错误由 axios 拦截器自动上报
- **国密链路**: `process.env.VUE_APP_ENCODE === 'Y'` 时由拦截器自动加解密；SM2 加密 SM4 会话密钥、SM4 加密业务数据、SM3 签名（`timestamp + token + data` 字典升序）；请求头携带 `headCode` + `enCodeKey`；公钥登录后写入 `localStorage.pubkey`
- **指令与工具**: 校验用 `v-debounce` / `v-is-figure` / `v-is-required`；日期用 `util.getNowDay()` / `util.changeDay()`；深拷贝用 `util.objDeepCopy()`

## 3. 修改后路径自检 (强制)

每次完成前端代码修改后，必须执行：
1. **构建验证**: `npm --prefix frontend run build` 无报错
2. **路径合规**: 所有 import/引用符合 `@/` 别名规范
3. **物理坐标**: 确认文件位置正确（视图/公共组件/API 分层不越界）

> 自动兜底: `.qoder/settings.local.json` 的 PostToolUse hook 已配置 `node .claude/scripts/smart-build.mjs`，对 `frontend/src/**`、`frontend/public/**` 做静态自检（BOM / 400 行 / 禁用语法 / 组件 name），`node_modules` 就绪时附加单文件 eslint。

## 4. 业务词汇表 (Domain Dictionary)

| 术语 | 代码标识 | 含义 |
| --- | --- | --- |
| ListSearch | `src/views/commons/ListSearch.vue` | 查询+列表容器，查询项走 `slot="search"` |
| EditTable | `src/views/commons/EditTable.vue` | 行内编辑/批量操作表格 |
| ApproveFlow | `src/views/commons/ApproveFlow.vue` | 审批流程图 |
| FuzzyMagnify | `src/views/commons/FuzzyMagnify.vue` | 模糊查询放大镜输入框 |
| 国密链路 | `src/utils/crypto.js` | SM2/SM4/SM3 加解密与签名 |
| 会话密钥 | `enCodeKey` | SM4 会话密钥（SM2 加密后随请求头传输） |
| 密钥指纹 | `headCode` | SM2 加密生成的请求头凭证 |
| 加密开关 | `VUE_APP_ENCODE` | 环境变量，`Y` 时启用国密链路 |

## 5. 跨端交接 (Handoff)

前端完成开发、需要后端接续时，**禁止直接改 `java_bff/`**，必须执行：
1. **更新全局游标**: 在 `.qoder/plans/plans.json` 中更新当前 plan 的 `status`/`updated`
2. **物理写盘交接**: 将 `src/api/` 中封装的请求体/响应体字段增量写入当前 plan 详情文件"跨端交接"章节（全局 `.handoff_state.md` 已退役）
3. **离场分支判定**: 任务未完结输出唤醒警告；任务闭环输出完成汇报并提示验收归档
