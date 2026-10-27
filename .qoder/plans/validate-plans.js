#!/usr/bin/env node
/**
 * plans.json 运行时校验脚本
 * 用法: node validate-plans.js [plans.json 路径]
 *
 * 校验范围（schema 表达不了的跨字段约束）:
 * 1. activePlan 必须指向 status ∈ {active, in-progress} 的 plan
 * 2. parent 引用必须存在且无环（A→B→A）
 * 3. children 必须与子 plan 的 parent 互为镜像
 * 4. qualityScore 范围 [0,24]（A 域 N/A 计满分规则由 architect 自评负责）
 * 5. complexity 范围 [0,10]
 * 6. plan 详情文件存在性（file 字段）
 * 7. 根级 updated 不早于所有 plan 的 updated
 */
const fs = require('fs');
const path = require('path');

const DEFAULT_PATH = path.join(__dirname, 'plans.json');
const filePath = process.argv[2] || DEFAULT_PATH;

if (!fs.existsSync(filePath)) {
  console.error(`[FAIL] 文件不存在: ${filePath}`);
  process.exit(1);
}

const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
const errors = [];
const warns = [];
const plans = data.plans || [];
const byId = new Map(plans.map(p => [p.id, p]));

// 1. activePlan 状态校验
if (data.activePlan) {
  const p = byId.get(data.activePlan);
  if (!p) errors.push(`activePlan="${data.activePlan}" 不存在于 plans[]`);
  else if (!['active', 'in-progress'].includes(p.status))
    errors.push(`activePlan="${data.activePlan}" 状态为 "${p.status}"（必须为 active/in-progress；revised→待修订、blocked→先解阻、draft→未激活、done/archived→终态异常）`);
}

// 2. parent 引用存在 + 无环
plans.forEach(p => {
  if (p.parent) {
    if (!byId.has(p.parent)) errors.push(`${p.id}: parent="${p.parent}" 不存在`);
  }
});
// 环检测
plans.forEach(p => {
  const seen = new Set();
  let cur = p;
  while (cur && cur.parent) {
    if (seen.has(cur.id)) { errors.push(`父子关系成环: ${[...seen, cur.id].join(' → ')}`); break; }
    seen.add(cur.id);
    cur = byId.get(cur.parent);
  }
});

// 3. children 镜像校验
plans.forEach(p => {
  (p.children || []).forEach(cid => {
    if (!byId.has(cid)) errors.push(`${p.id}: children 引用 "${cid}" 不存在`);
    else if (byId.get(cid).parent !== p.id)
      errors.push(`${p.id}: children 含 "${cid}"，但该 plan 的 parent="${byId.get(cid).parent}"（应为 ${p.id}）`);
  });
  if (p.parent && byId.get(p.parent)) {
    const parentChildren = byId.get(p.parent).children || [];
    if (!parentChildren.includes(p.id))
      warns.push(`${p.id}: parent="${p.parent}" 的 children 未包含本 plan（镜像不一致）`);
  }
});

// 4/5. 数值范围
plans.forEach(p => {
  if (p.qualityScore != null && (p.qualityScore < 0 || p.qualityScore > 24))
    errors.push(`${p.id}: qualityScore=${p.qualityScore} 超出 [0,24]`);
  if (p.complexity != null && (p.complexity < 0 || p.complexity > 10))
    errors.push(`${p.id}: complexity=${p.complexity} 超出 [0,10]`);
});

// 6. 详情文件存在
plans.forEach(p => {
  if (p.file) {
    const fp = path.join(__dirname, p.file);
    if (!fs.existsSync(fp)) errors.push(`${p.id}: 详情文件缺失 ${p.file}`);
  }
});

// 6.1 exploration 引用存在性（防幻觉链路断点）
plans.forEach(p => {
  if (p.exploration) {
    const ep = path.join(__dirname, 'explorations', p.exploration);
    if (!fs.existsSync(ep)) errors.push(`${p.id}: exploration 引用缺失 explorations/${p.exploration}`);
  }
});

// 6.2 contracts 引用存在性（基准 = 仓库根）
const repoRoot = path.join(__dirname, '..', '..');
plans.forEach(p => {
  (p.contracts || []).forEach(c => {
    const cp = path.join(repoRoot, c);
    if (!fs.existsSync(cp)) errors.push(`${p.id}: contracts 引用缺失 ${c}（基准=仓库根）`);
  });
});

