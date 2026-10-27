<template>
  <div class="intel-shell no-sidebar">
    <DashboardHeader :active="activeNav" @select="activeNav = $event" />
    <main class="intel-main">
      <div class="page-body" :class="{ 'page-body-brand': activeNav === '雇主品牌雷达' }">
        <!-- 面包屑：仅多层级页面展示（供给端雷达页头已有同名标题，避免重复） -->
        <div v-if="activeNav !== '供给端雷达'" class="breadcrumb"><span>情报驾驶舱</span><i>/</i><strong>{{ activeNav }}</strong></div>
        <RecruitmentRadar v-if="activeNav === '校招雷达'" :companies="timelineCompanies" @brief="briefVisible = true" @source="openSource" />
        <SupplyRadar v-else-if="activeNav === '供给端雷达'" @source="openSource" @toast="showToast" />
        <EmployerBrandRadar v-else-if="activeNav === '雇主品牌雷达'" @brief="briefVisible = true" @toast="showToast" />
        <section v-else class="panel module-placeholder"><div class="placeholder-icon">✦</div><p class="eyebrow">WORKSPACE MODULE</p><h1>{{ activeNav }}</h1><p>模块框架已预留，当前已完成校招雷达、供给端雷达与雇主品牌雷达的前端演示。</p><span class="demo-label">即将接入</span></section>
      </div>
    </main>
    <div v-if="briefVisible" class="modal-layer" @click.self="briefVisible = false"><div class="brief-modal"><div class="modal-head"><div><span class="modal-kicker">DAILY INTELLIGENCE BRIEF</span><h2>今日情报简报</h2></div><button class="icon-btn" @click="briefVisible = false">×</button></div><div class="brief-date">2026年10月26日 · 09:30 生成 · 演示数据</div><div class="brief-hero"><span>建议优先级</span><strong>P1</strong><p>竞品校招节奏提前，建议本周完成重点院校宣讲资源锁定。</p></div><div class="brief-list"><div><b>01</b><span>市场发生了什么</span><p>3 家头部企业发布秋招补录岗位，华东地区技术岗位增长明显。</p></div><div><b>02</b><span>对阳光意味着什么</span><p>候选人比较窗口缩短，岗位发展路径会成为转化关键。</p></div><div><b>03</b><span>今天建议行动</span><p>更新校招内容页，向目标院校发出宣讲排期确认。</p></div></div><div class="modal-foot"><span>报告编号：DEMO-1026-0930</span><button class="primary-btn" @click="briefVisible = false">开始执行</button></div></div></div>
    <transition name="toast"><div v-if="toast" class="toast-msg"><span>✓</span>{{ toast }}</div></transition>
  </div>
</template>
<script>
import DashboardHeader from './dashboard/DashboardHeader'
import RecruitmentRadar from './dashboard/RecruitmentRadar'
import SupplyRadar from './dashboard/SupplyRadar'
import EmployerBrandRadar from './dashboard/EmployerBrandRadar'
export default { name: 'MainPage', components: { DashboardHeader, RecruitmentRadar, SupplyRadar, EmployerBrandRadar }, data () { return { activeNav: '校招雷达', briefVisible: false, toast: '', toastTimer: null, timelineCompanies: [] } }, methods: { openSource (event) { var url = event && event.url && event.url !== '#' ? event.url : (event && event.sourceUrl ? event.sourceUrl : 'https://example.com/mock-source'); window.open(url, '_blank', 'noopener,noreferrer'); this.showToast('已打开演示来源链接') }, showToast (message) { clearTimeout(this.toastTimer); this.toast = message; this.toastTimer = setTimeout(function () { this.toast = '' }.bind(this), 2600) } } }
</script>
<style lang="scss">@import './dashboard/dashboard.css';</style>
