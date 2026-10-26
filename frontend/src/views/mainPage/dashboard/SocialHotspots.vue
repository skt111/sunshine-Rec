<template>
  <section class="panel hotspot-panel"><div class="panel-head compact"><div><div class="section-label"><span class="section-mark coral-mark"></span>候选人语境</div><h2>新媒体热点追踪</h2><p>发现年轻人才关注的话题，识别招聘机会及雇主品牌方向。</p></div><button class="more-btn" @click="$emit('detail', activeItems[0])">更多热点 <span>→</span></button></div><div class="platform-tabs"><button v-for="item in platforms" :key="item" :class="{ selected: platform === item }" @click="$emit('platform', item)">{{ item }}</button></div><div class="hotspot-list"><article v-for="item in activeItems" :key="item.title" class="hotspot-item" @click="$emit('detail', item)"><div class="hotspot-rank">0{{ activeItems.indexOf(item) + 1 }}</div><div class="hotspot-content"><h3>{{ item.title }}</h3><div class="hotspot-meta"><span class="heat">◉ {{ item.heat }}</span><b>{{ item.trend }}</b><span>{{ item.time }}</span></div><div class="tag-line"><span v-for="tag in item.tags" :key="tag"># {{ tag }}</span><em :class="item.toneClass">{{ item.tone }}</em><strong>{{ item.relevance }}</strong></div></div><span class="chevron">›</span></article><div v-if="!activeItems.length" class="empty-state">该平台暂无演示热点</div></div></section>
</template>
<script>
export default { name: 'SocialHotspots', props: { platform: String, items: Array }, data () { return { platforms: ['小红书', '抖音', '微信公众号', '微博'] } }, computed: { activeItems () { return this.items.filter(function (item) { return item.platform === this.platform }.bind(this)) } } }
</script>




