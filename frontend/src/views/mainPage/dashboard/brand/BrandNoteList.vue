<template>
  <section class="panel brand-note-panel">
    <div class="list-head">
      <h3 class="list-title">小红书爆款推文</h3>
      <div class="toolbar-actions">
        <select :value="sortBy" @change="onSortChange">
          <option value="publishedAt">按发布时间</option>
          <option value="like">按点赞数</option>
          <option value="favorite">按收藏数</option>
          <option value="comment">按评论数</option>
          <option value="score">按综合互动</option>
        </select>
        <button class="sort-btn" @click="$emit('sort', sortBy, sortDirection === 'desc' ? 'asc' : 'desc')">
          {{ sortDirection === 'desc' ? '降序 ↓' : '升序 ↑' }}
        </button>
      </div>
    </div>

    <div v-if="!rows.length" class="note-empty">
      <p>当前范围内还没有公开笔记记录。</p>
      <p class="note-empty-tip">数据只能来自人工录入：在小红书 App / 网页端查看公开笔记后，把链接与页面可见指标录入系统。</p>
      <button class="primary-btn" @click="$emit('create')">录入第一条笔记</button>
    </div>

    <div v-else class="note-grid">
      <article v-for="(row, idx) in rows" :key="row.id" class="note-card" @click="$emit('detail', row.id)">
        <div class="nc-head">
          <span class="nc-no">{{ seq(idx) }}</span>
          <h3 class="nc-title" :title="row.title">{{ row.title }}</h3>
        </div>

        <p v-if="row.contentSummary" class="nc-summary">{{ row.contentSummary }}</p>
        <p v-else class="nc-summary nc-summary-empty">暂无摘要，点击查看详情</p>

        <div class="nc-links">
          <a v-if="isHttp(row.noteUrl)" :href="row.noteUrl" target="_blank" rel="noopener noreferrer" @click.stop>查看原文 ↗</a>
          <span class="nc-spacer"></span>
          <button class="nc-detail" @click.stop="$emit('detail', row.id)">查看详情 ›</button>
        </div>

        <div class="nc-tags">
          <span class="tag-company">{{ row.companyName }}</span>
          <span v-if="row.accountName" class="tag-account">
            {{ row.accountName }}
            <i :class="['tag-verify', row.accountVerified ? 'ok' : 'pending']">
              {{ row.accountVerified ? '已核验' : '待核验' }}
            </i>
          </span>
          <span :class="['tag-owner', row.ownerType === 'OFFICIAL' ? 'official' : 'user']">
            {{ ownerLabel(row.ownerType) }}
          </span>
          <span v-if="row.cleanSource" class="tag-clean" :title="row.cleanBasis || ''">AI 已清洗</span>
          <span v-for="tag in row.topicTags" :key="tag" class="tag-topic">{{ tag }}</span>
        </div>
      </article>
    </div>

    <div class="list-foot">
      <span class="foot-tip">共 {{ total }} 条 · 每页 {{ size }} 条 · {{ sortLabel }}</span>
      <div class="pager">
        <button :disabled="page <= 1" @click="$emit('page', page - 1)">‹</button>
        <b>{{ page }}</b>
        <span>/ {{ totalPages || 1 }}</span>
        <button :disabled="page >= totalPages" @click="$emit('page', page + 1)">›</button>
      </div>
    </div>
  </section>
</template>

<script>
import { OWNER_LABELS } from '@/constants/employerBrand'

