<template>
  <div class="drawer-layer" @click.self="$emit('close')">
    <aside class="detail-drawer brand-manual-drawer">
      <div class="drawer-head">
        <div>
          <span class="modal-kicker">MANUAL ENTRY · 人工录入</span>
          <h2>录入一条公开笔记</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')">×</button>
      </div>

      <div class="drawer-body">
        <div class="entry-tip">
          在小红书 App / 网页端打开公开笔记，复制链接并填写页面上<b>看得见</b>的指标；
          看不到的指标请留空，不要填 0，也不要估算。
        </div>

        <div class="form-row">
          <label>笔记链接 <i>*</i></label>
          <input type="text" v-model="form.noteUrl" placeholder="https://www.xiaohongshu.com/...">
        </div>

        <div class="form-row">
          <label>标题 <i>*</i></label>
          <input type="text" v-model="form.title" maxlength="500" placeholder="以页面标题为准">
        </div>

        <div class="form-row">
          <label>摘要</label>
          <textarea v-model="form.contentSummary" rows="2" maxlength="1000" placeholder="不超过 200 字的关键信息，不要粘贴全文"></textarea>
        </div>

        <div class="form-grid">
          <div class="form-row">
            <label>关联企业 <i>*</i></label>
            <select v-model="form.companyId">
              <option value="">请选择</option>
              <option v-for="c in companies" :key="c.id" :value="c.id">{{ c.companyName }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>关联账号</label>
            <select v-model="form.accountId">
              <option value="">不归属具体账号</option>
              <option v-for="a in companyAccounts" :key="a.id" :value="a.id">
                {{ a.accountName }}（{{ a.verifiedStatus === 'VERIFIED' ? '已核验' : '待核验' }}）
              </option>
            </select>
          </div>
          <div class="form-row">
            <label>归属类型</label>
            <select v-model="form.ownerType">
              <option value="UNKNOWN">归属待核验</option>
              <option value="OFFICIAL">企业官方内容</option>
              <option value="USER_DISCUSSION">用户讨论</option>
            </select>
          </div>
          <div class="form-row">
            <label>核验状态</label>
            <select v-model="form.verificationStatus">
              <option value="PENDING">待核验</option>
              <option value="VERIFIED">已核验</option>
              <option value="FAILED">核验失败</option>
            </select>
          </div>
          <div class="form-row">
            <label>发布时间</label>
            <input type="datetime-local" v-model="form.publishedAt">
          </div>
        </div>

        <div class="form-row">
          <label>主题标签</label>
          <div class="topic-picker">
            <button
              v-for="tag in topics"
              :key="tag"
              type="button"
              :class="['topic-chip', form.topicTags.indexOf(tag) >= 0 ? 'on' : '']"
              @click="toggleTopic(tag)"
            >{{ tag }}</button>
          </div>
        </div>

        <div class="form-grid metrics">
          <div class="form-row">
            <label>点赞数</label>
            <input type="number" min="0" v-model="form.likeCount" placeholder="看不到就留空">
          </div>
          <div class="form-row">
            <label>收藏数</label>
            <input type="number" min="0" v-model="form.favoriteCount" placeholder="看不到就留空">
          </div>
          <div class="form-row">
            <label>评论数</label>
            <input type="number" min="0" v-model="form.commentCount" placeholder="看不到就留空">
          </div>
        </div>

        <div class="form-row">
          <label>指标缺失原因</label>
          <input type="text" v-model="form.missingReason" maxlength="256" placeholder="如：收藏数未公开">
        </div>

        <div class="form-row">
          <label>备注</label>
          <input type="text" v-model="form.remark" maxlength="512" placeholder="核验依据 / 截图位置等">
        </div>

        <div v-if="error" class="entry-error">{{ error }}</div>
      </div>

      <div class="drawer-foot">
        <button class="ghost-btn" @click="$emit('close')">取消</button>
        <button class="primary-btn" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : '保存并写入' }}
        </button>
      </div>
    </aside>
  </div>
</template>

<script>
import { createManualNote } from '@/api/employerBrand'
import { TOPIC_TAGS } from '@/constants/employerBrand'

