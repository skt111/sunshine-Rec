<template>
  <div class="drawer-layer" @click.self="$emit('close')">
    <aside class="detail-drawer supply-detail">
      <div class="drawer-head">
        <div>
          <span class="modal-kicker">SUPPLY RADAR · CRAWL</span>
          <h2>高校采集状态</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')">×</button>
      </div>

      <div class="drawer-body">
        <div class="evidence-note">
          <span>●</span>
          最近采集 {{ lastCrawlAt || '暂无记录' }} · 状态 {{ lastCrawlStatusText }}
        </div>

        <div class="detail-block">
          <label>高校采集状态（{{ rows.length }} 所）</label>
          <div v-if="loading">加载中…</div>
          <div v-else class="status-table">
            <div class="status-row status-head">
              <span>高校</span><span>城市</span><span>最近采集</span><span>状态</span>
            </div>
            <div v-for="row in rows" :key="row.schoolId" class="status-row">
              <span :title="row.lastFailReason || ''">{{ row.schoolName }}</span>
              <span>{{ row.schoolCity }}</span>
              <span>{{ shortTime(row.lastCrawlAt) }}</span>
              <span :class="['status-tag', row.lastCrawlStatus]">{{ statusText(row.lastCrawlStatus) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="drawer-foot">
        <button class="ghost-btn" @click="load">刷新</button>
        <button class="primary-btn" @click="$emit('close')">关闭</button>
      </div>
    </aside>
  </div>
</template>

<script>
import { fetchCrawlStatus } from '@/api/supplyRadar'
import { CRAWL_TASK_STATUS_LABELS, SCHOOL_CRAWL_STATUS_LABELS } from '@/constants/supplyRadar'

export default {
  name: 'CrawlTaskPanel',
  props: {
    lastCrawlAt: { type: String, default: '' },
    lastCrawlStatus: { type: String, default: '' }
  },
  data: function () {
    return { rows: [], loading: false }
  },
  computed: {
    lastCrawlStatusText: function () {
      return CRAWL_TASK_STATUS_LABELS[this.lastCrawlStatus] || '暂无'
    }
  },
  created: function () {
    this.load()
  },
  methods: {
    load: function () {
      var that = this
      this.loading = true
      fetchCrawlStatus({}).then(function (list) {
        that.rows = list || []
      }).catch(function () {
        that.rows = []
      }).then(function () {
        that.loading = false
      })
    },
    shortTime: function (t) {
      return t ? t.slice(5, 16) : '—'
    },
    statusText: function (s) {
      return SCHOOL_CRAWL_STATUS_LABELS[s] || '从未采集'
    }
  }
}
</script>

<style scoped>
.status-table { margin-top: 8px; font-size: 13px; }
.status-row { display: grid; grid-template-columns: 1.4fr 0.8fr 1.1fr 0.8fr; gap: 8px; padding: 7px 0; border-bottom: 1px solid #eef1f4; }
.status-head { color: #8c96a3; font-weight: 600; }
.status-tag { font-size: 12px; }
.status-tag.SUCCESS { color: #16a34a; }
.status-tag.PARTIAL { color: #d97706; }
.status-tag.FAILED { color: #dc2626; }
.status-tag.NEVER { color: #8c96a3; }
</style>
