<template>
  <div class="drawer-layer" @click.self="$emit('close')">
    <aside class="detail-drawer brand-note-drawer">
      <div class="drawer-head">
        <div>
          <span class="modal-kicker">XIAOHONGSHU NOTE · 公开笔记</span>
          <h2>笔记来源与指标快照</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')">×</button>
      </div>

      <div class="drawer-body">
        <div v-if="loading" class="drawer-loading">加载中…</div>
        <div v-else-if="error" class="drawer-error">{{ error }}</div>

        <template v-else>
          <div class="evidence-note">
            <span>●</span> 数据来源：{{ sourceLabel }} · 非实时抓取 · 指标以录入时页面可见为准
          </div>

          <h3 class="note-title">{{ detail.title }}</h3>
          <p v-if="detail.contentSummary" class="note-summary">{{ detail.contentSummary }}</p>

          <div class="meta-grid">
            <div><small>企业</small><b>{{ detail.companyName }}</b></div>
            <div v-if="detail.accountName"><small>账号</small><b>{{ detail.accountName }}</b></div>
            <div v-if="detail.accountName"><small>账号核验</small><b>{{ detail.accountVerified ? '已核验' : '待核验' }}</b></div>
            <div><small>归属类型</small><b>{{ ownerLabel }}</b></div>
            <div><small>发布时间</small><b>{{ shortTime(detail.publishedAt) }}</b></div>
            <div><small>核验状态</small><b>{{ verifyLabel }}</b></div>
          </div>

          <div class="tag-line">
            <span v-for="tag in detail.topicTags" :key="tag" class="tag">{{ tag }}</span>
            <span v-if="!detail.topicTags || !detail.topicTags.length" class="tag empty">未标注主题</span>
          </div>

          <p class="note-link">
            <a v-if="isHttp" :href="detail.noteUrl" target="_blank" rel="noopener noreferrer">打开原始笔记 ↗</a>
            <span v-else>本记录为截图录入，无网页直链；追溯依据见备注与录入时间。</span>
            <span v-if="isHttp">链接为唯一追溯依据，系统不保存正文全文与媒体文件</span>
          </p>

          <div class="snapshot-block">
            <h4>指标快照（{{ snapshots.length }} 次观察）</h4>
            <div v-if="!snapshots.length" class="snapshot-empty">
              尚无互动指标记录{{ detail.missingReason ? '：' + detail.missingReason : '' }}
            </div>
            <table v-else class="snapshot-table">
              <thead>
                <tr><th>观察时间</th><th>点赞</th><th>收藏</th><th>评论</th><th>来源</th></tr>
              </thead>
              <tbody>
                <tr v-for="s in snapshots" :key="s.id">
                  <td>{{ shortTime(s.metricObservedAt) }}</td>
                  <td :class="{ missing: isMissing(s, '点赞') }">{{ cell(s.likeCount, s, '点赞') }}</td>
                  <td :class="{ missing: isMissing(s, '收藏') }">{{ cell(s.favoriteCount, s, '收藏') }}</td>
                  <td :class="{ missing: isMissing(s, '评论') }">{{ cell(s.commentCount, s, '评论') }}</td>
                  <td>{{ s.metricSource }}</td>
                </tr>
              </tbody>
            </table>
            <p class="snapshot-tip">缺失指标显示为「未公开」，不以 0 补齐；仅当来源提供时才会出现对应数值。</p>
          </div>

          <div v-if="detail.remark" class="remark">备注：{{ detail.remark }}</div>
        </template>
      </div>

      <div class="drawer-foot">
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
      </div>
    </aside>
  </div>
</template>

<script>
import { fetchNoteDetail } from '@/api/employerBrand'
import { OWNER_LABELS, VERIFY_LABELS, SOURCE_LABELS } from '@/constants/employerBrand'

