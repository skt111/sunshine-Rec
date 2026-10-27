/*
 * 雇主品牌雷达常量（与后端 SocialBrandService 白名单保持一致）
 */

export const TOPIC_TAGS = [
  '校园招聘',
  '秋招/春招/补录',
  '实习招聘',
  '岗位与专业要求',
  '薪酬福利',
  '职业发展',
  '企业文化',
  '员工体验',
  '面试与求职经验',
  '人才培养',
  '工作环境',
  '其他',
  '待分类'
]

export const CATEGORY_LABELS = {
  INSURANCE: '保险竞品',
  FINANCE: '头部金融',
  INTERNET: '头部互联网',
  CROSS_INDUSTRY: '跨行业标杆'
}

export const ACCOUNT_NATURE_LABELS = {
  OFFICIAL_ENTERPRISE: '官方企业账号',
  OFFICIAL_RECRUIT: '官方招聘账号',
  EMPLOYEE_PERSONAL: '员工个人账号',
  PUBLIC: '普通公开账号',
  UNKNOWN: '账号性质未知'
}

export const VERIFY_LABELS = {
  VERIFIED: '已核验',
  PENDING: '待核验',
  FAILED: '核验失败'
}

export const OWNER_LABELS = {
  OFFICIAL: '企业官方内容',
  USER_DISCUSSION: '用户讨论',
  UNKNOWN: '归属待核验'
}

export const SOURCE_LABELS = {
  MANUAL: '人工录入',
  MANUAL_SCREENSHOT: '截图录入',
  AUTHORIZED_API: '授权接口',
  AUTHORIZED_SERVICE: '授权服务',
  PUBLIC_PAGE: '公开页面'
}

export const RANKING_TABS = [
  { key: 'composite', label: '综合互动' },
  { key: 'like', label: '点赞' },
  { key: 'favorite', label: '收藏' },
  { key: 'comment', label: '评论' },
  { key: 'topic', label: '主题' }
]

export const SOURCE_DISCLAIMER = '数据来源：人工录入的公开笔记快照 · 非实时抓取 · 指标以录入时页面可见为准'
