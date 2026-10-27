/**
 * 外链 HTTP 核验（提示词 v3 · 链接准确性协议配套脚本）
 *
 * 用法：
 *   node .qoder/scripts/link-check.mjs <html文件> [结果json路径]
 *
 * 输出：每条外链的最终状态码、跳转后 URL、落地页 <title>；
 *       标题与结论不一致的，人工按「落地页内容必须与结论一致」规则处置。
 *
 * 约定：
 *   - 抽取 href="http(s)://..." 并去重；
 *   - status=0 表示本通道请求失败（常见于站点 TLS 指纹拦截），
 *     需用 curl 备用通道复核后再判定，不可直接记为死链。
 */
import fs from 'node:fs';

const FILE = process.argv[2];
if (!FILE) {
  console.error('usage: node .qoder/scripts/link-check.mjs <html-file> [out.json]');
  process.exit(1);
}
const OUT = process.argv[3] || 'link-check.json';
const html = fs.readFileSync(FILE, 'utf8');

const urls = [];
const re = /href="(https?:\/\/[^"]+)"/g;
let m;
while ((m = re.exec(html)) !== null) {
  const u = m[1].replace(/&amp;/g, '&');
  if (!urls.includes(u)) urls.push(u);
}

const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36';

async function check(url) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 20000);
  try {
    const res = await fetch(url, {
      redirect: 'follow',
      signal: ctrl.signal,
      headers: { 'User-Agent': UA, 'Accept-Language': 'zh-CN,zh;q=0.9' }
    });
    let title = '';
    const ct = res.headers.get('content-type') || '';
    if (/text|html|json/.test(ct)) {
      const buf = await res.arrayBuffer();
      const txt = new TextDecoder('utf-8').decode(buf.slice(0, 400000));
      const t = txt.match(/<title[^>]*>([\s\S]*?)<\/title>/i);
      if (t) title = t[1].replace(/\s+/g, ' ').trim().slice(0, 120);
      else title = txt.slice(0, 80).replace(/\s+/g, ' ');
    }
    return { url, finalUrl: res.url, status: res.status, title };
  } catch (e) {
    return { url, status: 0, error: String((e && e.message) || e).slice(0, 120) };
  } finally {
    clearTimeout(timer);
  }
}

const results = [];
const CONCURRENCY = 4;
for (let i = 0; i < urls.length; i += CONCURRENCY) {
  const batch = urls.slice(i, i + CONCURRENCY);
  const r = await Promise.all(batch.map(check));
  results.push(...r);
  process.stderr.write(`done ${Math.min(i + CONCURRENCY, urls.length)}/${urls.length}\n`);
}

fs.writeFileSync(OUT, JSON.stringify(results, null, 2), 'utf8');
const bad = results.filter((r) => r.status !== 200);
console.log(`total ${urls.length} | non-200 ${bad.length}`);
for (const r of bad) {
  console.log(`${r.status}\t${r.url}\t${r.title || r.error || ''}`);
}
