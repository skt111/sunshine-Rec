<template>
  <header class="ab">
    <div class="ab-brand">
      <span class="ab-mark"><span>00</span></span>
      <span class="ab-bt">妙招</span>
      <span class="ab-bsub">洞察市场，招聘有招</span>
    </div>

    <nav class="ab-navs">
      <button
        v-for="item in items"
        :key="item.name"
        :class="['ab-nav', active === item.name ? 'on' : '']"
        @click="$emit('select', item.name)"
      >
        <span class="ab-ico" v-html="iconSvg(item.icon)"></span>
        <span class="ab-name">{{ item.name }}</span>
      </button>
    </nav>

    <div class="ab-user">
      <div class="ab-avatar">林</div>
      <span>林晓晴</span>
      <b>⌄</b>
    </div>
  </header>
</template>

<script>
export default {
  name: 'DashboardHeader',
  props: {
    active: { type: String, default: '校招雷达' }
  },
  data: function () {
    return {
      items: [
        { name: '校招雷达', icon: 'radar' },
        { name: '供给端雷达', icon: 'supply' },
        { name: '对策建议台', icon: 'strategy' },
        { name: '雇主品牌雷达', icon: 'brand' },
        { name: '战略洞察', icon: 'insight' }
      ]
    }
  },
  methods: {
    iconSvg: function (name) {
      var icons = {
        radar: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"></circle><circle cx="12" cy="12" r="3"></circle><path d="M12 4v2M20 12h-2M12 20v-2M4 12h2"></path></svg>',
        supply: '<svg viewBox="0 0 24 24"><path d="M4 17h16M6 13l4-4 3 3 5-6"></path><path d="M18 6h2v2"></path></svg>',
        strategy: '<svg viewBox="0 0 24 24"><path d="M12 3 4 7l8 4 8-4-8-4Z"></path><path d="m4 12 8 4 8-4M4 17l8 4 8-4"></path></svg>',
        brand: '<svg viewBox="0 0 24 24"><path d="M20.8 8.8c0 5.5-8.8 11-8.8 11s-8.8-5.5-8.8-11A4.8 4.8 0 0 1 12 6.3a4.8 4.8 0 0 1 8.8 2.5Z"></path></svg>',
        insight: '<svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="8"></circle><path d="M12 8v8M8 12h8"></path></svg>'
      }
      return icons[name] || icons.insight
    }
  }
}
</script>

<style lang="scss" scoped>
/* 单行顶栏：logo + 导航 + 用户 */
.ab {
  position: sticky;
  z-index: 3;
  top: 0;
  display: flex;
  align-items: center;
  gap: 22px;
  height: 62px;
  padding: 0 24px;
  border-bottom: 1px solid var(--line, #e6e9ee);
  background: rgba(247, 248, 250, .95);
  backdrop-filter: saturate(180%) blur(10px);
}

/* ---------- 品牌 ---------- */
.ab-brand {
  display: flex;
  align-items: baseline;
  gap: 7px;
  flex: none;
}

.ab-mark {
  position: relative;
  overflow: hidden;
  padding: 1px 7px 2px;
  border-radius: 6px;
  color: #fff;
  background: linear-gradient(155deg, #fb923c 0%, #ea580c 100%);
  box-shadow: 0 1px 3px rgba(217, 119, 6, .35);
  font-family: Georgia, "Songti SC", serif;
  font-size: 21px;
  font-weight: 700;
  letter-spacing: .02em;
}

.ab-mark::before {
  position: absolute;
  z-index: 0;
  inset: 0;
  background: repeating-radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0) 0 3.4px, rgba(255, 255, 255, .2) 3.4px 4.1px);
  content: "";
}

.ab-mark > span {
  position: relative;
  z-index: 2;
}

.ab-bt {
  color: #1d2024;
  font-family: Georgia, "Songti SC", serif;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: .06em;
}

.ab-bsub {
  margin-left: 2px;
  color: #8a9099;
  font-size: 11px;
  letter-spacing: .04em;
}

/* ---------- 导航（与 logo 同一行） ---------- */
.ab-navs {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1 1 auto;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.ab-navs::-webkit-scrollbar {
  display: none;
}

.ab-nav {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  flex: none;
  height: 34px;
  padding: 0 13px;
  border: 1px solid transparent;
  border-radius: 8px;
  color: #6f7783;
  background: transparent;
  font: inherit;
  font-size: 12px;
  white-space: nowrap;
  cursor: pointer;
  transition: color .15s, background .15s, border-color .15s, box-shadow .15s;
}

.ab-nav:hover {
  color: #1d2024;
  background: #eff1f5;
}

.ab-nav:focus {
  outline: none;
}

.ab-nav:focus-visible {
  outline: 2px solid #f7c08a;
  outline-offset: -2px;
}

.ab-nav.on {
  border-color: #f3ddc6;
  color: #b45309;
  background: #fff;
  box-shadow: 0 1px 2px rgba(16, 24, 40, .06);
  font-weight: 600;
}

.ab-ico svg {
  display: block;
  width: 15px;
  height: 15px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.6;
  stroke-linecap: round;
  stroke-linejoin: round;
}

/* ---------- 用户 ---------- */
.ab-user {
  display: flex;
  align-items: center;
  gap: 7px;
  flex: none;
  color: #46505e;
  font-size: 11px;
}

.ab-user b {
  color: #a0a7b2;
  font-size: 13px;
}

.ab-avatar {
  display: grid;
  place-items: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: #a84d13;
  background: #ffd7b6;
  font-weight: 700;
}

@media (max-width: 1100px) {
  .ab {
    gap: 12px;
    padding: 0 14px;
  }

  .ab-bsub {
    display: none;
  }

  .ab-nav {
    padding: 0 9px;
  }
}
</style>
