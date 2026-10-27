<template>
  <div class="supply-stats">
    <!-- 供给侧：重点企业宣讲布局 -->
    <template v-if="mode === 'talk'">
      <div class="supply-stat purple">
        <span>◈</span>
        <div><small>监测重点企业</small><strong>{{ n(companyCount) }}</strong><p>保险 / 金融 / 互联网</p></div>
      </div>
      <div class="supply-stat orange">
        <span>◉</span>
        <div><small>宣讲活动数</small><strong>{{ n(stats.totalEventCount) }}</strong><p>当前筛选范围</p></div>
      </div>
      <div class="supply-stat blue">
        <span>⌂</span>
        <div><small>覆盖高校数</small><strong>{{ n(stats.coveredSchoolCount) }}</strong><p>有企业宣讲</p></div>
      </div>
      <div class="supply-stat violet">
        <span>▣</span>
        <div><small>命中企业宣讲</small><strong>{{ n(stats.companyTalkCount) }}</strong><p>已归属重点企业</p></div>
      </div>
      <div class="supply-stat red">
        <span>!</span>
        <div><small>待核验</small><strong>{{ n(stats.pendingVerificationCount) }}</strong><p>企业待确认</p></div>
      </div>
    </template>

    <!-- 需求侧：双选会报名机会 -->
    <template v-else>
      <div class="supply-stat purple">
        <span>⌂</span>
        <div><small>监测高校数</small><strong>{{ n(stats.monitoredSchoolCount) }}</strong><p>启用采集</p></div>
      </div>
      <div class="supply-stat orange">
        <span>◈</span>
        <div><small>公开招聘活动</small><strong>{{ n(stats.totalEventCount) }}</strong><p>双选会 / 招聘会</p></div>
      </div>
      <div class="supply-stat green">
        <span>◎</span>
        <div><small>开放报名</small><strong>{{ n(stats.openRegistrationCount) }}</strong><p>含即将截止</p></div>
      </div>
      <div class="supply-stat blue">
        <span>◷</span>
        <div><small>7 天内截止</small><strong>{{ n(stats.closingSoonCount) }}</strong><p>即将截止</p></div>
      </div>
      <div class="supply-stat red">
        <span>!</span>
        <div><small>报名时间待核</small><strong>{{ n(stats.registrationPendingCount) }}</strong><p>暂未公布</p></div>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  name: 'SupplyRadarStats',
  props: {
    stats: { type: Object, default: function () { return {} } },
    crawlStatus: { type: String, default: '' },
    /** talk=供给侧宣讲 / fair=需求侧双选 */
    mode: { type: String, default: 'talk' },
    companyCount: { type: Number, default: 0 }
  },
  methods: {
    n: function (v) {
      return (v === null || v === undefined) ? 0 : v
    }
  }
}
</script>
