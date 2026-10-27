# 前端开发规范

## 前言

坚持制定好的代码规范。

无论团队人数多少，代码应该同出一门。项目兼容 IE9 及以上以及各种非主流浏览器。

前端需有校验规则，例：文本框输入要限制最大字数。Vue 单文件组件中 `<script>` 建议按以下顺序组织：

```javascript
export default {
  name: "", // 大写字母开头，驼峰式命名
  components: {},
  data() { return {}; },
  props: {},
  computed: {},
  watch: {},
  created() {},
  mounted() {},
  methods: {},
  beforeDestroy() {},
  beforeRouteEnter(to, from, next) {},
  beforeRouteLeave(to, from, next) {}
};
```

## 命名规范

### 项目名称

全部采用小写方式， 以下划线或者驼峰式分隔，一般建议使用“驼峰式”。

正例：mall_management_system / mallManagementSystem
反例：mall_management-system

### 目录命名

全部采用小写方式， 以驼峰式分隔，有复数结构时，要采用复数命名法， 缩写不用复数

正例： scripts / styles / components / images / utils / layouts / demoStyles / demoScripts / img / doc

反例： script / style / demo_scripts / demo-styles / imgs / docs

【特殊】Vue 项目中 `components` 下的组件命名请与团队约定一致（常见为 **PascalCase** 文件名或与路由一致的 **kebab-case**，以下沿用原文示例）。

正例：`HeadSearch` / `PageLoading` / `Authorized` / `NoticeIcon`

反例：`headSearch` / `pageLoading` / `head-search`（与约定冲突时）

【特殊】Vue 项目中除 `components` 外的目录命名与团队约定保持一致（常用 camelCase 或 kebab-case）。

正例：`pageOne` / `shoppingCar` / `userManagement`

反例：`ShoppingCar` / `UserManagement`（与目录命名约定不一致时）

#### JS、CSS、SCSS、HTML、PNG 等文件命名

全部采用小写方式，以下划线或驼峰分隔，建议采用驼峰式。

正例：`render_dom.js` / `signUp.css` / `index.html` / `company_logo.png`

反例：`render-dom.js` / `UserManagement.html`

### 命名严谨性

代码中的命名严禁使用拼音与英文混合的方式，更不允许直接使用中文的方式。 说明：正确的英文拼写和语法可以让阅读者易于理解，避免歧义。注意，即使纯拼音命名方式也要避免采用

正例：henan / luoyang / rmb 等国际通用的名称，可视同英文。
反例：DaZhePromotion [打折] / getPingfenByName() [评分] / int 某变量 = K

**杜绝完全不规范的缩写，避免望文不知义：**

反例：AbstractClass“缩写”命名成 AbsClass；condition“缩写”命名成 condi，此类随意缩写严重降低了代码的可阅读性。

## HTML 编码规范（Vue Template 同样适用）

### 语法

- 缩进使用 soft tab（4 个空格）；
- 嵌套的节点应该缩进；
- 在属性上，使用双引号，不要使用单引号；
- 属性名全小写开头，用中划线做分隔符或者驼峰式；
- 不要在自动闭合标签结尾处使用斜线（HTML5 规范 指出他们是可选的）；
- 不要忽略可选的关闭标签，例：</li> 和 </body>。

<!DOCTYPE html>

<html>

<head>

<title>Page title</title>

</head>

<body>

<img src="images/company_logo.png" alt="Company">

<h1 class="hello-world">Hello, world!</h1>

</body>

</html>

### HTML5 Doctype
在页面开头使用简单的 doctype 以启用标准模式，使页面在各浏览器中尽可能一致呈现；

虽然doctype不区分大小写，但是按照惯例，doctype大写 （关于 html属性，大写还是小写）。

<!DOCTYPE html>

<html>

...

</html>

### `lang` 属性

根据 HTML5 规范：

应在html标签上加上lang属性。这会给语音工具和翻译工具帮助，告诉它们应当怎么去发音和翻译。

