# IntelProduct Web - Vue2 + JS 项目

> 本文件为精简索引，详细规范已封装为 Project Skill `intelproduct-web-dev`，CodeBuddy 会按需自动加载。


## 项目概述

- **技术栈**: Vue 2.6 (严格 Options API) + JavaScript + iView UI 4.0 (`view-design`) + Vuex + Vue Router
- **构建工具**: Vue CLI 3
- **目录骨架**: `src/{api, assets, components, plugins, router, utils, views}`
- **CLI**:
  ```bash
  npm run serve    # 开发启动
  npm run build    # 生产构建
  npm run lint     # 代码检查
  ```

## 核心红线（必须遵守）

1. **框架**: Vue 2.6 严格 Options API，禁用 Composition API
2. **UI**: 优先使用 `src/components/` 公共组件（iView 的扩展封装），其次 `view-design` 原生组件
3. **公共组件优先**: 列表/查询/表格必须用 `src/views/commons/`（`ListSearch` / `EditTable` / `ApproveFlow` / `FuzzyMagnify`），禁止裸写
4. **API 隔离**: 网络请求必须封装在 `src/api/[功能名].js`，禁止在 `.vue` 内直接调用 `axios`/`fetch`，必须 `async/await`
5. **样式**: SCSS + `scoped`，全局变量引用 `@/assets/common.scss`
6. **加密**: 由 `process.env.VUE_APP_ENCODE === 'Y'` 控制；SM2 加密 SM4 会话密钥，SM4 加密业务数据，SM3 签名；请求头携带 `headCode` + `enCodeKey`
7. **Sentry**: 关键业务异常必须 `this.$Sentry.captureException` 上报，接口错误由 axios 拦截器自动上报
8. **组件名**: 每个 Vue 组件必须显式声明 `name`
9. **文件体积**: 单个 `.vue` 文件控制在 400 行内，超出必须拆分
10. **新公共组件**: 必须先放 `src/components/pending-review/`，Code Review 通过后方可转正
11. **接口返回**: 统一 `{ code: 0, msg: '', data: {} }` 结构
12. **JS 语法兼容**: 代码必须兼容 ES5/ES6 基础语法；禁用可选链 `?.`、空值合并 `??`、解构默认值；优先使用 `&&`、`||`、三元表达式

## 规范体系

| 资源 | 类型 | 用途 |
|------|------|------|
| `intelproduct-web-dev` Project Skill | 项目知识库 | 组件/API/加密/Sentry/工具函数详细规范（`.codebuddy/skills/`） |
| `taste-skill` Project Skill | UI 审美规范 | 视觉层次、配色、字体、间距、对齐、留白等审美判断（`.codebuddy/skills/`） |
| `.clauderules` | 开发流程规约 | 红线、SOP、跨端接力、复盘机制 |
| `frontend-code-style` User Skill | 通用编码风格 | HTML/CSS/SCSS/JS 通用规范（`~/.codebuddy/skills/`） |
| [开发规范.md](./开发规范.md) | 源文件 | 通用前端编码规范原文，供团队成员查阅 |

## 冲突优先级

当多处规范冲突时，按以下优先级处理：

1. 本文件 `核心红线`（最高）
2. `.clauderules`（流程规约）
3. `intelproduct-web-dev` skill（项目知识库）
4. `taste-skill` skill（UI 审美规范）
5. `frontend-code-style` skill（通用风格）

## 快速参考

- **公共组件用法**: skill reference `components-guide.md`
- **工具函数 API**: skill reference `utils-api.md`
- **SM2/SM4 加密流程**: skill reference `crypto-guide.md`
- **Sentry 配置**: skill reference `sentry-guide.md`
- **项目结构/依赖**: skill reference `project-overview.md`
- **API 参考**: skill reference `api_reference.md`
