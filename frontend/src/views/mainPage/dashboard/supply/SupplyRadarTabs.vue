<template>
  <div class="category-tabs">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      :class="{ selected: active === tab.key }"
      @click="$emit('change', tab.key)"
    >
      {{ tab.label }} <em>{{ countOf(tab.key) }}</em>
    </button>
  </div>
</template>

<script>
import { TABS } from '@/constants/supplyRadar'

var COUNT_KEY = {
  ALL: 'all',
  TALK: 'talk',
  JOB_FAIR: 'jobFair',
  OTHER: 'other'
}

export default {
  name: 'SupplyRadarTabs',
  props: {
    active: { type: String, default: 'ALL' },
    counts: { type: Object, default: function () { return {} } }
  },
  data: function () {
    return { tabs: TABS }
  },
  methods: {
    countOf: function (key) {
      var k = COUNT_KEY[key]
      var v = this.counts ? this.counts[k] : 0
      return (v === null || v === undefined) ? 0 : v
    }
  }
}
</script>
