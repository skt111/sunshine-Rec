<template>
  <section class="panel brand-matrix">
    <div class="mx-head">
      <div>
        <div class="section-label"><span class="section-mark orange-mark"></span>校招吸引动作矩阵</div>
        <h2>21 家雇主的四维校招动作 <span class="en">EMPLOYER BRANDING MATRIX</span></h2>
      </div>
      <span class="mx-meta">21 家 × 4 维 · 逐条可核验</span>
    </div>

    <div class="mx-bar">
      <div class="chips">
        <button
          v-for="c in chips"
          :key="c.key"
          :class="['chip', group === c.key ? 'on' : '']"
          @click="group = c.key"
        >{{ c.label }}</button>
      </div>
      <div class="mx-search">
        <span>⌕</span>
        <input v-model="keyword" type="text" placeholder="搜索公司 / 动作关键词">
      </div>
    </div>

    <div class="mx-src">
      收录规则：仅收录<b>官方公告 / 官网背书</b>的渠道与项目（同业 2026-08-10 检索复核，本企业 2026-10-27 实采）；
      <b>公众号为各公司标配，不单独计入矩阵</b>；<span class="mk">A</span> = 官方原文，
      <span class="mkB">B</span> = 权威转载 / 校方或媒体对官方动作的报道；
      点击 ↗ 查看证据。没有证据的格子写「未见官方披露」，不做推测。
    </div>

    <div class="mx-scroll">
      <table class="mx">
        <thead>
          <tr>
            <th class="h-co">公司<span>覆盖度 · 分组</span></th>
            <th v-for="d in dims" :key="d.key">
              {{ d.name }}<span class="th-sub">{{ d.sub }}</span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in renderRows" :key="row.company" :class="{ self: row.self }">
            <td class="co">
              <div class="co-n">
                {{ row.company }}<span v-if="row.self" class="self-badge">本企业</span>
              </div>
              <div class="co-dots">
                <i v-for="(on, i) in row.dots" :key="i" :class="{ on: on }"></i>
              </div>
              <div class="co-gp">{{ row.scoreLabel }}</div>
            </td>
            <td v-for="(cell, ci) in row.cells" :key="ci" :class="['dim', cell.none ? 'none' : '']">
              <template v-if="cell.none">
                {{ cell.text }}
              </template>
              <template v-else>
                <div v-for="(line, li) in cell.lines" :key="li" class="dim-line">
                  <template v-for="(s, si) in line">
                    <a
                      v-if="s.k === 'a'"
                      :key="'a' + si"
                      :href="s.href"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="ev-arrow"
                      title="查看证据"
                    >{{ arrowText(s.v) }}</a>
                    <span v-else-if="s.k === 'st'" :key="'st' + si" :class="['st', s.cls]">{{ s.v }}</span>
                    <small v-else-if="s.k === 'note'" :key="'n' + si" class="tn">{{ s.v }}</small>
                    <span v-else :key="'t' + si">{{ s.v }}</span>
                  </template>
                </div>
                <button v-if="cell.more" class="dim-more" @click="toggle(cell.key)">
                  {{ cell.open ? '收起 ▴' : '展开 +' + cell.more + ' 行 ▾' }}
                </button>
                <!-- 证据等级徽标 + 被折叠隐藏的链接：均不参与折叠，始终可见 -->
                <div v-if="cell.badges.length || cell.links.length" class="dim-links">
                  <span
                    v-for="(b, bi) in cell.badges"
                    :key="'b' + bi"
                    :class="['ev', b.v]"
                    :title="b.title"
                  >{{ b.count > 1 ? b.v + '×' + b.count : b.v }}</span>
                  <a
                    v-for="(l, li) in cell.links"
                    :key="'l' + li"
                    :href="l.href"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="ev-link"
                  >{{ l.label }} ↗</a>
                </div>
              </template>
            </td>
          </tr>
          <tr v-if="!renderRows.length">
            <td colspan="5" class="mx-empty">没有匹配的公司或动作关键词</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script>
import { MATRIX_ROWS } from '@/constants/employerBrandMatrix'

var GROUP_LABELS = { '本企业': '本企业', '保险': '保险', '金融': '金融', '互联网': '互联网', '标杆': '跨行业标杆' }