更多关于 `lang` 的说明见 [HTML 标准：html 元素](http://www.w3.org/html/wg/drafts/html/master/semantics.html#the-html-element)；可在 sitepoint 查阅 [ISO 639-1 语言代码列表](https://www.sitepoint.com/iso-2-letter-language-codes/)。

sitepoint 仅列出大类（如中文为 `zh`），未区分地区。微软文档提供了 [更细的语言标识列表](http://msdn.microsoft.com/en-us/library/ms533052(v=vs.85).aspx)，其中包含 `zh-cn`、`zh-hk`、`zh-tw` 等。

<!DOCTYPE html>

<html lang="zh-CN">

...

</html>

### 字符编码

通过声明一个明确的字符编码，让浏览器轻松、快速的确定适合网页内容的渲染方式，通常指定为'UTF-8'。

<!DOCTYPE html>

<html>

<head>

<meta charset="UTF-8">

</head>

...

</html>

### IE 兼容模式
用 <meta> 标签可以指定页面应该用什么版本的IE来渲染；如果你想要了解更多，请点击[这里](http://stackoverflow.com/questions/6771258/whats-the-difference-if-meta-http-equiv-x-ua-compatible-content-ie-edge-e)；

不同doctype在不同浏览器下会触发不同的渲染模式（[这篇文章](https://hsivonen.fi/doctype/)总结的很到位）。

<!DOCTYPE html>

<html>

<head>

<meta http-equiv="X-UA-Compatible" content="IE=Edge">

</head>

...

</html>

### 引入 CSS 与 JS

根据 HTML5 规范，引入 CSS 和 JS 时通常不需要写 `type`，`text/css` 与 `text/javascript` 分别为其默认值。

HTML5 规范链接

- [使用link](http://www.w3.org/TR/2011/WD-html5-20110525/semantics.html#the-link-element)
- [使用style](http://www.w3.org/TR/2011/WD-html5-20110525/semantics.html#the-style-element)
- [使用script](http://www.w3.org/TR/2011/WD-html5-20110525/scripting-1.html#the-script-element)

<!-- External CSS -->

<link rel="stylesheet" href="code_guide.css">

<!-- In-document CSS -->

<style>

...

</style>

<!-- External JS -->

<script src="code_guide.js"></script>

<!-- In-document JS -->

<script>

...

</script>

### 属性顺序

属性应该按照特定的顺序出现以保证易读性；

- class
- id
- name
- data-*
- src, for, type, href, value , max-length, max, min, pattern
- placeholder, title, alt
- aria-*, role
- required, readonly, disabled

class是为高可复用组件设计的，所以应处在第一位； id更加具体且应该尽量少使用，所以将它放在第二位。

<a class="..." id="..." data-modal="toggle" href="#">Example link</a>

<input class="form-control" type="text">

<img src="..." alt="...">

### Boolean 属性

Boolean 属性指不需要写取值的属性；XHTML 中曾为每个属性写取值，HTML5 中不必如此。

更多内容可以参考 [WhatWG section on boolean attributes](http://www.whatwg.org/specs/web-apps/current-work/multipage/common-microsyntaxes.html#boolean-attributes)： boolean属性的存在表示取值为true，不存在则表示取值为false。

<input type="text" disabled>

<input type="checkbox" value="1" checked>

<select>

<option value="1" selected>1</option>

</select>

### JS生成标签

在JS文件中生成标签让内容变得更难查找，更难编辑，性能更差。应该尽量避免这种情况的出现。

### 减少标签数量

在编写 HTML 时，应尽量避免多余的父节点；必要时通过迭代与重构收紧结构。

<!-- Not well -->

<span class="avatar">

<img src="...">

</span>

<!-- Better -->

<img class="avatar" src="...">

### 实用高于完美

尽量遵循 HTML 标准与语义，但不必为「绝对正确」牺牲可维护性与简洁性；在可接受范围内用更少标签、更低复杂度解决问题。

## CSS 规范

### 缩进

使用 soft tab（4 个空格）。
```css
.element {

position: absolute; top: 10px;

left: 10px;

border-radius: 10px; width: 50px;

height: 50px;

}
```
### 分号

每个属性声明末尾都要加分号。
```css
.element {

width: 20px; height: 20px;

background-color: red;

}
```
### 空格

以下几种情况不需要空格：

- 属性名后
- 多个规则的分隔符 `,` 前
- `!important` 的 `!` 后
- 属性值中 `(` 后与 `)` 前
- 行末不要有多余的空格

以下几种情况需要空格：

- 属性值前
- 选择器 `>`、`+`、`~` 前后
- `{` 前
- `!important` 的 `!` 前
- @else 前后
- 属性值中的 `,` 后
- 注释 `/*` 后与 `*/` 前
```css
/* not good */

.element {

color :red! important;

background-color: rgba(0,0,0,.5);

}

/* good */

.element {

color: red !important;

background-color: rgba(0, 0, 0, .5);

}

/* not good */

.element ,

.dialog{

...

}

/* good */

.element,

.dialog {

}

/* not good */

.element>.dialog{

...

}

/* good */

.element > .dialog{

...

}

/* not good */

.element{

...

}

/* good */

.element {

...

}

/* not good */ @if{

...

}@else{

...

}

/* good */ @if {

...

} @else {

...

}
```
### 空行

以下几种情况需要空行：

- 文件最后保留一个空行
- '}'后最好跟一个空行，包括scss中嵌套的规则
- 属性之间需要适当的空行，具体见[属性声明顺序](http://alloyteam.github.io/CodeGuide/#css-declaration-order)
```css
/* not good */

.element {

...

}

.dialog {

color: red; &:after {

...

}

}

/* good */

.element {

...

}

.dialog {

color: red; &:after {

...

}

}
```
### 换行

以下几种情况不需要换行：

'{'前

以下几种情况需要换行：

'{'后和'}'前

- 每个属性独占一行
- 多个规则的分隔符','后
```css
/* not good */

.element

{color: red; background-color: black;}

/* good */

.element {

color: red;

background-color: black;

}

/* not good */

.element, .dialog {

...

}

/* good */

.element,

.dialog {

...

}
```
### 注释

注释统一用'/* */'（scss中也不要用'//'），具体参照右边的写法；缩进与下一行代码保持一致；

可位于一个代码行的末尾，与代码间隔一个空格。
```css
/* Modal header */

.modal-header {

...

}

/*

* Modal header

*/

.modal-header {

...

}

.modal-header {

/* 50px */ width: 50px;

color: red; /* color red */

}
```
### 引号

最外层统一使用双引号；`url()` 内的地址建议加引号；

属性选择器中的属性值需要引号。
```css
.element:after { content: "";

background-image: url("logo.png");

}

li[data-type="single"] {

...

}
```
### 命名

- 类名使用小写字母，以中划线分隔，或采用驼峰式命名
- id采用驼峰式命名
- scss中的变量、函数、混合、placeholder采用驼峰式命名
```css
/* class */

.element-content {

...

}

/* id */ #myDialog {

...

}

/* 变量 */

$colorBlack: #000;

/* 函数 */

@function pxToRem($px) {

...

}

/* 混合 */

@mixin centerBlock {

...

}

/* placeholder */

%myDialog {

...

}
```
### 属性声明顺序

相关的属性声明按右边的顺序做分组处理，组之间需要有一个空行。
```css
.declaration-order { display: block; float: right;

position: absolute; top: 0;

right: 0;

bottom: 0;

left: 0;

z-index: 100;

border: 1px solid #e5e5e5; border-radius: 3px;

width: 100px; height: 100px;

font: normal 13px "Helvetica Neue", sans-serif; line-height: 1.5;

text-align: center;

color: #333;

background-color: #f5f5f5;

opacity: 1;

}

// 下面是推荐的属性的顺序

[

[

"display", "visibility", "float", "clear", "overflow", "overflow-x", "overflow-y", "clip",

"zoom"

], [

"table-layout",

"empty-cells", "caption-side", "border-spacing", "border-collapse", "list-style",

"list-style-position", "list-style-type", "list-style-image"

], [

"-webkit-box-orient",

"-webkit-box-direction",

"-webkit-box-decoration-break", "-webkit-box-pack",

"-webkit-box-align", "-webkit-box-flflex"

], [

"position", "top",

"right",

"bottom",

"left",

"z-index"

], [

"margin",

"margin-top", "margin-right", "margin-bottom", "margin-left",

"-webkit-box-sizing", "-moz-box-sizing", "box-sizing",

"border", "border-width", "border-style", "border-color", "border-top",

"border-top-width", "border-top-style", "border-top-color", "border-right", "border-right-width", "border-right-style", "border-right-color", "border-bottom", "border-bottom-width", "border-bottom-style", "border-bottom-color", "border-left",

"border-left-width", "border-left-style", "border-left-color",

"-webkit-border-radius", "-moz-border-radius", "border-radius",

"-webkit-border-top-left-radius", "-moz-border-radius-topleft", "border-top-left-radius",

"-webkit-border-top-right-radius", "-moz-border-radius-topright", "border-top-right-radius",

"-webkit-border-bottom-right-radius", "-moz-border-radius-bottomright", "border-bottom-right-radius",

"-webkit-border-bottom-left-radius", "-moz-border-radius-bottomleft", "border-bottom-left-radius",

"-webkit-border-image",

"-moz-border-image", "-o-border-image", "border-image",

"-webkit-border-image-source", "-moz-border-image-source",

"-o-border-image-source", "border-image-source",

"-webkit-border-image-slice", "-moz-border-image-slice",

"-o-border-image-slice", "border-image-slice",

"-webkit-border-image-width", "-moz-border-image-width",

"-o-border-image-width", "border-image-width",

"-webkit-border-image-outset", "-moz-border-image-outset",

"-o-border-image-outset", "border-image-outset",

"-webkit-border-image-repeat", "-moz-border-image-repeat",

"-o-border-image-repeat", "border-image-repeat", "padding",

"padding-top", "padding-right", "padding-bottom", "padding-left", "width",

"min-width",

"max-width",

"height",

"min-height", "max-height"

], [

"font",

"font-family", "font-size", "font-weight", "font-style", "font-variant",

"font-size-adjust", "font-stretch", "font-effect", "font-emphasize",

"font-emphasize-position", "font-emphasize-style", "font-smooth",

"line-height", "text-align",

"-webkit-text-align-last", "-moz-text-align-last",

"-ms-text-align-last", "text-align-last", "vertical-align", "white-space",

"text-decoration", "text-emphasis",

"text-emphasis-color", "text-emphasis-style", "text-emphasis-position", "text-indent",

"-ms-text-justify", "text-justify", "letter-spacing", "word-spacing",

"-ms-writing-mode", "text-outline", "text-transform",

"text-wrap",

"-ms-text-overflow", "text-overflow",

"text-overflow-ellipsis", "text-overflow-mode",

"-ms-word-wrap", "word-wrap",

"-ms-word-break", "word-break"

], [

"color", "background",

"filter:progid:DXImageTransform.Microsoft.AlphaImageLoader", "background-color",

"background-image", "background-repeat", "background-attachment", "background-position",

"-ms-background-position-x", "background-position-x",

"-ms-background-position-y", "background-position-y",

"-webkit-background-clip", "-moz-background-clip", "background-clip", "background-origin",

"-webkit-background-size", "-moz-background-size",

"-o-background-size", "background-size"

], [

"outline", "outline-width", "outline-style", "outline-color",

"outline-offffset", "opacity",

"filter:progid:DXImageTransform.Microsoft.Alpha(Opacity", "-ms-filter:\\\\'progid:DXImageTransform.Microsoft.Alpha", "-ms-interpolation-mode",

"-webkit-box-shadow", "-moz-box-shadow", "box-shadow",

"filter:progid:DXImageTransform.Microsoft.gradient",

"-ms-filter:\\\\'progid:DXImageTransform.Microsoft.gradient", "text-shadow"

], [

"-webkit-transition", "-moz-transition",

"-ms-transition", "-o-transition", "transition",

"-webkit-transition-delay", "-moz-transition-delay",

"-ms-transition-delay", "-o-transition-delay", "transition-delay",

"-webkit-transition-timing-function", "-moz-transition-timing-function",

"-ms-transition-timing-function", "-o-transition-timing-function", "transition-timing-function",

"-webkit-transition-duration", "-moz-transition-duration",

"-ms-transition-duration", "-o-transition-duration", "transition-duration",

"-webkit-transition-property", "-moz-transition-property",

"-ms-transition-property", "-o-transition-property", "transition-property",

"-webkit-transform", "-moz-transform",

"-ms-transform",

"-o-transform", "transform",

"-webkit-transform-origin", "-moz-transform-origin",

"-ms-transform-origin", "-o-transform-origin", "transform-origin",

"-webkit-animation", "-moz-animation",

"-ms-animation",

"-o-animation", "animation",

"-webkit-animation-name", "-moz-animation-name",

"-ms-animation-name", "-o-animation-name", "animation-name",

"-webkit-animation-duration", "-moz-animation-duration",

"-ms-animation-duration", "-o-animation-duration", "animation-duration",

"-webkit-animation-play-state", "-moz-animation-play-state",

"-ms-animation-play-state", "-o-animation-play-state", "animation-play-state",

"-webkit-animation-timing-function", "-moz-animation-timing-function",

"-ms-animation-timing-function", "-o-animation-timing-function", "animation-timing-function",

"-webkit-animation-delay", "-moz-animation-delay",

"-ms-animation-delay", "-o-animation-delay", "animation-delay",

"-webkit-animation-iteration-count", "-moz-animation-iteration-count",

"-ms-animation-iteration-count", "-o-animation-iteration-count", "animation-iteration-count",

"-webkit-animation-direction", "-moz-animation-direction",

"-ms-animation-direction", "-o-animation-direction", "animation-direction"

], [

"content", "quotes", "counter-reset",

"counter-increment", "resize",

"cursor",

"-webkit-user-select", "-moz-user-select",

"-ms-user-select", "user-select", "nav-index",

"nav-up",

"nav-right",

"nav-down",

"nav-left",

"-moz-tab-size",

"-o-tab-size", "tab-size",

"-webkit-hyphens", "-moz-hyphens", "hyphens", "pointer-events"

]

]
```
### 颜色

颜色16进制用小写字母；颜色16进制尽量用简写。
```css
/* not good */

.element {

color: #ABCDEF;

background-color: #001122;

}

/* good */

.element {

color: #abcdef; background-color: #012;

}
```
### 属性简写

属性简写需要你非常清楚属性值的正确顺序，而且在大多数情况下并不需要设置属性简写中包含的所有值，

所以建议尽量分开声明会更加清晰； margin 和 padding 相反，需要使用简写；常见的属性简写包括：

- font
- background
- transition
- animation
```css
/* not good */

.element {

transition: opacity 1s linear 2s;

}

/* good */

.element {

transition-delay: 2s;

transition-timing-function: linear; transition-duration: 1s;

transition-property: opacity;

}
```
### 媒体查询

尽量将媒体查询写在与其相关的规则附近，不要全部堆到一个独立文件或文档最底部，否则后续难以维护、也容易被遗忘。
```css
.element {

...

}

.element-avatar{

...

}

@media (min-width: 480px) {

.element {

...

}

.element-avatar {

...

}

}
```
### SCSS 相关

提交的代码中不要有 @debug；声明顺序：

- @extend
- 不包含 @content 的 @include
- 包含 @content 的 @include
- 自身属性
- 嵌套规则

@import 引入的文件不需要开头的'_'和结尾的'.scss'；嵌套最多不能超过5层；

@extend 中使用placeholder选择器；去掉不必要的父级引用符号'&'。
```css
/* not good */

@import "_dialog.scss";

/* good */ @import "dialog";

/* not good */

.fatal {

@extend .error;

}

/* good */

.fatal {

@extend %error;

}

/* not good */

.element {

& > .dialog {

...

}

}

/* good */

.element {

> .dialog {

...

}

}
```
### 杂项

不允许有空的规则；

元素选择器用小写字母；去掉小数点前面的0；

去掉数字中不必要的小数点和末尾的0；属性值'0'后⾯不要加单位；

同个属性不同前缀的写法需要在垂直方向保持对齐，具体参照右边的写法；

无前缀的标准属性应该写在有前缀的属性后⾯；

不要在同个规则里出现重复的属性，如果重复的属性是连续的则没关系；

不要在一个文件里出现两个相同的规则；用 border: 0; 代替 border: none;；

选择器不要超过4层（在scss中如果超过M层应该考虑用嵌套的方式来写）；

发布的代码中不要使用 `@import`；尽量少用 `*` 通配选择器。
```css
/* not good */

.element {

}

/* not good */ LI {

...

}

/* good */ li {

...

}

/* not good */

.element {

color: rgba(0, 0, 0, 0.5);

}

/* good */

.element {

color: rgba(0, 0, 0, .5);

}

/* not good */

.element {

width: 50.0px;

}

/* good */

.element {

width: 50px;

}

/* not good */

.element {

width: 0px;

}

/* good */

.element {

width: 0;

}

/* not good */

.element {

border-radius: 3px;

-webkit-border-radius: 3px;

-moz-border-radius: 3px;

background: linear-gradient(to bottom, #fffff 0, #eee 100%); background: -webkit-linear-gradient(top, #fffff 0, #eee 100%);

background: -moz-linear-gradient(top, #fffff 0, #eee 100%);

}

/* good */

.element {

-webkit-border-radius: 3px;

-moz-border-radius: 3px; border-radius: 3px;

background: -webkit-linear-gradient(top, #fffff 0, #eee 100%);

background: -moz-linear-gradient(top, #fffff 0, #eee 100%); background: linear-gradient(to bottom, #fffff 0, #eee 100%);

}

/* not good */

.element {

color: rgb(0, 0, 0); width: 50px;

color: rgba(0, 0, 0, .5);

}

/* good */

.element {

color: rgb(0, 0, 0);

color: rgba(0, 0, 0, .5);

}
```
## Web / App 规范

### 设计规范

设计稿以 **iPhone 6** 为基准时，常见单屏尺寸为 **750 × 1334 px**（以项目设计约定为准）。

### 单位 rem

**px 转 rem（示例：根字号随视口变化）**

```javascript
// 适配手机及 iPad
!function () {
  var docEl = document.documentElement;
  var resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize';
  var recalc = function () {
    var clientWidth = document.documentElement.clientWidth;
    if (!clientWidth) return;
    docEl.style.fontSize = clientWidth / 7.5 + 'px';
  };
  if (!document.addEventListener) return;
  window.addEventListener(resizeEvt, recalc, false);
  document.addEventListener('DOMContentLoaded', recalc, false);
}();
```

**前提**：设计稿按上述基准出图；在项目中引入上述脚本后，将设计稿上以 **px** 为单位的尺寸（字体或块级宽高）**除以 100** 得到 **rem**（示例：设计稿 `width: 200px` → 代码中 `width: 2rem`，与根字号算法一致即可）。

### 矢量图标

为提高设计与前端协作效率，图标建议统一走 **阿里 iconfont**（或团队选定的同一套矢量图标方案）：由设计师统一命名并维护图标库，前端**不要**擅自增删或改名项目内约定使用的图标。

**接入步骤简述**：

1. 在 iconfont 项目页选择 **「Font class」**，点击 **「点此复制代码」**，复制样式表地址。
2. 在 `index.html`（或入口 HTML）中用 `<link rel="stylesheet" href="...">` 引入上述地址（`href` 填复制的 URL）。

**在页面中使用**（第一个类名固定为 `iconfont`，其后为具体图标类名，可在 iconfont 站点复制）：

```html
<i class="iconfont icon-phone"></i>
```

