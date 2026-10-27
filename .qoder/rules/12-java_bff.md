---
paths:
- java_bff/**
---

# java_bff 端规则（Spring Boot 2.7 + Maven + MyBatis-Plus）

> 生成日期: 2026-10-26 | 技术栈: Java (Spring Boot/Maven) → 实测 **Spring Boot 2.7.18 / JDK 8** | 仅当访问 `java_bff/**` 下文件时生效
> 来源: `/complete-harness` 按 `java_bff/pom.xml` 实际依赖补全

## 1. 技术栈与物理环境

- **定位**: BFF（Backend For Frontend）—— 为 `frontend/` 聚合、编排、裁剪下游接口，不承载领域业务规则
- **运行时**: JDK 1.8，Spring Boot 2.7.18，Maven（`groupId org.example` / `artifactId java_bff` / `1.0-SNAPSHOT`）
- **关键依赖**: MyBatis-Plus 3.5.3、dynamic-datasource 3.5.2（MySQL / Oracle / OceanBase 多数据源）、Spring Data Redis、Nacos（config + discovery）、java-jwt 3.8.0、Caffeine、Jasypt、fastjson 1.2.84、Apache POI 5.2.3、AWS S3 SDK、腾讯云 COS、火山方舟 + 腾讯云 ASR、BouncyCastle 1.70、Lombok
- **本地命令清单**（在仓库根执行）:
  - 编译: `mvn -f java_bff/pom.xml -q compile`
  - 测试: `mvn -f java_bff/pom.xml test`
  - 打包: `mvn -f java_bff/pom.xml -DskipTests package`
  - 🚨 **熔断红线**: 编译/测试报错且 2 次无法修复 → 立即向人类求助，严禁带病交接
  - 依赖仓库走阿里云镜像 + Spring Milestones（见 pom `<repositories>`），离线环境需提前 `mvn dependency:go-offline`

## 2. 架构红线

- **职责边界**: 只做参数校验、聚合编排、DTO 转换、鉴权透传；业务规则与事务下沉到下游服务
- **契约对齐**: 出入参字段必须与前端 `frontend/src/api/*.js` 的封装一致，禁止单侧改字段名
- **响应壳**: 与前端约定一致 `{ code: 0, msg: '', data: {} }`
- **分层**: `controller → service → client/mapper`，禁止 controller 直连 mapper 或拼接下游 URL
- **多数据源**: 切换必须走 dynamic-datasource 注解，禁止在业务代码里手工建连
- **敏感信息**: 配置项用 Jasypt 加密；日志脱敏（手机号/身份证/密钥）；禁止把密钥写入代码或配置明文
- **加解密**: 企业微信等外部报文用 BouncyCastle 处理；与前端国密链路（SM2/SM4/SM3）对接时保持算法与字段名一致

## 3. 修改后路径自检 (强制)

每次完成 `java_bff/` 代码修改后，必须执行：
1. **构建验证**: `mvn -f java_bff/pom.xml -q compile` 无报错
2. **契约核验**: 若改动出入参，同步确认 `frontend/src/api/` 是否需同改（跨端见 `.qoder/rules/20-cross-end.md`）
3. **物理坐标**: `src/main/java/<package>/` 下按层归位，资源文件进 `src/main/resources/`

## 4. 业务词汇表 (Domain Dictionary)

| 术语 | 代码标识 | 含义 |
| --- | --- | --- |
| BFF | `java_bff` | 前端专属聚合层，不做领域逻辑 |
| 多数据源 | `@DS` / dynamic-datasource | MySQL / Oracle / OceanBase 动态切换 |
| 语音转写 | 火山方舟 SDK / 腾讯云 ASR | 招聘场景音频转文字 |
| 对象存储 | AWS S3 SDK / 腾讯云 COS | 附件与大音频存储、预签名 URL |
| 报表导出 | Apache POI | Excel 生成 |

## 5. 跨端交接 (Handoff)

后端完成开发、需要前端接续时，**禁止直接改 `frontend/`**，必须执行：
1. **更新全局游标**: 在 `.qoder/plans/plans.json` 中更新当前 plan 的 `status`/`updated`
2. **物理写盘交接**: 将接口路径、请求体/响应体字段增量写入当前 plan 详情文件"跨端交接"章节
3. **离场分支判定**: 任务未完结输出唤醒警告；任务闭环输出完成汇报并提示验收归档

## 6. 已知工程债务

- `pom.xml` 的 `spring-boot-maven-plugin` mainClass 配置为 `org.example.ticketcore.TicketCoreApplication`，但当前源码仅有 `src/main/java/org/example/Main.java` → **`package` 阶段会失败**，接入真实启动类前需同步修正 mainClass
- 尚无 `src/main/resources` 下的 Nacos/数据源配置样板，本地启动需自行补齐
