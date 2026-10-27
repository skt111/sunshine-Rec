# 雇主品牌雷达 · 小红书公开笔记 API 契约（阶段一）

- 基础路径：`/api/supply-radar/social`
- 响应壳：`{ status, message, result }`，`status === 'success'` 时为成功；失败为 `fail` + 中文 `message`
- 认证：沿用 `LoginInterceptor`（请求头 `token` / `userToken`），本地联调可 `--supply-radar.auth.enabled=false`
- 边界：**阶段一只支持人工录入**，不含任何平台抓取；指标缺失为 `null`，禁止用 0 或点赞数顶替

## 数据表对应

| 表 | 用途 |
|---|---|
| `social_account` | 企业账号、账号性质、核验状态 |
| `social_note` | 笔记元数据、主题标签、来源、核验状态、原始链接（唯一） |
| `social_note_metric_snapshot` | 按观察时间保存互动指标，字段可空 |

## 1. GET /stats

| 参数 | 类型 | 说明 |
|---|---|---|
| startDate / endDate | `yyyy-MM-dd` | 可选，按 `published_at` 过滤 |

返回 `SocialStatsVO`：`verifiedCompanyCount, enabledAccountCount, verifiedAccountCount, noteCount, highInteractionCount, highInteractionInsufficient, pendingNoteCount, pendingAccountCount, rangeStart, rangeEnd, lastObservedAt, sourceNote`

口径：已核验企业 = 有 `VERIFIED` 账号的启用企业；高互动 = 综合分前 20%，样本 < `min-sample-size` 时返回 `highInteractionInsufficient=true` 且不计算。

## 2. GET /accounts

| 参数 | 说明 |
|---|---|
| companyId / companyCategory / nature / verifiedStatus | 可选过滤 |

返回 `SocialAccountVO[]`：`id, companyId, companyName, companyCategory, accountName, homeUrl, accountNature, verifiedStatus, verifyBasis, verifiedAt, companyConfidence, enabled`。仅返回 `enabled=1` 账号。

## 3. GET /notes

| 参数 | 说明 |
|---|---|
| companyIds | 逗号分隔企业 ID |
| companyCategory | `INSURANCE / FINANCE / INTERNET` |
| accountNature | 账号性质 |
| topic | 主题标签（`like` 匹配） |
| startDate / endDate | `yyyy-MM-dd` |
| metricType | `like / favorite / comment`，仅保留该指标非空的笔记 |
| sourceType / verificationStatus / ownerType / keyword | 可选 |
| page / size | 默认 1 / 20，size 上限 100 |
| sortBy / sortDirection | `publishedAt / like / favorite / comment / score`，默认 `publishedAt desc` |

返回：`{ list: SocialNoteVO[], total, page, size, totalPages }`

`SocialNoteVO`：`id, noteUrl, title, contentSummary, publishedAt, firstObservedAt, lastObservedAt, companyId, companyName, companyCategory, accountId, accountName, accountNature, accountVerifiedStatus, accountVerified, ownerType, sourceType, verificationStatus, topicTags[], missingReason, remark, metrics`

`metrics`（最近一次快照，可能为 null）：`id, likeCount, favoriteCount, commentCount, shareCount, viewCount, metricObservedAt, metricSource, missingFields[]`

## 4. GET /notes/{id}

返回 `SocialNoteDetailVO`：列表字段 + `snapshots[]`（按 `metric_observed_at` 倒序）。不存在时 `status=fail, message=笔记不存在`。

## 5. GET /rankings

| 参数 | 说明 |
|---|---|
| type | `like / favorite / comment / composite / topic`，默认 `composite` |
| startDate / endDate / companyId | 可选 |
| limit | 默认 10，上限 50 |

返回 `RankingVO`：`type, available, reason, range, sampleSize, totalNotes, completeness, insufficient, metricSource, items[]`
`RankingItemVO`：`rank, noteId, title, companyName, noteUrl, metricValue, score, metricObservedAt, topic`

规则：指标缺失的笔记不参与排名，`completeness = 可用条数/总条数`；某指标完全不可用时 `available=false` 并给出 `reason`；样本 < `min-sample-size` 时 `insufficient=true`。

## 6. GET /source-status

返回 `SourceStatusVO[]`：`MANUAL=ENABLED`，其余 `AUTHORIZED_API / AUTHORIZED_SERVICE = NOT_CONFIGURED`，`PUBLIC_PAGE = DISABLED_BY_POLICY`。

## 7. POST /notes/manual

请求体 `ManualNoteRequest`：

```json
{
  "noteUrl": "https://www.xiaohongshu.com/...",
  "title": "页面标题",
  "contentSummary": "不超过 1000 字",
  "publishedAt": "2026-10-20T10:30:00",
  "companyId": 12,
  "accountId": null,
  "ownerType": "OFFICIAL",
  "verificationStatus": "PENDING",
  "topicTags": ["校园招聘", "实习招聘"],
  "likeCount": 120,
  "favoriteCount": 30,
  "commentCount": 12,
  "missingReason": null,
  "remark": "截图见共享盘 2026-10-27"
}
```

返回：新笔记 `id`。

校验与错误信息（`status=fail`）：

| 场景 | message |
|---|---|
| 链接为空 | 笔记链接不能为空 |
| 非小红书域名 | 仅支持小红书笔记链接（xiaohongshu.com / xhslink.com） |
| 链接重复 | 该链接已录入 |
| 企业不存在/停用 | 企业不存在或未启用 |
| 账号不属于该企业 | 账号与企业不匹配 |
| 发布时间超前 | 发布时间不能晚于当前时间 |
| 指标为负 | 点赞数/收藏数/评论数/分享数/浏览量必须为非负整数 |
| 标签越界 | 主题标签不在允许范围内：xxx |
| 枚举越界 | 归属类型/核验状态不合法 |

副作用：`source_type` 固定 `MANUAL`；`first_observed_at / last_observed_at = now`；有任一指标时写入一条快照，`metric_source = MANUAL_yyyy-MM-dd`。

## 配置项（application.yml）

```yaml
social-brand:
  platform: XIAOHONGSHU
  auth-source-enabled: false
  composite-score: { like: 1, favorite: 2, comment: 3 }
  high-interaction-percentile: 0.8
  min-sample-size: 10
```

## 前端字段对齐

| 前端位置 | 字段 |
|---|---|
| `BrandStats.vue` | `SocialStatsVO` 全部字段 |
| `BrandFilter.vue` | `companyCategory / companyId / accountNature / topic / timeRange / metricType / sourceType / verificationStatus / keyword` |
| `BrandNoteList.vue` | `SocialNoteVO` + `metrics.missingFields` |
| `BrandRankingPanel.vue` | `RankingVO` + `RankingItemVO` |
| `ManualNoteDialog.vue` | `ManualNoteRequest` |
| `BrandNoteDetail.vue` | `SocialNoteDetailVO.snapshots` |
