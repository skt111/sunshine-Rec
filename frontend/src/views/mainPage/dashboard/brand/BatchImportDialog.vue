<template>
  <div class="drawer-layer" @click.self="$emit('close')">
    <aside class="detail-drawer brand-batch-drawer">
      <div class="drawer-head">
        <div>
          <span class="modal-kicker">BATCH IMPORT · 批量导入</span>
          <h2>粘贴采集结果</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')">×</button>
      </div>

      <div class="drawer-body">
        <div class="entry-tip">
          把「小红书采集助手」复制出来的 JSON 数组，或从平台后台导出的 CSV 粘贴到下方；每行是一条公开笔记。
          <b>看不到的指标请留空或删掉该字段</b>，系统不会用 0 补齐。
        </div>

        <div class="form-row">
          <label>默认企业（行内没写 companyId 时使用）</label>
          <select v-model="defaultCompanyId">
            <option value="">请选择</option>
            <option v-for="c in companies" :key="c.id" :value="c.id">{{ c.companyName }}</option>
          </select>
        </div>

        <div class="form-row">
          <label>JSON 数组 / CSV <i>*</i></label>
          <textarea
            v-model="raw"
            rows="12"
            spellcheck="false"
            placeholder='[
  {"noteUrl":"https://www.xiaohongshu.com/...","title":"标题","likeCount":12,"favoriteCount":3,"commentCount":1}
]

或 CSV：
noteUrl,title,likeCount,favoriteCount,commentCount'
          ></textarea>
        </div>

        <div class="parse-line">
          <span v-if="parsed">已解析 {{ parsed.length }} 条</span>
          <span v-else>尚未解析</span>
          <button class="ghost-btn" @click="parse">解析预览</button>
        </div>

        <div v-if="parseError" class="entry-error">{{ parseError }}</div>

        <div v-if="parsed && parsed.length" class="preview">
          <div v-for="(row, idx) in parsed" :key="idx" class="preview-row">
            <span class="idx">{{ idx + 1 }}</span>
            <span class="title">{{ row.title || '(无标题)' }}</span>
            <span class="company">{{ companyName(row.companyId) }}</span>
            <span class="nums">赞 {{ num(row.likeCount) }} / 藏 {{ num(row.favoriteCount) }} / 评 {{ num(row.commentCount) }}</span>
          </div>
        </div>

        <div v-if="result" class="result-box">
          <b>导入完成：成功 {{ result.successCount }} 条，失败 {{ result.failCount }} 条</b>
          <ul v-if="result.errors && result.errors.length">
            <li v-for="(e, i) in result.errors" :key="i">第 {{ e.index }} 条（{{ e.title || e.noteUrl }}）：{{ e.message }}</li>
          </ul>
        </div>

        <div v-if="error" class="entry-error">{{ error }}</div>
      </div>

      <div class="drawer-foot">
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
        <button class="primary-btn" :disabled="submitting || !parsed || !parsed.length" @click="submit">
          {{ submitting ? '导入中…' : '导入 ' + (parsed ? parsed.length : 0) + ' 条' }}
        </button>
      </div>
    </aside>
  </div>
</template>

<script>
import { createNotesBatch } from '@/api/employerBrand'

/**
 * 批量导入：只接受人工/半自动采集得到的 JSON 数组，
 * 不做任何平台请求，字段校验全部交给后端。
 */
