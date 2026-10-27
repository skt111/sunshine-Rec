<template>
  <section class="panel supply-filter">
    <div class="filter-head">
      <div class="section-label"><span class="section-mark purple-mark"></span>筛选情报范围</div>
      <span class="filter-state">{{ stateText }}</span>
    </div>

    <div class="supply-filter-grid">
      <!-- 高校选择：短下拉（限高滚动，不撑长页面） -->
      <div class="school-picker">
        <button type="button" class="picker-trigger" @click="schoolOpen = !schoolOpen">
          {{ schoolPickerText }}<span class="caret">⌄</span>
        </button>
        <div v-if="schoolOpen" class="picker-panel" @click.stop>
          <label v-for="s in schools" :key="s.schoolId" class="picker-item">
            <input type="checkbox" :value="s.schoolId" v-model="local.schoolIds" @change="emitChange">
            <span>{{ s.schoolName }}</span>
          </label>
          <div class="picker-foot">
            <button type="button" class="ghost-btn" @click="clearSchools">清空</button>
            <button type="button" class="primary-btn" @click="schoolOpen = false">确定</button>
          </div>
        </div>
      </div>

      <select v-model="local.city" @change="emitChange">
        <option value="">全部城市</option>
        <option v-for="city in cities" :key="city" :value="city">{{ city }}</option>
      </select>

      <select v-model="local.timeRange" @change="emitChange">
        <option value="all">不限时间</option>
        <option value="today">今日</option>
        <option value="7">未来 7 天</option>
        <option value="30">未来 30 天</option>
        <option value="custom">自定义</option>
      </select>

      <template v-if="local.timeRange === 'custom'">
        <input type="date" v-model="local.startDate" @change="emitChange">
        <input type="date" v-model="local.endDate" @change="emitChange">
      </template>

      <select v-model="local.status" @change="emitChange">
        <option value="">全部状态</option>
        <option value="UPCOMING">未开始</option>
        <option value="ONGOING">进行中</option>
        <option value="FINISHED">已结束</option>
        <option value="CANCELLED">已取消</option>
        <option value="POSTPONED">已延期</option>
      </select>

      <!-- 供给侧：重点企业筛选 -->
      <template v-if="viewMode === 'talk'">
        <select v-model="local.companyCategory" @change="emitChange">
          <option value="">全部企业类别</option>
          <option value="INSURANCE">保险竞品</option>
          <option value="FINANCE">头部金融</option>
          <option value="INTERNET">头部互联网</option>
        </select>
        <select v-model="local.companyName" @change="emitChange">
          <option value="">全部重点企业</option>
          <option v-for="c in filteredCompanies" :key="c.id" :value="c.companyName">{{ c.companyName }}</option>
        </select>
      </template>

      <!-- 需求侧：报名筛选 -->
      <template v-else>
        <select v-model="local.registrationStatus" @change="emitChange">
          <option value="">全部报名状态</option>
          <option value="OPEN">开放报名</option>
          <option value="CLOSING_SOON">即将截止</option>
          <option value="NOT_OPEN">未开放</option>
          <option value="CLOSED">已截止</option>
          <option value="NOT_PUBLISHED">暂未公布</option>
        </select>
        <label class="reg-only">
          <input type="checkbox" v-model="local.hasRegistrationUrl" @change="emitChange">
          仅看有报名入口
        </label>
      </template>

      <select v-model="local.verificationStatus" @change="emitChange">
        <option value="">全部核验</option>
        <option value="PENDING">待核验</option>
        <option value="VERIFIED">已核验</option>
        <option value="REJECTED">已驳回</option>
        <option value="NEED_REVIEW">需复核</option>
      </select>

      <button class="primary-btn" @click="emitChange">查询</button>
      <button class="ghost-btn" @click="$emit('reset')">重置</button>
    </div>
  </section>
</template>

<script>
function clone (f) {
  var o = {}
  Object.keys(f || {}).forEach(function (k) {
    o[k] = Array.isArray(f[k]) ? f[k].slice() : f[k]
  })
  return o
}

export default {
  name: 'SupplyRadarFilter',
  props: {
    filters: { type: Object, default: function () { return {} } },
    schools: { type: Array, default: function () { return [] } },
    cities: { type: Array, default: function () { return [] } },
    companies: { type: Array, default: function () { return [] } },
    /** talk=供给侧宣讲 / fair=需求侧双选 */
    viewMode: { type: String, default: 'talk' }
  },
  data: function () {
    return { local: clone(this.filters), schoolOpen: false }
  },
  computed: {
    schoolPickerText: function () {
      var n = (this.local.schoolIds || []).length
      return n ? '已选 ' + n + ' 所' : '选择高校'
    },
    filteredCompanies: function () {
      var cat = this.local.companyCategory
      if (!cat) return this.companies
      return this.companies.filter(function (c) { return c.companyCategory === cat })
    },
    stateText: function () {
      var parts = []
      if (this.local.city) parts.push(this.local.city)
      if (this.local.schoolIds && this.local.schoolIds.length) parts.push(this.local.schoolIds.length + ' 所高校')
      var timeMap = { all: '不限时间', today: '今日', '7': '未来7天', '30': '未来30天', custom: '自定义' }
      if (this.local.timeRange) parts.push(timeMap[this.local.timeRange] || '')
      return parts.filter(Boolean).join(' · ') || '全部高校 · 全部时间'
    }
  },
  watch: {
    filters: function (v) {
      this.local = clone(v)
    }
  },
  methods: {
    emitChange: function () {
      this.$emit('change', clone(this.local))
    },
    clearSchools: function () {
      this.local.schoolIds = []
      this.emitChange()
    }
  }
}
</script>

<style scoped>
.reg-only {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #5b6572;
  white-space: nowrap;
}
.school-picker { position: relative; }
.school-picker .picker-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 27px;
  padding: 0 7px;
  border: 1px solid #dfe4ea;
  border-radius: 6px;
  color: #5b6572;
  background: #fff;
  font-size: 9px;
  cursor: pointer;
}
.school-picker .picker-trigger .caret { color: #9aa4b1; }
.picker-panel {
  position: absolute;
  top: 30px;
  left: 0;
  z-index: 30;
  width: 220px;
  max-height: 190px;
  overflow-y: auto;
  padding: 4px 0;
  border: 1px solid #e2e7ec;
  border-radius: 7px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(32, 41, 54, .12);
}
.picker-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 9px;
  color: #4c5764;
  font-size: 9px;
  cursor: pointer;
}
.picker-item:hover { background: #f6f3ff; }
.picker-foot {
  display: flex;
  gap: 6px;
  padding: 5px 8px 3px;
  border-top: 1px solid #eef1f4;
}
.picker-foot .ghost-btn,
.picker-foot .primary-btn { flex: 1; height: 22px; font-size: 9px; }
</style>
