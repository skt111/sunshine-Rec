# Visual System — 视觉系统令牌

本文件是视觉取值的唯一事实来源。当需要选择颜色、字号、间距、圆角、阴影时加载本文件。

## 一、色彩系统

### 主色（Primary）

用于主要操作、选中状态、品牌强调。全项目仅一个主色族。

| Token | 色值 | 用途 |
|-------|------|------|
| primary-light | `#5cadff` | hover / 浅色背景 |
| primary-base | `#2d8cf0` | 默认主色（按钮、链接、选中） |
| primary-dark | `#2b85e4` | active / pressed |

> 与 iView UI 4.0 默认主色 `#2d8cf0` 一致。

### 语义色（Semantic）

每种语义色仅用于其语义场景，禁止挪作装饰。

| Token | 色值 | 语义 |
|-------|------|------|
| success | `#19be6b` | 成功、完成、正向 |
| warning | `#ff9900` | 警告、需注意 |
| error | `#ed4014` | 错误、失败、危险操作 |
| info | `#909399` | 中性提示信息 |

### 中性灰阶（Neutral）

构成界面 80% 的颜色——背景、边框、文字、分割线。

| Token | 色值 | 用途 |
|-------|------|------|
| text-primary | `#303133` | 主要文字（标题、正文） |
| text-regular | `#606266` | 常规文字 |
| text-secondary | `#909399` | 次要文字（说明、时间） |
| text-placeholder | `#c0c4cc` | 占位符、禁用文字 |
| border-base | `#dcdfe6` | 边框、分割线 |
| border-light | `#e4e7ed` | 浅边框 |
| border-lighter | `#ebeef5` | 更浅边框/表格线 |
| bg-page | `#f5f7fa` | 页面背景 |
| bg-card | `#ffffff` | 卡片/容器背景 |
| bg-hover | `#f3f3f3` | hover 背景 |

### 色彩使用红线

- **禁止**用语义色做装饰（红色按钮不代表"删除"时不要用红色）
- **禁止**在文字上使用低于 4.5:1 对比度的颜色组合
- **禁止**引入调色盘外的随意 hex 值——需要新颜色先扩展本表
- 配色比例建议：主色 10%，语义色 5%，中性灰 85%

---

## 二、字体排版系统

### 字体族

```css
font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
             "Hiragino Sans GB", "Microsoft YaHei", "Helvetica Neue", Helvetica,
             Arial, sans-serif;
```

代码字体：
```css
font-family: "SF Mono", "Fira Code", "Consolas", "Microsoft YaHei", monospace;
```

### 字号层次（Modular Scale）

基于 14px 基准，1.25 比例的模数尺度。

| Token | 字号 | 行高 | 字重 | 用途 |
|-------|------|------|------|------|
| display | 28px | 1.3 | 600 | 仪表盘大数字 |
| h1 | 24px | 1.35 | 600 | 页面主标题 |
| h2 | 20px | 1.4 | 600 | 区块标题 |
| h3 | 18px | 1.4 | 500 | 卡片标题 |
| body | 14px | 1.5 | 400 | 正文（默认） |
| body-lg | 16px | 1.5 | 400 | 重要正文/表格表头 |
| caption | 12px | 1.5 | 400 | 辅助说明、时间戳、标签 |

### 字重使用

- `400` regular：正文默认
- `500` medium：小标题、强调
- `600` semibold：页面/区块大标题
- **禁止**使用 `700` bold（过粗，屏幕上不协调）
- **禁止**用字重代替字号做层次（小号 bold 不如大号 regular 清晰）

### 行高规则

- 标题：`1.2 - 1.4`（紧凑）
- 正文：`1.5 - 1.6`（舒适阅读）
- 按钮/标签：`1`（单行，配合 height 控制）

### 字间距

- 中文：默认即可，无需 letter-spacing
- 英文大写标签/按钮：`letter-spacing: 0.5px`（轻微透气）
- **禁止**给中文加 letter-spacing（会导致断行异常）

---

## 三、间距系统（8px Grid）

所有间距值必须是 4 的倍数，优先使用 8 的倍数。

