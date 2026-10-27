# Project Overview

## 技术栈

- **基础框架**: Vue 2.6 + JavaScript + iView UI 4.0 (view-design) + Vuex + Vue Router
- **构建工具**: Vue CLI 3 (webpack + npm)
- **风格**: 严格使用 Options API

## 目录结构

```
src/
├── api/           # API 请求模块
├── assets/        # 静态资源
├── components/    # 公共组件 (本项目的核心复用单元)
├── plugins/       # 插件配置 (iView 等)
├── router/        # 路由配置
├── utils/         # 工具函数
├── views/         # 页面视图
├── App.vue        # 根组件
├── main.js        # 入口文件
└── router.js      # 路由定义
```

## 静态资源

```
src/assets/
├── img/
│   ├── common/        # 通用图片
│   ├── head/          # 头部图片
│   ├── menu/          # 菜单图片
│   └── svgImg/        # SVG 图片
├── fonts/             # 字体文件
├── logo.png           # Logo
└── common.scss        # 全局样式
```

## 项目依赖

详见 `package.json`，主要分类：

| 类别 | 说明 |
|------|------|
| 核心框架 | vue, vue-router, vuex |
| UI 组件 | view-design (iView UI), echarts |
| 工具库 | crypto-js, jquery, lodash-es, sm-crypto, xe-utils, vxe-table |
| 编辑器 | tinymce |
| 其他插件 | vuedraggable, vue-preview, vue-lazyload, nprogress, @sentry/browser |

## CLI 常用命令

```bash
npm run serve    # 开发环境启动
npm run int      # 测试环境构建
npm run build    # 生产环境构建
npm run lint     # 代码检查
```

---

## 组件开发规范

所有公共组件位于 `src/components/` 目录。

### 组件文件结构

```vue
<template>
  <!-- 模板内容 -->
</template>

<script>
/*--------
组件说明及使用示例
--------*/
export default {
  name: "ComponentName",  // 必须：组件名称
  props: { /* 属性定义 */ },
  data() { return { /* 数据 */ } },
  methods: { /* 方法 */ }
}
</script>

<style lang="scss" scoped>
/* 样式 */
</style>
```

### Props 定义规范

```javascript
props: {
  // 基础类型
  propA: { type: String, default: "" },
  propB: { type: Number, default: 0 },
  propC: { type: Boolean, default: false },
  // 复杂类型（数组/对象必须使用函数返回）
  propD: { type: Array, default: () => [] },
  propE: { type: Object, default: () => ({}) }
}
```

## API 请求规范

使用 `src/api/` 目录管理 API，结合 `async`/`await`：

```javascript
import { postDataList, getDataList } from "@/api/dataRequst";

async getData() {
  const res = await postDataList('/url', data)
  // 处理响应
}
```

**红线**: 绝对禁止在 `.vue` 组件中直接调用 `axios` 或 `fetch`，所有网络请求必须封装在 `src/api/[功能名].js` 中。

## 路由规范

```javascript
{
  path: '/module/page',
  name: 'PageName',
  component: () => import('@/views/module/Page.vue')
}
```

## 样式规范

- 使用 SCSS 预处理器
- 使用 iView 组件库主题变量
- 组件样式添加 `scoped` 限制作用域
- 全局样式放置 `src/assets/common.scss`

---

## 通用注意事项

1. **组件注册**: 公共组件无需手动 import，通过全局配置自动可用
2. **iView 组件**: 优先使用 `src/components/` 下的公共组件，再使用 `view-design` (iView UI 4.0) 提供的组件，公共组件是对 iView 的扩展封装
3. **兼容旧版**: 部分组件保留旧版实现（如 `tinyMceEditor/old.vue`），新开发请使用新版
4. **加密密钥**: SM2/SM4 密钥对在 `.env` 文件中配置，生产环境需单独管理
5. **Sentry 上报**: 接口错误在 `src/plugins/axios.js` 拦截器中自动上报，无需手动处理
