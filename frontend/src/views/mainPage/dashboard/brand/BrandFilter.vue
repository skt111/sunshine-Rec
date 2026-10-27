<template>
  <section class="panel brand-filter">
    <div class="filter-head">
      <div>
        <div class="section-label"><span class="section-mark purple-mark"></span>筛选监测范围</div>
      </div>
      <button class="ghost-btn" @click="$emit('reset')">重置条件</button>
    </div>

    <div class="filter-grid">
      <select v-model="local.companyCategory" @change="onCategoryChange">
        <option value="">全部企业类别</option>
        <option v-for="(label, key) in categoryLabels" :key="key" :value="key">{{ label }}</option>
      </select>

      <select v-model="local.companyId" @change="emitChange">
        <option value="">全部重点企业</option>
        <option v-for="c in filteredCompanies" :key="c.id" :value="c.id">{{ c.companyName }}</option>
      </select>

      <select v-model="local.accountNature" @change="emitChange">
        <option value="">全部账号性质</option>
        <option v-for="(label, key) in natureLabels" :key="key" :value="key">{{ label }}</option>
      </select>

      <select v-model="local.topic" @change="emitChange">
        <option value="">全部主题</option>
        <option v-for="t in topics" :key="t" :value="t">{{ t }}</option>
      </select>

      <button class="primary-btn" @click="emitChange">查询</button>
    </div>
  </section>
</template>

<script>
import { TOPIC_TAGS, CATEGORY_LABELS, ACCOUNT_NATURE_LABELS } from '@/constants/employerBrand'

export default {
  name: 'BrandFilter',
  props: {
    filter: { type: Object, required: true },
    companies: { type: Array, default: function () { return [] } }
  },
  data: function () {
    return {
      local: Object.assign({}, this.filter),
      topics: TOPIC_TAGS,
      categoryLabels: CATEGORY_LABELS,
      natureLabels: ACCOUNT_NATURE_LABELS
    }
  },
  computed: {
    filteredCompanies: function () {
      var cat = this.local.companyCategory
      if (!cat) return this.companies
      return this.companies.filter(function (c) { return c.companyCategory === cat })
    }
  },
  watch: {
    filter: {
      deep: true,
      handler: function (val) {
        this.local = Object.assign({}, val)
      }
    }
  },
  methods: {
    onCategoryChange: function () {
      this.local.companyId = ''
      this.emitChange()
    },
    emitChange: function () {
      this.$emit('change', Object.assign({}, this.local))
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-filter {
  padding: 12px 14px 11px;
  margin-bottom: 0;
}

.filter-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 9px;
}

.filter-head h2 {
  margin: 2px 0 0;
  font-size: 13px;
  letter-spacing: -.01em;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 7px;
  align-items: center;
}

.filter-grid select {
  height: 28px;
  padding: 0 8px;
  border: 1px solid #e2e6eb;
  border-radius: 5px;
  color: #5f6a78;
  background: #fff;
  font: inherit;
  font-size: 10px;
}

.filter-grid .primary-btn {
  height: 28px;
  padding: 0 14px;
  font-size: 10px;
}
</style>
