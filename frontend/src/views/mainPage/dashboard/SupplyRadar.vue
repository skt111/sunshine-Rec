<template>
  <section class="radar-page supply-radar">
    <!-- 工具条：左侧类型页签 + 右侧操作，同一行（标题由全局 Header 承担） -->
    <div class="radar-heading radar-toolbar">
      <SupplyRadarTabs :active="typeTab" :counts="stats.typeCounts" @change="onTypeChange" />
      <div class="radar-heading-actions">
        <span class="demo-label">最近采集 {{ lastCrawlAtText }} · 监测 {{ stats.monitoredSchoolCount || 0 }} 所</span>
        <button class="ghost-btn" :disabled="crawling" @click="triggerCrawl">
          {{ crawling ? '采集进行中…' : '立即更新' }}
        </button>
        <button class="ghost-btn" @click="taskPanelVisible = true">采集任务</button>
        <div class="view-switch">
          <button :class="{ selected: view === 'list' }" @click="switchView('list')">☷ 列表视图</button>
          <button :class="{ selected: view === 'school' }" @click="switchView('school')">▦ 高校分组</button>
        </div>
      </div>
    </div>

    <!-- 统计卡行已移除（顶部页签已带总数）；倒计时 + 预警条仅在「双选会」页签下展示 -->
    <SupplyRadarCountdown
      v-if="typeTab === 'JOB_FAIR'"
      :events="countdownList"
      :schools="schools"
      @detail="openDetail"
      @source="openSource"
    />

    <!-- 筛选区 -->
    <SupplyRadarFilter
      :filters="filters"
      :schools="schools"
      :cities="cities"
      :companies="companies"
      :view-mode="filterMode"
      @change="onFilterChange"
      @reset="resetFilters"
    />

    <!-- 三态 -->
    <div v-if="loading" class="panel" style="padding:48px;text-align:center;color:#8c96a3">数据加载中…</div>
    <div v-else-if="error" class="panel" style="padding:48px;text-align:center;color:#d9534f">
      {{ error }}
      <div style="margin-top:12px"><button class="ghost-btn" @click="reload">重试</button></div>
    </div>

    <template v-else>
      <!-- 列表视图 -->
      <section v-if="view === 'list'" class="panel event-list-panel">
        <div class="list-toolbar">
          <span>共 {{ pageData.total }} 条 · {{ sortLabel }}</span>
          <div>
            <select v-model="sortBy" @change="applySort" style="margin-right:8px">
              <option value="eventDate">按活动日期</option>
              <option value="lastSeenAt">按最近采集</option>
              <option value="sourcePublishedAt">按发布时间</option>
            </select>
            <button class="sort-btn" @click="toggleDirection">{{ sortAsc ? '升序 ↑' : '降序 ↓' }}</button>
          </div>
        </div>

        <SupplyEventCard
          v-for="event in pageData.list"
          :key="event.id"
          :event="event"
          @detail="openDetail"
          @source="openSource"
        />

        <div v-if="!pageData.list.length" class="empty-state">{{ TEXT.EMPTY_LIST }}</div>

        <SupplyPager
          :total="pageData.total"
          :page="pageData.page"
          :size="pageData.size"
          :total-pages="pageData.totalPages"
          @change="changePage"
        />
      </section>

      <!-- 分组视图 -->
      <section v-else class="school-grid">
        <SupplySchoolGroup
          v-for="group in pageData.list"
          :key="group.schoolId"
          :group="group"
          @expand="expandSchool"
          @detail="openDetail"
        />
        <div v-if="!pageData.list.length" class="empty-state">{{ TEXT.EMPTY_LIST }}</div>
      </section>
    </template>

    <!-- 详情抽屉 -->
    <SupplyDetailDrawer
      v-if="detailId"
      :event-id="detailId"
      @close="detailId = null"
      @source="openSource"
    />

    <!-- 采集任务面板 -->
    <CrawlTaskPanel
      v-if="taskPanelVisible"
      :last-crawl-at="lastCrawlAtText"
      :last-crawl-status="lastCrawlStatus"
      @close="taskPanelVisible = false"
    />
  </section>
</template>