| Token | 值 | 典型用途 |
|-------|----|---------|
| space-xs | 4px | 图标与文字间距、紧凑组内 |
| space-sm | 8px | 相关元素间距、标签内边距 |
| space-md | 12px | 表单项间距（紧凑模式） |
| space-base | 16px | 默认间距、表单项间距、卡片内边距 |
| space-lg | 24px | 区块内间距、卡片间距 |
| space-xl | 32px | 区块间距 |
| space-2xl | 48px | 大区块间距、页面级留白 |

### 间距红线

- **禁止**使用 `7px`、`13px`、`15px` 等非 4 倍数值（除非修正渲染亚像素）
- **禁止**用 `margin` 做对齐——用 flex/grid 的 `gap`、`align-items`、`justify-content`
- 内边距（padding）< 外边距（margin）：容器内留白小于容器间留白

---

## 四、圆角系统

| Token | 值 | 用途 |
|-------|----|------|
| radius-sm | 2px | 按钮、输入框、小标签 |
| radius-base | 4px | 卡片、下拉面板、弹层 |
| radius-lg | 8px | 大卡片、图片容器、头像 |
| radius-full | 9999px | 胶囊标签、圆形头像、开关 |

### 圆角规则

- 同一视觉层级保持一致圆角
- 圆角越大越"柔和/休闲"，后台系统偏小圆角（2-4px）保持专业感
- 嵌套元素内层圆角 < 外层圆角

---

## 五、阴影与层级（Elevation）

扁平为默认。阴影仅用于"浮起"的元素，按层级递进。

| Token | 阴影值 | 用途 |
|-------|--------|------|
| shadow-none | `none` | 默认扁平状态 |
| shadow-sm | `0 1px 2px rgba(0,0,0,0.06)` | 卡片轻微浮起、输入框聚焦 |
| shadow-base | `0 2px 8px rgba(0,0,0,0.1)` | hover 卡片、下拉菜单 |
| shadow-lg | `0 4px 16px rgba(0,0,0,0.12)` | 弹窗、抽屉、浮层 |
| shadow-xl | `0 8px 32px rgba(0,0,0,0.15)` | 全屏遮罩上的主弹窗 |

### 阴影规则

- 阴影方向统一向下（光源在顶部）
- 阴影颜色用 `rgba(0,0,0,...)`，不用纯黑 `#000`
- 同一页面最多 2 个层级阴影，过多阴影 = 视觉混乱
- **禁止**给静态卡片加重阴影——扁平 + 边框足够

---

## 六、图标规范

| 属性 | 规范 |
|------|------|
| 风格 | 统一线性（stroke）或统一面性（fill），不混用 |
| 尺寸 | 16px（行内）/ 20px（按钮内）/ 24px（标题旁） |
| 线宽 | 统一 1px 或 2px，不混用 |
| 颜色 | 跟随当前文字颜色 `currentColor`，不硬编码 |
| 间距 | 图标与文字间 4-8px，用 flex + gap 实现 |

---

## 七、交互反馈

| 状态 | 处理方式 |
|------|----------|
| hover | 背景变浅 `bg-hover` / 主色变浅 `primary-light` / 阴影升一级 |
| active | 主色变深 `primary-dark` / scale(0.98) 轻微下沉 |
| focus | 主色 outline 2px + 2px offset，或 box-shadow 环 |
| disabled | opacity: 0.5 + cursor: not-allowed |
| loading | spinner 图标 + 文字"加载中" / 按钮 disabled |

### 过渡动画

```css
transition: all 0.2s ease;  /* 通用默认 */
transition: background-color 0.15s ease;  /* 精确控制时指定属性 */
```

- 时长：150ms（快速反馈）~ 300ms（弹层展开）
- 缓动：统一 `ease` 或 `ease-out`
- **禁止**超过 400ms 的动画（感觉迟钝）
- **禁止** `linear`（机械感，无自然过渡）

---

## 八、响应式断点

| 断点 | 宽度 | 场景 |
|------|------|------|
| xs | < 768px | 移动端 |
| sm | ≥ 768px | 平板 |
| md | ≥ 992px | 小屏笔记本 |
| lg | ≥ 1200px | 桌面（后台默认目标） |
| xl | ≥ 1600px | 大屏 |

后台系统以 `lg` 为设计基准，向下适配至 `md`，移动端仅做关键流程。
