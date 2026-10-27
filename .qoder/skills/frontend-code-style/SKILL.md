---
name: frontend-code-style
description: 本技能适用于编写或修改前端代码，包括 HTML、CSS、SCSS、JavaScript 或 Vue 单文件组件。它提供文件/目录/变量命名、HTML/Vue 模板结构、CSS/SCSS 样式规则及 Web/App 响应式设计的编码规范。在创建新组件、页面样式、重构前端代码、编写 Vue SFC 骨架或对照团队标准审查代码质量时触发本技能。
---

# 前端代码规范

## 概述

在 HTML、CSS、SCSS、JavaScript 和 Vue 模板中推行统一的前端编码标准。完整规范见 `references/code-style.md`，按需加载以保持上下文精简。

## 适用场景

当执行以下任何操作时加载本技能：

- 创建或编辑 `.html`、`.css`、`.scss`、`.vue` 或 `.js` 文件
- 为新文件、目录、组件或变量命名
- 编写 Vue 单文件组件骨架（script 顺序、props、生命周期钩子）
- 应用 CSS/SCSS 格式规则（缩进、空格、排序、嵌套、媒体查询）
- 实现响应式布局（rem 适配、iconfont 集成）
- 审查代码是否符合团队规范

## 使用方法

### 1. 查阅参考文档

权威规则在 `references/code-style.md` 中。编写或审查代码前，阅读相关章节而非依赖记忆。参考文档组织如下：

| 章节 | 涵盖内容 | 使用时机 |
|---------|--------|----------|
| 命名规范 | 项目/目录/文件/变量命名 | 命名任何新内容时 |
| HTML 编码规范 | 缩进、doctype、lang、charset、属性顺序、布尔属性 | 编写 HTML 或 Vue `<template>` 时 |
| CSS 规范 | 缩进、分号、空格、换行、注释、引号、排序、颜色、简写、媒体查询、SCSS 规则 | 编写 CSS/SCSS 或 Vue `<style>` 时 |
| Web / App 规范 | rem 适配脚本、iconfont 用法 | 构建响应式或移动端页面时 |

### 2. 快速检索模式

由于 `references/code-style.md` 较大，优先使用定向 grep 检索而非全文读取：

```
# 命名规范
grep -n "命名\|正例\|反例" references/code-style.md

# Vue SFC script 顺序
grep -n "components\|props\|computed\|watch\|created\|mounted\|methods" references/code-style.md

# CSS 格式规则
grep -n "缩进\|分号\|空格\|空行\|换行\|注释\|引号" references/code-style.md

# SCSS 专属规则
grep -n "@extend\|@include\|@import\|placeholder\|嵌套" references/code-style.md

# 响应式 / rem
grep -n "rem\|iconfont\|iPhone\|7.5" references/code-style.md
```

### 3. 主动应用规则

生成代码时，默认遵循规范而非询问用户。关键不可妥协项：

- **缩进**：HTML/CSS/SCSS 使用 4 空格（软制表符）
- **引号**：HTML 属性和 CSS `url()`/content 使用双引号
- **CSS 排序**：按参考文档的属性声明顺序列表分组
- **SCSS**：禁用 `@debug`；`@extend` 使用占位选择器；最多 5 层嵌套；`@import` 不带前导 `_` 或后缀 `.scss`
- **颜色**：hex 小写，优先简写（如 `#012` 而非 `#001122`）
- **Vue SFC script 顺序**：`name` → `components` → `data` → `props` → `computed` → `watch` → 生命周期 → `methods` → 路由钩子
- **命名**：禁拼音英文混用；禁随意缩写；`components/` 文件名用 PascalCase，其余 camelCase，除非团队规范另有约定

### 4. 冲突解决

若项目级 `CLAUDE.md` 或框架规范（如 Vue 2 Options API、iView UI）与本技能冲突，以项目 `CLAUDE.md` 为准。本技能提供通用前端样式规则；项目规则提供框架和业务约束。

## 资源

### references/

- `code-style.md` —— 完整前端编码规范（HTML/CSS/SCSS/JS/Vue 模板约定、命名规则、响应式设计指南）。使用上述 grep 模式按需加载。