<script>
import SupplyRadarCountdown from './supply/SupplyRadarCountdown'
import SupplyRadarFilter from './supply/SupplyRadarFilter'
import SupplyRadarTabs from './supply/SupplyRadarTabs'
import SupplyEventCard from './supply/SupplyEventCard'
import SupplySchoolGroup from './supply/SupplySchoolGroup'
import SupplyPager from './supply/SupplyPager'
import CrawlTaskPanel from './supply/CrawlTaskPanel'
import crawlPollingMixin from './supply/crawlPollingMixin'
import SupplyDetailDrawer from './SupplyDetailDrawer'
import { fetchSchools, fetchCompanies, fetchEvents, fetchStatistics } from '@/api/supplyRadar'
import { TEXT } from '@/constants/supplyRadar'

function defaultFilters () {
  return {
    keyword: '',
    city: '',
    schoolIds: [],
    timeRange: 'all',
    startDate: '',
    endDate: '',
    status: '',
    venueType: '',
    verificationStatus: '',
    companyCategory: '',
    companyName: '',
    registrationStatus: '',
    hasRegistrationUrl: false
  }
}

function formatDate (d) {
  var y = d.getFullYear()
  var m = ('0' + (d.getMonth() + 1)).slice(-2)
  var day = ('0' + d.getDate()).slice(-2)
  return y + '-' + m + '-' + day
}

