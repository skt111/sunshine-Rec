<template>
  <div class="cd-block">
    <!-- 空态：未来 30 天内无已采集的双选会 -->
    <div v-if="!items.length" class="cd-empty">
      <span>未来 30 天内暂无双选会报名安排</span>
      <small>采集补全目标院校双选会后将自动展示报名倒计时</small>
    </div>

    <template v-else>
      <!-- 顶部预警条：取最紧急的一条（剩余天数最少） -->
      <div v-if="urgent" class="cd-alert">
        <div class="cd-alert-main">
          <span :class="['cd-alert-tag', urgent.tone]">{{ urgent.alertLabel }}</span>
          <div class="cd-alert-text">
            <h3>{{ urgent.alertTitle }}</h3>
            <p>{{ urgent.alertDesc }}</p>
          </div>
        </div>
        <button class="cd-alert-btn" @click="onGo(urgent)">{{ urgent.alertText }} →</button>
      </div>

      <!-- 未来 30 天倒计时卡片（横向滚动，两侧渐变提示 + 悬浮抬起） -->
      <div class="cd-panel">
        <h3 class="cd-panel-title">未来30天 · 目标院校双选会报名倒计时</h3>
        <div class="cd-scroll" :class="{ 'sc-left': canLeft, 'sc-right': canRight }">
          <div ref="scroller" class="cd-list" @scroll="updateScroll">
            <div v-for="item in items" :key="item.id" :class="['cd-card', item.tone]">
              <h4 :title="item.schoolName">{{ item.schoolName }}</h4>
              <p class="cd-name" :title="item.eventTitle">{{ item.eventTitle }}</p>
              <p class="cd-date" :title="item.dateText">{{ item.dateText }}</p>
              <div class="cd-count">
                <strong>{{ item.daysText }}</strong>
                <span>{{ item.daysLabel }}</span>
              </div>
              <button v-if="item.hasLink" class="cd-link" @click="onGo(item)">{{ item.linkText }} ●</button>
              <span v-else class="cd-link disabled">入口待补</span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import { REGISTRATION_STATUS_LABELS } from '@/constants/supplyRadar'

var DAY_MS = 86400000
/** 卡片展示条数 */
var MAX_CARDS = 4

