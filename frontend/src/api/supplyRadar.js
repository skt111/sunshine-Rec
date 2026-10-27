/*
 * 供给端雷达 API 封装
 * 使用独立 axios 实例（不挂全局拦截器），避免 401 触发不存在的 sessionFail 命名路由。
 * 契约见 .qoder/api-contract/supply-radar-api.md
 */
import axios from 'axios'

const http = axios.create({
  baseURL: '/api/supply-radar',
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
    // 401 兜底：不跳命名路由，由调用方提示
    // P1-8: 优先展示后端 message（区分“未登录”与“登录过期”）
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

/** 高校列表 */
export function fetchSchools(params) {
  return http.get('/schools', { params: params })
}

/** 重点监测企业（供给侧筛选用） */
export function fetchCompanies(params) {
  return http.get('/companies', { params: params })
}

/** 活动列表（列表 / groupBy=school 分组模式） */
export function fetchEvents(params) {
  return http.get('/events', { params: params })
}

/** 统计指标 */
export function fetchStatistics(params) {
  return http.get('/statistics', { params: params })
}

/** 活动详情 */
export function fetchEventDetail(id) {
  return http.get('/events/' + id)
}

/** 启动采集任务 */
export function startCrawlTask(data) {
  return http.post('/crawl-tasks', data)
}

/** 查询采集任务 */
export function fetchCrawlTask(taskId) {
  return http.get('/crawl-tasks/' + taskId)
}

/** 采集状态（各高校） */
export function fetchCrawlStatus(params) {
  return http.get('/crawl-status', { params: params })
}

export default {
  fetchSchools: fetchSchools,
  fetchCompanies: fetchCompanies,
  fetchEvents: fetchEvents,
  fetchStatistics: fetchStatistics,
  fetchEventDetail: fetchEventDetail,
  startCrawlTask: startCrawlTask,
  fetchCrawlTask: fetchCrawlTask,
  fetchCrawlStatus: fetchCrawlStatus
}
