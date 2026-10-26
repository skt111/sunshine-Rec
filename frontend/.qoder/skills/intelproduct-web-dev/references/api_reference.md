# API 请求参考

项目所有网络请求统一封装在 `src/api/dataRequst.js`，底层依赖 `src/plugins/axios.js` 实例。**禁止在 `.vue` 文件中直接调用 `axios` 或 `fetch`**。

## 请求方法一览

| 方法 | 请求方式 | 用途 | 参数 |
|------|----------|------|------|
| `postDataList(url, data, noEncode)` | POST | 通用数据提交 | `url` 接口地址；`data` 请求体；`noEncode` 可选，`true` 跳过加密 |
| `getList(url, data, noEncode)` | GET | 通用数据查询 | `url` 接口地址；`data` 查询参数对象；`noEncode` 可选，`true` 跳过加密 |
| `deleteList(url)` | DELETE | 删除资源 | `url` 接口地址 |
| `postFileList(url, data, noEncode)` | POST | 下载文件流（POST） | `url` 接口地址；`data` 请求体；`noEncode` 可选 |
| `getFileList(url, data, noEncode)` | GET | 下载文件流（GET） | `url` 接口地址；`data` 查询参数；`noEncode` 可选 |

## 加密机制

加密由环境变量 `process.env.VUE_APP_ENCODE` 全局控制：

| 环境变量值 | 行为 |
|-----------|------|
| `Y` | 请求自动加密（SM4 加密 data，放入 `encrypt` 字段） |
| `N` 或未设置 | 明文传输 |

- **全局开关**：所有方法默认遵循环境变量。开发环境通常 `VUE_APP_ENCODE=N`。
- **单次跳过**：传入 `noEncode: true` 可让单次请求不加密，优先级高于全局开关。
- **加密细节**：详见 `references/crypto-guide.md`。

### 加密时的请求体结构

当 `VUE_APP_ENCODE=Y` 且未传 `noEncode` 时：

```javascript
// 原始 data
{ name: "张三", age: 18 }

// 加密后实际发送
{ encrypt: "SM4加密后的密文字符串" }
```

GET 请求加密时，密文会经 `encodeURIComponent` 编码后拼接到 URL 的 `?param=` 参数中。

## 使用示例

### 引入

```javascript
import { postDataList, getList, deleteList, postFileList, getFileList } from "@/api/dataRequst";
```

### POST 提交数据

```javascript
async submitForm() {
  try {
    const res = await postDataList('/user/save', { name: "张三", age: 18 });
    if (res.code === 0) {
      this.$Message.success('保存成功');
    }
  } catch (err) {
    // API 错误由 axios 拦截器自动上报 Sentry
  }
}
```

### GET 查询数据

```javascript
async fetchList() {
  const res = await getList('/user/list', { page: 1, size: 10 });
  // res.data 为列表数据
}
```

### 单次不加密请求

```javascript
// 即使全局开启了加密，本次请求也明文传输
const res = await postDataList('/public/info', { id: 1 }, true);
```

### DELETE 删除

```javascript
async removeItem(id) {
  const res = await deleteList(`/user/delete/${id}`);
}
```

### 下载文件

```javascript
// POST 方式下载（携带请求体）
async downloadReport() {
  const res = await postFileList('/report/export', { startDate: '2026-01-01' });
  // res 为 blob 数据，配合 util.downloadStream() 处理
  this.util.downloadStream(res);
}

// GET 方式下载（参数拼 URL）
async downloadTemplate() {
  const res = await getFileList('/template/download', { type: 'user' });
  this.util.downloadStream(res);
}
```

## 接口响应规范

所有接口统一返回结构：

```javascript
{
  code: 0,        // 状态码，0 表示成功
  msg: '',        // 消息提示
  data: {         // 数据体
    // 具体字段
  }
}
```

判断成功统一用 `res.code === 0`，不要用 `res.status` 或其它字段。

## 红线

1. **禁止**在 `.vue` 文件中直接 `import axios` 或使用 `fetch`
2. **禁止**绕过 `dataRequst.js` 自行封装请求方法
3. **必须**使用 `async/await` 处理请求，避免 `.then()` 链
4. 新增 API 时，直接调用 `dataRequst.js` 导出的方法即可，无需新建 api 文件（项目采用统一入口模式）
5. 文件流下载须用 `postFileList` / `getFileList`，配合 `util.downloadStream()` 处理 blob
