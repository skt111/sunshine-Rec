<template>
  <section class="panel analysis-panel"><div class="panel-head compact"><div><div class="section-label"><span class="section-mark dark-mark"></span>从信号到行动</div><h2>热点洞察与 AI 分析</h2><p>模拟分析：把讨论热度转化为可执行的招聘决策。</p></div><span class="ai-chip">✦ AI 模拟分析</span></div><div class="analysis-flow"><div v-for="(step, index) in steps" :key="step.title" :class="['analysis-step', index === 3 ? 'last' : '']"><div class="step-no">0{{ index + 1 }}</div><div><b>{{ step.title }}</b><p>{{ step.text }}</p></div><span v-if="index < 3" class="flow-arrow">→</span></div></div><div class="decision-box"><div class="decision-head"><span class="priority">P1</span><b>建议强化“成长路径”表达</b><span class="confidence">置信度 78%</span></div><p>发现候选人高频讨论职业发展与工作稳定性，建议在校招传播中补充培养机制、岗位发展路径和福利保障。</p><div class="decision-grid"><div><small>建议负责人</small><strong>校招内容组</strong></div><div><small>建议完成</small><strong>本周五前</strong></div><div><small>预期效果</small><strong>提升投递转化</strong></div></div><div class="decision-actions"><button :class="['primary-btn', adopted ? 'adopted' : '']" @click="$emit('action', adopted ? 'hold' : 'adopt')">{{ adopted ? '✓ 已采纳' : '采纳建议' }}</button><button class="ghost-btn" @click="$emit('action', 'hold')">暂缓处理</button><button class="link-btn" @click="$emit('detail', steps)">查看完整分析 →</button></div></div></section>
</template>
<script>
export default { name: 'InsightAnalysis', props: { adopted: Boolean }, data () { return { steps: [{ title: '热点发现', text: '职业发展 / 稳定性讨论升温' }, { title: '爆火原因', text: '真实体验与可预期感缺口' }, { title: '招聘影响', text: '候选人比较窗口缩短' }, { title: '行动建议', text: '补充成长路径与培养机制' }] } } }
</script>




