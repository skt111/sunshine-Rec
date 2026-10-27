<template>
  <section class="panel brand-legend" aria-label="矩阵图例">
    <div class="lg-list">
      <span class="lg-item"><i class="sw d1"></i>官方新媒体矩阵</span>
      <span class="lg-item"><i class="sw d2"></i>线上营销动作</span>
      <span class="lg-item"><i class="sw d3"></i>校企合作 · 奖学金</span>
      <span class="lg-item"><i class="sw d4"></i>专属培养项目</span>
      <span class="lg-item"><i class="sw partial"></i>部分达成</span>
      <span class="lg-item"><b class="pillA">A</b>官方原文</span>
      <span class="lg-item lg-b"><b>B</b>权威转载 / 校方或媒体对官方动作的报道</span>
    </div>
  </section>
</template>

<script>
export default {
  name: 'EmployerBrandLegend'
}
</script>

<style lang="scss" scoped>
.brand-legend {
  display: flex;
  align-items: center;
  margin-top: 12px;
  padding: 9px 14px;
}

.lg-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 18px;
}

.lg-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #3c4148;
  font-size: 10px;
  white-space: nowrap;
}

.sw {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.sw.d1 { background: #2563eb; }
.sw.d2 { background: #ea580c; }
.sw.d3 { background: #059669; }
.sw.d4 { background: #7c3aed; }

.sw.partial {
  background: repeating-linear-gradient(45deg, #2563eb 0 3px, #93b4f5 3px 6px);
}

.pillA {
  display: inline-grid;
  place-items: center;
  width: 14px;
  height: 14px;
  border-radius: 3px;
  color: #4d9666;
  background: #e9f7ee;
  font-size: 9px;
  font-weight: 700;
}

.lg-b {
  color: #2563eb;
}

.lg-b b {
  font-size: 10px;
  font-weight: 700;
}
</style>