export default {
  name: 'BrandNoteList',
  props: {
    rows: { type: Array, default: function () { return [] } },
    total: { type: Number, default: 0 },
    page: { type: Number, default: 1 },
    size: { type: Number, default: 6 },
    totalPages: { type: Number, default: 0 },
    sortBy: { type: String, default: 'publishedAt' },
    sortDirection: { type: String, default: 'desc' }
  },
  computed: {
    sortLabel: function () {
      var map = { publishedAt: '按发布时间', like: '按点赞数', favorite: '按收藏数', comment: '按评论数', score: '按综合互动' }
      return map[this.sortBy] || '按发布时间'
    }
  },
  methods: {
    onSortChange: function (e) {
      this.$emit('sort', e.target.value, this.sortDirection)
    },
    /** 全局序号：跨页连续（每页 6 条时第 2 页从 07 开始） */
    seq: function (idx) {
      var n = (this.page - 1) * this.size + idx + 1
      return n < 10 ? '0' + n : '' + n
    },
    ownerLabel: function (v) {
      return OWNER_LABELS[v] || '归属待核验'
    },
    isHttp: function (url) {
      return !!url && url.indexOf('http') === 0
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-note-panel {
  margin-top: 8px;
  padding: 12px 14px 10px;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f4;
}

.list-title {
  margin: 0;
  color: #36414d;
  font-size: 12.5px;
  font-weight: 600;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 7px;
}

.toolbar-actions select {
  height: 26px;
  padding: 0 7px;
  border: 1px solid #e2e6eb;
  border-radius: 5px;
  color: #5f6a78;
  background: #fff;
  font: inherit;
  font-size: 10px;
}

.sort-btn {
  height: 26px;
  padding: 0 9px;
  border: 1px solid #e2e6eb;
  border-radius: 5px;
  color: #6b7683;
  background: #fff;
  font: inherit;
  font-size: 10px;
  cursor: pointer;
}

/* 三列 × 两行：每页 6 条 */
.note-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  padding: 10px 0 4px;
}

.note-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 11px 9px;
  border: 1px solid #eef1f4;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: border-color .15s, box-shadow .15s, transform .15s;
}

.note-card:hover {
  border-color: #f3d2ae;
  box-shadow: 0 3px 12px rgba(217, 119, 6, .08);
}

.nc-head {
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.nc-no {
  flex: 0 0 auto;
  padding: 1px 5px;
  border-radius: 4px;
  color: #fff;
  background: #d97706;
  font-size: 9px;
  font-weight: 700;
  line-height: 15px;
}

.nc-title {
  margin: 0;
  overflow: hidden;
  color: #36414d;
  font-size: 11.5px;
  font-weight: 600;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  text-overflow: ellipsis;
  word-break: break-word;
}

.nc-summary {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #8b95a1;
  font-size: 10px;
  line-height: 1.6;
  /* 摘要最多 3 行，超出省略号；点「查看详情」看全文 */
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  text-overflow: ellipsis;
  word-break: break-word;
}

.nc-summary-empty {
  color: #b3bac3;
}

.nc-links {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.nc-spacer {
  flex: 1;
}

.nc-links a {
  color: #d97706;
  font-size: 10px;
  text-decoration: none;
}

.nc-detail {
  padding: 0;
  border: 0;
  color: #d97706;
  background: transparent;
  font: inherit;
  font-size: 10px;
  cursor: pointer;
}

.nc-detail:hover {
  color: #b45309;
  text-decoration: underline;
}

.nc-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.tag-company {
  padding: 2px 6px;
  border-radius: 4px;
  color: #4b6b8d;
  background: #edf3fa;
  font-size: 9px;
}

.tag-account {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  color: #7d6a52;
  background: #fbf5ec;
  font-size: 9px;
}

.tag-verify {
  padding: 0 4px;
  border-radius: 3px;
  font-size: 8px;
  font-style: normal;
}

.tag-verify.ok { color: #4d9666; background: #e9f7ee; }
.tag-verify.pending { color: #b07b33; background: #fff4e2; }

.tag-owner {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 9px;
}

.tag-owner.official { color: #4d9666; background: #edf8f0; }
.tag-owner.user { color: #718090; background: #f1f3f5; }

.tag-clean {
  padding: 2px 6px;
  border-radius: 4px;
  color: #5b47c0;
  background: #f1eeff;
  font-size: 9px;
}

.tag-topic {
  padding: 2px 6px;
  border-radius: 4px;
  color: #6c7480;
  background: #f5f6f8;
  font-size: 9px;
}

.note-empty {
  padding: 40px 10px;
  text-align: center;
  color: #8b95a1;
}

.note-empty p {
  margin: 0 0 6px;
  font-size: 12px;
}

.note-empty-tip {
  max-width: 560px;
  margin: 0 auto 14px;
  color: #a6aeb8;
  font-size: 10px;
  line-height: 1.7;
}

.note-empty .primary-btn {
  height: 30px;
  padding: 0 16px;
  font-size: 10px;
}

.list-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
}

.foot-tip {
  color: #a6aeb8;
  font-size: 9px;
}

.pager {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8d97a3;
  font-size: 10px;
}

.pager button {
  width: 22px;
  height: 22px;
  border: 1px solid #e6e9ed;
  border-radius: 5px;
  color: #697386;
  background: #fff;
  cursor: pointer;
}

.pager button:disabled {
  color: #c6cbd2;
  cursor: default;
}

.pager b {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 5px;
  color: #fff;
  background: #d97706;
  font-weight: 500;
}

@media (max-width: 1360px) {
  .note-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 960px) {
  .note-grid { grid-template-columns: 1fr; }
}
</style>
