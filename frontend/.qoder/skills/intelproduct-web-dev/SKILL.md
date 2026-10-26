---
name: intelproduct-web-dev
description: 本技能适用于在 intelProduct-web 项目中开发（Vue 2.6 Options API + iView UI 4.0 + Vuex + Vue Router）。它提供项目级规范，包括公共组件用法（ListSearch/EditTable/ApproveFlow/FuzzyMagnify）、API 加密的 SM2/SM4 国密算法、Sentry 错误监控、工具函数 API 及目录结构规则。在创建页面或组件、编写 API 请求、实现加密/签名、使用工具函数或在 intelProduct-web 代码库中工作时触发本技能。
---

# IntelProduct Web 开发指南

## 概述

`intelProduct-web` 项目的专属开发指南：基于 Vue 2.6 + iView UI 4.0 的企业级后台脚手架。涵盖组件规范、API 隔离、SM2/SM4 加密、Sentry 监控及工具函数 API。详细参考文档按需加载以保持上下文精简。

## 适用场景

在 `intelProduct-web` 项目中执行以下任一操作时加载本技能：

- 创建或编辑 `.vue` 页面或组件
- 编写 API 请求（必须放在 `src/api/`）
- 使用公共组件（`ListSearch` / `EditTable` / `ApproveFlow` / `FuzzyMagnify`）
- 实现或调试 SM2/SM4 加密/签名
- 使用工具函数（`util.js` / `directives.js` / `menuBtn.js` / `smjs`）
- 配置 Sentry 错误上报
- 在 `src/views/` 中配置路由或页面

## 核心红线（必须遵守）

1. **框架**：Vue 2.6 严格 Options API，禁用 Composition API。
2. **UI 库**：iView UI 4.0（`view-design`）。优先使用 `src/components/` 封装组件而非原生 iView 组件。
3. **公共组件优先**：绝不手写原生表格/查询区域。使用 `src/views/commons/` 组件（`ListSearch` / `EditTable` / `ApproveFlow` / `FuzzyMagnify`）。
4. **API 隔离**：所有网络请求必须放在 `src/api/[功能名].js`。禁止在 `.vue` 文件中直接调用 `axios`/`fetch`。使用 `async/await`。
5. **样式**：SCSS + `scoped`。引用 `@/assets/common.scss` 中的全局变量。
6. **加密**：由 `process.env.VUE_APP_ENCODE === 'Y'` 控制。SM2 加密 SM4 会话密钥；SM4 加密数据；SM3 用于签名。请求头携带 `headCode` + `enCodeKey`。
7. **Sentry**：关键业务逻辑用 try-catch 包裹并通过 `this.$Sentry.captureException` 上报。API 错误由 axios 拦截器自动上报。
8. **文件大小**：单个 `.vue` 文件应保持在 400 行以内；接近时需拆分。
9. **新组件**：新建公共组件放入 `src/components/pending-review/`，经代码审查后方可合并入主库。
10. **组件名**：每个 Vue 组件必须在 Options API 中显式声明 `name`。
11. **API 响应**：所有接口返回 `{ code: 0, msg: '', data: {} }` 结构。

## 参考文档使用方式

详细文档在 `references/` 中。按需加载相关文档而非全部读取：

| 参考文档 | 涵盖内容 | 加载时机 |
|-----------|--------|-----------|
| `project-overview.md` | 技术栈、目录、依赖、CLI、组件/API/路由/样式约定、props 规则 | 新建页面/组件、检查项目结构时 |
| `components-guide.md` | 公共组件用法、页面目录结构、架构红线、代码示例 | 构建列表/表单/表格页、放置新组件时 |
| `utils-api.md` | `util.js` / `directives.js` / `menuBtn.js` / `smjs` API 参考 | 使用日期/cookie/复制工具、应用 v-debounce/v-is-figure 指令时 |
| `api_reference.md` | 请求方法清单、加密机制、使用示例、响应规范 | 编写接口请求、处理文件下载、调试加密请求时 |
| `crypto-guide.md` | SM2/SM4 加密流程、签名机制、请求头规范 | 实现加密、调试加密请求、编写签名逻辑时 |
| `sentry-guide.md` | Sentry 配置、错误上报示例、使用规则 | 添加错误上报、调试监控时 |

### 快速检索模式

```
# 查找工具函数
grep -n "util\.\|getNowDay\|changeDay\|objDeepCopy\|format" references/utils-api.md

# 查找指令
grep -n "v-debounce\|v-is-figure\|v-is-required\|v-clickoutside" references/utils-api.md

# 查找请求方法
grep -n "postDataList\|getList\|deleteList\|postFileList\|getFileList\|noEncode" references/api_reference.md

# 查找加密方法
grep -n "sm2\|sm4\|sign\|headCode\|enCodeKey\|VUE_APP_ENCODE" references/crypto-guide.md

# 查找公共组件用法
grep -n "ListSearch\|EditTable\|ApproveFlow\|FuzzyMagnify\|slot=" references/components-guide.md

# 查找 CLI 命令
grep -n "npm run\|serve\|build\|lint" references/project-overview.md
```

## 与其他文件的关系

- **`CLAUDE.md`**（项目根目录）：常驻项目上下文，保持精简；本技能承载详细规范。
- **`.clauderules`**：开发工作流规则（红线、SOP、交接、复盘）。与本技能互补——`.clauderules` 定义*流程*，本技能定义*知识*。
- **`frontend-code-style` 用户技能**：通用 HTML/CSS/SCSS/JS 编码风格。本技能为项目专属；样式冲突时以本技能为准。

## 资源

### references/

- `project-overview.md` —— 技术栈、目录结构、依赖、CLI 命令、组件/API/路由/样式约定、Props 定义
- `components-guide.md` —— 公共组件用法指南、页面目录结构、架构红线、典型代码示例
- `api_reference.md` —— API 请求方法清单（postDataList/getList/deleteList/postFileList/getFileList）、加密机制、使用示例、响应规范
- `utils-api.md` —— 工具函数 API 参考（util.js、directives.js、menuBtn.js、smjs）
- `crypto-guide.md` —— SM2/SM4 国密加密流程、签名机制、请求头规范
- `sentry-guide.md` —— Sentry 监控配置、错误上报示例、使用规则
