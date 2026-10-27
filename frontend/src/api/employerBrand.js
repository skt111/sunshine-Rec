/*
 * 雇主品牌雷达 API 封装（小红书公开笔记监测 · 阶段一人工录入）
 * 使用独立 axios 实例，避免与全局拦截器耦合。契约见 .qoder/api-contract/social-brand-api.md
 */
import axios from 'axios'

const http = axios.create({
  baseURL: '/api/supply-radar/social',
  timeout: 30000
})

http.interceptors.request.use(
  function (config) {
    var token = localStorage.getItem('token')
    var userToken = localStorage.getItem('userToken')
    if (token) config.headers.token = token
    if (userToken) config.headers.userToken = userToken
    return config
  },
  function (error) {
    return Promise.reject(error)
  }
)

http.interceptors.response.use(
  function (response) {
    var data = response.data || {}
    if (data.status == 401) {
      return Promise.reject(new Error(data.message || '未登录，请先登录系统'))
    }
    if (data.status !== 'success') {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data.result
  },
  function (error) {
    return Promise.reject(error)
  }
)

/** 统计卡 */
export function fetchStats(params) {
  return http.get('/stats', { params: params })
}

/** 企业账号（含核验状态） */
export function fetchAccounts(params) {
  return http.get('/accounts', { params: params })
}

/** 笔记列表 */
export function fetchNotes(params) {
  return http.get('/notes', { params: params })
}

/** 笔记详情（含快照历史） */
export function fetchNoteDetail(id) {
  return http.get('/notes/' + id)
}

/** 榜单 */
export function fetchRankings(params) {
  return http.get('/rankings', { params: params })
}

/** 数据来源状态 */
export function fetchSourceStatus() {
  return http.get('/source-status')
}

/** 来源核验记录 */
export function fetchSourceChecks(params) {
  return http.get('/sources/checks', { params: params })
}

/** 单次可用性探测（先 robots，不采集内容） */
export function probeSource(url) {
  return http.post('/sources/probe', { url: url })
}

/** 人工录入笔记 */
export function createManualNote(data) {
  return http.post('/notes/manual', data)
}

/** 批量导入笔记（单次最多 100 条，逐条校验） */
export function createNotesBatch(list) {
  return http.post('/notes/batch', list)
}

/** 大模型数据清洗（标题去噪 + 归属 + 主题）；失败自动降级规则清洗 */
export function cleanNotes(data) {
  return http.post('/notes/clean', data)
}

export default {
  fetchStats: fetchStats,
  fetchAccounts: fetchAccounts,
  fetchNotes: fetchNotes,
  fetchNoteDetail: fetchNoteDetail,
  fetchRankings: fetchRankings,
  fetchSourceStatus: fetchSourceStatus,
  fetchSourceChecks: fetchSourceChecks,
  probeSource: probeSource,
  createManualNote: createManualNote,
  createNotesBatch: createNotesBatch,
  cleanNotes: cleanNotes
}
