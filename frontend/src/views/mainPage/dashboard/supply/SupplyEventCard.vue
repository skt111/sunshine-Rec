<template>
  <div class="event-row" @click="$emit('detail', event)">
    <div class="event-date">
      <b>{{ dayText }}</b>
      <span>{{ monthText }}</span>
    </div>

    <div class="event-main">
      <h3>{{ event.eventTitle }}</h3>
      <div class="event-meta">
        <span v-if="event.companyName" class="event-company">{{ event.companyName }}</span>
        <span>{{ event.schoolName }}</span>
        <span>{{ event.schoolCity }}</span>
        <span :class="['event-type', categoryTone]">{{ categoryLabel }}</span>
        <span>{{ timeText }}</span>
        <span>{{ event.venueName || '地点待核验' }}</span>
        <span v-if="event.recruitmentEntity" class="event-entity">{{ event.recruitmentEntity }}</span>
        <span v-if="!event.eventDate" class="event-type pending">日期待核</span>
      </div>
      <div v-if="isFair" class="event-registration">
        <span :class="['event-status', regTone]">{{ regLabel }}</span>
        <!-- 报名时间未知时不展示「暂未公布 ~ 暂未公布」，详情页看具体链接 -->
        <span v-if="hasRegTime">报名：{{ regStartText }} ~ {{ regDeadlineText }}</span>
        <span v-if="event.registrationMethod" class="event-method">{{ event.registrationMethod }}</span>
        <button v-if="event.registrationUrl" class="reg-link" @click.stop="$emit('source', { sourceUrl: event.registrationUrl })">报名入口 ↗</button>
      </div>
    </div>

    <div class="event-side">
      <span :class="['event-status', statusTone]">{{ statusLabel }}</span>
      <small>更新 {{ lastSeenText }}</small>
    </div>

    <div class="event-actions">
      <button @click.stop="$emit('detail', event)">查看详情</button>
      <button v-if="sourceLink" @click.stop="$emit('source', { sourceUrl: sourceLink })">来源 ↗</button>
      <span v-else class="src-missing">来源待补</span>
    </div>
  </div>
</template>

<script>
import {
  CATEGORY_LABELS, CATEGORY_TONES, EVENT_TYPE_LABELS, EVENT_STATUS_LABELS, EVENT_STATUS_TONES,
  REGISTRATION_STATUS_LABELS, REGISTRATION_STATUS_TONES
} from '@/constants/supplyRadar'

export default {
  name: 'SupplyEventCard',
  props: {
    event: { type: Object, required: true }
  },
  computed: {
    dayText: function () {
      var d = this.event.eventDate
      return d ? d.slice(8, 10) : '--'
    },
    monthText: function () {
      var d = this.event.eventDate
      if (!d) return '日期待核'
      return d.slice(5, 7) + '月'
    },
    timeText: function () {
      var s = this.event.startTime
      var e = this.event.endTime
      if (!s && !e) return '时间待补充'
      if (s && e) return s + '—' + e
      return s || e
    },
    categoryLabel: function () {
      // 按活动类型展示（宣讲会/双选会），不再使用校内/校外分类口径
      return EVENT_TYPE_LABELS[this.event.eventType] ||
        CATEGORY_LABELS[this.event.standardCategory] || '其他活动'
    },
    categoryTone: function () {
      return CATEGORY_TONES[this.event.standardCategory] || 'other'
    },
    statusLabel: function () {
      return EVENT_STATUS_LABELS[this.event.eventStatus] || '未知'
    },
    statusTone: function () {
      var tone = EVENT_STATUS_TONES[this.event.eventStatus] || 'ended'
      // 兼容现有 css 类名
      var map = { UPCOMING: 'upcoming', ONGOING: 'running', FINISHED: 'ended', CANCELLED: 'cancelled', POSTPONED: 'pending', UNKNOWN: 'ended' }
      return map[this.event.eventStatus] || tone
    },
    venueLabel: function () {
      return CATEGORY_LABELS[this.event.standardCategory] || ''
    },
    lastSeenText: function () {
      var t = this.event.lastSeenAt || this.event.crawledAt
      return t ? t.slice(5, 16) : '—'
    },
    isFair: function () {
      return this.event.eventType === 'JOB_FAIR'
    },
    /** 来源链接：活动详情优先，其次来源列表页 */
    sourceLink: function () {
      return this.event.sourceUrl || this.event.sourceListUrl || ''
    },
    regLabel: function () {
      return REGISTRATION_STATUS_LABELS[this.event.registrationStatus] || '暂未公布'
    },
    regTone: function () {
      return REGISTRATION_STATUS_TONES[this.event.registrationStatus] || 'gray'
    },
    /** 报名起止时间任一存在才展示区间文案 */
    hasRegTime: function () {
      return !!(this.event.registrationStartAt || this.event.registrationDeadline)
    },
    regStartText: function () {
      var t = this.event.registrationStartAt
      return t ? t.slice(5, 16) : '暂未公布'
    },
    regDeadlineText: function () {
      var t = this.event.registrationDeadline
      return t ? t.slice(5, 16) : '暂未公布'
    }
  }
}
</script>

<style scoped>
.event-company {
  background: #fff3e6;
  color: #d96512;
  border-radius: 4px;
  padding: 1px 8px;
  font-weight: 600;
}
.event-entity {
  color: #8c96a3;
}
.event-registration {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
  font-size: 12px;
  color: #5b6572;
}
.event-method {
  color: #8c96a3;
}
.reg-link {
  border: none;
  background: #f58220;
  color: #fff;
  border-radius: 4px;
  padding: 3px 10px;
  cursor: pointer;
  font-size: 12px;
}
.reg-link:hover {
  background: #d96512;
}
.src-missing {
  color: #b9c0c9;
  font-size: 12px;
}
</style>
