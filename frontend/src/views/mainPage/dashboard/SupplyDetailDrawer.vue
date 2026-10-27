<template>
  <div class="drawer-layer" @click.self="$emit('close')">
    <aside class="detail-drawer supply-detail">
      <div class="drawer-head">
        <div>
          <span class="modal-kicker">SUPPLY RADAR · EVENT DETAIL</span>
          <h2>活动详情</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')">×</button>
      </div>

      <div class="drawer-body">
        <div v-if="loading" style="padding:24px 0;color:#8c96a3">加载中…</div>
        <div v-else-if="error" style="padding:24px 0;color:#d9534f">{{ error }}</div>

        <template v-else-if="detail">
          <div class="evidence-note"><span>●</span>{{ verificationText }} · 最近采集 {{ text(detail.crawledAt) }}</div>

          <div class="supply-detail-title">
            <span :class="['event-type', categoryTone]">{{ categoryLabel }}</span>
            <h3>{{ detail.eventTitle }}</h3>
            <p>{{ detail.schoolName }} · {{ detail.schoolCity }}</p>
          </div>

          <div class="detail-block">
            <label>类型与分类</label>
            <div class="detail-grid">
              <span>活动类型<strong>{{ eventTypeLabel }}</strong></span>
              <span>地点属性<strong>{{ venueTypeLabel }}</strong></span>
              <span>标准分类<strong>{{ categoryLabel }}</strong></span>
              <span>原始类型<strong>{{ text(detail.eventTypeOriginal) }}</strong></span>
            </div>
          </div>

          <div class="detail-block">
            <label>日期与时间</label>
            <div class="detail-grid">
              <span>活动日期<strong>{{ text(detail.eventDate) }}</strong></span>
              <span>起止时间<strong>{{ timeText }}</strong></span>
            </div>
          </div>

          <div class="detail-block">
            <label>举办地点</label>
            <div class="detail-grid">
              <span>场馆<strong>{{ text(detail.venueName) }}</strong></span>
              <span>详细地址<strong>{{ text(detail.eventAddress) }}</strong></span>
            </div>
          </div>

          <div class="detail-block">
            <label>主办与企业</label>
            <div class="detail-grid">
              <span>主办单位<strong>{{ text(detail.organizer) }}</strong></span>
              <span>相关企业<strong>{{ companyText }}</strong></span>
            </div>
          </div>

          <div class="detail-block">
            <label>报名信息</label>
            <div class="detail-grid">
              <span>报名截止<strong>{{ text(detail.registrationDeadline) }}</strong></span>
              <span>
                报名链接
                <strong v-if="detail.registrationUrl">
                  <button type="button" class="reg-inline-link" @click="$emit('source', { sourceUrl: detail.registrationUrl })">报名入口 ↗</button>
                </strong>
                <strong v-else>暂无</strong>
              </span>
            </div>
          </div>

          <div class="detail-block">
            <label>活动说明</label>
            <p>{{ text(detail.eventDescription) }}</p>
          </div>

          <div class="detail-block">
            <label>来源与采集</label>
            <div class="detail-grid">
              <span>来源列表页<strong>{{ text(detail.sourceListUrl) }}</strong></span>
              <span>发布时间<strong>{{ text(detail.sourcePublishedAt) }}</strong></span>
              <span>最近采集<strong>{{ text(detail.crawledAt) }}</strong></span>
            </div>
            <a v-if="detail.sourceUrl" class="source-link" @click="$emit('source', detail)">打开原始来源 ↗</a>
            <p v-else>暂无公开来源</p>
          </div>

          <div class="detail-block">
            <label>最近一次变更</label>
            <div v-if="detail.changes && detail.changes.length">
              <div v-for="(c, idx) in detail.changes" :key="idx" class="change-line">
                <b>{{ changeTypeText(c.changeType) }}</b>
                <span v-if="c.fieldName">{{ c.fieldName }}：{{ c.oldValue || '空' }} → {{ c.newValue || '空' }}</span>
                <small>{{ text(c.changedAt) }}</small>
              </div>
            </div>
            <p v-else>暂无变更记录</p>
          </div>
        </template>
      </div>

      <div class="drawer-foot">
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
        <button class="primary-btn" :disabled="!detail || !detail.sourceUrl" @click="$emit('source', detail)">打开原始来源</button>
      </div>
    </aside>
  </div>
</template>

<script>
import { fetchEventDetail } from '@/api/supplyRadar'
import {
  CATEGORY_LABELS, CATEGORY_TONES, EVENT_TYPE_LABELS, VENUE_TYPE_LABELS,
  VERIFICATION_LABELS, TEXT
} from '@/constants/supplyRadar'

var CHANGE_TYPE = { CREATED: '新建', UPDATED: '更新', CANCELLED: '取消', POSTPONED: '延期' }

export default {
  name: 'SupplyDetailDrawer',
  props: {
    eventId: { type: [Number, String], required: true }
  },
  data: function () {
    return { detail: null, loading: false, error: '' }
  },
  computed: {
    categoryLabel: function () {
      var d = this.detail
      return (EVENT_TYPE_LABELS && EVENT_TYPE_LABELS[d && d.eventType]) ||
        CATEGORY_LABELS[d && d.standardCategory] || '其他活动'
    },
    categoryTone: function () {
      return CATEGORY_TONES[this.detail && this.detail.standardCategory] || 'other'
    },
    eventTypeLabel: function () {
      return (EVENT_TYPE_LABELS && EVENT_TYPE_LABELS[this.detail && this.detail.eventType]) || this.text(this.detail && this.detail.eventType)
    },
    venueTypeLabel: function () {
      return VENUE_TYPE_LABELS[this.detail && this.detail.venueType] || '未知'
    },
    verificationText: function () {
      return VERIFICATION_LABELS[this.detail && this.detail.verificationStatus] || '待核验'
    },
    timeText: function () {
      var s = this.detail && this.detail.startTime
      var e = this.detail && this.detail.endTime
      if (!s && !e) return TEXT.NO_INFO
      return (s || '') + (e ? '—' + e : '')
    },
    companyText: function () {
      var v = this.detail && this.detail.companyNames
      if (!v) return TEXT.NO_INFO
      return String(v).split(/[，,、\s]+/).filter(Boolean).join('、')
    }
  },
  created: function () {
    this.load()
  },
  methods: {
    load: function () {
      var that = this
      this.loading = true
      this.error = ''
      fetchEventDetail(this.eventId).then(function (d) {
        that.detail = d
      }).catch(function (e) {
        that.error = (e && e.message) ? e.message : TEXT.LOAD_ERROR
      }).then(function () {
        that.loading = false
      })
    },
    text: function (v) {
      if (v === null || v === undefined || v === '') return TEXT.NO_INFO
      return v
    },
    changeTypeText: function (t) {
      return CHANGE_TYPE[t] || t || '变更'
    }
  }
}
</script>

<style scoped>
.change-line { display: flex; flex-direction: column; gap: 2px; padding: 6px 0; border-bottom: 1px dashed #eef1f4; font-size: 13px; }
.change-line small { color: #8c96a3; }
.reg-inline-link { padding: 0; border: 0; color: #d97706; background: transparent; font: inherit; cursor: pointer; }
.reg-inline-link:hover { text-decoration: underline; }
</style>
