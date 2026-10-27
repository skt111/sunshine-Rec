<template>
  <article class="school-card">
    <div class="school-card-head">
      <span class="school-avatar">{{ initial }}</span>
      <div>
        <h3>{{ group.schoolName }}</h3>
        <p>{{ group.schoolCity || '—' }} · {{ crawlText }}</p>
      </div>
      <b>{{ group.eventCount || 0 }} 场</b>
    </div>

    <div class="school-metrics">
      <span><i class="purple-dot"></i>当前筛选范围内</span>
      <span><i class="orange-dot"></i>更新 {{ lastCrawlText }}</span>
    </div>

    <div v-if="open" class="school-events">
      <div
        v-for="event in group.recentEvents"
        :key="event.id"
        class="event-mini"
        @click="$emit('detail', event)"
      >
        <span>{{ event.eventTitle }}</span>
        <b>{{ event.eventDate || '日期待核' }}</b>
      </div>
      <div v-if="!group.recentEvents || !group.recentEvents.length" class="event-mini">
        <span>暂无近期活动</span>
      </div>
    </div>

    <div class="school-card-foot">
      <button class="ghost-btn" @click="toggle">{{ open ? '收起' : '展开' }}</button>
      <button class="ghost-btn" @click="$emit('expand', group.schoolId)">查看全部 →</button>
    </div>
  </article>
</template>

<script>
import { SCHOOL_CRAWL_STATUS_LABELS } from '@/constants/supplyRadar'

export default {
  name: 'SupplySchoolGroup',
  props: {
    group: { type: Object, required: true }
  },
  data: function () {
    return { open: false }
  },
  computed: {
    initial: function () {
      return (this.group.schoolName || '?').slice(0, 1)
    },
    crawlText: function () {
      return SCHOOL_CRAWL_STATUS_LABELS[this.group.lastCrawlStatus] || '从未采集'
    },
    lastCrawlText: function () {
      var t = this.group.lastCrawlAt
      return t ? t.slice(5, 16) : '—'
    }
  },
  methods: {
    toggle: function () {
      this.open = !this.open
    }
  }
}
</script>