function startOfDay (date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

/** yyyy-MM-dd 或 yyyy-MM-dd HH:mm → Date（仅取日期，避免时区漂移） */
function parseDate (text) {
  if (!text) return null
  var parts = String(text).slice(0, 10).split('-')
  if (parts.length !== 3) return null
  var y = Number(parts[0])
  var m = Number(parts[1])
  var d = Number(parts[2])
  if (!y || !m || !d) return null
  return new Date(y, m - 1, d)
}

function monthDay (date) {
  return (date.getMonth() + 1) + '月' + date.getDate() + '日'
}

/** 天数分级：≤3 红 / ≤7 橙 / 其余绿 */
function toneOf (days) {
  if (days <= 3) return 'red'
  if (days <= 7) return 'amber'
  return 'green'
}

/**
 * 挑选展示卡片：先按活动名去重，再保证各高校至少各 1 条，最后按天数升序补齐；
 * 避免整排都是同一所学校的重复活动。
 */
function pickDiverse (list, limit) {
  var seenTitle = {}
  var unique = []
  for (var i = 0; i < list.length; i++) {
    var title = (list[i].eventTitle || '').trim()
    if (title && seenTitle[title]) continue
    if (title) seenTitle[title] = true
    unique.push(list[i])
  }
  var picked = []
  var seenSchool = {}
  for (var j = 0; j < unique.length && picked.length < limit; j++) {
    var school = unique[j].schoolName
    if (seenSchool[school]) continue
    seenSchool[school] = true
    picked.push(unique[j])
  }
  for (var k = 0; k < unique.length && picked.length < limit; k++) {
    if (picked.indexOf(unique[k]) >= 0) continue
    picked.push(unique[k])
  }
  picked.sort(function (a, b) { return a.daysDistance - b.daysDistance })
  return picked
}

export default {
  name: 'SupplyRadarCountdown',
  props: {
    /** 未来 30 天已采集的双选会（EventVO 列表） */
    events: { type: Array, default: function () { return [] } },
    /** 高校列表（SchoolVO），用于活动无任何链接时兜底到学校就业网 */
    schools: { type: Array, default: function () { return [] } }
  },
  data: function () {
    return {
      /** 横向滚动位置标记：驱动两侧渐变遮罩 */
      canLeft: false,
      canRight: false
    }
  },
  mounted: function () {
    var that = this
    this.$nextTick(function () { that.updateScroll() })
    window.addEventListener('resize', this.updateScroll)
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.updateScroll)
  },
  watch: {
    items: function () {
      var that = this
      this.$nextTick(function () { that.updateScroll() })
    }
  },
  computed: {
    /** schoolId → 学校就业网首页（链接兜底） */
    schoolSiteMap: function () {
      var map = {}
      this.schools.forEach(function (s) {
        if (s && s.schoolId && s.siteUrl) map[s.schoolId] = s.siteUrl
      })
      return map
    },
    /** 全量倒计时条目（按天数升序，未截断） */
    sortedEvents: function () {
      var that = this
      var today = startOfDay(new Date())
      var out = []
      this.events.forEach(function (e) {
        if (!e || !e.id) return
        var eventDay = parseDate(e.eventDate)
        if (!eventDay) return
        // 报名截止日优先；缺失时以活动日期兜底（当前采集侧 deadline 多为空）
        var deadline = parseDate(e.registrationDeadline)
        var target = deadline || eventDay
        var days = Math.round((target - today) / DAY_MS)
        var dayValue = days < 0 ? 0 : days
        // 入口优先级：报名链接 → 活动详情 → 双选会列表页 → 学校就业网首页
        var url = e.registrationUrl || e.sourceUrl || e.sourceListUrl || that.schoolSiteMap[e.schoolId] || ''
        var linkText = '就业网'
        if (e.registrationUrl) linkText = '报名入口'
        else if (e.sourceUrl) linkText = '活动详情'
        else if (e.sourceListUrl) linkText = /^https?:\/\/[^/]+\/?$/.test(e.sourceListUrl) ? '就业网' : '双选会列表'
        out.push({
          id: e.id,
          schoolName: e.schoolName || '院校待核',
          eventTitle: e.eventTitle || '校招活动',
          dateText: monthDay(eventDay) + (e.venueName ? ' · ' + e.venueName : (e.schoolCity ? ' · ' + e.schoolCity : '')),
          daysText: dayValue === 0 ? '今天' : String(dayValue),
          daysLabel: dayValue === 0
            ? (deadline ? '截止' : '举办')
            : (deadline ? '天后截止' : '天后举办'),
          hasDeadline: !!deadline,
          targetText: monthDay(target),
          daysDistance: dayValue,
          tone: toneOf(dayValue),
          regLabel: REGISTRATION_STATUS_LABELS[e.registrationStatus] || '报名状态待核',
          hasLink: !!url,
          linkText: linkText,
          alertText: e.registrationUrl ? '前往报名' : '查看活动',
          url: url
        })
      })
      out.sort(function (a, b) { return a.daysDistance - b.daysDistance })
      return out
    },
    /** 卡片展示：去重 + 跨高校挑选后截断到 MAX_CARDS */
    items: function () {
      return pickDiverse(this.sortedEvents, MAX_CARDS)
    },
    urgent: function () {
      // 预警基于全量条目（不受卡片条数限制）：优先「@用人单位」专场，其次有报名截止日的，最后取最近一条
      var list = this.sortedEvents
      if (!list.length) return null
      var first = null
      for (var i = 0; i < list.length; i++) {
        if ((list[i].eventTitle || '').indexOf('@用人单位') >= 0) { first = list[i]; break }
      }
      if (!first) {
        for (var j = 0; j < list.length; j++) {
          if (list[j].hasDeadline) { first = list[j]; break }
        }
      }
      if (!first) first = list[0]
      var label = '活动提醒'
      if (first.hasDeadline && first.daysDistance <= 3) label = '红色预警'
      else if (first.daysDistance <= 3) label = '紧急提醒'
      else if (first.daysDistance <= 7) label = '临近提醒'
      var remain = first.daysDistance === 0 ? '今天' : ('剩 ' + first.daysDistance + ' 天')
      var targetLabel = first.hasDeadline ? '报名截止' : '活动日期'
      return {
        id: first.id,
        tone: first.tone,
        alertLabel: label,
        alertTitle: first.schoolName + first.eventTitle + ' · ' + targetLabel + ' ' + first.targetText + '（' + remain + '）',
        alertDesc: first.dateText + ' · ' + first.regLabel + (first.hasLink ? ' · 可在线报名' : ' · 报名入口待公布'),
        alertText: first.alertText,
        url: first.url
      }
    }
  },
  methods: {
    /** 计算横向滚动位置，控制两侧渐变遮罩显隐 */
    updateScroll: function () {
      var el = this.$refs.scroller
      if (!el) {
        this.canLeft = false
        this.canRight = false
        return
      }
      this.canLeft = el.scrollLeft > 2
      this.canRight = el.scrollLeft + el.clientWidth < el.scrollWidth - 2
    },
    onGo: function (item) {
      if (item.url) {
        // 父级 openSource 读取 sourceUrl，这里做最小适配以复用同一打开逻辑
        this.$emit('source', { sourceUrl: item.url })
        return
      }
      this.$emit('detail', { id: item.id })
    }
  }
}
</script>

<style scoped>
.cd-block { margin-top: 6px; }