export default {
  name: 'SupplyRadar',
  mixins: [crawlPollingMixin],
  components: {
    SupplyRadarCountdown, SupplyRadarFilter, SupplyRadarTabs, SupplyEventCard,
    SupplySchoolGroup, SupplyPager, CrawlTaskPanel, SupplyDetailDrawer
  },
  data () {
    return {
      TEXT: TEXT,
      /** 类型页签：ALL / TALK / JOB_FAIR / OTHER（不区分校内/校外） */
      typeTab: 'TALK',
      companies: [],
      stats: { categoryCounts: {}, typeCounts: {} },
      lastCrawlAt: null,
      lastCrawlStatus: null,
      schools: [],
      filters: defaultFilters(),
      view: 'list',
      page: 1,
      /** 列表每页条数（产品要求：一屏 10 条后翻页） */
      size: 10,
      sortBy: 'eventDate',
      sortDirection: 'asc',
      pageData: { list: [], total: 0, page: 1, size: 10, totalPages: 0 },
      /** 未来 30 天双选会倒计时数据（页面顶部常驻，口径独立于筛选） */
      countdownList: [],
      loading: false,
      error: '',
      detailId: null,
      taskPanelVisible: false
    }
  },
  computed: {
    /** 筛选器组合与统计口径：由类型页签驱动（宣讲→企业筛选 / 双选→报名筛选） */
    filterMode: function () {
      return this.typeTab === 'JOB_FAIR' ? 'fair' : 'talk'
    },
    cities: function () {
      var set = {}
      this.schools.forEach(function (s) { if (s.schoolCity) set[s.schoolCity] = 1 })
      return Object.keys(set)
    },
    lastCrawlAtText: function () {
      return this.lastCrawlAt || '暂无采集记录'
    },
    sortAsc: function () {
      return this.sortDirection === 'asc'
    },
    sortLabel: function () {
      var map = { eventDate: '活动日期', lastSeenAt: '最近采集', sourcePublishedAt: '发布时间' }
      return (map[this.sortBy] || '活动日期') + (this.sortAsc ? ' 升序' : ' 降序')
    }
  },
  created: function () {
    this.loadSchools()
    this.loadCompanies()
    this.reload()
  },
  methods: {
    /** 时间范围 → startDate/endDate */
    buildQuery: function (includePaging) {
      var f = this.filters
      var q = {
        keyword: f.keyword || undefined,
        city: f.city || undefined,
        venueType: f.venueType || undefined,
        status: f.status || undefined,
        verificationStatus: f.verificationStatus || undefined,
        companyCategory: f.companyCategory || undefined,
        companyName: f.companyName || undefined,
        registrationStatus: f.registrationStatus || undefined,
        hasRegistrationUrl: f.hasRegistrationUrl || undefined
      }
      // 类型页签（不区分校内/校外）
      if (this.typeTab === 'TALK') {
        q.eventType = 'TALK'
      } else if (this.typeTab === 'JOB_FAIR') {
        q.eventType = 'JOB_FAIR'
      } else if (this.typeTab === 'OTHER') {
        q.excludeEventTypes = 'TALK,JOB_FAIR'
      }
      if (f.schoolIds && f.schoolIds.length) q.schoolIds = f.schoolIds.join(',')
      var range = this.resolveRange()
      if (range) {
        q.startDate = range[0]
        q.endDate = range[1]
        q.timeFilterEnabled = true
      } else {
        q.timeFilterEnabled = false
      }
      if (includePaging) {
        q.page = this.page
        q.size = this.size
        q.sortBy = this.sortBy
        q.sortDirection = this.sortDirection
        if (this.view === 'school') q.groupBy = 'school'
      }
      return q
    },
    resolveRange: function () {
      var f = this.filters
      var today = new Date()
      if (f.timeRange === 'all') return null
      if (f.timeRange === 'today') return [formatDate(today), formatDate(today)]
      if (f.timeRange === '7') {
        var d7 = new Date(); d7.setDate(today.getDate() + 7)
        return [formatDate(today), formatDate(d7)]
      }
      if (f.timeRange === '30') {
        var d30 = new Date(); d30.setDate(today.getDate() + 30)
        return [formatDate(today), formatDate(d30)]
      }
      if (f.timeRange === 'custom' && f.startDate && f.endDate) {
        return [f.startDate, f.endDate]
      }
      return null
    },
    /** 倒计时口径：仅未来 30 天已采集的双选会（不受列表筛选影响） */
    buildCountdownQuery: function () {
      var today = new Date()
      var end = new Date()
      end.setDate(today.getDate() + 30)
      return {
        eventType: 'JOB_FAIR',
        timeFilterEnabled: true,
        startDate: formatDate(today),
        endDate: formatDate(end),
        page: 1,
        size: 50,
        sortBy: 'eventDate',
        sortDirection: 'asc'
      }
    },
    loadSchools: function () {
      var that = this
      fetchSchools({}).then(function (list) {
        that.schools = list || []
      }).catch(function () {
        that.schools = []
      })
    },
    loadCompanies: function () {
      var that = this
      fetchCompanies({}).then(function (list) {
        that.companies = list || []
      }).catch(function () {
        that.companies = []
      })
    },
    reload: function () {
      this.loadAll(true)
    },
    /** @param {Boolean} refreshCountdown 是否刷新顶部倒计时数据（初次加载/采集完成后） */
    loadAll: function (refreshCountdown) {
      var that = this
      this.loading = true
      this.error = ''
      var statQuery = this.buildQuery(false)
      var listQuery = this.buildQuery(true)
      var tasks = [fetchStatistics(statQuery), fetchEvents(listQuery)]
      if (refreshCountdown) tasks.push(fetchEvents(this.buildCountdownQuery()))
      Promise.all(tasks)
        .then(function (res) {
          var stats = res[0] || {}
          that.stats = stats
          that.lastCrawlAt = stats.lastCrawlAt || null
          that.lastCrawlStatus = stats.lastCrawlTaskStatus || null
          that.pageData = res[1] || { list: [], total: 0, page: 1, size: 10, totalPages: 0 }
          if (!refreshCountdown) return
          that.countdownList = (res[2] && res[2].list) ? res[2].list : []
        })
        .catch(function (e) {
          that.error = (e && e.message) ? e.message : TEXT.LOAD_ERROR
        })
        .then(function () {
          that.loading = false
        })
    },
    onFilterChange: function (filters) {
      this.filters = filters
      this.page = 1
      this.loadAll()
    },
    resetFilters: function () {
      this.filters = defaultFilters()
      this.page = 1
      this.loadAll()
    },
    onTypeChange: function (key) {
      this.typeTab = key
      this.page = 1
      this.loadAll()
    },
    switchView: function (view) {
      this.view = view
      this.page = 1
      this.loadAll()
    },
    changePage: function (page) {
      this.page = page
      this.loadAll()
    },
    applySort: function () {
      this.page = 1
      this.loadAll()
    },
    toggleDirection: function () {
      this.sortDirection = this.sortAsc ? 'desc' : 'asc'
      this.page = 1
      this.loadAll()
    },
    expandSchool: function (schoolId) {
      this.filters.schoolIds = [schoolId]
      this.view = 'list'
      this.page = 1
      this.loadAll()
    },
    openDetail: function (event) {
      this.detailId = event.id
    },
    openSource: function (event) {
      var url = event && (event.sourceUrl || event.url)
      if (url) window.open(url, '_blank', 'noopener,noreferrer')
    }
  }
}
</script>

<style scoped>
.category-tabs { margin-bottom: 0; }
</style>
