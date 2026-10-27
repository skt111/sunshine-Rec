<template>
  <div class="drawer-layer" @click.self="$emit('close')">
    <aside class="detail-drawer brand-clean-drawer">
      <div class="drawer-head">
        <div>
          <span class="modal-kicker">LLM CLEANING · 大模型清洗</span>
          <h2>标题去噪 + 归属判定 + 主题打标</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')">×</button>
      </div>

      <div class="drawer-body">
        <div class="entry-tip">
          清洗会把原始标题留档到 <b>title_raw</b>，并记录清洗方式与依据；
          模型不可用或返回不合法时自动降级为规则清洗，不会写回脏数据。
          <b>已核验的记录不会被模型改写归属。</b>
        </div>

        <div class="form-grid">
          <div class="form-row">
            <label>清洗范围</label>
            <select v-model="scopeCompany">
              <option value="">全部企业</option>
              <option v-for="c in companies" :key="c.id" :value="c.id">{{ c.companyName }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>本次条数</label>
            <select v-model.number="limit">
              <option :value="10">10 条</option>
              <option :value="20">20 条</option>
              <option :value="50">50 条（上限受后端配置约束）</option>
            </select>
          </div>
        </div>

        <label class="check-line">
          <input type="checkbox" v-model="includeCleaned">
          包含已清洗过的记录（会重新判定，历史仍可从 title_raw 还原）
        </label>

        <div class="run-line">
          <button class="primary-btn" :disabled="running" @click="run">
            {{ running ? '清洗中…（每条约 1–2 秒）' : '开始清洗' }}
          </button>
          <span v-if="result" class="run-summary">
            共 {{ result.total }} 条 · 变更 {{ result.changed }} · 失败 {{ result.failed }}
          </span>
        </div>

        <div v-if="error" class="entry-error">{{ error }}</div>

        <div v-if="result && result.items && result.items.length" class="result-list">
          <div v-for="it in result.items" :key="it.id" class="result-row">
            <div class="row-head">
              <span :class="['src-badge', it.source === 'LLM' ? 'llm' : 'rule']">{{ it.source === 'LLM' ? 'LLM' : '规则' }}</span>
              <span class="row-id">#{{ it.id }}</span>
              <span v-if="it.error" class="row-error">{{ it.error }}</span>
            </div>
            <p class="row-before">{{ it.beforeTitle }}</p>
            <p class="row-after">{{ it.afterTitle }}</p>
            <div class="row-tags">
              <span v-if="it.topicTags" v-for="t in splitTags(it.topicTags)" :key="t" class="tag">{{ t }}</span>
              <span class="owner">{{ ownerLabel(it.ownerType) }}</span>
              <span v-if="it.basis" class="basis">{{ it.basis }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="drawer-foot">
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
      </div>
    </aside>
  </div>
</template>

<script>
import { cleanNotes } from '@/api/employerBrand'
import { OWNER_LABELS } from '@/constants/employerBrand'

export default {
  name: 'CleanNotesDialog',
  props: {
    companies: { type: Array, default: function () { return [] } },
    defaultCompanyId: { type: [String, Number], default: '' }
  },
  data: function () {
    return {
      scopeCompany: this.defaultCompanyId || '',
      limit: 10,
      includeCleaned: false,
      running: false,
      error: '',
      result: null
    }
  },
  methods: {
    run: function () {
      var self = this
      this.running = true
      this.error = ''
      this.result = null
      cleanNotes({
        companyId: this.scopeCompany ? Number(this.scopeCompany) : null,
        limit: this.limit,
        includeCleaned: this.includeCleaned
      }).then(function (res) {
        self.running = false
        self.result = res || {}
        self.$emit('cleaned', res)
      }).catch(function (e) {
        self.running = false
        self.error = e.message || '清洗失败'
      })
    },
    splitTags: function (raw) {
      return String(raw || '').split(',').filter(function (t) { return t })
    },
    ownerLabel: function (v) {
      return OWNER_LABELS[v] || '归属待核验'
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-clean-drawer .drawer-body {
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

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.form-row label {
  display: block;
  margin-bottom: 4px;
  color: #8d97a3;
  font-size: 9px;
}

.form-row select {
  width: 100%;
  height: 28px;
  padding: 0 8px;
  border: 1px solid #e2e6eb;
  border-radius: 5px;
  color: #5f6a78;
  background: #fff;
  font: inherit;
  font-size: 10px;
}

.check-line {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 10px 0;
  color: #7d8794;
  font-size: 10px;
}

.run-line {
  display: flex;
  align-items: center;
  gap: 10px;
}

.run-line .primary-btn {
  height: 30px;
  padding: 0 16px;
  font-size: 10px;
}

.run-summary {
  color: #6b7683;
  font-size: 10px;
}

.entry-error {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  color: #c75763;
  background: #fff0f1;
  font-size: 10px;
}

.result-list {
  margin-top: 12px;
  border-top: 1px solid #eef1f4;
}

.result-row {
  padding: 9px 0;
  border-bottom: 1px solid #f4f6f8;
}

.row-head {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 4px;
}

.src-badge {
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 8px;
}

.src-badge.llm { color: #5b47c0; background: #f1eeff; }
.src-badge.rule { color: #8a6a45; background: #fbf5ec; }

.row-id {
  color: #b3bac3;
  font-size: 9px;
}

.row-error {
  color: #c75763;
  font-size: 9px;
}

.row-before {
  margin: 0 0 2px;
  color: #b3bac3;
  font-size: 9px;
  line-height: 1.5;
  text-decoration: line-through;
}

.row-after {
  margin: 0 0 4px;
  color: #39434f;
  font-size: 11px;
  line-height: 1.5;
}

.row-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  align-items: center;
}

.tag {
  padding: 1px 6px;
  border-radius: 4px;
  color: #6c7480;
  background: #f5f6f8;
  font-size: 8px;
}

.owner {
  padding: 1px 6px;
  border-radius: 4px;
  color: #4b6b8d;
  background: #edf3fa;
  font-size: 8px;
}

.basis {
  color: #a6aeb8;
  font-size: 8px;
}
</style>
