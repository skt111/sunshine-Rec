<template><section class="panel practices-panel"><div class="panel-head compact"><div><div class="section-label"><span class="section-mark green-mark"></span>外部借鉴</div><h2>全球招聘实践与策略借鉴</h2><p>精选可迁移的企业实践，为阳光校招提供下一步参考。</p></div><button class="more-btn">策略库 <span>→</span></button></div><div class="practice-grid"><article v-for="item in items" :key="item.company" class="practice-card" @click="$emit('detail', item)"><div class="practice-top"><span class="company-avatar">{{ item.company.slice(0, 1) }}</span><div><b>{{ item.company }}</b><small>示例案例 · 待核验来源</small></div><span class="chevron">↗</span></div><h3>{{ item.topic }}</h3><p>{{ item.text }}</p><div class="practice-bottom"><span>{{ item.use }}</span><strong>{{ item.value }}</strong></div></article></div></section></template>
<script>export default { name: 'GlobalPractices', props: { items: Array } }</script>




