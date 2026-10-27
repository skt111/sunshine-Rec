<template>
  <div class="drawer-layer" @click.self="$emit('close')">
    <aside class="detail-drawer brand-source-drawer">
      <div class="drawer-head">
        <div>
          <span class="modal-kicker">SOURCE CHECK · 来源核验</span>
          <h2>能不能自动读取，看证据不看感觉</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')">×</button>
      </div>

      <div class="drawer-body">
        <div class="entry-tip">
          探测只做两件事：读 <b>robots.txt</b> 判定是否允许；允许时再发<b>一次</b> GET 看是否需要登录。
          <b>不翻页、不解析正文、不保存内容、不重试、不换 UA/IP。</b>
        </div>

        <div class="probe-row">
          <input v-model="url" type="text" placeholder="https://www.xiaohongshu.com/explore">
          <button class="primary-btn" :disabled="probing" @click="runProbe">
            {{ probing ? '探测中…' : '探测一次' }}
          </button>
        </div>

        <div v-if="probeError" class="entry-error">{{ probeError }}</div>

        <div v-if="result" class="probe-result">
          <div class="result-head">
            <span :class="['conclusion', conclusionClass(result.conclusion)]">{{ conclusionText(result.conclusion) }}</span>
            <span class="result-url">{{ result.targetUrl }}</span>
          </div>
          <div class="result-grid">
            <div><small>robots 判定</small><b>{{ result.robotsDecision || '—' }}</b></div>
            <div><small>命中规则</small><b>{{ result.robotsRule || '—' }}</b></div>
            <div><small>HTTP 状态</small><b>{{ result.httpStatus === null || result.httpStatus === undefined ? '（未请求页面）' : result.httpStatus }}</b></div>
            <div><small>登录要求</small><b>{{ result.loginRequired ? '需要登录' : '未检出' }}</b></div>
            <div><small>拦截特征</small><b>{{ result.blockMarkers || '—' }}</b></div>
          </div>
          <p class="result-remark">{{ result.remark }}</p>
        </div>

        <h4 class="block-title">来源登记（{{ sources.length }}）</h4>
        <div class="source-list">
          <div v-for="s in sources" :key="s.sourceType" class="source-row">
            <span :class="['source-badge', s.status === 'ENABLED' ? 'on' : (s.status === 'DISABLED_BY_POLICY' ? 'off' : 'idle')]">
              {{ statusText(s.status) }}
            </span>
            <div class="source-main">
              <b>{{ s.label }}</b>
              <p>{{ s.description }}</p>
            </div>
            <span class="source-auth">{{ s.authorized ? '已授权' : '未授权' }}</span>
          </div>
        </div>

        <h4 class="block-title">核验记录（最近 {{ checks.length }} 条）</h4>
        <div v-if="!checks.length" class="checks-empty">还没有核验记录</div>
        <table v-else class="checks-table">
          <thead>
            <tr><th>时间</th><th>目标</th><th>robots</th><th>状态</th><th>结论</th></tr>
          </thead>
          <tbody>
            <tr v-for="c in checks" :key="c.id">
              <td>{{ shortTime(c.checkedAt) }}</td>
              <td class="url" :title="c.targetUrl">{{ shortUrl(c.targetUrl) }}</td>
              <td>{{ c.robotsDecision || '—' }}</td>
              <td>{{ c.httpStatus === null || c.httpStatus === undefined ? '—' : c.httpStatus }}</td>
              <td :class="['conclusion', conclusionClass(c.conclusion)]">{{ conclusionText(c.conclusion) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="drawer-foot">
        <button class="ghost-btn" @click="$emit('close')">关闭</button>
      </div>
    </aside>
  </div>
</template>

<script>
import { fetchSourceStatus, fetchSourceChecks, probeSource } from '@/api/employerBrand'

export default {
  name: 'SourceCheckDrawer',
  data: function () {
    return {
      url: 'https://www.xiaohongshu.com/explore',
      sources: [],
      checks: [],
      result: null,
      probeError: '',
      probing: false
    }
  },
  mounted: function () {
    this.load()
  },
  methods: {
    load: function () {
      var self = this
      fetchSourceStatus().then(function (list) { self.sources = list || [] }).catch(function () { self.sources = [] })
      fetchSourceChecks({ limit: 20 }).then(function (list) { self.checks = list || [] }).catch(function () { self.checks = [] })
    },
    runProbe: function () {
      var self = this
      this.probing = true
      this.probeError = ''
      this.result = null
      probeSource(this.url).then(function (res) {
        self.probing = false
        self.result = res || null
        self.load()
        self.$emit('probed', res)
      }).catch(function (e) {
        self.probing = false
        self.probeError = e.message || '探测失败'
      })
    },
    statusText: function (s) {
      if (s === 'ENABLED') return '已启用'
      if (s === 'DISABLED_BY_POLICY') return '合规禁用'
      return '未配置'
    },
    conclusionText: function (c) {
      if (c === 'ALLOWED') return '允许访问'
      if (c === 'DISALLOWED_BY_ROBOTS') return 'robots 禁止'
      if (c === 'LOGIN_REQUIRED') return '需要登录'
      if (c === 'BLOCKED') return '被限制访问'
      return '待复核'
    },
    conclusionClass: function (c) {
      if (c === 'ALLOWED') return 'ok'
      if (c === 'DISALLOWED_BY_ROBOTS' || c === 'BLOCKED') return 'bad'
      if (c === 'LOGIN_REQUIRED') return 'warn'
      return 'idle'
    },
    shortTime: function (v) {
      if (!v) return '—'
      return String(v).replace('T', ' ').slice(5, 16)
    },
    shortUrl: function (v) {
      if (!v) return '—'
      return v.replace(/^https?:\/\//, '').slice(0, 42)
    }
  }
}
</script>

<style lang="scss" scoped>
.brand-source-drawer .drawer-body {
  padding-top: 16px;
}

.entry-tip {
  margin-bottom: 12px;
  padding: 9px 10px;
  border-radius: 6px;
  color: #7d6a52;
  background: #fbf5ec;
  font-size: 10px;
  line-height: 1.7;
}

.probe-row {
  display: flex;
  gap: 8px;
}

.probe-row input {
  flex: 1;
  height: 30px;
  padding: 0 9px;
  border: 1px solid #e2e6eb;
  border-radius: 5px;
  outline: 0;
  color: #4f5a68;
  font: inherit;
  font-size: 10px;
}

.probe-row .primary-btn {
  height: 30px;
  padding: 0 14px;
  font-size: 10px;
}

.entry-error {
  margin-top: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  color: #c75763;
  background: #fff0f1;
  font-size: 10px;
}

.probe-result {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #eef1f4;
  border-radius: 7px;
  background: #fafbfc;
}

.result-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.result-url {
  color: #98a1ad;
  font-size: 9px;
  word-break: break-all;
}

.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 9px;
}

.result-grid small {
  display: block;
  color: #a3abb6;
  font-size: 9px;
}

.result-grid b {
  display: block;
  margin-top: 2px;
  color: #4a5461;
  font-size: 10px;
  font-weight: 500;
  word-break: break-all;
}

.result-remark {
  margin: 9px 0 0;
  color: #7d8794;
  font-size: 9px;
  line-height: 1.65;
}

.block-title {
  margin: 16px 0 7px;
  color: #5f6a77;
  font-size: 11px;
}

.source-list {
  border: 1px solid #eef1f4;
  border-radius: 7px;
  overflow: hidden;
}

.source-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 9px;
  border-bottom: 1px solid #f4f6f8;
}

.source-row:last-child {
  border-bottom: 0;
}

.source-badge {
  flex: 0 0 auto;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 8px;
}

.source-badge.on { color: #4d9666; background: #edf8f0; }
.source-badge.off { color: #c75763; background: #fff0f1; }
.source-badge.idle { color: #718090; background: #f1f3f5; }

.source-main {
  min-width: 0;
  flex: 1;
}

.source-main b {
  display: block;
  color: #414b57;
  font-size: 10px;
}

.source-main p {
  margin: 3px 0 0;
  color: #9aa3ae;
  font-size: 9px;
  line-height: 1.55;
}

.source-auth {
  flex: 0 0 auto;
  color: #b3bac3;
  font-size: 9px;
}

.checks-empty {
  padding: 12px;
  border-radius: 6px;
  color: #8b95a1;
  background: #f7f8fa;
  font-size: 10px;
}

.checks-table {
  width: 100%;
  border-collapse: collapse;
}

.checks-table th,
.checks-table td {
  padding: 6px 5px;
  border-bottom: 1px solid #f0f2f4;
  color: #5f6a77;
  font-size: 9px;
  text-align: left;
}

.checks-table th {
  color: #9aa3ae;
  font-weight: 500;
}

.checks-table td.url {
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conclusion {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 9px;
}

.conclusion.ok { color: #4d9666; background: #edf8f0; }
.conclusion.warn { color: #b07b33; background: #fff4e2; }
.conclusion.bad { color: #c75763; background: #fff0f1; }
.conclusion.idle { color: #718090; background: #f1f3f5; }
</style>
