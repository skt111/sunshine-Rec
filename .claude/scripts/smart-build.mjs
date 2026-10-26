#!/usr/bin/env node
/**
 * smart-build — PostToolUse 自检调度器（harness 跨轨共享资产，禁止硬编码绝对路径）
 *
 * 触发：.qoder/settings.local.json 的 PostToolUse hook
 *       matcher = Write|SearchReplace|edit_file → command = node .claude/scripts/smart-build.mjs
 * 输入：stdin 为 hook 载荷 JSON，从中提取被编辑文件的路径
 * 输出约定（见 .qoder/rules/01-execution-protocol.md §6.1）：
 *       通过 → 静默；失败 → 打印 FRONTEND_BUILD_FAILED / PLANS_VALIDATE_FAILED /
 *       JAVA_BUILD_FAILED 标记并 exit 2（stderr 回灌给 agent）
 *
 * 路由（monorepo 根视角；默认快速档，毫秒级；避免逐次编辑触发全量构建导致假失败）：
 *   .qoder/plans/plans.json          → node .qoder/plans/validate-plans.js（权威校验）
 *   frontend/src|public + 根配置      → 静态自检（BOM / 400 行红线 / 禁用语法 / name 声明）
 *                                       + frontend/node_modules 就绪时单文件 eslint
 *   java_bff/**                      → 仅 SMART_BUILD_FULL=1 时执行 mvn -f java_bff/pom.xml -q compile
 *   .qoder/**（非 plans） .claude/**  → 跳过
 *
 * 环境变量：SMART_BUILD_FULL=1 全量档（额外跑构建）；SMART_BUILD_SKIP=1 直接跳过
 */
import { readFileSync, existsSync } from 'node:fs'
import { spawnSync } from 'node:child_process'
import { dirname, resolve, relative, isAbsolute, sep } from 'node:path'
import { fileURLToPath } from 'node:url'

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..')
const FRONTEND = resolve(ROOT, 'frontend')
const FULL = process.env.SMART_BUILD_FULL === '1'
const MARK = {
  plans: 'PLANS_VALIDATE_FAILED',
  frontend: 'FRONTEND_BUILD_FAILED',
  java: 'JAVA_BUILD_FAILED',
}
const warnings = []

/** 读取 stdin 载荷（无输入/非法 JSON 时返回空对象，不阻断） */
function readPayload() {
  try {
    const raw = readFileSync(0, 'utf8')
    return raw.trim() ? JSON.parse(raw) : {}
  } catch (_) {
    return {}
  }
}

/** 从多种 hook 载荷形态中提取被编辑文件路径 */
function extractFilePath(payload) {
  const input = payload.tool_input || payload.toolInput || payload.input || {}
  const direct = [input.file_path, input.filePath, input.path, input.target_file, payload.file_path]
  const hit = direct.find(function (v) { return typeof v === 'string' && v.trim() })
  return hit ? hit.trim() : ''
}

/** 统一成相对项目根的 posix 路径 */
function toRel(file) {
  const abs = isAbsolute(file) ? file : resolve(ROOT, file)
  return relative(ROOT, abs).split(sep).join('/')
}

/** 失败收口：打印标记后 exit 2（stderr 回灌 agent） */
function fail(marker, detail) {
  process.stderr.write(marker + '\n' + detail + '\n')
  process.exit(2)
}

function flushWarnings() {
  if (!warnings.length) return
  process.stderr.write('[smart-build] 提示（不阻断）：\n' + warnings.map(function (w) { return '  - ' + w }).join('\n') + '\n')
}

/** 前端源码静态自检：只做确定性检查，误报以提示形式输出 */
function staticCheck(rel) {
  let text
  try {
    text = readFileSync(resolve(ROOT, rel), 'utf8')
  } catch (_) {
    return
  }
  if (text.charCodeAt(0) === 0xfeff) warnings.push(rel + '：文件含 UTF-8 BOM，请去除')

  const isVue = rel.endsWith('.vue')
  const isJs = rel.endsWith('.js')

  if (isVue) {
    const lines = text.split('\n').length
    if (lines > 400) warnings.push(rel + '：' + lines + ' 行，超出单文件 400 行红线，需拆分')
    if (!/name\s*:\s*['"][^'"]+['"]/.test(text)) warnings.push(rel + '：未显式声明组件 name')
  }
  if (isVue || isJs) {
    // 项目红线：禁用可选链与空值合并（ES5/ES6 兼容）
    if (/[A-Za-z0-9_$\)\]]\?\./.test(text)) warnings.push(rel + '：使用了可选链 ?.，违反 ES5/ES6 兼容红线')
    if (/\?\?/.test(text)) warnings.push(rel + '：使用了空值合并 ??，违反 ES5/ES6 兼容红线')
  }
}

/** 单文件 eslint：仅在 frontend/node_modules 就绪时执行，避免工具链缺失造成假失败 */
function lintFile(rel) {
  if (!existsSync(resolve(FRONTEND, 'node_modules'))) return
  const cli = resolve(FRONTEND, 'node_modules', '.bin', process.platform === 'win32' ? 'vue-cli-service.cmd' : 'vue-cli-service')
  if (!existsSync(cli)) return
  const target = rel.replace(/^frontend\//, '')
  const r = spawnSync(cli, ['lint', '--no-fix', target], { cwd: FRONTEND, encoding: 'utf8', shell: process.platform === 'win32' })
  if (r.status !== 0) fail(MARK.frontend, (r.stdout || '') + (r.stderr || ''))
}

/** 权威校验：任务大盘 */
function validatePlans() {
  const script = resolve(ROOT, '.qoder', 'plans', 'validate-plans.js')
  if (!existsSync(script)) return
  const r = spawnSync(process.execPath, [script], { cwd: ROOT, encoding: 'utf8' })
  if (r.status !== 0) fail(MARK.plans, (r.stdout || '') + (r.stderr || ''))
}

/** 全量档：后端编译（默认关闭，逐次编辑跑构建代价过高） */
function buildJava() {
  if (!FULL) return
  if (!existsSync(resolve(ROOT, 'java_bff', 'pom.xml'))) return
  const r = spawnSync('mvn', ['-f', 'java_bff/pom.xml', '-q', 'compile'], { cwd: ROOT, encoding: 'utf8', shell: process.platform === 'win32' })
  if (r.status !== 0) fail(MARK.java, (r.stdout || '') + (r.stderr || ''))
}

function main() {
  if (process.env.SMART_BUILD_SKIP === '1') return

  const file = extractFilePath(readPayload())
  if (!file) return

  const rel = toRel(file)
  if (rel.startsWith('..')) return
  if (!rel || rel === '.') return
  if (/^(node_modules|\.git|\.idea|\.upgrade-backup-)/.test(rel)) return

  if (rel === '.qoder/plans/plans.json') {
    validatePlans()
    return
  }
  if (/^\.(qoder|claude)\//.test(rel)) return

  if (/^frontend\/(src|public)\//.test(rel) || /^frontend\/(package\.json|vue\.config\.js|babel\.config\.js)$/.test(rel)) {
    staticCheck(rel)
    lintFile(rel)
    flushWarnings()
    return
  }
  if (/^java_bff\//.test(rel)) buildJava()
}

main()
