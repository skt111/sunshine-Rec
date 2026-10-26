# 公共组件说明文档

本文档详细说明 `src/components/` 目录下所有公共组件的用途、参数和使用方法。

---

## 目录

1. [Dialog 对话框](#1-dialog-对话框)
2. [UploadFile 文件上传](#2-uploadfile-文件上传)
3. [muchUploadFile 批量文件上传](#3-muchuploadfile-批量文件上传)
4. [muchUploadFileImg 图片批量上传](#4-muchuploadfileimg-图片批量上传)
5. [Import 数据导入](#5-import-数据导入)
6. [OrganizationTree 组织树](#6-organizationtree-组织树)
7. [StructureTree 结构树](#7-structuretree-结构树)
8. [StaffTree 员工树](#8-stafftree-员工树)
9. [tabTree 标签树](#9-tabtree-标签树)
10. [StructrueTreeTab 树形标签页](#10-structruetreetab-树形标签页)
11. [MultipleSelect 多选下拉](#11-multipleselect-多选下拉)
12. [ChangeChoice / ChangeChoiceNew 选择组件](#12-changechoice--changechoicenew-选择组件)
13. [SelectFilter 筛选过滤](#13-selectfilter-筛选过滤)
14. [Tab / Pane 标签页](#14-tab--pane-标签页)
15. [Loading 加载组件](#15-loading-加载组件)
16. [Header 头部组件](#16-header-头部组件)
17. [LeftMenu 左侧菜单](#17-leftmenu-左侧菜单)
18. [Menu 菜单组件](#18-menu-菜单组件)
19. [MagnifySlot 放大镜插槽](#19-magnifyslot-放大镜插槽)
20. [tinyMceEditor 富文本编辑器](#20-tinymceeditor-富文本编辑器)

---

## 1. Dialog 对话框

**文件**: `Dialog.vue`

### 说明
封装 iView Modal 的对话框组件，提供确认、取消等操作。

### 使用方法
```vue
<Dialog ref="dialog" :content="content" :type="type" @onOk="dialogOk" @onClose="dialogClose"></Dialog>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| content | String | "" | 弹窗内容（支持 HTML） |
| type | String | "sure" | 弹窗类型: `confirm`(取消+确认), `sure`(仅确认), `sureTo`(确认+回调) |
| dialogClass | String | "" | 自定义样式类 |

### 事件

| 事件名 | 说明 | 返回值 |
|--------|------|--------|
| onOk | 点击确定按钮 | `{ btnType: element }` |
| onClose | 点击取消按钮 | `{ btnType: element }` |
| onOkTo | sureTo 类型确认回调 | 无 |

### 方法

| 方法名 | 说明 | 参数 |
|--------|------|------|
| show(ele) | 显示对话框 | ele: 触发元素信息 |
| hide() | 隐藏对话框 | - |

### 示例
```javascript
// 显示确认对话框
this.$refs.dialog.show();
// 隐藏
this.$refs.dialog.hide();
```

---

## 2. UploadFile 文件上传

**文件**: `UploadFile.vue`

### 说明
单文件上传组件，支持上传、预览、删除。

### 使用方法
```vue
<UploadFile :list="files" :upUrl="uploadUrl" :isHome="false" :isUpdate="true" @getFile="handleFile"></UploadFile>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| list | Array | [] | 已上传文件列表 `[{fileName, attachLink}]` |
| upUrl | String | "/indMan/file/upload/batch?fileType=picLogo" | 上传接口地址 |
| postData | String | "" | 上传时附带的参数 |
| isHome | Boolean | false | 首页模式（只展示一个文件） |
| isUpdate | Boolean | true | 是否可编辑上传 |
| uploadType | String | "" | 上传类型，如 `importAndExport` |
| businessType | String | "" | 业务类型 |
| showList | Boolean | true | 是否显示文件列表 |

### 事件

| 事件名 | 说明 | 返回值 |
|--------|------|--------|
| getFile | 文件列表变化 | `uploadType=importAndExport` 时返回 response，其他返回 list |

### 文件对象结构
```javascript
{
  fileName: "文件名.xls",    // 文件名
  attachLink: "/path/to/file" // 文件路径
}
```

---

## 3. muchUploadFile 批量文件上传

**文件**: `muchUploadFile.vue`

### 说明
多文件批量上传组件，支持批量上传、删除管理。

### 使用方法
```vue
<muchUploadFile ref="upload" :fileList="fileList" :upUrl="uploadUrl" @getFile="handleFileList"></muchUploadFile>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| fileList | Array | [] | 已上传文件列表 |
| upUrl | String | "" | 上传接口地址 |
| isUpdate | Boolean | true | 是否可编辑 |

### 事件

| 事件名 | 说明 | 返回值 |
|--------|------|--------|
| getFile | 文件列表变化 | 文件列表数组 |

---

## 4. muchUploadFileImg 图片批量上传

**文件**: `muchUploadFileImg.vue`

### 说明
图片批量上传组件，支持图片预览、删除。

### 使用方法
```vue
<muchUploadFileImg ref="uploadImg" :imgList="imgList" :upUrl="uploadUrl" @getImg="handleImgList"></muchUploadFileImg>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| imgList | Array | [] | 已上传图片列表 |
| upUrl | String | "" | 上传接口地址 |
| isUpdate | Boolean | true | 是否可编辑 |

### 事件

| 事件名 | 说明 | 返回值 |
|--------|------|--------|
| getImg | 图片列表变化 | 图片列表数组 |

---

## 5. Import 数据导入

**文件**: `Import.vue`

### 说明
Excel 数据导入组件，只接受 `.xls/.xlsx` 文件。

### 使用方法
```vue
<Import :importUrl="importUrl" :postData="postData"></Import>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| importUrl | String | "/indMan/excel/indexInput" | 导入接口地址 |
| postData | Object | `{ date: "GHQ" }` | 上传时附带的参数 |

### 限制
- 文件大小限制: 30MB
- 只接受 `.xls`, `.xlsx` 格式

---

## 6. OrganizationTree 组织树

**文件**: `OrganizationTree.vue`

### 说明
组织机构树形选择组件，支持单选/多选、懒加载、搜索。

### 使用方法
```vue
<OrganizationTree
  v-model="orgId"
  :text="orgText"
  :checkbox="false"
  :lazyLoad="true"
  :treeData="orgTreeData"
  :callback="handleSelect"
  @getData="handleTreeData"
></OrganizationTree>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value / v-model | String | - | 选中节点 ID，单选返回 ID，多选返回 `;` 分隔的 ID |
| text | String | - | 选中节点显示文本 |
| checkbox | Boolean | false | 是否多选模式 |
| lazyLoad | Boolean | true | 是否懒加载 |
| treeData | Array | - | 树数据 |
| cache | Boolean | false | 是否缓存数据（表格嵌套时使用） |
| checkAuth | Boolean | true | 是否权限校验 |
| disabled | Boolean | false | 是否禁用 |
| treeStyle | Object | - | 自定义树容器样式 |
| callback | Function | - | 选择节点回调函数 |

### 返回值格式
- 单选: `"10000001"` 或 `"10000001-集团"` / `"集团"`
- 多选: `"10000001;10000002;20080000"` 或 `"10000001-信报, 10000002-产险"`

---

## 7. StructureTree 结构树

**文件**: `StructureTree.vue`

### 说明
通用结构树组件，支持多种配置。

### 使用方法
```vue
<StructureTree
  :data="treeData"
  :render="renderFunc"
  :loadData="loadDataFunc"
  :multiple="false"
  @on-select="handleSelect"
></StructureTree>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| data | Array | - | 树数据 |
| render | Function | - | 自定义渲染函数 |
| loadData | Function | - | 懒加载数据函数 |
| checkStrictly | Boolean | - | check-strictly 模式 |
| showCheckbox | Boolean | - | 显示复选框 |
| multiple | Boolean | - | 是否多选 |

---

## 8. StaffTree 员工树

**文件**: `StaffTree.vue`

### 说明
员工选择树组件，支持按组织筛选员工。

### 使用方法
```vue
<StaffTree
  v-model="staffId"
  :staffData="staffData"
  :orgId="orgId"
  :multiple="false"
  @getStaff="handleStaff"
></StaffTree>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value / v-model | String/Array | - | 选中员工 ID |
| staffData | Array | - | 员工数据 |
| orgId | String | - | 组织 ID 筛选 |
| multiple | Boolean | false | 是否多选 |
| disabled | Boolean | false | 是否禁用 |

---

## 9. tabTree 标签树

**文件**: `tabTree.vue`

### 说明
带标签页的树形结构组件，支持标签切换和树形展示。

### 使用方法
```vue
<tabTree
  :tabList="tabList"
  :treeData="treeData"
  @tab-change="handleTabChange"
  @node-click="handleNodeClick"
></tabTree>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| tabList | Array | - | 标签页列表 |
| treeData | Object | - | 树形数据 |
| defaultSelect | String | - | 默认选中节点 |

---

## 10. StructrueTreeTab 树形标签页

**文件**: `StructrueTreeTab.vue`

### 说明
树形结构配合标签页展示的组件。

### 使用方法
```vue
<StructrueTreeTab
  :data="treeData"
  :tabs="tabConfig"
  @tab-change="handleChange"
  @node-select="handleSelect"
></StructrueTreeTab>
```

---

## 11. MultipleSelect 多选下拉

**文件**: `MultipleSelect.vue`

### 说明
多选下拉组件，支持模糊搜索。

### 使用方法
```vue
<MultipleSelect
  :values="selectedValues"
  :selectList="options"
  @onchange="handleChange"
></MultipleSelect>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| values | Array | [] | 默认选中值 |
| selectList | Array | [] | 下拉选项 `[{content, code, checked}]` |
| disabled | Boolean | false | 是否禁用 |
| isFuzzy | Boolean | false | 是否启用模糊搜索 |
| position | String | "bottom" | 下拉展开位置 `top/bottom` |

### selectList 数据结构
```javascript
[
  { content: "选项1", code: "opt1", checked: false },
  { content: "选项2", code: "opt2", checked: true }
]
```

### 事件

| 事件名 | 说明 | 返回值 |
|--------|------|--------|
| onchange | 选中变化 | 选中项数据 |

---

## 12. ChangeChoice / ChangeChoiceNew 选择组件

**文件**: `ChangeChoice.vue`, `ChangeChoiceNew.vue`

### 说明
下拉选择组件，New 版本为更新迭代版本。

### 使用方法
```vue
<ChangeChoice
  :value="selectedValue"
  :options="optionList"
  @change="handleChange"
></ChangeChoice>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String/Number | - | 选中值 |
| options | Array | [] | 选项列表 |
| disabled | Boolean | false | 是否禁用 |

---

## 13. SelectFilter 筛选过滤

**文件**: `SelectFilter.vue`

### 说明
筛选条件过滤组件，支持多种筛选条件组合。

### 使用方法
```vue
<SelectFilter
  :filterList="filters"
  @confirm="handleFilter"
></SelectFilter>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| filterList | Array | [] | 筛选条件列表 |

---

## 14. Tab / Pane 标签页

**文件**: `tabs/Tab.vue`, `tabs/Pane.vue`

### 说明
标签页切换组件，包含 Tab 容器和 Pane 内容面板。

### 使用方法
```vue
<Tab v-model="currentTab">
  <Pane label="标签1" name="1">
    内容1
  </Pane>
  <Pane label="标签2" name="2">
    内容2
  </Pane>
</Tab>
```

### Tab 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value / v-model | String | - | 当前激活的标签 name |

### Pane 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| label | String | - | 标签显示名称 |
| name | String | - | 标签标识值 |

### 事件

| 事件名 | 说明 | 返回值 |
|--------|------|--------|
| tabClick | 点击标签 | 当前标签 name |

---

## 15. Loading 加载组件

**文件**: `Loading.vue`

### 说明
全屏/局部加载遮罩组件。

### 使用方法
```vue
<Loading :spinShow="loading"></Loading>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| spinShow | Boolean | false | 是否显示加载状态 |

---

## 16. Header 头部组件

**文件**: `Header.vue`

### 说明
系统头部导航组件。

### 使用方法
```vue
<Header :userInfo="userInfo" @logout="handleLogout"></Header>
```

---

## 17. LeftMenu 左侧菜单

**文件**: `LeftMenu.vue`

### 说明
左侧导航菜单组件。

### 使用方法
```vue
<LeftMenu
  :menuList="menus"
  :activeName="currentMenu"
  @select="handleSelect"
></LeftMenu>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| menuList | Array | [] | 菜单列表 |
| activeName | String | - | 当前激活菜单 |
| collapsed | Boolean | false | 是否折叠 |

---

## 18. Menu 菜单组件

**文件**: `Menu.vue`

### 说明
通用菜单组件。

### 使用方法
```vue
<Menu :menus="menuData" @click="handleMenuClick"></Menu>
```

---

## 19. MagnifySlot 放大镜插槽

**文件**: `magnifySlot/MagnifySlot.vue`

### 说明
表格列扩展插槽组件，支持列展开查看详情。

### 使用方法
```vue
<MagnifySlot
  :columns="columns"
  :data="tableData"
  @view-detail="handleDetail"
></MagnifySlot>
```

### 配套文件
- `magnifySlot/columns.js` - 列配置定义

---

## 20. tinyMceEditor 富文本编辑器

**文件**: `tinyMceEditor/tinyMceEditor.vue`

### 说明
TinyMCE 富文本编辑器封装组件，支持富文本编辑、格式化。

### 使用方法
```vue
<tinyMceEditor
  v-model="content"
  :disabled="false"
  :plugins="plugins"
  :toolbar="toolbar"
></tinyMceEditor>
```

### 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value / v-model | String | - | 编辑内容 HTML |
| disabled | Boolean | false | 是否禁用 |
| plugins | Array | - | 插件配置 |
| toolbar | String | - | 工具栏配置 |

---

## 组件开发注意事项

### 1. Props 传递规范
- 基础类型使用直接值
- 数组/对象类型必须使用函数返回默认值
- 必填参数添加注释说明

### 2. 事件命名
- 使用 `on` 前缀表示回调: `@onOk`, `@onChange`
- 使用 `get` 前缀表示数据获取: `@getFile`, `@getData`

### 3. 双向绑定
- 使用 `value` + `@input` 实现
- 或使用 `v-model` 简化写法

### 4. 样式隔离
- 组件样式添加 `scoped`
- 需要影响子组件时使用 `/deep/` 或 `::v-deep`