// 6.3 status × qualityScore 时序约束
plans.forEach(p => {
  if (p.status === 'draft' && p.qualityScore != null && p.qualityScore >= 19)
    warns.push(`${p.id}: draft 状态携带 qualityScore=${p.qualityScore}≥19（按 x-rules 应置 active）`);
  if (p.status === 'revised' && p.qualityScore != null)
    warns.push(`${p.id}: revised 状态仍携带旧 qualityScore=${p.qualityScore}（x-rules 要求 architect 修订后重评）`);
  if (p.status === 'archived' && (p.qualityScore == null || p.qualityScore < 19) && p.created >= '2026-09-02')
    warns.push(`${p.id}: archived 但 qualityScore=${p.qualityScore} 未达标（2026-09-02 新体系后归档应完成质量门禁；历史归档豁免）`);
});

// 6.4 需求基线校验（V-A7，2026-09-07）——适用: complexity ≥2 且 created ≥ S5 挂载日(2026-09-07) 的 plan；
//     基线节缺失 / yaml 未填写(REPLACE_ME) / 裁决存在但确认记录为空 → 告警
const BASELINE_SINCE = '2026-09-07';
plans.forEach(p => {
  if (!p.file) return;
  if (/todo-/.test(p.file)) return; // todo 登记型（harness 工程待办）不走 S5——豁免
  if ((p.created || '') === '' && ['active', 'in-progress'].includes(p.status)) warns.push(`${p.id}: created 缺失（V-A7/激活闸门适用性无法判定——建议补 created）`);
  if (p.complexity == null && ['active', 'in-progress'].includes(p.status)) warns.push(`${p.id}: complexity 未评估（V-A7 与激活闸门依赖此字段——流程违例提示）`);
  const num = (p.complexity == null ? 0 : Number(p.complexity));
  if (num < 2) return;
  if ((p.created || '') <= BASELINE_SINCE) return; // 存量豁免（P2-9）——挂载日（含）前创建的业务 plan 不追溯；次日起的新 plan 要求基线
  const fp = path.join(__dirname, p.file);
  let content = '';
  try { content = fs.readFileSync(fp, 'utf8'); } catch (e) { return; }
  // 提取「需求基线」yaml 块
  const m = content.match(/需求基线:[\s\S]*?```/);
  if (!m) { warns.push(`${p.id}: complexity=${num} 但详情无「需求基线」节（S5 流程缺口——architect §1.9 应产出）`); return; }
  const yaml = m[0];
  if (yaml.includes('REPLACE_ME')) { warns.push(`${p.id}: 需求基线未填写（含 REPLACE_ME 占位）`); return; }
  const val = (key) => {
    const line = yaml.split('\n').find((l) => l.trim().startsWith(key + ':'));
    return line ? line.split(':').slice(1).join(':').trim() : '';
  };
  const decision = val('复用决策');
  const records = val('确认记录');
  const domain = val('域引用');
  if (/REUSE|PARTIAL/.test(decision) && (records === '' || records === '[]' || records === 'N/A')) {
    warns.push(`${p.id}: 复用决策=${decision} 但确认记录为空（A1/A2 缺失——audit 将按 V-A4 判 P0）`);
  }
  if (/^NEW/.test(decision) && !/档案缺失|catalog 为空|直通/.test(decision) && (records === '' || records === '[]' || records === 'N/A')) {
    warns.push(`${p.id}: NEW 决策=${decision} 无直通豁免标注且确认记录为空（需 A1 或注明直通原因）`);
  }
  if (domain === 'REPLACE_ME' || domain === '') { warns.push(`${p.id}: 域引用未填写`); }
  for (const k of ['域引用', '复用决策', '风格声明', '表清单', '确认记录']) {
    if (!yaml.split('\n').some((l) => l.trim().startsWith(k + ':'))) warns.push(`${p.id}: 需求基线缺必填键 ${k}（键行被删）`);
  }
});

// 6.5 激活凭证校验（P1-5，2026-09-07）：激活前置沙盘闸门的机器侧——active/in-progress 且 complexity ≥4 的 plan 需 sandboxRef 凭证
plans.forEach(p => {
  const n5 = (p.complexity == null ? 0 : Number(p.complexity));
  if (n5 < 4) return;
  if (!['active', 'in-progress'].includes(p.status)) return;
  if ((p.created || '') <= BASELINE_SINCE) return; // 存量豁免（闸门挂载日前激活的 plan 不追溯）
  if (!p.sandboxRef) warns.push(`${p.id}: complexity=${n5} 激活但无 sandboxRef 凭证（激活前置沙盘闸门——绿灯=执行许可唯一签发点；--lightweight/--skip-typo 报告不得充当凭证）`);
});

// 6.6 revised 修订提示（P2-7，2026-09-07）：revised 且 updated > 挂载日的 plan 若涉及 scope/复杂度变更需重走基线
plans.forEach(p => {
  if (p.status === 'revised' && (p.updated || '') > BASELINE_SINCE) {
    warns.push(`${p.id}: revised 修订于 ${p.updated}——若 scope/复杂度变更需重走 S5 基线（architect §1.9）`);
  }
});

// 6.7 V-Intent（task-103 W1 · 2026-09-09）：意图资产一致性校验——校验矩阵节为唯一权威（task-103 plan 详情）
//     适用/豁免：created ≤ 生效日(2026-09-09)视同存量豁免（当日创建不追溯）；todo 型与 complexity<2 豁免；
//     四校验面：引用存在(error)/缺口告警(warn)/状态语义(warn)/反向 1:N(warn，intent.plan 集合含任一引用 plan 即通过)
const INTENT_SINCE = '2026-09-09';
const intentsDir = path.join(__dirname, '..', 'intents');
plans.forEach(p => {
  if (!p.file) return;
  const n7 = (p.complexity == null ? 0 : Number(p.complexity));
  const isTodo7 = /todo-/.test(p.file);
  const exempt7 = isTodo7 || (p.created || '') <= INTENT_SINCE || n7 < 2;
  // 引用存在性（error）
  if (p.intent) {
    const ip7 = path.join(intentsDir, p.intent);
    if (!fs.existsSync(ip7)) errors.push(`${p.id}: intent 引用缺失 .qoder/intents/${p.intent}（V-Intent 引用断裂）`);
  }
  // 缺口告警（非豁免新 plan 应建档未建档）
  if (!exempt7 && !p.intent && ['draft', 'active', 'in-progress'].includes(p.status))
    warns.push(`${p.id}: complexity=${n7} 新 plan 无 intent 引用（应建档未建档——V-Intent 缺口告警，audit 将按 P1 回指补建档或豁免注记）`);
  // 状态语义 + 反向 1:N（读 intent frontmatter）
  if (p.intent) {
    const ip7 = path.join(intentsDir, p.intent);
    if (!fs.existsSync(ip7)) return;
    let ic = '';
    try { ic = fs.readFileSync(ip7, 'utf8'); } catch (e) { return; }
    const mSt = ic.match(/^status:\s*["']?(\w+)["']?/m);
    const iSt = mSt ? mSt[1] : '';
    if (iSt === 'archived' && ['active', 'in-progress'].includes(p.status))
      warns.push(`${p.id}: 引用 intent(${p.intent}) 已 archived——不应被活跃 plan 引用（状态语义）`);
    if (iSt === 'draft' && ['active', 'in-progress'].includes(p.status))
      warns.push(`${p.id}: 引用 intent(${p.intent}) 仍为 draft（未确认）——未确认意图被激活执行（状态语义）`);
    const mPl = ic.match(/^plan:\s*(.+)$/m);
    if (mPl) {
      const pv = mPl[1].replace(/["']/g, '').trim();
      if (pv && pv !== 'null' && !pv.split(/[,\s]+/).filter(Boolean).includes(p.id))
        warns.push(`${p.id}: intent(${p.intent}) frontmatter plan 未含本 plan id（回填缺失——V-Intent 反向 1:N）`);
    }
  }
});
// 反向扫描：intent.plan 指向的 plan 存在性 + intent 字段回填缺失
if (fs.existsSync(intentsDir)) {
  try {
    fs.readdirSync(intentsDir).filter(f => f.endsWith('.md') && !f.startsWith('_') && !['README.md', 'INTENT-SPEC.md'].includes(f)).forEach(f => {
      const ic = fs.readFileSync(path.join(intentsDir, f), 'utf8');
      const mPl = ic.match(/^plan:\s*(.+)$/m);
      if (!mPl) return;
      const pv = mPl[1].replace(/["']/g, '').trim();
      if (!pv || pv === 'null') return;
      pv.split(/[,\s]+/).filter(Boolean).forEach(pid => {
        const pp = byId.get(pid);
        if (!pp) warns.push(`intent ${f}: plan 引用 "${pid}" 不存在于 plans.json`);
        else if (!pp.intent) warns.push(`intent ${f}: plan ${pid} 未回填 intent 字段（互指缺失——V-Intent 反向 1:N）`);
      });
    });
  } catch (e) { /* intents 目录不可读时跳过 */ }
}

// 6.8 V-Report（task-106 W3 · 2026-09-10）：凭证内容机器断言（断言矩阵见 task-106 plan 详情）
//     形态归一（P0-1——防崩溃/误伤/漏检）：string（含全路径）→ basename；object → report；空 → 跳过+warn
//     ① 报告文件存在(error) ② plan_id 绑定(error) ③ 结构行自洽(P0>0 且 pass → error)+fail 凭证(warn 全量)
//     ④ 新报告缺结构行 warn（created > 生效日）；存量豁免 = created ≤ 生效日行仅验①②
const REPORT_SINCE = '2026-09-10';
const sandboxReportsDir = path.join(__dirname, '..', 'skills', 'sandbox', 'reports');
plans.forEach(p => {
  if (!p.sandboxRef) return;
  const ref = typeof p.sandboxRef === 'string' ? path.basename(p.sandboxRef) : (p.sandboxRef && p.sandboxRef.report);
  if (!ref || typeof ref !== 'string' || ref === 'undefined') { warns.push(`${p.id}: sandboxRef 非规范形态（存量漂移——无法深校验，建议回填 {report, level, passedAt} 对象）`); return; }
  const rp = path.join(sandboxReportsDir, ref);
  if (!fs.existsSync(rp)) { errors.push(`${p.id}: 沙盘凭证报告缺失 ${ref}（V-Report ①——凭证断裂）`); return; }
  let rc = '';
  try { rc = fs.readFileSync(rp, 'utf8'); } catch (e) { return; }
  const mPid = rc.match(/^plan_id:\s*["']?([\w.-]+)["']?/m);
  if (mPid && mPid[1] !== p.id) errors.push(`${p.id}: 凭证报告 plan_id=${mPid[1]} 与本行不匹配（V-Report ②——凭证错配）`);
  const statsLines = [...rc.matchAll(/REPORT-STATS:\s*plan_id=([\w.-]+)\s+P0=(\d+)\s+P1=(\d+)\s+P2=(\d+)\s+VERDICT=(pass|fail)/g)];
  if (statsLines.length > 0) {
    const last = statsLines[statsLines.length - 1]; // 取最后一条匹配/尾部容差
    const [, sid, p0s, p1s, p2s, verdict] = last;
    if (Number(p0s) > 0 && verdict === 'pass') errors.push(`${p.id}: 报告结构行自洽失败——P0=${p0s} 但 VERDICT=pass（V-Report ③）`);
    if (verdict === 'fail') warns.push(`${p.id}: 凭证指向 VERDICT=fail 报告（P0=${p0s} P1=${p1s} P2=${p2s}）——未通过报告不应作为激活凭证（存量列账）`);
    if (sid !== p.id) warns.push(`${p.id}: 结构行 plan_id=${sid} 与行 id 不匹配（V-Report ③——结构行绑定）`);
  } else if ((p.created || '') > REPORT_SINCE) {
    warns.push(`${p.id}: 报告缺机器可断言结构行 REPORT-STATS（V-Report ④——2026-09-10 起新报告必附，见 sandbox/audit SKILL 报告规范）`);
  }
});

// 7. updated 一致性
plans.forEach(p => {
  if (p.updated && data.updated && p.updated > data.updated)
    warns.push(`${p.id}: updated(${p.updated}) 晚于根级 updated(${data.updated})，需同步根级`);
});

// 8. 双向覆盖：.qoder/plans/ 顶层 *.md 未入册 = 游离（无执行许可）
try {
  const topFiles = fs.readdirSync(__dirname).filter(f => f.endsWith('.md'));
  const registered = new Set((plans || []).map(p => p.file).filter(Boolean));
  topFiles.forEach(f => {
    if (!registered.has(f) && f !== 'kanban.html')
      warns.push(`游离 plan 文件 ${f} 未登记于 plans.json（不入册 = 无执行许可，应补录或移出）`);
  });
} catch (e) { /* 目录不可读时跳过 */ }

console.log(`校验 ${filePath}`);
console.log(`plans 总数: ${plans.length} | activePlan: ${data.activePlan}`);
if (errors.length) {
  console.error(`\n[FAIL] ${errors.length} 个错误:`);
  errors.forEach(e => console.error(`  - ${e}`));
  process.exit(1);
}
if (warns.length) {
  console.log(`\n[WARN] ${warns.length} 个警告（不阻断）:`);
  warns.forEach(w => console.log(`  - ${w}`));
}
console.log('\n[OK] plans.json 校验通过');
