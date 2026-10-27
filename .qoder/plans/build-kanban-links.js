#!/usr/bin/env node
/**
 * 链路索引生成器（kanban 投影数据源）
 * —— 只读扫描 plans.json / intents / reports / checkpoints，输出同目录 kanban-links.json。
 * —— 投影原则：不产生权威数据、可随时重建；单个挂载项异常不影响整体（fail-soft）。
 * 用法: node build-kanban-links.js   （start-kanban.bat 自动调用；也可手动重跑刷新）
 */
const fs = require('fs');
const path = require('path');

const PLANS_DIR = __dirname;                    // .qoder/plans
const QODER = path.resolve(PLANS_DIR, '..');    // .qoder
const ROOT = path.resolve(QODER, '..');         // 项目根（服务器根）
const SANDBOX_REPORTS = path.join(QODER, 'skills', 'sandbox', 'reports');
const AUDIT_REPORTS = path.join(QODER, 'skills', 'audit', 'reports');
const CHECKPOINTS = path.join(QODER, 'skills', 'audit', 'checkpoints');
const INTENTS = path.join(QODER, 'intents');

function readJson(p, dft) { try { return JSON.parse(fs.readFileSync(p, 'utf8')); } catch (e) { return dft; } }
function listDir(d) { try { return fs.readdirSync(d); } catch (e) { return []; } }
function exists(p) { try { return fs.existsSync(p); } catch (e) { return false; } }

/** 绝对路径 → 服务器根（项目根）相对 URL（以 / 开头，供浏览器 fetch） */
function toWeb(abs) {
  return '/' + path.relative(ROOT, abs).split(path.sep).join('/');
}

/** 最长匹配归属：文件名中出现的所有 plan id 取最长者（task-96-C 优先于 task-96） */
function longestMatchId(name, ids) {
  let best = null;
  for (const id of ids) {
    if (name.includes(id) && (!best || id.length > best.length)) best = id;
  }
  return best;
}

/** 按修订版序号降序（rN 大者在前；无 r 后缀视为 r0） */
function sortDesc(names) {
  const rn = (n) => { const m = n.match(/-r(\d+)\.md$/); return m ? parseInt(m[1], 10) : 0; };
  return names.slice().sort((a, b) => rn(b) - rn(a) || b.localeCompare(a));
}

function main() {
  const data = readJson(path.join(PLANS_DIR, 'plans.json'), null);
  const plans = data && Array.isArray(data.plans) ? data.plans : [];
  const ids = plans.map((p) => p.id).filter(Boolean).sort((a, b) => b.length - a.length);

  // 目录扫描一次，归入各 plan（最长匹配）
  const sandboxAll = listDir(SANDBOX_REPORTS).filter((n) => n.endsWith('.md'));
  const auditAll = listDir(AUDIT_REPORTS).filter((n) => n.endsWith('.md'));
  const checkpointAll = listDir(CHECKPOINTS).filter((n) => n.endsWith('.md'));
  const groupBy = (names) => {
    const m = {};
    for (const n of names) {
      const id = longestMatchId(n, ids);
      if (!id) continue;
      (m[id] = m[id] || []).push(n);
    }
    return m;
  };
  const sandboxByPlan = groupBy(sandboxAll);
  const auditByPlan = groupBy(auditAll);
  const checkpointByPlan = groupBy(checkpointAll);

  const links = {};
  const counts = { requirementPackage: 0, intent: 0, sandbox: 0, audit: 0, checkpoints: 0 };
  for (const p of plans) {
    if (!p.id) continue;
    const entry = {};

    // ① intent 直链（plans.intent 为裸文件名，拼 .qoder/intents/）
    if (p.intent) {
      const ip = path.join(INTENTS, p.intent);
      if (exists(ip)) { entry.intent = toWeb(ip); counts.intent++; }
    }

    // ② 需求包（解析 intent 内 `需求包: \`路径\` **vX**` 行——无行则自然不挂）
    if (entry.intent) {
      try {
        const ic = fs.readFileSync(path.join(INTENTS, p.intent), 'utf8');
        const rm = ic.match(/需求包[:：]\s*`([^`]+)`/);
        if (rm) {
          const pkgAbs = path.resolve(ROOT, rm[1].replace(/\/+$/, ''));
          if (pkgAbs.startsWith(ROOT) && exists(pkgAbs)) {
            const vm = ic.match(/需求包[:：][^\n]*?\*\*v([\d.]+)\*\*/);
            const readme = path.join(pkgAbs, 'README.md');
            entry.requirementPackage = { path: toWeb(exists(readme) ? readme : pkgAbs), version: vm ? vm[1] : null };
            counts.requirementPackage++;
          }
        }
      } catch (e) { /* fail-soft：解析失败不挂载 */ }
    }

    // ③ 沙盘报告：sandboxRef 字段（双格式归一）∪ 目录扫描（最长匹配）
    const sandboxSet = new Map(); // abs -> name
    if (p.sandboxRef) {
      const ref = typeof p.sandboxRef === 'object' && p.sandboxRef !== null
        ? String(p.sandboxRef.report || '') : String(p.sandboxRef);
      const abs = ref.startsWith('.qoder') ? path.join(ROOT, ref) : path.join(SANDBOX_REPORTS, ref);
      if (ref && abs.startsWith(ROOT) && exists(abs)) sandboxSet.set(abs, path.basename(abs));
    }
    for (const n of sandboxByPlan[p.id] || []) sandboxSet.set(path.join(SANDBOX_REPORTS, n), n);
    if (sandboxSet.size) {
      entry.sandbox = sortDesc([...sandboxSet.keys()]).map(toWeb);
      counts.sandbox += sandboxSet.size;
    }

    // ④ 审计报告 / ⑤ 检查点：目录扫描（最长匹配）
    if ((auditByPlan[p.id] || []).length) {
      entry.audit = sortDesc(auditByPlan[p.id]).map((n) => toWeb(path.join(AUDIT_REPORTS, n)));
      counts.audit += auditByPlan[p.id].length;
    }
    if ((checkpointByPlan[p.id] || []).length) {
      entry.checkpoints = sortDesc(checkpointByPlan[p.id]).map((n) => toWeb(path.join(CHECKPOINTS, n)));
      counts.checkpoints += checkpointByPlan[p.id].length;
    }

    if (Object.keys(entry).length) links[p.id] = entry;
  }

  const out = { generatedAt: new Date().toISOString(), plans: links };
  fs.writeFileSync(path.join(PLANS_DIR, 'kanban-links.json'), JSON.stringify(out, null, 2), 'utf8');
  console.log('[kanban-links] 已生成: ' + Object.keys(links).length + ' 个 plan 有挂载'
    + ' | 需求包 ' + counts.requirementPackage + ' / intent ' + counts.intent
    + ' / 沙盘 ' + counts.sandbox + ' / 审计 ' + counts.audit + ' / 检查点 ' + counts.checkpoints);
}

try { main(); } catch (e) {
  console.error('[kanban-links] 生成失败（看板将显示无链路数据）: ' + (e && e.message ? e.message : e));
  process.exit(1);
}
