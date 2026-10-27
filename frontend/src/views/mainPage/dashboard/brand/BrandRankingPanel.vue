<template>
  <section class="panel brand-rank-panel">
    <div class="rank-head" role="button" @click="collapsed = !collapsed">
      <div>
        <div class="section-label"><span class="section-mark blue-mark"></span>热点排行</div>
        <h2>
          公开笔记互动榜
          <span class="rank-toggle">{{ collapsed ? '点击展开 ▾' : '收起 ▴' }}</span>
        </h2>
      </div>
      <span class="rank-meta">{{ ranking.range || '不限时间' }}</span>
    </div>

    <div v-show="!collapsed" class="rank-body-wrap">
      <div class="rank-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="{ selected: activeType === tab.key }"
          @click="$emit('change-type', tab.key)"
        >{{ tab.label }}</button>
      </div>

      <div class="rank-caption">
        <span>样本 {{ ranking.sampleSize || 0 }} / 共 {{ ranking.totalNotes || 0 }} 条</span>
        <span v-if="ranking.completeness">完整度 {{ ranking.completeness }}</span>
        <span v-if="ranking.metricSource">指标来源 {{ ranking.metricSource }}</span>
      </div>

      <div v-if="!ranking.available" class="rank-empty">
        {{ ranking.reason || '当前范围内暂无可排行的数据' }}
      </div>
      <div v-else-if="ranking.insufficient" class="rank-warn">
        样本不足（少于 10 条），排名仅供参考，不据此下结论
      </div>

      <ol v-if="ranking.available && pagedItems.length" class="rank-list">
        <li v-for="item in pagedItems" :key="item.noteId || item.topic">
          <span class="rank-no">{{ item.rank }}</span>
          <div class="rank-body">
            <a v-if="item.noteUrl" :href="item.noteUrl" target="_blank" rel="noopener noreferrer" class="rank-title">{{ item.title }}</a>
            <span v-else class="rank-title">{{ item.topic || item.title }}</span>
            <span class="rank-sub">
              {{ item.companyName || '主题聚合' }}
              <em v-if="item.metricObservedAt">· 观察 {{ shortTime(item.metricObservedAt) }}</em>
            </span>
          </div>
          <b class="rank-value">{{ valueText(item) }}</b>
        </li>
      </ol>

      <div v-if="totalPages > 1" class="rank-pager">
        <button :disabled="page <= 1" @click.stop="page = page - 1">‹</button>
        <b>{{ page }}</b>
        <span>/ {{ totalPages }}</span>
        <button :disabled="page >= totalPages" @click.stop="page = page + 1">›</button>
      </div>

      <p class="rank-foot">
        综合互动 = 点赞×1 + 收藏×2 + 评论×3（业务候选口径，非平台官方标准）；指标缺失的笔记不参与排名。
      </p>
    </div>
  </section>
</template>

<script>
import { RANKING_TABS } from '@/constants/employerBrand'

export default {
  name: 'BrandRankingPanel',
  props: {
    ranking: { type: Object, default: function () { return {} } },
    activeType: { type: String, default: 'composite' }
  },
  data: function () {
    return {
      tabs: RANKING_TABS,
      /** 默认折叠，点击标题展开 */
      collapsed: true,
      page: 1,
      pageSize: 5
    }
  },
  computed: {
    items: function () {
      var list = this.ranking && this.ranking.items ? this.ranking.items : []
      return list
    },
    totalPages: function () {
      return Math.ceil(this.items.length / this.pageSize) || 0
    },
    pagedItems: function () {
      var start = (this.page - 1) * this.pageSize
      return this.items.slice(start, start + this.pageSize)
    }
  },
  watch: {
    ranking: function () {
      // 榜单数据或类型变化后回到第一页
      this.page = 1
    }
  },
  methods: {
    shortTime: function (v) {
      if (!v) return ''
      return String(v).replace('T', ' ').slice(5, 16)
    },
    valueText: function (item) {
      if (this.activeType === 'topic') return (item.metricValue || 0) + ' 条'
      return item.metricValue === null || item.metricValue === undefined ? '—' : item.metricValue
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-rank-panel {
  padding: 12px 14px 10px;
}

.rank-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8px;
  cursor: pointer;
  user-select: none;
}

.rank-toggle {
  margin-left: 6px;
  color: #d97706;
  font-size: 9px;
  font-weight: 400;
}

.rank-head h2 {
  margin: 2px 0 0;
  font-size: 13px;
}

.rank-meta {
  color: #a3abb6;
  font-size: 9px;
}

.rank-tabs {
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #f0f2f4;
}

.rank-tabs button {
  position: relative;
  padding: 0 0 7px;
  border: 0;
  color: #9aa3af;
  background: transparent;
  font: inherit;
  font-size: 10px;
  cursor: pointer;
}

.rank-tabs button.selected {
  color: #39434f;
  font-weight: 600;
}

.rank-tabs button.selected:after {
  position: absolute;
  right: 0;
  bottom: -1px;
  left: 0;
  height: 2px;
  background: #d97706;
  content: "";
}

.rank-caption {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 7px 0 5px;
  color: #a3abb6;
  font-size: 9px;
}

.rank-empty,
.rank-warn {
  margin: 8px 0;
  padding: 9px 10px;
  border-radius: 6px;
  font-size: 10px;
  line-height: 1.6;
}

.rank-empty {
  color: #8b95a1;
  background: #f7f8fa;
}

.rank-warn {
  color: #9a7331;
  background: #fff8ec;
}

.rank-list {
  margin: 4px 0 0;
  padding: 0;
  list-style: none;
}

.rank-list li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f2f4f6;
}

.rank-list li:last-child {
  border-bottom: 0;
}

.rank-no {
  flex: 0 0 auto;
  width: 18px;
  color: #c3c9d1;
  font-size: 10px;
  font-weight: 700;
  text-align: center;
}

.rank-list li:nth-child(1) .rank-no { color: #d97706; }
.rank-list li:nth-child(2) .rank-no { color: #c98a3f; }
.rank-list li:nth-child(3) .rank-no { color: #b39257; }

.rank-body {
  min-width: 0;
  flex: 1;
}

.rank-title {
  display: block;
  overflow: hidden;
  color: #414b57;
  font-size: 10px;
  font-weight: 500;
  line-height: 1.5;
  text-decoration: none;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-title:hover {
  color: #b45309;
}

.rank-sub {
  display: block;
  margin-top: 3px;
  color: #a6aeb8;
  font-size: 9px;
}

.rank-sub em {
  font-style: normal;
}

.rank-value {
  flex: 0 0 auto;
  color: #6b7683;
  font-size: 11px;
  font-weight: 600;
}

.rank-foot {
  margin: 8px 0 0;
  color: #aab2bc;
  font-size: 9px;
  line-height: 1.6;
}

.rank-pager {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  padding-top: 8px;
  color: #8d97a3;
  font-size: 10px;
}

.rank-pager button {
  width: 22px;
  height: 22px;
  border: 1px solid #e6e9ed;
  border-radius: 5px;
  color: #697386;
  background: #fff;
  font: inherit;
  font-size: 10px;
  cursor: pointer;
}

.rank-pager button:disabled {
  color: #c6cbd2;
  cursor: default;
}

.rank-pager b {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 5px;
  color: #fff;
  background: #d97706;
  font-weight: 500;
}
</style>
