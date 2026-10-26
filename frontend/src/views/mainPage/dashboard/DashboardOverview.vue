<template><section class="stats-grid"><div v-for="stat in stats" :key="stat.label" :class="['stat-card', stat.tone]"><div class="stat-top"><span>{{ stat.label }}</span><i>{{ stat.icon }}</i></div><div class="stat-value">{{ stat.value }}</div><div class="stat-bottom"><b>{{ stat.delta }}</b><span>{{ stat.hint }}</span></div><div class="stat-spark"><i v-for="n in 5" :key="n" :style="{ height: (18 + n * 5 + (stat.value.length * n % 12)) + 'px' }"></i></div></div></section></template>
<script>export default { name: 'DashboardOverview', props: { stats: Array } }</script>