export default {
  name: 'BrandNoteDetail',
  props: {
    noteId: { type: [Number, String], required: true }
  },
  data: function () {
    return { loading: true, error: '', detail: {}, snapshots: [] }
  },
  computed: {
    ownerLabel: function () {
      return OWNER_LABELS[this.detail.ownerType] || '归属待核验'
    },
    verifyLabel: function () {
      return VERIFY_LABELS[this.detail.verificationStatus] || '待核验'
    },
    sourceLabel: function () {
      return SOURCE_LABELS[this.detail.sourceType] || this.detail.sourceType || '人工录入'
    },
    isHttp: function () {
      return !!this.detail.noteUrl && this.detail.noteUrl.indexOf('http') === 0
    }
  },
  mounted: function () {
    this.load()
  },
  methods: {
    load: function () {
      var self = this
      this.loading = true
      this.error = ''
      fetchNoteDetail(this.noteId).then(function (data) {
        self.detail = data || {}
        self.snapshots = (data && data.snapshots) || []
        self.loading = false
      }).catch(function (e) {
        self.error = e.message || '加载失败'
        self.loading = false
      })
    },
    shortTime: function (v) {
      if (!v) return '—'
      return String(v).replace('T', ' ').slice(0, 16)
    },
    isMissing: function (s, name) {
      return (s.missingFields || []).indexOf(name) >= 0
    },
    cell: function (value) {
      if (value === null || value === undefined) return '未公开'
      return value
    }
  }
}
</script>

<style lang="scss" scoped>
.drawer-loading,
.drawer-error {
  padding: 30px 0;
  color: #8b95a1;
  font-size: 11px;
  text-align: center;
}

.drawer-error {
  color: #c75763;
}

.note-title {
  margin: 4px 0 8px;
  color: #333c47;
  font-size: 15px;
  line-height: 1.5;
}

.note-summary {
  margin: 0 0 12px;
  color: #6f7987;
  font-size: 11px;
  line-height: 1.75;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  padding: 10px 0;
  border-top: 1px solid #eef1f4;
  border-bottom: 1px solid #eef1f4;
}

.meta-grid small {
  display: block;
  color: #a3abb6;
  font-size: 9px;
}

.meta-grid b {
  display: block;
  margin-top: 3px;
  color: #4a5461;
  font-size: 11px;
  font-weight: 500;
}

.tag-line {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  padding: 10px 0 4px;
}

.tag {
  padding: 2px 7px;
  border-radius: 4px;
  color: #6c7480;
  background: #f5f6f8;
  font-size: 9px;
}

.tag.empty {
  color: #b3bac3;
}

.note-link {
  margin: 8px 0 0;
  color: #a6aeb8;
  font-size: 9px;
}

.note-link a {
  margin-right: 8px;
  color: #d97706;
  font-size: 10px;
  text-decoration: none;
}

.snapshot-block {
  margin-top: 14px;
}

.snapshot-block h4 {
  margin: 0 0 7px;
  color: #5f6a77;
  font-size: 11px;
}

.snapshot-empty {
  padding: 12px;
  border-radius: 6px;
  color: #8b95a1;
  background: #f7f8fa;
  font-size: 10px;
}

.snapshot-table {
  width: 100%;
  border-collapse: collapse;
}

.snapshot-table th,
.snapshot-table td {
  padding: 7px 6px;
  border-bottom: 1px solid #f0f2f4;
  color: #5f6a77;
  font-size: 10px;
  text-align: left;
}

.snapshot-table th {
  color: #9aa3ae;
  font-weight: 500;
}

.snapshot-table td.missing {
  color: #b9c0c9;
}

.snapshot-tip {
  margin: 8px 0 0;
  color: #aab2bc;
  font-size: 9px;
  line-height: 1.6;
}

.remark {
  margin-top: 12px;
  padding: 9px 10px;
  border-radius: 6px;
  color: #7d6a52;
  background: #fbf5ec;
  font-size: 10px;
  line-height: 1.6;
}
</style>
