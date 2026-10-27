/*
 * 采集任务触发 + 轮询逻辑（从 SupplyRadar.vue 拆出，控制单文件 400 行红线）
 * 使用方需提供 loadAll() 方法，用于采集完成后刷新列表与统计。
 */
import { startCrawlTask, fetchCrawlTask } from '@/api/supplyRadar'
import { TEXT } from '@/constants/supplyRadar'

/** 轮询间隔（毫秒）与最大轮询次数（约 5 分钟后放弃） */
var POLL_INTERVAL_MS = 3000
var MAX_POLL_COUNT = 100

export default {
  data: function () {
    return {
      crawling: false,
      pollTimer: null,
      pollCount: 0
    }
  },
  beforeDestroy: function () {
    this.stopPoll()
  },
  methods: {
    /** 触发采集 + 开始轮询 */
    triggerCrawl: function () {
      var that = this
      if (this.crawling) return
      this.crawling = true
      this.pollCount = 0
      startCrawlTask({ taskType: 'ALL', triggeredBy: 'manual' })
        .then(function (task) {
          that.pollTask(task.taskId)
        })
        .catch(function (e) {
          that.crawling = false
          if (window.$Message) window.$Message.error((e && e.message) ? e.message : '采集启动失败')
        })
    },
    pollTask: function (taskId) {
      var that = this
      this.pollCount++
      if (this.pollCount > MAX_POLL_COUNT) {
        this.stopPoll()
        this.crawling = false
        if (window.$Message) window.$Message.warning(TEXT.CRAWL_TIMEOUT)
        return
      }
      // 页面切到后台时降低请求频率
      if (document.hidden) {
        this.pollTimer = setTimeout(function () { that.pollTask(taskId) }, POLL_INTERVAL_MS)
        return
      }
      fetchCrawlTask(taskId).then(function (task) {
        var status = task && task.status
        if (status === 'SUCCESS' || status === 'PARTIAL' || status === 'FAILED') {
          that.stopPoll()
          that.crawling = false
          that.loadAll(true)
          if (window.$Message) window.$Message.success('采集完成：' + status)
        } else {
          that.pollTimer = setTimeout(function () { that.pollTask(taskId) }, POLL_INTERVAL_MS)
        }
      }).catch(function () {
        that.stopPoll()
        that.crawling = false
      })
    },
    stopPoll: function () {
      if (this.pollTimer) {
        clearTimeout(this.pollTimer)
        this.pollTimer = null
      }
    }
  }
}
