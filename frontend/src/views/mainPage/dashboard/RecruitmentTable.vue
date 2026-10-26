<template>
  <section class="panel recruitment-panel"><div class="panel-head"><div><div class="section-label"><span class="section-mark orange-mark"></span>核心情报流</div><h2>今日招聘盘点</h2><p>持续跟踪头部企业招聘动态，识别竞品策略与校招机会。</p></div><div class="panel-meta"><span class="demo-label">演示数据</span><button class="text-btn" @click="$emit('export')">⇩ 导出数据</button></div></div>
    <div class="filter-row"><label class="search-box"><span>⌕</span><input :value="query" placeholder="搜索企业名称" @input="$emit('query', $event.target.value)"></label><select :value="type" @change="$emit('type', $event.target.value)"><option>全部类型</option><option>新增岗位</option><option>宣讲即将开始</option><option>报名即将截止</option><option>岗位调整</option><option>待核验</option></select><select :value="industry" @change="$emit('industry', $event.target.value)"><option>全部行业</option><option>互联网</option><option>通信科技</option><option>生活服务</option><option>消费电子</option><option>新能源</option></select><button class="filter-btn">筛选 <span>⌄</span></button><span class="filter-tip">共 {{ rows.length }} 条情报</span></div>
    <div class="table-wrap"><table><thead><tr><th>企业名称</th><th>官方招聘信息</th><th>线下招聘活动</th><th>最新岗位需求</th><th>招聘渠道</th><th>更新时间</th><th>情报来源</th><th>操作</th></tr></thead><tbody><tr v-for="row in pageRows" :key="row.id" @click="$emit('detail', row)"><td><strong>{{ row.company }}</strong><small>{{ row.industry }}</small></td><td><span class="status-tag" :class="row.statusClass">{{ row.status }}</span><p>{{ row.official }}</p></td><td>{{ row.offline }}</td><td>{{ row.roles }}</td><td class="muted">{{ row.channel }}</td><td class="muted">{{ row.time }}</td><td><span :class="['source-tag', row.sourceClass]"><i></i>{{ row.source }}</span></td><td><button class="row-action" @click.stop="$emit('detail', row)">查看详情</button></td></tr><tr v-if="!pageRows.length"><td class="empty-row" colspan="8">没有匹配的演示情报</td></tr></tbody></table></div>
    <div class="table-foot"><span>数据来源：公开渠道模拟采集 · 所有信息均需人工复核</span><div class="pager"><button :disabled="page <= 1" @click="$emit('page', page - 1)">‹</button><b>{{ page }}</b><span>/ {{ pageCount }}</span><button :disabled="page >= pageCount" @click="$emit('page', page + 1)">›</button></div></div>
  </section>
</template>
<script>
export default { name: 'RecruitmentTable', props: { rows: Array, query: String, type: String, industry: String, page: Number, pageCount: Number }, computed: { pageRows () { return this.rows.slice((this.page - 1) * 4, this.page * 4) } } }
</script>