/* 空态 */
.cd-empty {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  border: 1px dashed #dfe3e9;
  border-radius: 10px;
  background: #fafbfc;
  color: #6f7b89;
  font-size: 12px;
}
.cd-empty small { color: #9aa4b1; font-size: 11px; }

/* 顶部预警条 */
.cd-alert {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 16px;
  border-left: 5px solid #e2543a;
  border-radius: 10px;
  background: linear-gradient(90deg, #fdeee7 0%, #fdf7f3 55%, #ffffff 100%);
}
.cd-alert-main { display: flex; align-items: center; gap: 14px; min-width: 0; }
.cd-alert-tag {
  flex: none;
  padding: 4px 10px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: #e2543a;
}
.cd-alert-tag.amber { background: #e59a24; }
.cd-alert-tag.green { background: #4d9666; }
.cd-alert-text { min-width: 0; }
.cd-alert-text h3 {
  margin: 0 0 3px;
  font-size: 15px;
  font-weight: 700;
  color: #2c333c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cd-alert-text p {
  margin: 0;
  font-size: 12px;
  color: #7b8492;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cd-alert-btn {
  flex: none;
  height: 32px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  background: #f07c22;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.cd-alert-btn:hover { background: #d96512; }

/* 倒计时面板 */
.cd-panel {
  margin-top: 10px;
  padding: 12px 14px 10px;
  border: 1px solid #eef0f3;
  border-radius: 10px;
  background: #fff;
}
.cd-panel-title {
  margin: 0 0 10px;
  padding-left: 9px;
  border-left: 3px solid #f07c22;
  font-size: 13px;
  font-weight: 700;
  color: #2c333c;
  line-height: 1.2;
}
/* 抽屉式滚动区：两侧渐变遮罩提示可滑动 */
.cd-scroll {
  position: relative;
  padding: 2px 0 4px;
}
.cd-scroll::before,
.cd-scroll::after {
  position: absolute;
  z-index: 2;
  top: 0;
  bottom: 4px;
  width: 44px;
  opacity: 0;
  transition: opacity .2s ease;
  pointer-events: none;
  content: "";
}
.cd-scroll::before {
  left: 0;
  background: linear-gradient(90deg, #fff 18%, rgba(255, 255, 255, 0));
}
.cd-scroll::after {
  right: 0;
  background: linear-gradient(270deg, #fff 18%, rgba(255, 255, 255, 0));
}
.cd-scroll.sc-left::before { opacity: 1; }
.cd-scroll.sc-right::after { opacity: 1; }

.cd-list {
  display: flex;
  align-items: stretch;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  scroll-behavior: smooth;
}
.cd-list::-webkit-scrollbar {
  height: 6px;
}
.cd-list::-webkit-scrollbar-thumb {
  border-radius: 3px;
  background: #d5dae1;
}
.cd-card {
  display: flex;
  flex-direction: column;
  flex: 1 1 0;
  width: auto;
  min-width: 170px;
  max-width: none;
  overflow: hidden;
  padding: 10px 12px 11px;
  border: 1px solid #e3e7ec;
  border-radius: 9px;
  background: #fbfcfd;
  transition: transform .18s ease, box-shadow .18s ease;
}
.cd-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(31, 41, 55, .10);
}
.cd-card h4 {
  margin: 0 0 4px;
  max-width: 100%;
  font-size: 14px;
  font-weight: 700;
  color: #2c333c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cd-name {
  margin: 0 0 3px;
  max-width: 100%;
  font-size: 11px;
  color: #7b8492;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cd-date {
  margin: 0;
  max-width: 100%;
  font-size: 11px;
  color: #9aa4b1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cd-count { display: flex; align-items: baseline; gap: 4px; margin: 7px 0 8px; }
.cd-count strong { font-size: 26px; font-weight: 700; line-height: 1; }
.cd-count span { font-size: 11px; color: #7b8492; }
.cd-link {
  align-self: flex-start;
  height: 24px;
  margin-top: auto;
  padding: 0 10px;
  border: none;
  border-radius: 12px;
  background: #eef2fb;
  color: #4c6fb5;
  font-size: 11px;
  cursor: pointer;
}
.cd-link:hover { background: #e2e9f8; }
.cd-link.disabled {
  display: inline-flex;
  align-items: center;
  background: #f1f3f5;
  color: #9aa4b1;
  cursor: default;
}

/* 分级配色 */
.cd-card.red { border-color: #f0b4a9; background: #fdf3f0; }
.cd-card.red .cd-count strong { color: #e2543a; }
.cd-card.amber { border-color: #f3d79a; background: #fdf9ee; }
.cd-card.amber .cd-count strong { color: #e59a24; }
.cd-card.green { border-color: #bfe0c9; background: #f2f9f4; }
.cd-card.green .cd-count strong { color: #3f8a5b; }
</style>
