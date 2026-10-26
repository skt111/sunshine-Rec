# Utils API Reference

工具函数位于 `src/utils/` 目录。

## util.js - 通用工具

```javascript
import util from "@/utils/util";
```

### 日期处理

| 方法 | 说明 |
|------|------|
| `util.getNowDay()` | 获取当前日期 YYYY-MM-DD |
| `util.changeDay(date)` | 日期转字符串 |
| `util.getWeek()` | 获取本周日期范围 |
| `util.getNowMonth()` | 获取上一个月 YYYY-MM |

### 数字格式化

| 方法 | 说明 |
|------|------|
| `util.format(num)` | 数字千分位格式化 |

### Cookie 操作

| 方法 | 说明 |
|------|------|
| `util.setCookie(name, value, expire)` | 设置 Cookie |
| `util.getCookie(name)` | 获取 Cookie |
| `util.delCookie(name)` | 删除 Cookie |

### 对象处理

| 方法 | 说明 |
|------|------|
| `util.objDeepCopy(source)` | 深拷贝对象 |
| `util.arrDeepCopy(source)` | 深拷贝数组 |
| `util.groupByFour(data, len)` | 数据分组 |

### 文件下载

| 方法 | 说明 |
|------|------|
| `util.downloadStream(param)` | 下载文件流 |
| `util.excelDownload(url, path)` | Excel 下载 |

---

## directives.js - Vue 自定义指令

| 指令 | 用法 | 说明 |
|------|------|------|
| `v-debounce` | `v-debounce="handler"` | 函数防抖（1秒延迟） |
| `v-clickoutside` | `v-clickoutside="handleClose"` | 点击元素外部关闭 |
| `v-is-figure` | `v-is-figure.modifier.float="val"` | 数字输入校验 |
| `v-is-figure.float` | 同上 | 支持浮点数 |
| `v-is-figure` + `max-len` | `max-len="10"` | 最大长度限制 |
| `v-is-email` | `v-is-email="email"` | 邮箱格式校验 |
| `v-is-required` | 配合 `class="required"` | 必填校验 |
| `v-is-num` | 配合 `class="isNums"` | 数字校验 |
| `v-is-require-num` | 配合 `scope="[0,100]"` | 范围数字校验 |

---

## menuBtn.js - 菜单处理

```javascript
import menuBtn from "@/utils/menuBtn";

menuBtn.butFun(butData)              // 提取按钮操作码
menuBtn.menuFun(menuData, modeType)  // 菜单数据处理
```

---

## smjs/ - 国密算法

| 文件 | 算法 | 用途 |
|------|------|------|
| `sm2.js` | SM2 | 非对称加密/签名（需引入） |
| `sm4.js` | SM4 | 对称加密（需引入） |

---

## 使用规范

- **日期处理**: 必须调用 `util.getNowDay()` 或 `util.changeDay()`，禁止重复造轮子
- **深拷贝**: 必须使用 `util.objDeepCopy()`
- **数据校验**: 必须使用自定义指令 `v-debounce` / `v-is-figure` / `v-is-required` 等
- **异常监控**: 关键业务逻辑异常必须使用 `this.$Sentry.captureException` 上报
