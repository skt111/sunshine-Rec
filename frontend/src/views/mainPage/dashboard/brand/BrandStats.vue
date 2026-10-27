<template>
  <div class="brand-stats">
    <div v-for="card in cards" :key="card.key" :class="['brand-stat', card.tone]">
      <span class="stat-icon">{{ card.icon }}</span>
      <div>
        <small>{{ card.label }}</small>
        <strong>{{ card.value }}</strong>
        <p>{{ card.note }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BrandStats',
  props: {
    stats: { type: Object, default: function () { return {} } }
  },
  computed: {
    cards: function () {
      var s = this.stats || {}
      return [
        {
          key: 'company',
          label: '已核验重点企业',
          value: this.n(s.verifiedCompanyCount),
          note: '已核验官方账号的企业',
          icon: '◈',
          tone: 'purple'
        },
        {
          key: 'accounts',
          label: '启用账号',
          value: this.n(s.enabledAccountCount),
          note: '已核验 ' + this.n(s.verifiedAccountCount) + ' · 待核验 ' + this.n(s.pendingAccountCount),
          icon: '◎',
          tone: 'blue'
        },
        {
          key: 'notes',
          label: '公开笔记',
          value: this.n(s.noteCount),
          note: this.rangeText(s),
          icon: '▤',
          tone: 'orange'
        },
        {
          key: 'high',
          label: '高互动笔记',
          value: s.highInteractionInsufficient ? '—' : this.n(s.highInteractionCount),
          note: s.highInteractionInsufficient ? '样本不足，暂不计算' : '综合分前 20%',
          icon: '↗',
          tone: 'green'
        },
        {
          key: 'pendingNotes',
          label: '待核验笔记',
          value: this.n(s.pendingNoteCount),
          note: '需人工确认归属或链接',
          icon: '!',
          tone: 'red'
        },
        {
          key: 'observed',
          label: '最近指标时间',
          value: this.shortTime(s.lastObservedAt),
          note: '指标以录入时页面可见为准',
          icon: '◷',
          tone: 'violet'
        }
      ]
    }
  },
  methods: {
    n: function (v) {
      return (v === null || v === undefined) ? 0 : v
    },
    rangeText: function (s) {
      if (!s.rangeStart && !s.rangeEnd) return '不限时间'
      return (s.rangeStart || '不限') + ' ~ ' + (s.rangeEnd || '不限')
    },
    shortTime: function (v) {
      if (!v) return '暂无'
      var s = String(v).replace('T', ' ')
      return s.slice(5, 16)
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-stats {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  margin: 4px 0 14px;
}

.brand-stat {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  padding: 10px 11px;
  border: 1px solid #e8ebef;
  border-radius: 9px;
  background: #fff;
}

.brand-stat .stat-icon {
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  width: 26px;
  height: 26px;
  border-radius: 7px;
  color: #8065d4;
  background: #f2efff;
  font-size: 13px;
}

.brand-stat.blue .stat-icon { color: #4385cf; background: #edf5ff; }
.brand-stat.orange .stat-icon { color: #d9791f; background: #fff4e6; }
.brand-stat.green .stat-icon { color: #4d9666; background: #edf8f0; }
.brand-stat.red .stat-icon { color: #c75763; background: #fff0f1; }
.brand-stat.violet .stat-icon { color: #6a5ca8; background: #f4f2ff; }

.brand-stat small {
  display: block;
  color: #8d97a3;
  font-size: 10px;
}

.brand-stat strong {
  display: block;
  margin: 2px 0 1px;
  color: #2f3742;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -.02em;
}

.brand-stat p {
  margin: 0;
  color: #a3abb6;
  font-size: 9px;
  line-height: 1.4;
}

@media (max-width: 1360px) {
  .brand-stats { grid-template-columns: repeat(3, 1fr); }
}
</style>
