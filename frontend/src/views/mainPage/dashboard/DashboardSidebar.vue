<template>
  <aside :class="['sidebar', collapsed ? 'collapsed' : '']">
    <div class="brand"><div class="brand-mark"><span></span><span></span><span></span></div><div class="brand-copy"><strong>00妙招</strong><small>情报与人才战略</small></div></div>
    <div v-if="!collapsed" class="workspace-pill"><span class="live-dot"></span><span>校招情报中心</span><i>⌄</i></div>
    <p v-if="!collapsed" class="nav-caption">工作台</p>
    <nav><button v-for="item in items" :key="item.name" :title="collapsed ? item.name : ''" :class="['nav-item', active === item.name ? 'active' : '']" @click="$emit('select', item.name)"><span class="nav-icon" v-html="iconSvg(item.icon)"></span><span v-if="!collapsed">{{ item.name }}</span><em v-if="!collapsed && item.badge">{{ item.badge }}</em></button></nav>
    <div class="sidebar-bottom"><div v-if="!collapsed" class="season-card"><span>2027 届秋招</span><b>进行中</b><div class="season-track"><i></i></div><small>情报覆盖度 86%</small></div><button class="collapse-btn" :title="collapsed ? '展开侧边栏' : '折叠侧边栏'" @click="$emit('toggle-collapse')"><span>{{ collapsed ? '»' : '«' }}</span><span v-if="!collapsed">折叠侧边栏</span></button><div v-if="!collapsed" class="user-mini"><div class="avatar">林</div><div><b>林晓晴</b><small>人才战略组</small></div><span>⋯</span></div></div>
  </aside>
</template>
<script>
export default { name: 'DashboardSidebar', props: { active: { type: String, default: '今日校招雷达' }, collapsed: Boolean }, data () { return { items: [{ name: '今日校招雷达', icon: 'radar', badge: '3' }, { name: '供给端雷达', icon: 'supply', badge: '18' }, { name: '对策建议台', icon: 'strategy', badge: '7' }, { name: '雇主品牌雷达', icon: 'brand' }, { name: '战略洞察', icon: 'insight' }] } }, methods: { iconSvg (name) { var icons = { radar: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"></circle><circle cx="12" cy="12" r="3"></circle><path d="M12 4v2M20 12h-2M12 20v-2M4 12h2"></path></svg>', supply: '<svg viewBox="0 0 24 24"><path d="M4 17h16M6 13l4-4 3 3 5-6"></path><path d="M18 6h2v2"></path></svg>', strategy: '<svg viewBox="0 0 24 24"><path d="M12 3 4 7l8 4 8-4-8-4Z"></path><path d="m4 12 8 4 8-4M4 17l8 4 8-4"></path></svg>', brand: '<svg viewBox="0 0 24 24"><path d="M20.8 8.8c0 5.5-8.8 11-8.8 11s-8.8-5.5-8.8-11A4.8 4.8 0 0 1 12 6.3a4.8 4.8 0 0 1 8.8 2.5Z"></path></svg>', insight: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"></circle><path d="M12 8v8M8 12h8"></path></svg>' }; return icons[name] || icons.insight } } }
</script>