function emptyForm () {
  return {
    noteUrl: '',
    title: '',
    contentSummary: '',
    companyId: '',
    accountId: '',
    ownerType: 'UNKNOWN',
    verificationStatus: 'PENDING',
    publishedAt: '',
    topicTags: [],
    likeCount: '',
    favoriteCount: '',
    commentCount: '',
    missingReason: '',
    remark: ''
  }
}

export default {
  name: 'ManualNoteDialog',
  props: {
    companies: { type: Array, default: function () { return [] } },
    accounts: { type: Array, default: function () { return [] } }
  },
  data: function () {
    return {
      form: emptyForm(),
      topics: TOPIC_TAGS,
      submitting: false,
      error: ''
    }
  },
  computed: {
    companyAccounts: function () {
      if (!this.form.companyId) return []
      var companyId = Number(this.form.companyId)
      return this.accounts.filter(function (a) { return a.companyId === companyId })
    }
  },
  methods: {
    toggleTopic: function (tag) {
      var idx = this.form.topicTags.indexOf(tag)
      if (idx >= 0) {
        this.form.topicTags.splice(idx, 1)
      } else {
        this.form.topicTags.push(tag)
      }
    },
    toNumber: function (v) {
      if (v === '' || v === null || v === undefined) return null
      var n = Number(v)
      return isNaN(n) ? null : n
    },
    toDateTime: function (v) {
      if (!v) return null
      return v.length === 16 ? v + ':00' : v
    },
    validate: function () {
      if (!this.form.noteUrl) return '请填写笔记链接'
      if (!/^https?:\/\//i.test(this.form.noteUrl)) return '链接必须以 http(s):// 开头'
      if (!this.form.title) return '请填写标题'
      if (!this.form.companyId) return '请选择关联企业'
      return ''
    },
    submit: function () {
      var msg = this.validate()
      if (msg) {
        this.error = msg
        return
      }
      var self = this
      this.submitting = true
      this.error = ''
      var payload = {
        noteUrl: this.form.noteUrl.trim(),
        title: this.form.title.trim(),
        contentSummary: this.form.contentSummary || null,
        companyId: Number(this.form.companyId),
        accountId: this.form.accountId ? Number(this.form.accountId) : null,
        ownerType: this.form.ownerType,
        verificationStatus: this.form.verificationStatus,
        publishedAt: this.toDateTime(this.form.publishedAt),
        topicTags: this.form.topicTags,
        likeCount: this.toNumber(this.form.likeCount),
        favoriteCount: this.toNumber(this.form.favoriteCount),
        commentCount: this.toNumber(this.form.commentCount),
        missingReason: this.form.missingReason || null,
        remark: this.form.remark || null
      }
      createManualNote(payload).then(function (id) {
        self.submitting = false
        self.$emit('saved', id)
      }).catch(function (e) {
        self.submitting = false
        self.error = e.message || '保存失败'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-manual-drawer .drawer-body {
  padding-top: 16px;
}

.entry-tip {
  margin-bottom: 12px;
  padding: 9px 10px;
  border-radius: 6px;
  color: #7d6a52;
  background: #fbf5ec;
  font-size: 10px;
  line-height: 1.7;
}

.form-row {
  margin-bottom: 9px;
}

.form-row label {
  display: block;
  margin-bottom: 4px;
  color: #8d97a3;
  font-size: 9px;
}

.form-row label i {
  color: #d9534f;
  font-style: normal;
}

.form-row input,
.form-row select,
.form-row textarea {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #e2e6eb;
  border-radius: 5px;
  outline: 0;
  color: #4f5a68;
  background: #fff;
  font: inherit;
  font-size: 10px;
}

.form-row textarea {
  resize: vertical;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 10px;
}

.topic-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.topic-chip {
  padding: 3px 8px;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  color: #6f7987;
  background: #fff;
  font: inherit;
  font-size: 9px;
  cursor: pointer;
}

.topic-chip.on {
  color: #b45309;
  border-color: #fbd9ae;
  background: #fff6ea;
}

.entry-error {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  color: #c75763;
  background: #fff0f1;
  font-size: 10px;
}

.drawer-foot .primary-btn {
  height: 32px;
  padding: 0 18px;
  font-size: 10px;
}
</style>