export default {
  name: 'BatchImportDialog',
  props: {
    companies: { type: Array, default: function () { return [] } }
  },
  data: function () {
    return {
      raw: '',
      defaultCompanyId: '',
      parsed: null,
      parseError: '',
      error: '',
      result: null,
      submitting: false
    }
  },
  watch: {
    raw: function () {
      this.parsed = null
      this.parseError = ''
      this.result = null
    }
  },
  methods: {
    parse: function () {
      this.parseError = ''
      this.parsed = null
      var text = (this.raw || '').trim()
      if (!text) {
        this.parseError = '请先粘贴采集结果'
        return
      }
      var data
      var first = text.charAt(0)
      if (first === '[' || first === '{') {
        try {
          data = JSON.parse(text)
        } catch (e) {
          this.parseError = 'JSON 解析失败：' + e.message
          return
        }
        if (!Array.isArray(data)) {
          data = [data]
        }
      } else {
        data = this.parseCsv(text)
        if (!data.length) {
          this.parseError = 'CSV 未解析出数据，请确认第一行是表头（noteUrl,title,...）'
          return
        }
      }
      var self = this
      var out = []
      var bad = ''
      data.forEach(function (row, idx) {
        if (!row || typeof row !== 'object') {
          bad = '第 ' + (idx + 1) + ' 条不是对象'
          return
        }
        var companyId = row.companyId || self.defaultCompanyId
        if (!companyId) {
          bad = '第 ' + (idx + 1) + ' 条缺少 companyId，且未选择默认企业'
          return
        }
        out.push(Object.assign({}, row, { companyId: Number(companyId) }))
      })
      if (bad) {
        this.parseError = bad
        return
      }
      this.parsed = out
    },
    /** CSV 解析：支持中文/英文表头与逗号、引号转义 */
    parseCsv: function (text) {
      var rows = []
      var row = []
      var cell = ''
      var inQuote = false
      for (var i = 0; i < text.length; i++) {
        var ch = text.charAt(i)
        if (inQuote) {
          if (ch === '"') {
            if (text.charAt(i + 1) === '"') {
              cell += '"'
              i++
            } else {
              inQuote = false
            }
          } else {
            cell += ch
          }
        } else if (ch === '"') {
          inQuote = true
        } else if (ch === ',') {
          row.push(cell)
          cell = ''
        } else if (ch === '\n') {
          row.push(cell)
          rows.push(row)
          row = []
          cell = ''
        } else if (ch !== '\r') {
          cell += ch
        }
      }
      if (cell !== '' || row.length) {
        row.push(cell)
        rows.push(row)
      }
      if (rows.length < 2) {
        return []
      }
      var map = {
        'noteurl': 'noteUrl', '链接': 'noteUrl', '笔记链接': 'noteUrl', 'url': 'noteUrl',
        'title': 'title', '标题': 'title',
        'contentsummary': 'contentSummary', '摘要': 'contentSummary',
        'publishedat': 'publishedAt', '发布时间': 'publishedAt',
        'likecount': 'likeCount', '点赞': 'likeCount', '点赞数': 'likeCount',
        'favoritecount': 'favoriteCount', '收藏': 'favoriteCount', '收藏数': 'favoriteCount',
        'commentcount': 'commentCount', '评论': 'commentCount', '评论数': 'commentCount',
        'companyid': 'companyId', '企业id': 'companyId',
        'topictags': 'topicTags', '主题': 'topicTags', '主题标签': 'topicTags',
        'ownertype': 'ownerType', '归属类型': 'ownerType',
        'verificationstatus': 'verificationStatus', '核验状态': 'verificationStatus'
      }
      var header = rows[0].map(function (h) {
        var k = String(h).replace(/^\uFEFF/, '').trim().toLowerCase()
        return map[k] || ''
      })
      var out = []
      for (var r = 1; r < rows.length; r++) {
        var line = rows[r]
        if (line.length === 1 && !String(line[0]).trim()) {
          continue
        }
        var obj = {}
        header.forEach(function (key, ci) {
          if (!key) return
          var v = line[ci] === undefined ? '' : String(line[ci]).trim()
          if (!v) return
          if (key === 'likeCount' || key === 'favoriteCount' || key === 'commentCount' || key === 'companyId') {
            var n = Number(v.replace(/[,\s]/g, ''))
            obj[key] = isNaN(n) ? null : n
          } else if (key === 'topicTags') {
            obj[key] = v.split(/[,、|/]/).map(function (t) { return t.trim() }).filter(function (t) { return t })
          } else {
            obj[key] = v
          }
        })
        out.push(obj)
      }
      return out
    },
    submit: function () {
      if (!this.parsed || !this.parsed.length) return
      var self = this
      this.submitting = true
      this.error = ''
      createNotesBatch(this.parsed).then(function (res) {
        self.submitting = false
        self.result = res || {}
        if (res && res.successCount > 0) {
          self.$emit('imported', res)
        }
      }).catch(function (e) {
        self.submitting = false
        self.error = e.message || '导入失败'
      })
    },
    companyName: function (id) {
      if (!id) return '未指定企业'
      var hit = this.companies.filter(function (c) { return c.id === Number(id) })
      return hit.length ? hit[0].companyName : '企业#' + id
    },
    num: function (v) {
      return (v === null || v === undefined || v === '') ? '未公开' : v
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-batch-drawer .drawer-body {
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
  font-family: Consolas, Monaco, monospace;
  line-height: 1.6;
  resize: vertical;
}

.parse-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #8d97a3;
  font-size: 10px;
}

.parse-line .ghost-btn {
  height: 26px;
  padding: 0 10px;
  font-size: 10px;
}

.preview {
  margin-top: 8px;
  max-height: 180px;
  overflow-y: auto;
  border: 1px solid #eef1f4;
  border-radius: 6px;
}

.preview-row {
  display: grid;
  grid-template-columns: 24px 1fr 90px 130px;
  gap: 6px;
  padding: 6px 8px;
  border-bottom: 1px solid #f4f6f8;
  font-size: 9px;
  color: #6b7683;
}

.preview-row:last-child {
  border-bottom: 0;
}

.preview-row .idx {
  color: #b3bac3;
}

.preview-row .title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-box {
  margin-top: 10px;
  padding: 10px;
  border-radius: 6px;
  background: #f3fbf5;
  color: #477b58;
  font-size: 10px;
  line-height: 1.7;
}

.result-box ul {
  margin: 6px 0 0;
  padding-left: 16px;
  color: #a9624f;
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
