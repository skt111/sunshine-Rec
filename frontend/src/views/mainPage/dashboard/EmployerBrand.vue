<template><section class="panel brand-panel"><div class="panel-head compact"><div><div class="section-label"><span class="section-mark rose-mark"></span>品牌温度</div><h2>雇主品牌与校园舆情</h2><p>候选人正在关注什么，哪些问题影响校招吸引力。</p></div><span class="demo-label">模拟样本 n=1,240</span></div><div class="sentiment-body"><div class="donut"><div><strong>46%</strong><span>正向讨论</span></div></div><div class="sentiment-bars"><div v-for="item in sentiments" :key="item.label"><span>{{ item.label }}</span><div><i :class="item.color" :style="{ width: item.value + '%' }"></i></div><b>{{ item.value }}%</b></div></div></div><div class="keyword-list"><span v-for="word in keywords" :key="word">{{ word }}</span></div><div class="brand-callout"><span>↗</span><p><b>建议关注：</b>“培养机制”连续 3 日成为高频讨论词，建议在宣讲问答环节主动回应。</p></div></section></template>
<script>export default { name: 'EmployerBrand', props: { sentiments: Array, keywords: Array } }</script>




