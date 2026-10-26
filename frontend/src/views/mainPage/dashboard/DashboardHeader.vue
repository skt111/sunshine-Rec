<template>
  <header class="topbar"><div class="topbar-left"><button class="mobile-menu" aria-label="打开导航">☰</button><div class="top-title"><b>{{ title }}</b><span>实时掌握校招市场变化</span></div></div><div class="top-actions"><div class="period-switch" role="group" aria-label="时间范围"><button v-for="item in periods" :key="item" :class="{ selected: period === item }" :aria-pressed="period === item" @click="$emit('period-change', item)">{{ item }}</button></div><span class="updated"><i></i> 更新于 {{ lastUpdated }}</span><button class="top-icon" aria-label="搜索"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="6.5"></circle><path d="m16 16 4 4"></path></svg></button><button class="top-icon notice" aria-label="通知" @click="$emit('toast', '暂无新的系统通知')"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 8h18c0-1-3-1-3-8"></path><path d="M10 21h4"></path></svg><em>3</em></button><div class="top-user"><div class="avatar">林</div><span>林晓晴</span><b>⌄</b></div></div></header>
</template>
<script>
export default { name: 'DashboardHeader', props: { period: String, lastUpdated: String, title: { type: String, default: '今日校招雷达' } }, data () { return { periods: ['今日', '近 7 天', '近 30 天'] } } }
</script>



