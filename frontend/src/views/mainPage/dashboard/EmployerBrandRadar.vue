<template>
  <section class="brand-radar-page">
    <BrandFilter
      :filter="filter"
      :companies="companies"
      @change="onFilterChange"
      @reset="resetFilter"
    />

    <div v-if="loading" class="panel brand-loading">数据加载中…</div>
    <div v-else-if="error" class="panel brand-error">
      {{ error }}
      <div style="margin-top:10px"><button class="ghost-btn" @click="reload">重试</button></div>
    </div>

    <div v-else class="brand-main-grid">
      <BrandNoteList
        :rows="pageData.list"
        :total="pageData.total"
        :page="pageData.page"
        :size="pageData.size"
        :total-pages="pageData.totalPages"
        :sort-by="filter.sortBy"
        :sort-direction="filter.sortDirection"
        @page="changePage"
        @sort="changeSort"
        @detail="openDetail"
        @create="manualVisible = true"
      />
      <BrandRankingPanel
        :ranking="ranking"
        :active-type="rankingType"
        @change-type="changeRankingType"
      />
    </div>

    <!-- 校招吸引动作矩阵（复用 02-雇主品牌建设·模块页，位于互动榜下方整行） -->
    <EmployerBrandMatrix />

    <!-- 图例（页面最下方） -->
    <EmployerBrandLegend />

    <ManualNoteDialog
      v-if="manualVisible"
      :companies="companies"
      :accounts="accounts"
      @close="manualVisible = false"
      @saved="onSaved"
    />

    <BatchImportDialog
      v-if="batchVisible"
      :companies="companies"
      @close="batchVisible = false"
      @imported="onImported"
    />

    <SourceCheckDrawer
      v-if="sourceVisible"
      @close="sourceVisible = false"
    />

    <CleanNotesDialog
      v-if="cleanVisible"
      :companies="companies"
      :default-company-id="filter.companyId"
      @close="cleanVisible = false"
      @cleaned="onCleaned"
    />

    <BrandNoteDetail
      v-if="detailId"
      :note-id="detailId"
      @close="detailId = null"
    />
  </section>
</template>

<script>
import BrandFilter from './brand/BrandFilter'
import BrandNoteList from './brand/BrandNoteList'
import BrandRankingPanel from './brand/BrandRankingPanel'
import ManualNoteDialog from './brand/ManualNoteDialog'
import BatchImportDialog from './brand/BatchImportDialog'
import SourceCheckDrawer from './brand/SourceCheckDrawer'
import CleanNotesDialog from './brand/CleanNotesDialog'
import BrandNoteDetail from './brand/BrandNoteDetail'
import EmployerBrandMatrix from './brand/EmployerBrandMatrix'
import EmployerBrandLegend from './brand/EmployerBrandLegend'
import { fetchCompanies } from '@/api/supplyRadar'
import { fetchAccounts, fetchNotes, fetchRankings } from '@/api/employerBrand'

/** 列表每页 6 条（三列 × 两行，需求：三列每列两个推文，超出翻页） */
var PAGE_SIZE = 6

function defaultFilter () {
  return {
    companyCategory: '',
    companyId: '',
    accountNature: '',
    topic: '',
    sortBy: 'publishedAt',
    sortDirection: 'desc'
  }
}

export default {
  name: 'EmployerBrandRadar',
  components: { BrandFilter, BrandNoteList, BrandRankingPanel, ManualNoteDialog, BatchImportDialog, SourceCheckDrawer, CleanNotesDialog, BrandNoteDetail, EmployerBrandMatrix, EmployerBrandLegend },
  data: function () {
    return {
      filter: defaultFilter(),
      companies: [],
      accounts: [],
      pageData: { list: [], total: 0, page: 1, size: PAGE_SIZE, totalPages: 0 },
      ranking: {},
      rankingType: 'composite',
      loading: true,
      error: '',
      manualVisible: false,
      batchVisible: false,
      sourceVisible: false,
      cleanVisible: false,
      detailId: null
    }
  },
  mounted: function () {
    this.loadBase()
    this.reload()
  },
  methods: {
    loadBase: function () {
      var self = this
      fetchCompanies().then(function (list) {
        self.companies = list || []
      }).catch(function () {
        self.companies = []
      })
      fetchAccounts().then(function (list) {
        self.accounts = list || []
      }).catch(function () {
        self.accounts = []
      })
    },
    reload: function () {
      this.loadNotes()
      this.loadRankings()
    },
    loadNotes: function () {
      var self = this
      var params = this.baseParams()
      params.page = this.pageData.page
      params.size = this.pageData.size
      params.sortBy = this.filter.sortBy
      params.sortDirection = this.filter.sortDirection
      this.loading = true
      this.error = ''
      fetchNotes(params).then(function (data) {
        self.pageData = data || { list: [], total: 0, page: 1, size: PAGE_SIZE, totalPages: 0 }
        self.loading = false
      }).catch(function (e) {
        self.error = e.message || '加载失败'
        self.loading = false
      })
    },
    loadRankings: function () {
      var self = this
      // 榜单每页 5 条，取 20 条足够翻页
      var params = { limit: 20 }
      params.type = this.rankingType
      if (this.filter.companyId) params.companyId = this.filter.companyId
      fetchRankings(params).then(function (data) {
        self.ranking = data || {}
      }).catch(function (e) {
        self.ranking = { available: false, reason: e.message || '榜单加载失败', items: [] }
      })
    },
    baseParams: function () {
      var p = {}
      if (this.filter.companyId) p.companyIds = this.filter.companyId
      if (this.filter.companyCategory) p.companyCategory = this.filter.companyCategory
      if (this.filter.accountNature) p.accountNature = this.filter.accountNature
      if (this.filter.topic) p.topic = this.filter.topic
      return p
    },
    onFilterChange: function (next) {
      this.filter = Object.assign({}, this.filter, next)
      this.pageData.page = 1
      this.reload()
    },
    resetFilter: function () {
      this.filter = defaultFilter()
      this.pageData.page = 1
      this.reload()
    },
    changePage: function (page) {
      this.pageData.page = page
      this.loadNotes()
    },
    changeSort: function (sortBy, sortDirection) {
      this.filter.sortBy = sortBy
      this.filter.sortDirection = sortDirection
      this.pageData.page = 1
      this.loadNotes()
    },
    changeRankingType: function (type) {
      this.rankingType = type
      this.loadRankings()
    },
    openDetail: function (id) {
      this.detailId = id
    },
    onSaved: function () {
      this.manualVisible = false
      this.loadBase()
      this.reload()
    },
    onImported: function () {
      this.loadBase()
      this.reload()
    },
    onCleaned: function () {
      this.loadNotes()
      this.loadRankings()
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 12px;
  align-items: start;
}

.brand-loading,
.brand-error {
  padding: 44px;
  color: #8c96a3;
  font-size: 11px;
  text-align: center;
}

.brand-error {
  color: #d9534f;
}

@media (max-width: 1360px) {
  .brand-main-grid { grid-template-columns: 1fr; }
}
</style>