export default {
  name: 'EmployerBrandMatrix',
  data: function () {
    return {
      rows: MATRIX_ROWS,
      group: 'all',
      keyword: '',
      /** 单元格展开状态：key = 公司#列索引 */
      expanded: {},
      /** 每格默认最多显示的行数（2 行，避免单行过长把整行拉高） */
      maxLines: 2,
      dims: [
        { key: 'media', name: '官方新媒体矩阵', sub: '视频号 / 小红书 / 抖音 / B站' },
        { key: 'mkt', name: '线上营销动作', sub: '综艺 / TVC / Campaign' },
        { key: 'sch', name: '校企合作 · 奖学金', sub: '高校 / 实验室 / 奖学金' },
        { key: 'prog', name: '专属培养项目', sub: '管培 / 技术专项 / 顶尖计划' }
      ]
    }
  },
  computed: {
    chips: function () {
      var rows = this.rows
      var out = [{ key: 'all', label: '全部 ' + rows.length }]
      var order = ['本企业', '保险', '金融', '互联网', '标杆']
      for (var i = 0; i < order.length; i++) {
        var g = order[i]
        var n = rows.filter(function (r) { return r.group === g }).length
        if (!n) continue
        out.push({ key: g, label: g === '本企业' ? GROUP_LABELS[g] : GROUP_LABELS[g] + ' ' + n })
      }
      return out
    },
    filteredRows: function () {
      var self = this
      var kw = (this.keyword || '').trim()
      return this.rows.filter(function (r) {
        if (self.group !== 'all' && r.group !== self.group) return false
        if (!kw) return true
        var hay = (r.company + ' ' + r.searchKey + ' ' + r.scoreLabel).toLowerCase()
        return hay.indexOf(kw.toLowerCase()) >= 0
      })
    },
    /** 渲染用的行：把每格切成行，并截断到 maxLines */
    renderRows: function () {
      var self = this
      return this.filteredRows.map(function (row) {
        var cells = row.dims.map(function (cell, ci) {
          var key = row.company + '#' + ci
          var lines = self.splitLines(cell)
          var open = !!self.expanded[key]
          var visible = open ? lines : lines.slice(0, self.maxLines)
          return {
            none: cell.none,
            text: cell.none ? self.cellText(cell) : '',
            lines: visible,
            more: lines.length > self.maxLines ? lines.length - self.maxLines : 0,
            // 证据等级徽标不参与折叠；只有被折叠隐藏的链接落到下方
            badges: self.cellBadges(cell),
            links: open ? [] : self.hiddenLinks(lines.slice(self.maxLines)),
            open: open,
            key: key
          }
        })
        return {
          company: row.company,
          self: row.self,
          dots: row.dots,
          scoreLabel: row.scoreLabel,
          cells: cells
        }
      })
    }
  },
  methods: {
    splitLines: function (cell) {
      var lines = []
      var cur = []
      for (var i = 0; i < cell.segs.length; i++) {
        var s = cell.segs[i]
        if (s.k === 'br') {
          if (cur.length) lines.push(cur)
          cur = []
          continue
        }
        // 徽标已单独固定展示，不参与行内折叠
        if (s.k === 'ev') continue
        cur.push(s)
      }
      if (cur.length) lines.push(cur)
      return lines
    },
    /** 证据等级徽标（去重后统计条数，始终展示） */
    cellBadges: function (cell) {
      var order = []
      var map = {}
      for (var i = 0; i < cell.segs.length; i++) {
        var s = cell.segs[i]
        if (s.k !== 'ev') continue
        var hit = map[s.v]
        if (!hit) {
          hit = { v: s.v, title: s.title || (s.v === 'A' ? '官方原文' : '权威转载 / 校方或媒体对官方动作的报道'), count: 0 }
          map[s.v] = hit
          order.push(hit)
        }
        hit.count++
        if (hit.count > 1) {
          hit.title = hit.title.replace(/ · 共 \d+ 条$/, '') + ' · 共 ' + hit.count + ' 条'
        }
      }
      return order
    },
    /** 行内箭头文字：锚点自身带文案就显示文案，否则用 ↗ */
    arrowText: function (v) {
      var t = (v || '').replace(/↗/g, '').trim()
      return t ? t + ' ↗' : '↗'
    },
    /** 被折叠掉的行里的链接，汇总为单元格底部的兜底标签 */
    hiddenLinks: function (hiddenLines) {
      var out = []
      for (var i = 0; i < hiddenLines.length; i++) {
        var line = hiddenLines[i]
        for (var j = 0; j < line.length; j++) {
          var s = line[j]
          if (s.k !== 'a') continue
          var label = (s.v || '').replace(/↗/g, '').trim()
          out.push({ href: s.href, label: label || '查看证据' })
        }
      }
      return out
    },
    toggle: function (key) {
      this.$set(this.expanded, key, !this.expanded[key])
    },
    cellText: function (cell) {
      for (var i = 0; i < cell.segs.length; i++) {
        if (cell.segs[i].k === 't') return cell.segs[i].v
      }
      return '—'
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-matrix {
  margin-top: 12px;
  padding: 12px 14px 14px;
}

.mx-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 9px;
}

.mx-head h2 {
  margin: 2px 0 0;
  font-size: 13px;
  letter-spacing: -.01em;
}

.mx-head .en {
  margin-left: 6px;
  color: #b3bac3;
  font-size: 9px;
  font-weight: 400;
  letter-spacing: .06em;
}

.mx-meta {
  color: #a3abb6;
  font-size: 9px;
}

.mx-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  height: 24px;
  padding: 0 10px;
  border: 1px solid #e6e9ed;
  border-radius: 12px;
  color: #6b7683;
  background: #fff;
  font: inherit;
  font-size: 10px;
  cursor: pointer;
}

.chip.on {
  border-color: #d97706;
  color: #fff;
  background: #d97706;
}

.mx-search {
  display: flex;
  align-items: center;
  height: 26px;
  padding: 0 8px;
  border: 1px solid #e2e6eb;
  border-radius: 5px;
  background: #fff;
  color: #a1a9b4;
}

.mx-search span {
  margin-right: 5px;
  font-size: 12px;
}

.mx-search input {
  width: 180px;
  border: 0;
  outline: 0;
  color: #5f6a78;
  font: inherit;
  font-size: 10px;
}

.mx-src {
  margin-bottom: 8px;
  padding: 7px 10px;
  border-radius: 6px;
  color: #8a7452;
  background: #fdfaf4;
  font-size: 9px;
  line-height: 1.7;
}

.mk,
.mkB {
  display: inline-grid;
  place-items: center;
  width: 13px;
  height: 13px;
  border-radius: 3px;
  font-size: 8px;
  font-weight: 700;
}

.mk { color: #4d9666; background: #e9f7ee; }
.mkB { color: #2563eb; background: #eaf1fe; }

.mx-scroll {
  overflow-x: auto;
}

.mx {
  width: 100%;
  min-width: 1080px;
  border-collapse: collapse;
  font-size: 10px;
}

.mx thead th {
  padding: 7px 9px;
  border-bottom: 1px solid #e6e9ed;
  color: #41627e;
  background: #f7f9fb;
  font-size: 10px;
  font-weight: 600;
  text-align: left;
  white-space: nowrap;
}

.mx thead th .th-sub {
  display: inline;
  margin: 0 0 0 5px;
  color: #a3abb6;
  font-size: 8px;
  font-weight: 400;
}

.h-co {
  width: 168px;
}

.mx tbody td {
  padding: 8px 9px;
  border-bottom: 1px solid #f2f4f6;
  color: #4b5563;
  line-height: 1.65;
  vertical-align: top;
}

.mx tbody tr:hover td {
  background: #fffdf9;
}

.mx tbody tr.self td {
  background: #fffaf1;
}

.mx tbody tr.self:hover td {
  background: #fff6e8;
}

.co-n {
  color: #36414d;
  font-size: 11px;
  font-weight: 600;
}

.self-badge {
  margin-left: 5px;
  padding: 1px 5px;
  border-radius: 3px;
  color: #b45309;
  background: #ffedd5;
  font-size: 8px;
  font-weight: 500;
}

.co-dots {
  display: flex;
  gap: 3px;
  margin: 4px 0 2px;
}

.co-dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #e6e9ed;
}

.co-dots i.on {
  background: #d97706;
}

.co-gp {
  color: #a3abb6;
  font-size: 9px;
}

.dim.none {
  color: #b3bac3;
}

.dim-line {
  line-height: 1.6;
}

.dim-line + .dim-line {
  margin-top: 1px;
}

.dim-more {
  margin-top: 3px;
  padding: 0;
  border: 0;
  color: #2563eb;
  background: transparent;
  font: inherit;
  font-size: 9px;
  cursor: pointer;
}

.dim-more:hover {
  text-decoration: underline;
}

.dim-links {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}

.dim-links .ev-link {
  display: inline-block;
  margin-left: 0;
  padding: 0 5px;
  border: 1px solid #f3d2ae;
  border-radius: 3px;
  color: #b45309;
  background: #fff8ef;
  font-size: 9px;
  line-height: 15px;
  text-decoration: none;
  white-space: nowrap;
}

.dim-links .ev-link:hover {
  border-color: #e9a866;
  background: #fff1e0;
  text-decoration: none;
}

.ev {
  display: inline-grid;
  place-items: center;
  min-width: 13px;
  height: 13px;
  margin: 0 1px;
  padding: 0 3px;
  border-radius: 3px;
  font-size: 8px;
  font-weight: 700;
  cursor: help;
}

.ev.A { color: #4d9666; background: #e9f7ee; }
.ev.B { color: #2563eb; background: #eaf1fe; }

.st {
  display: inline-block;
  margin-right: 4px;
  padding: 0 5px;
  border-radius: 3px;
  font-size: 9px;
}

.st.add { color: #4d9666; background: #edf8f0; }
.st.todo { color: #b07b33; background: #fff4e2; }

.tn {
  color: #a6aeb8;
  font-size: 9px;
}

.ev-link {
  margin-left: 2px;
  color: #d97706;
  font-size: 10px;
  text-decoration: none;
}

/* 行内证据箭头（每一条目自身的链接） */
.ev-arrow {
  margin-left: 3px;
  color: #d97706;
  font-size: 10px;
  text-decoration: none;
  white-space: nowrap;
}

.ev-arrow:hover {
  color: #b45309;
  text-decoration: underline;
}

.ev-link:hover {
  text-decoration: underline;
}

.mx-empty {
  padding: 26px;
  color: #a6aeb8;
  text-align: center;
}

@media (max-width: 1360px) {
  .mx-bar { flex-direction: column; align-items: flex-start; }
}
</style>
