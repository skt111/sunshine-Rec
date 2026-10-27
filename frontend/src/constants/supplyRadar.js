/*
 * 供给端雷达常量（枚举单源的前端侧）
 * 类型页签 4 个（全部/宣讲/双选/其他，不区分校内校外）；PENDING_REVIEW 归入「其他活动」但保留展示标签。
 * 与后端 org.example.supplyradar.enums 保持一致。
 */

/** 企业类别 */
export const COMPANY_CATEGORY_LABELS = {
  INSURANCE: '保险竞品',
  FINANCE: '头部金融',
  INTERNET: '头部互联网'
}

/** 报名状态（文档 4.4） */
export const REGISTRATION_STATUS_LABELS = {
  NOT_OPEN: '未开放',
  OPEN: '开放报名',
  CLOSING_SOON: '即将截止',
  CLOSED: '已截止',
  NOT_PUBLISHED: '暂未公布',
  CANCELLED: '已取消',
  NEEDS_REVIEW: '待核验'
}

export const REGISTRATION_STATUS_TONES = {
  NOT_OPEN: 'gray',
  OPEN: 'green',
  CLOSING_SOON: 'orange',
  CLOSED: 'gray',
  NOT_PUBLISHED: 'blue',
  CANCELLED: 'red',
  NEEDS_REVIEW: 'blue'
}

/** 分类页签（4 个：不区分校内/校外；双选会靠前，重点突出双选会） */
export const TABS = [
  { key: 'ALL', label: '全部活动' },
  { key: 'JOB_FAIR', label: '双选会' },
  { key: 'TALK', label: '宣讲会' },
  { key: 'OTHER', label: '其他活动' }
]

/** 分类展示标签（含 PENDING_REVIEW，用于卡片/详情） */
export const CATEGORY_LABELS = {
  ON_CAMPUS_TALK: '校内宣讲会',
  OFF_CAMPUS_TALK: '校外宣讲会',
  ON_CAMPUS_FAIR: '校内双选会',
  OFF_CAMPUS_FAIR: '校外双选会',
  OTHER: '其他就业活动',
  PENDING_REVIEW: '待人工核验'
}

/** 分类标签配色（class 后缀） */
export const CATEGORY_TONES = {
  ON_CAMPUS_TALK: 'orange',
  OFF_CAMPUS_TALK: 'blue',
  ON_CAMPUS_FAIR: 'green',
  OFF_CAMPUS_FAIR: 'purple',
  OTHER: 'gray',
  PENDING_REVIEW: 'red'
}

/** 活动类型 */
export const EVENT_TYPE_LABELS = {
  TALK: '宣讲会',
  JOB_FAIR: '双选会/招聘会',
  RECRUITMENT_EVENT: '其他招聘活动',
  EMPLOYMENT_SERVICE: '就业服务活动',
  OTHER_EMPLOYMENT_ACTIVITY: '其他就业相关活动',
  UNKNOWN: '待判断'
}

/** 活动状态 */
export const EVENT_STATUS_LABELS = {
  UPCOMING: '未开始',
  ONGOING: '进行中',
  FINISHED: '已结束',
  CANCELLED: '已取消',
  POSTPONED: '已延期',
  UNKNOWN: '未知'
}

export const EVENT_STATUS_TONES = {
  UPCOMING: 'green',
  ONGOING: 'blue',
  FINISHED: 'gray',
  CANCELLED: 'red',
  POSTPONED: 'orange',
  UNKNOWN: 'gray'
}

/** 地点属性 */
export const VENUE_TYPE_LABELS = {
  ON_CAMPUS: '校内',
  OFF_CAMPUS: '校外',
  ONLINE: '线上',
  HYBRID: '线上线下混合',
  UNKNOWN: '未知'
}

/** 核验状态 */
export const VERIFICATION_LABELS = {
  PENDING: '待核验',
  VERIFIED: '已核验',
  REJECTED: '已驳回',
  NEED_REVIEW: '需人工复核'
}

/** 采集任务状态（任务级） */
export const CRAWL_TASK_STATUS_LABELS = {
  PENDING: '排队中',
  RUNNING: '运行中',
  SUCCESS: '全部成功',
  PARTIAL: '部分成功',
  FAILED: '全部失败'
}

/** 高校采集状态（高校级） */
export const SCHOOL_CRAWL_STATUS_LABELS = {
  NEVER: '从未采集',
  SUCCESS: '成功',
  PARTIAL: '部分成功',
  FAILED: '失败'
}

/** 空态与缺失文案（写死，勿散落） */
export const TEXT = {
  EMPTY_LIST: '当前筛选条件下暂无招聘活动，请调整时间范围或高校筛选条件。',
  NO_INFO: '暂无公开信息',
  LOAD_ERROR: '数据加载失败，请稍后重试。',
  CRAWL_TIMEOUT: '采集仍在进行，请稍后在采集任务面板查看结果。'
}

export default {
  TABS: TABS,
  COMPANY_CATEGORY_LABELS: COMPANY_CATEGORY_LABELS,
  REGISTRATION_STATUS_LABELS: REGISTRATION_STATUS_LABELS,
  REGISTRATION_STATUS_TONES: REGISTRATION_STATUS_TONES,
  CATEGORY_LABELS: CATEGORY_LABELS,
  CATEGORY_TONES: CATEGORY_TONES,
  EVENT_TYPE_LABELS: EVENT_TYPE_LABELS,
  EVENT_STATUS_LABELS: EVENT_STATUS_LABELS,
  EVENT_STATUS_TONES: EVENT_STATUS_TONES,
  VENUE_TYPE_LABELS: VENUE_TYPE_LABELS,
  VERIFICATION_LABELS: VERIFICATION_LABELS,
  CRAWL_TASK_STATUS_LABELS: CRAWL_TASK_STATUS_LABELS,
  SCHOOL_CRAWL_STATUS_LABELS: SCHOOL_CRAWL_STATUS_LABELS,
  TEXT: TEXT
}
