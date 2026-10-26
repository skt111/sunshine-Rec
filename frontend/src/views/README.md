# Views 页面说明文档

本文档说明 `src/views/` 目录下各模块的用途和页面开发规范。

---

## 目录结构

```
views/
├── commons/           # 通用公共页面组件
├── mainPage/          # 主页面框架
├── authManage/         # 认证管理模块（业务示例）
└── pages/              # 页面模板
```

---

## 1. mainPage 主框架

**文件**: `mainPage/MainPage.vue`

### 说明
应用主框架容器，包含头部、左侧菜单、路由内容区。

### 页面结构
```
MainPage
├── MainHeader (头部)
├── Menus (左侧菜单)
├── router-view (动态内容区)
└── Loading (加载组件)
```

### 使用方法
所有业务页面通过路由嵌套在 MainPage 的 `<router-view>` 中显示。

---

## 2. commons 公共页面组件

### 2.1 ListSearch 列表查询封装

**文件**: `commons/ListSearch.vue`

#### 说明
整合了查询条件、表格、分页的列表页面封装。

#### 使用方法
```vue
<ListSearch
  :titleName="'页面标题'"
  :headData="columns"
  :dataList="tableData"
  :total="total"
  :btnList="buttons"
  :isSearch="true"
  :isReset="true"
  :isMagnify="true"
  @query="handleQuery"
  @reset="handleReset"
>
  <!-- 查询条件插槽 -->
  <template v-slot:data="{ data }">
    <div>查询表单项</div>
  </template>
</ListSearch>
```

#### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| titleName | String | 页面标题 |
| headData | Array | 表格列配置 |
| dataList | Array | 表格数据 |
| total | Number | 总记录数 |
| btnList | Array | 操作按钮 `[{name, class, url}]` |
| isSearch | Boolean | 是否显示查询按钮 |
| isReset | Boolean | 是否显示重置按钮 |
| isMagnify | Boolean | 是否显示放大镜按钮 |
| isIn | Boolean | 是否显示内部操作区 |
| isOut | Boolean | 是否显示外部操作区 |
| isPage | Boolean | 是否显示分页 |
| isAdd | Boolean | 是否显示新增按钮 |
| isEdit | Boolean | 是否可编辑 |
| iHeight | Number | 表格最小高度 |

#### 事件

| 事件名 | 说明 |
|--------|------|
| query | 点击查询按钮 |
| reset | 点击重置按钮 |
| btnOperate | 点击操作按钮 |
| pageChange | 分页变化 |

---

### 2.2 EditTable 可编辑表格

**文件**: `commons/EditTable.vue`

#### 说明
支持行内编辑的表格封装组件。

#### 使用方法
```vue
<EditTable
  :headData="columns"
  :dataList="tableData"
  :total="total"
  :btnList="buttons"
  :isAdd="true"
  :isEdit="true"
  :isPage="true"
  @query="handleQuery"
  @addInfo="handleAdd"
>
</EditTable>
```

#### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| headData | Array | 表格列配置 |
| dataList | Array | 表格数据 |
| total | Number | 总记录数 |
| btnList | Array | 操作按钮 |
| isAdd | Boolean | 是否显示新增 |
| isEdit | Boolean | 是否可编辑 |
| isPage | Boolean | 是否显示分页 |
| isDefault | Boolean | 是否显示默认操作（下载、放大镜） |
| isMagnify | Boolean | 是否显示放大镜 |
| iHeight | Number | 表格高度 |
| tableClass | String | 表格样式类 |

#### 事件

| 事件名 | 说明 |
|--------|------|
| query | 查询操作 |
| addInfo | 新增操作 |
| btnOperate | 按钮操作 |
| exportInfo | 导出操作 |
| openModal | 打开放大镜 |
| changePage | 分页变化 |

---

### 2.3 ApproveFlow 审批流程

**文件**: `commons/ApproveFlow.vue`

#### 说明
展示审批流程图组件。

#### 使用方法
```vue
<ApproveFlow
  :apprList="approveList"
  @node-click="handleNodeClick"
>
</ApproveFlow>
```

#### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| apprList | Array | 审批节点列表 `[{approvalStatus, ...}]` |

#### 状态说明
- `未提交` - 灰色
- `已提交` - 蓝色
- `已审批` - 绿色

---

### 2.4 FuzzyMagnify 模糊查询放大镜

**文件**: `commons/FuzzyMagnify.vue`

#### 说明
表格数据模糊查询并放大展示的组件。

#### 使用方法
```vue
<FuzzyMagnify
  :columns="columns"
  :dataList="tableData"
  @confirm="handleConfirm"
>
</FuzzyMagnify>
```

---

## 3. mainPage 主框架组件

### 3.1 MainHeader

**文件**: `mainPage/MainHeader.vue`

#### 说明
系统头部导航组件，包含用户信息、退出等功能。

### 3.2 Menus

**文件**: `mainPage/Menus.vue`

#### 说明
左侧菜单组件。

#### 使用方法
```vue
<Menus
  :isShow="isShow"
  :menuData="menuList"
  @toPage="showPage"
>
</Menus>
```

#### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| isShow | Boolean | 菜单是否展开 |
| menuData | Array | 菜单数据 |

---

## 4. 业务页面开发规范

### 4.1 页面创建流程

1. 在 `views/` 对应模块下创建 `.vue` 文件
2. 在 `src/router.js` 中配置路由
3. 使用公共组件完成页面开发

### 4.2 页面开发模式

```vue
<template>
  <div class="pageContainer">
    <!-- 方式1: 使用 ListSearch 封装 -->
    <ListSearch ...>
      <template v-slot:data>
        <!-- 查询条件 -->
      </template>
    </ListSearch>

    <!-- 方式2: 自由组合 -->
    <div class="searchArea">...</div>
    <Table :columns="columns" :data="data"></Table>
    <Page @on-change="changePage"></Page>
  </div>
</template>

<script>
import ListSearch from "@/views/commons/ListSearch";
import { getDataList } from "@/api/dataRequst";

export default {
  name: "BusinessPage",
  components: { ListSearch },
  data() {
    return {
      // 页面数据
    };
  },
  methods: {
    // 业务方法
  }
};
</script>
```

### 4.3 路由配置

在 `src/router.js` 中配置：

```javascript
{
  path: '/module/page',
  name: 'PageName',
  component: () => import('@/views/module/Page.vue')
}
```

嵌套路由示例（显示在 MainPage 内）：

```javascript
{
  path: '/main',
  component: () => import('@/views/mainPage/MainPage'),
  children: [
    {
      path: 'page',
      name: 'PageName',
      component: () => import('@/views/module/Page.vue')
    }
  ]
}
```

---

## 5. 404 / 错误页面

| 页面 | 文件 | 说明 |
|------|------|------|
| 404 | `commons/404.vue` | 页面不存在 |
| 无权限 | `commons/NoAuthority.vue` | 权限不足 |
| 会话失效 | `commons/SessionFail.vue` | 登录失效 |

这些页面在路由守卫中自动触发，无需手动调用。
