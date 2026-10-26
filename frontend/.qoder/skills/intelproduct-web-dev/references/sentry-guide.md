# Sentry 监控

项目集成 Sentry 进行前端错误监控和性能追踪，配置文件位于 `src/plugins/sentry.js`。

## 功能特性

- **Vue 集成**: 自动捕获 Vue 组件错误，并关联组件信息
- **接口监控**: 拦截 axios 响应错误，自动上报
- **版本追踪**: 支持按 release 版本筛选问题
- **环境区分**: 支持 development/test/production 环境隔离

## 配置说明

| 配置项 | 说明 |
|--------|------|
| dsn | Sentry 服务地址（项目唯一标识） |
| release | 版本号，来自 `process.env.VUE_APP_RELEASE_VERSION` |
| environment | 运行环境 |
| attachProps | 是否暴露 Vue 组件 props |
| logErrors | 是否在控制台打印错误 |

## 错误上报示例

```javascript
// 在组件中手动上报业务错误
this.$Sentry.captureException(new Error('业务错误信息'));

// 接口错误已在 axios 拦截器中自动上报，无需手动处理
```

## 使用规范

- 关键业务逻辑异常必须使用 `this.$Sentry.captureException` 上报
- try-catch 代码块中均应接入 Sentry 上报闭环
- 接口错误由 `src/plugins/axios.js` 拦截器自动上报

## 注意事项

- 生产环境 dsn 和测试环境不同，详见 `.sentryclirc` 配置
- 上报会自动排除特定响应状态码（如 `status: 'success'`）
- 敏感信息不会自动脱敏，需业务代码注意
