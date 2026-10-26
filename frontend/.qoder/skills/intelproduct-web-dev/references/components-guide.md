# Components Guide

## 公共组件优先原则

严禁直接编写基础表格或查询区域，必须优先使用 `src/views/commons/` 下对 iView (ViewUI 4.0) 扩展封装的公共组件。

| 组件名 | 路径 | 核心规范与插槽 (Slots) |
| :--- | :--- | :--- |
| **ListSearch** | `src/views/commons/ListSearch.vue` | 必须使用 `slot="search"` 承载查询条件表单项。 |
| **EditTable** | `src/views/commons/EditTable.vue` | 用于行内编辑或批量操作场景。 |
| **ApproveFlow** | `src/views/commons/ApproveFlow.vue` | 用于展示审批流程图。 |
| **FuzzyMagnify** | `src/views/commons/FuzzyMagnify.vue` | 用于带有模糊查询功能的放大镜输入框。 |

### ListSearch 功能

列表查询封装，包含：标题、查询条件插槽、表格、分页、导出。

### EditTable 功能

可编辑表格，支持：行编辑、按钮操作、分页。

### 公共组件详细说明

参见 `src/components/README.md`。

---

## 页面目录结构

页面位于 `src/views/` 目录：

```
views/
├── commons/           # 通用公共组件
│   ├── ListSearch.vue       # 列表查询封装（标题+查询+表格+分页）
│   ├── EditTable.vue        # 可编辑表格封装
│   ├── ApproveFlow.vue      # 审批流程组件
│   ├── FuzzyMagnify.vue     # 模糊查询放大镜
│   ├── 404.vue              # 404页面
│   ├── SessionFail.vue      # 会话失效
│   └── NoAuthority.vue      # 无权限页面
│
├── mainPage/          # 主页面框架
│   ├── MainPage.vue         # 主容器（Header + Menu + router-view）
│   ├── MainHeader.vue       # 头部导航
│   ├── Menus.vue            # 左侧菜单
│   └── MainFooter.vue       # 底部
│
├── authManage/         # 认证管理模块（业务页面示例）
│   ├── UserManage.vue       # 用户管理
│   ├── RoleManage.vue       # 角色管理
│   ├── AuthManage.vue       # 权限管理
│   └── Logs.vue             # 日志管理
│
└── pages/              # 页面模板
    ├── first/FirstPage.vue  # 示例页面1
    └── second/SecondPage.vue # 示例页面2
```

## 页面开发规范

- 业务页面放在 `views/` 对应模块目录下
- 使用 `mainPage/MainPage.vue` 作为主框架容器
- 页面内使用 `@/components/` 中的公共组件
- 路由配置参见 `src/router.js`

## 架构红线

- **页面视图**: `src/views/[模块名]/[页面名].vue`
- **公共页面组件**: `src/views/commons/`
- **公共基础组件**: `src/components/`
- **新生成的公共组件**: 必须统一放置在独立文件夹 `src/components/pending-review/` 下，严禁直接混入现有成熟组件库，待人工 Code Review 通过后方可转正。

## 典型代码示例

```vue
<template>
  <ListSearch :columns="columns" :data="tableData" @on-search="fetchData">
    <template slot="search">
      <FormItem label="关键词">
        <Input v-model="query.key" v-debounce="fetchData" placeholder="请输入..." />
      </FormItem>
    </template>
  </ListSearch>
</template>

<script>
export default {
  name: 'ExampleList',  // 必须显式声明组件名称
  data() {
    return {
      query: { key: '' },
      columns: [],
      tableData: []
    }
  },
  methods: {
    /**
     * 异步获取业务列表数据
     * @async
     * @returns {Promise<void>}
     */
    async fetchData() {
      try {
        const res = await this.$api.getData(this.query);
        this.tableData = res.data;
      } catch (error) {
        this.$Sentry.captureException(error);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* 优先引用 @/assets/common.scss 中的全局变量 */
</style>
```
