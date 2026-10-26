<template><section class="panel signals-panel"><div class="panel-head compact"><div><div class="section-label"><span class="section-mark blue-mark"></span>持续观察</div><h2>人才流动与招聘弱信号</h2><p>区分已确认事实与待验证推测，保持对趋势的敏感度。</p></div><button class="more-btn">查看全部 <span>→</span></button></div><div class="signal-list"><article v-for="signal in signals" :key="signal.title" class="signal-item"><div class="signal-gauge"><span>{{ signal.value }}</span><i :class="signal.level === '高' ? 'high' : signal.level === '中' ? 'mid' : 'low'"></i></div><div class="signal-copy"><b>{{ signal.title }}</b><p>{{ signal.note }}</p><span :class="['fact-label', signal.fact ? 'fact' : 'guess']">{{ signal.fact ? '已确认事实' : '待验证推测' }}</span></div><div class="signal-side"><span :class="['priority', signal.priority.toLowerCase()]">{{ signal.priority }}</span><small>置信度{{ signal.level }}</small></div></article></div></section></template>
<script>export default { name: 'TalentSignals', props: { signals: Array } }</script>




