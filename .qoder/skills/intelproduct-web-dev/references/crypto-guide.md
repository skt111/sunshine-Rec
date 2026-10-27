# Crypto Guide - 国密 SM2/SM4 接口加密

项目使用国密 SM2 + SM4 算法实现接口数据加密，通过环境变量 `VUE_APP_ENCODE` 控制是否启用加密。

## 加密开关

| 环境变量值 | 行为 |
|-----------|------|
| `Y` | 请求加密，响应解密 |
| `N` 或未设置 | 数据明文传输 |

开发环境默认不加密（`VUE_APP_ENCODE=N`）。

## 加密工具说明

位于 `src/utils/crypto.js`，提供以下方法：

| 方法 | 说明 |
|------|------|
| `sm2.encrypt(data, publicKey)` | SM2 非对称加密（用于加密 SM4 密钥） |
| `sm2.decrypt(data, privateKey)` | SM2 非对称解密 |
| `sm4.encrypt(data, key)` | SM4 对称加密（用于加密实际数据） |
| `sm4.decrypt(data, key)` | SM4 对称解密 |
| `base64.en(data)` | Base64 编码 |
| `base64.de(data)` | Base64 解码 |
| `sign(token, timestamp, data)` | 接口签名生成（SM3 摘要） |

## 加密流程

当 `VUE_APP_ENCODE=Y` 时：

1. **初始化**: 从登录回调获取后台返回的公钥（`pubKey`），存储到 `localStorage`
2. **生成会话密钥**: 使用 SM4 生成随机会话密钥（`enCodeKey`）
3. **加密密钥**: 使用 SM2 公钥加密会话密钥，生成 `headCode`（04 开头的十六进制字符串）
4. **加密数据**: 使用 SM4 会话密钥加密实际请求数据
5. **请求头**: 将 `headCode` 和 `enCodeKey` 放到请求头中

```javascript
import { generateKeySm4, enCodeSm4 } from "@/utils/smjs/sm4";

// 判断是否启用加密
const isEncode = process.env.VUE_APP_ENCODE === 'Y';

// 从登录回调获取公钥并存储
// localStorage.setItem("pubkey", to.query.key);

if (isEncode) {
  // 生成 SM4 会话密钥
  const enCodeKey = generateKeySm4(); // 16字节随机密钥
  const pubkey = localStorage.getItem("pubkey");

  // 使用 SM2 公钥加密会话密钥，生成 headCode（04 开头）
  const sm2Encrypt = require("sm-crypto").sm2;
  const headCode = "04" + sm2Encrypt.doEncrypt(enCodeKey, pubkey);

  // 存储加密密钥和头信息
  localStorage.setItem("headCode", headCode);
  localStorage.setItem("enCodeKey", enCodeKey);

  // 请求头携带
  // headers: { headCode, enCodeKey }
} else {
  // 明文传输，无需加密处理
}
```

## 签名机制

```javascript
// 签名格式：timestamp + token + data(字典升序)
const signature = sign(token, timestamp, { key1: value1, key2: value2 });
```

签名采用 **SM3** 摘要规则。

## 国密标准

- **SM2 (非对称)**: 加密 SM4 会话密钥
- **SM4 (对称)**: 加密业务数据
- **SM3 (摘要)**: 接口签名

## 使用场景

- 敏感数据加密传输（需启用加密）
- 接口请求签名验证
- 数据完整性校验

## 注意事项

- SM2/SM4 密钥对在 `.env` 文件中配置，生产环境需单独管理
- 加密启用时，后端需配合实现对应的解密逻辑
- 开发环境默认不加密（`VUE_APP_ENCODE=N`）
- **全局开关**: 接口加密由 `process.env.VUE_APP_ENCODE === 'Y'` 控制，设置为 `Y` 时由公共拦截器自动实现请求加密与响应解密
- **密钥管理**: 登录回调后，需从后台获取公钥并存储至 `localStorage.setItem("pubkey", ...)`
- **请求头规范**: 启用加密时，请求头必须携带 SM2 加密生成的 `headCode` 及 SM4 会话密钥 `enCodeKey`
- **签名机制**: 关键业务接口需调用 `sign(token, timestamp, data)`，签名格式严格遵循 `timestamp + token + data(字典升序)` 的 SM3 摘要规则

## 接口返回数据规范

所有接口返回数据必须遵循：

```javascript
{
  code: 0,        // 状态码，0 表示成功
  msg: '',        // 消息
  data: {         // 数据体
    [fieldName]: ...
  }
}
```
